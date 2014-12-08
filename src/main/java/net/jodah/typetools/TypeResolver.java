/**
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.jodah.typetools;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import sun.reflect.ConstantPool;

/**
 * Enhanced type resolution utilities. Originally based on
 * org.springframework.core.GenericTypeResolver.
 * 
 * @author Jonathan Halterman
 */
@SuppressWarnings("restriction")
public final class TypeResolver {
  private static Method GET_CONSTANT_POOL;

  private TypeResolver() {
  }

  /** An unknown type. */
  public static final class Unknown {
    private Unknown() {
    }
  }

  /** Cache of type variable/argument pairs */
  private static final Map<Class<?>, Reference<Map<TypeVariable<?>, Type>>> typeVariableCache = Collections.synchronizedMap(new WeakHashMap<Class<?>, Reference<Map<TypeVariable<?>, Type>>>());
  private static boolean cacheEnabled = true;

  static {
    try {
      GET_CONSTANT_POOL = Class.class.getDeclaredMethod("getConstantPool");
      GET_CONSTANT_POOL.setAccessible(true);
    } catch (Exception e) {
    }
  }

  /**
   * Enables the internal caching of resolved TypeVariables.
   */
  public static void enableCache() {
    cacheEnabled = true;
  }

  /**
   * Disables the internal caching of resolved TypeVariables.
   */
  public static void disableCache() {
    typeVariableCache.clear();
    cacheEnabled = false;
  }

  /**
   * Returns the raw class representing the argument for the {@code type} using type variable
   * information from the {@code subType}. If no arguments can be resolved then
   * {@code Unknown.class} is returned.
   * 
   * @param type to resolve argument for
   * @param subType to extract type variable information from
   * @return argument for {@code type} else {@link Unknown.class} if no type arguments are declared
   * @throws IllegalArgumentException if more or less than one argument is resolved for the
   *           {@code type}
   */
  public static <T, S extends T> Class<?> resolveRawArgument(Class<T> type, Class<S> subType) {
    return resolveRawArgument(resolveGenericType(type, subType), subType);
  }

  /**
   * Returns the raw class representing the argument for the {@code genericType} using type variable
   * information from the {@code subType}. If {@code genericType} is an instance of class, then
   * {@code genericType} is returned. If no arguments can be resolved then {@code Unknown.class} is
   * returned.
   * 
   * @param genericType to resolve argument for
   * @param subType to extract type variable information from
   * @return argument for {@code genericType} else {@link Unknown.class} if no type arguments are
   *         declared
   * @throws IllegalArgumentException if more or less than one argument is resolved for the
   *           {@code genericType}
   */
  public static Class<?> resolveRawArgument(Type genericType, Class<?> subType) {
    Class<?>[] arguments = resolveRawArguments(genericType, subType);
    if (arguments == null)
      return Unknown.class;

    if (arguments.length != 1)
      throw new IllegalArgumentException("Expected 1 argument for generic type " + genericType
        + " but found " + arguments.length);

    return arguments[0];
  }

  /**
   * Returns an array of raw classes representing arguments for the {@code type} using type variable
   * information from the {@code subType}. Arguments for {@code type} that cannot be resolved are
   * returned as {@code Unknown.class}. If no arguments can be resolved then {@code null} is
   * returned.
   * 
   * @param type to resolve arguments for
   * @param subType to extract type variable information from
   * @return array of raw classes representing arguments for the {@code type} else {@code null} if
   *         no type arguments are declared
   */
  public static <T, S extends T> Class<?>[] resolveRawArguments(Class<T> type, Class<S> subType) {
    return resolveRawArguments(resolveGenericType(type, subType), subType);
  }

  /**
   * Returns an array of raw classes representing arguments for the {@code genericType} using type
   * variable information from the {@code subType}. Arguments for {@code genericType} that cannot be
   * resolved are returned as {@code Unknown.class}. If no arguments can be resolved then
   * {@code null} is returned.
   * 
   * @param genericType to resolve arguments for
   * @param subType to extract type variable information from
   * @return array of raw classes representing arguments for the {@code genericType} else
   *         {@code null} if no type arguments are declared
   */
  public static Class<?>[] resolveRawArguments(Type genericType, Class<?> subType) {
    Class<?>[] result = null;

    // Handle lambdas
    if (GET_CONSTANT_POOL != null && subType.isSynthetic()) {
      Class<?> functionalInterface = genericType instanceof ParameterizedType
        && ((ParameterizedType) genericType).getRawType() instanceof Class ? (Class<?>) ((ParameterizedType) genericType).getRawType()
        : genericType instanceof Class ? (Class<?>) genericType : null;
      if (functionalInterface != null && functionalInterface.isInterface())
        getTypeVariableMap(subType, functionalInterface);
    }

    if (genericType instanceof ParameterizedType) {
      ParameterizedType paramType = (ParameterizedType) genericType;
      Type[] arguments = paramType.getActualTypeArguments();
      result = new Class[arguments.length];
      for (int i = 0; i < arguments.length; i++)
        result[i] = resolveRawClass(arguments[i], subType);
    } else if (genericType instanceof TypeVariable) {
      result = new Class[1];
      result[0] = resolveRawClass(genericType, subType);
    } else if (genericType instanceof Class) {
      TypeVariable<?>[] typeParams = ((Class<?>) genericType).getTypeParameters();
      result = new Class[typeParams.length];
      for (int i = 0; i < typeParams.length; i++)
        result[i] = resolveRawClass(typeParams[i], subType);
    }

    return result;
  }

  /**
   * Returns the generic {@code type} using type variable information from the {@code subType} else
   * {@code null} if the generic type cannot be resolved.
   * 
   * @param type to resolve generic type for
   * @param subType to extract type variable information from
   * @return generic {@code type} else {@code null} if it cannot be resolved
   */
  public static Type resolveGenericType(Class<?> type, Type subType) {
    Class<?> rawType;
    if (subType instanceof ParameterizedType)
      rawType = (Class<?>) ((ParameterizedType) subType).getRawType();
    else
      rawType = (Class<?>) subType;

    if (type.equals(rawType))
      return subType;

    Type result;
    if (type.isInterface()) {
      for (Type superInterface : rawType.getGenericInterfaces())
        if (superInterface != null && !superInterface.equals(Object.class))
          if ((result = resolveGenericType(type, superInterface)) != null)
            return result;
    }

    Type superClass = rawType.getGenericSuperclass();
    if (superClass != null && !superClass.equals(Object.class))
      if ((result = resolveGenericType(type, superClass)) != null)
        return result;

    return null;
  }

  /**
   * Resolves the raw class for the {@code genericType}, using the type variable information from
   * the {@code subType} else {@link Unknown} if the raw class cannot be resolved.
   * 
   * @param type to resolve raw class for
   * @param subType to extract type variable information from
   * @return raw class for the {@code genericType} else {@link Unknown} if it cannot be resolved
   */
  public static Class<?> resolveRawClass(Type genericType, Class<?> subType) {
    if (genericType instanceof Class) {
      return (Class<?>) genericType;
    } else if (genericType instanceof ParameterizedType) {
      return resolveRawClass(((ParameterizedType) genericType).getRawType(), subType);
    } else if (genericType instanceof GenericArrayType) {
      GenericArrayType arrayType = (GenericArrayType) genericType;
      Class<?> compoment = resolveRawClass(arrayType.getGenericComponentType(), subType);
      return Array.newInstance(compoment, 0).getClass();
    } else if (genericType instanceof TypeVariable) {
      TypeVariable<?> variable = (TypeVariable<?>) genericType;
      genericType = getTypeVariableMap(subType, null).get(variable);
      genericType = genericType == null ? resolveBound(variable) : resolveRawClass(genericType,
        subType);
    }

    return genericType instanceof Class ? (Class<?>) genericType : Unknown.class;
  }

  private static Map<TypeVariable<?>, Type> getTypeVariableMap(final Class<?> targetType,
    Class<?> functionalInterface) {
    Reference<Map<TypeVariable<?>, Type>> ref = typeVariableCache.get(targetType);
    Map<TypeVariable<?>, Type> map = ref != null ? ref.get() : null;

    if (map == null) {
      map = new HashMap<TypeVariable<?>, Type>();

      // Populate lambdas
      if (functionalInterface != null)
        buildTypeVariableMap(functionalInterface, targetType, map);

      // Populate interfaces
      buildTypeVariableMap(targetType.getGenericInterfaces(), map, functionalInterface != null);

      // Populate super classes and interfaces
      Type genericType = targetType.getGenericSuperclass();
      Class<?> type = targetType.getSuperclass();
      while (type != null && !Object.class.equals(type)) {
        if (genericType instanceof ParameterizedType)
          buildTypeVariableMap((ParameterizedType) genericType, map, false);
        buildTypeVariableMap(type.getGenericInterfaces(), map, false);

        genericType = type.getGenericSuperclass();
        type = type.getSuperclass();
      }

      // Populate enclosing classes
      type = targetType;
      while (type.isMemberClass()) {
        genericType = type.getGenericSuperclass();
        if (genericType instanceof ParameterizedType)
          buildTypeVariableMap((ParameterizedType) genericType, map, functionalInterface != null);

        type = type.getEnclosingClass();
      }

      if (cacheEnabled)
        typeVariableCache.put(targetType, new WeakReference<Map<TypeVariable<?>, Type>>(map));
    }

    return map;
  }

  /**
   * Populates the {@code map} with variable/argument pairs for the {@code functionalInterface}.
   */
  private static void buildTypeVariableMap(Class<?> functionalInterface, final Class<?> lambdaType,
    Map<TypeVariable<?>, Type> map) {
    try {
      // Find SAM
      for (Method m : functionalInterface.getMethods()) {
        if (!m.isDefault() && !Modifier.isStatic(m.getModifiers()) && !m.isBridge()) {
          // Get functional interface's type params
          Type returnTypeVar = m.getGenericReturnType();
          Type[] paramTypeVars = m.getGenericParameterTypes();

          // Get lambda's type arguments
          ConstantPool constantPool = (ConstantPool) GET_CONSTANT_POOL.invoke(lambdaType);
          String[] methodRefInfo = constantPool.getMemberRefInfoAt(constantPool.getSize() - 2);

          if (returnTypeVar instanceof TypeVariable) {
            String returnTypeName = TypeDescriptor.getReturnType(methodRefInfo[2]).getClassName();
            if (!returnTypeName.equals("void"))
              map.put((TypeVariable<?>) returnTypeVar, Class.forName(returnTypeName));
          }

          TypeDescriptor[] arguments = TypeDescriptor.getArgumentTypes(methodRefInfo[2]);
          for (int i = 0; i < arguments.length; i++)
            if (paramTypeVars[i] instanceof TypeVariable)
              map.put((TypeVariable<?>) paramTypeVars[i],
                Class.forName(arguments[i].getClassName()));
          break;
        }
      }

    } catch (Exception ignore) {
    }
  }

  /**
   * Populates the {@code map} with with variable/argument pairs for the given {@code types}.
   */
  private static void buildTypeVariableMap(final Type[] types,
    final Map<TypeVariable<?>, Type> map, boolean depthFirst) {
    for (Type type : types) {
      if (type instanceof ParameterizedType) {
        ParameterizedType parameterizedType = (ParameterizedType) type;
        if (!depthFirst)
          buildTypeVariableMap(parameterizedType, map, depthFirst);
        Type rawType = parameterizedType.getRawType();
        if (rawType instanceof Class)
          buildTypeVariableMap(((Class<?>) rawType).getGenericInterfaces(), map, depthFirst);
        if (depthFirst)
          buildTypeVariableMap(parameterizedType, map, depthFirst);
      } else if (type instanceof Class) {
        buildTypeVariableMap(((Class<?>) type).getGenericInterfaces(), map, depthFirst);
      }
    }
  }

  /**
   * Populates the {@code typeVariableMap} with type arguments and parameters for the given
   * {@code type}.
   */
  private static void buildTypeVariableMap(ParameterizedType type,
    Map<TypeVariable<?>, Type> typeVariableMap, boolean depthFirst) {
    if (type.getRawType() instanceof Class) {
      TypeVariable<?>[] typeVariables = ((Class<?>) type.getRawType()).getTypeParameters();
      Type[] typeArguments = type.getActualTypeArguments();

      if (type.getOwnerType() != null) {
        Type owner = type.getOwnerType();
        if (owner instanceof ParameterizedType)
          buildTypeVariableMap((ParameterizedType) owner, typeVariableMap, depthFirst);
      }

      for (int i = 0; i < typeArguments.length; i++) {
        TypeVariable<?> variable = typeVariables[i];
        Type typeArgument = typeArguments[i];

        if (typeArgument instanceof Class) {
          typeVariableMap.put(variable, typeArgument);
        } else if (typeArgument instanceof GenericArrayType) {
          typeVariableMap.put(variable, typeArgument);
        } else if (typeArgument instanceof ParameterizedType) {
          typeVariableMap.put(variable, typeArgument);
        } else if (typeArgument instanceof TypeVariable) {
          TypeVariable<?> typeVariableArgument = (TypeVariable<?>) typeArgument;
          if (depthFirst) {
            Type existingType = typeVariableMap.get(variable);
            if (existingType != null) {
              typeVariableMap.put(typeVariableArgument, existingType);
              continue;
            }
          }

          Type resolvedType = typeVariableMap.get(typeVariableArgument);
          if (resolvedType == null)
            resolvedType = resolveBound(typeVariableArgument);
          typeVariableMap.put(variable, resolvedType);
        }
      }
    }
  }

  /**
   * Resolves the first bound for the {@code typeVariable}, returning {@code Unknown.class} if none
   * can be resolved.
   */
  public static Type resolveBound(TypeVariable<?> typeVariable) {
    Type[] bounds = typeVariable.getBounds();
    if (bounds.length == 0)
      return Unknown.class;

    Type bound = bounds[0];
    if (bound instanceof TypeVariable)
      bound = resolveBound((TypeVariable<?>) bound);

    return bound == Object.class ? Unknown.class : bound;
  }
}

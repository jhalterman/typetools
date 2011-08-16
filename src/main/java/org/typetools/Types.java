package org.typetools;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

/**
 * Tools for working with Types. Resolution is performed on demand, binding type arguments and
 * parameters across a hierarchy.
 * 
 * @author Jonathan Halterman
 */
public final class Types {
  private Types() {
  }

  /**
   * Resolves the class for the given {@code type} using the following resolution sequence:
   * 
   * <p>
   * <ul>
   * <li>If {@code type} is a Class then the class is returned</li>
   * <li>else if type is a ParameterizedType then the raw type is returned</li>
   * <li>else null is returned</li>
   * </ul>
   * 
   * @param type to resolve class for
   * @return raw class or null if one cannot be resolved
   */
  @SuppressWarnings("unchecked")
  public static <T> Class<T> resolveClass(Type type) {
    return type instanceof Class ? (Class<T>) type
        : type instanceof ParameterizedType ? (Class<T>) ((ParameterizedType) type).getRawType()
            : null;
  }

  /**
   * Resolves the upper bound for the given {@code type} using the following resolution sequence:
   * 
   * <p>
   * <ul>
   * <li>If {@code type} is a Class then the class is returned</li>
   * <li>else if type is a ParameterizedType then the raw type is returned</li>
   * <li>else if {@code type} is a TypeVariable then the bound at {@code boundIndex} is returned</li>
   * <li>else null is returned</li>
   * </ul>
   * 
   * @param to resolve upper bound for
   * @param boundIndex index of upper bound for {@code type} to resolve
   * @return resolved bound or null if one cannot be resolved
   */
  public static <T> Class<T> resolveBound(Type type, int boundIndex) {
    Class<T> clazz = Types.<T>resolveClass(type);
    if (clazz != null)
      return clazz;
    if (type instanceof TypeVariable) {
      Type[] bounds = ((TypeVariable<?>) type).getBounds();
      if (bounds.length > boundIndex)
        return Types.<T>resolveClass(bounds[boundIndex]);
    }
    return null;
  }

  /**
   * Resolves an array of raw classes representing type arguments for {@code target} resolved
   * upwards from {@code intiailType}.
   * 
   * <p>
   * Arguments for {@code target} that cannot be resolved to a Class are returned as null. If no
   * arguments can be resolved then null is returned.
   * 
   * @param target to resolve arguments for
   * @param initialType to resolve upwards from
   * @return array of raw classes representing type arguments for {@code initial}, else null.
   */
  public static <T, I extends T> Class<?>[] resolveArguments(Class<T> target, Class<I> initialType) {
    Type[] typeArguments = resolveArguments(initialType, target, target.isInterface(), null, null);
    if (typeArguments == null)
      return null;
    Class<?>[] result = new Class[typeArguments.length];
    for (int i = 0; i < typeArguments.length; i++)
      result[i] = Types.resolveClass(typeArguments[i]);

    return result;
  }

  /**
   * Resolves an array of type arguments for {@code target} resolved upwards from
   * {@code intiailType}.
   * 
   * <p>
   * Arguments for {@code target} that cannot be resolved are returned as null. If no arguments can
   * be resolved then null is returned.
   * 
   * @param target to resolve arguments for
   * @param initialType to resolve upwards from
   * @return array of type arguments for {@code initial}, else null.
   */
  public static <T, I extends T> Type[] resolveArgumentsAsTypes(Class<T> target,
      Class<I> initialType) {
    TypeVariable<Class<T>>[] targetParams = target.getTypeParameters();
    if (targetParams.length == 0)
      return null;

    return resolveArguments(initialType, target, target.isInterface(), null, null);
  }

  /**
   * Binds {@code declaration} type parameters to {@code instance} type arguments, returning
   * populated the {@code typeArguments}.
   */
  private static Type[] bindArguments(Type[] typeArguments, Class<?> declaration,
      ParameterizedType instance) {
    if (declaration != null && instance != null) {
      Type[] instanceArguments = instance.getActualTypeArguments();
      for (int i = 0; i < typeArguments.length; i++) {
        TypeVariable<?>[] declartionParameters = declaration.getTypeParameters();
        for (int j = 0; j < declartionParameters.length; j++)
          if (declartionParameters[j].equals(typeArguments[i]))
            typeArguments[i] = instanceArguments[j];
      }
    }

    return typeArguments;
  }

  /**
   * Resolves type arguments for the {@code rawTarget} working upwards from {@code subject}.
   * 
   * @param subject to work from
   * @param rawTarget to resolve for
   * @param resolveInterfaces indicates whether generic super interfaces should be resolved
   * @param previousDeclaration the raw subject from the previous recursive invocation
   * @param previousInstance the argument instance from the previous recursive invocation
   * @return array of fully resolved type arguments for {@code rawTarget}
   */
  private static Type[] resolveArguments(Type subject, Class<?> rawTarget,
      boolean resolveInterfaces, Class<?> previousDeclaration, ParameterizedType previousInstance) {
    ParameterizedType parameterizedSubject = null;
    Class<?> rawSubject;
    if (subject instanceof ParameterizedType) {
      parameterizedSubject = (ParameterizedType) subject;
      rawSubject = (Class<?>) parameterizedSubject.getRawType();
    } else
      rawSubject = (Class<?>) subject;

    if (rawSubject.equals(rawTarget)) {
      if (parameterizedSubject == null)
        return rawSubject.getTypeParameters();
      Type[] typeArguments = parameterizedSubject.getActualTypeArguments();
      Type[] copy = new Type[typeArguments.length];
      System.arraycopy(typeArguments, 0, copy, 0, typeArguments.length);
      return bindArguments(copy, previousDeclaration, previousInstance);
    }

    Type superType = rawSubject.getGenericSuperclass();
    if (superType != null && !superType.equals(Object.class)) {
      Type[] typeArguments = resolveArguments(superType, rawTarget, resolveInterfaces, rawSubject,
          parameterizedSubject);
      if (typeArguments != null)
        return bindArguments(typeArguments, previousDeclaration, previousInstance);
    }

    if (resolveInterfaces)
      for (Type interfaceType : rawSubject.getGenericInterfaces()) {
        Type[] typeArguments = resolveArguments(interfaceType, rawTarget, resolveInterfaces,
            rawSubject, parameterizedSubject);
        if (typeArguments != null)
          return bindArguments(typeArguments, previousDeclaration, previousInstance);
      }

    return null;
  }
}

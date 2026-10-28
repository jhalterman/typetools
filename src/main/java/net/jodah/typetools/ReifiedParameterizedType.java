package net.jodah.typetools;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.IdentityHashMap;
import java.util.Map;

class ReifiedParameterizedType implements ParameterizedType {
  private final ParameterizedType original;
  private final Type[] reifiedTypeArguments;
  // Set after the arguments so recursive owner bounds can reuse this instance.
  private Type ownerType;
  private int reified = 0;

  ReifiedParameterizedType(ParameterizedType original) {
    this.original = original;
    this.ownerType = original.getOwnerType();
    this.reifiedTypeArguments = new Type[original.getActualTypeArguments().length];
  }

  /**
   * This method is used to set reified types as they are processed. For example,
   * When reifying some {@code T<E1, E2>}, in order to reify {@code T} we need to
   * reify first {@code E1} and then {@code E2} in order. The reified counterpart
   * of {@code T} is allocated before, and then the results from reifying
   * {@code E1} and {@code E2} are added through this method.
   * 
   * @param type the reification result to be added
   */
  /* package-private */ void addReifiedTypeArgument(Type type) {
    if (reified >= reifiedTypeArguments.length) {
      return;
    }
    reifiedTypeArguments[reified++] = type;
  }

  @Override
  public Type[] getActualTypeArguments() {
    return reifiedTypeArguments;
  }

  @Override
  public Type getRawType() {
    return original.getRawType();
  }

  @Override
  public Type getOwnerType() {
    return ownerType;
  }

  void setOwnerType(Type ownerType) {
    this.ownerType = ownerType;
  }

  /**
   * Keep this consistent with
   * {@link sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl#toString}
   */
  @Override
  public String toString() {
    return toString(new IdentityHashMap<Type, Boolean>());
  }

  private String toString(Map<Type, Boolean> visiting) {
    if (visiting.containsKey(this))
      return "...";
    visiting.put(this, Boolean.TRUE);
    final Type ownerType = getOwnerType();
    final Type rawType = getRawType();
    final Type[] actualTypeArguments = getActualTypeArguments();

    final StringBuilder sb = new StringBuilder();

    if (ownerType != null) {
      if (ownerType instanceof Class) {
        sb.append(((Class) ownerType).getName());
      } else {
        sb.append(typeName(ownerType, visiting));
      }

      sb.append("$");

      if (ownerType instanceof ParameterizedType) {
        // Find simple name of nested type by removing the
        // shared prefix with owner.
        sb.append(rawType.getTypeName().replace(((ParameterizedType) ownerType).getRawType().getTypeName() + "$", ""));
      } else if (rawType instanceof Class) {
        sb.append(((Class) rawType).getSimpleName());
      } else {
        sb.append(rawType.getTypeName());
      }
    } else {
      sb.append(rawType.getTypeName());
    }

    if (actualTypeArguments.length > 0) {
      sb.append("<");

      for (int i = 0; i < actualTypeArguments.length; i++) {
        if (i != 0) {
          sb.append(", ");
        }

        final Type t = actualTypeArguments[i];

        if (i >= reified) {
          sb.append("?");
        } else if (t == null) {
          sb.append("null");
        } else if (t == this) {
          // Instead of recursing into this argument, which would
          // overflow the stack,
          // print three dots to indicate "self-loop structure is
          // here". Note
          // that if the full string examined is some other type that
          // contains this
          // instance, then the notation is ambiguous: We don't know
          // which type
          // is self-loop where.
          sb.append("...");
        } else {
          sb.append(typeName(t, visiting));
        }
      }
      sb.append(">");
    }

    visiting.remove(this);
    return sb.toString();
  }

  private static String typeName(Type type, Map<Type, Boolean> visiting) {
    return type instanceof ReifiedParameterizedType ? ((ReifiedParameterizedType) type).toString(visiting)
        : type.getTypeName();
  }

  @Override
  public boolean equals(Object o) {
    return equals(this, o, new IdentityHashMap<Object, Object>(), new IdentityHashMap<Object, Object>());
  }

  private static boolean equals(Object a, Object b, Map<Object, Object> left, Map<Object, Object> right) {
    // Compare back references before identity: a shared node can occur at different
    // positions in the two graphs.
    if (left.containsKey(a) || right.containsKey(b))
      return left.get(a) == b && right.get(b) == a;
    if (a == b)
      return true;
    if ((a instanceof ParameterizedType) != (b instanceof ParameterizedType))
      return false;
    if (!(a instanceof ParameterizedType))
      return a != null && a.equals(b);

    ParameterizedType first = (ParameterizedType) a;
    ParameterizedType second = (ParameterizedType) b;
    left.put(a, b);
    right.put(b, a);
    try {
      if (!equals(first.getRawType(), second.getRawType(), left, right)
          || !equals(first.getOwnerType(), second.getOwnerType(), left, right))
        return false;
      Type[] firstArguments = first.getActualTypeArguments();
      Type[] secondArguments = second.getActualTypeArguments();
      if (firstArguments.length != secondArguments.length)
        return false;
      for (int i = 0; i < firstArguments.length; i++) {
        if (!equals(firstArguments[i], secondArguments[i], left, right))
          return false;
      }
      return true;
    } finally {
      left.remove(a);
      right.remove(b);
    }
  }

  @Override
  public int hashCode() {
    return hashCode(this, new IdentityHashMap<Type, Boolean>());
  }

  static int hashCode(Object o) {
    // Replace with Objects.hashCode
    return o != null ? o.hashCode() : 0;
  }

  private static int hashCode(Type type, Map<Type, Boolean> visiting) {
    if (!(type instanceof ParameterizedType))
      return hashCode(type);
    // Track the current path, not all visited nodes: repeated acyclic arguments
    // must retain the same hash as independently constructed equal arguments.
    if (visiting.containsKey(type))
      return 0;
    visiting.put(type, Boolean.TRUE);
    ParameterizedType parameterizedType = (ParameterizedType) type;
    try {
      int argumentsHash = 1;
      for (Type argument : parameterizedType.getActualTypeArguments())
        argumentsHash = 31 * argumentsHash + hashCode(argument, visiting);
      return argumentsHash ^ hashCode(parameterizedType.getRawType(), visiting)
          ^ hashCode(parameterizedType.getOwnerType(), visiting);
    } finally {
      visiting.remove(type);
    }
  }
}

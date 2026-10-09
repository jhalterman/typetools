package net.jodah.typetools;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

class ReifiedParameterizedType implements ParameterizedType {
    private final ParameterizedType original;
    private final Type[] reifiedTypeArguments;
    private int reified = 0;

    ReifiedParameterizedType(ParameterizedType original) {
      this.original = original;
      this.reifiedTypeArguments = new Type[original.getActualTypeArguments().length];
    }

    /**
     * This method is used to set reified types as they are processed. For example,
     * When reifying some {@code T<E1, E2>}, in order to reify {@code T} we need
     * to reify first {@code E1} and then {@code E2} in order. The reified counterpart
     * of {@code T} is allocated before, and then the results from reifying {@code E1}
     * and {@code E2} are added through this method.
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
      return original.getOwnerType();
    }

    /**
     * Keep this consistent with {@link sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl#toString}
     */
    @Override
    public String toString() {
      final Type ownerType = getOwnerType();
      final Type rawType = getRawType();
      final Type[] actualTypeArguments = getActualTypeArguments();

      final StringBuilder sb = new StringBuilder();

      if (ownerType != null) {
        if (ownerType instanceof Class) {
          sb.append(((Class) ownerType).getName());
        } else {
          sb.append(ownerType.toString());
        }

        sb.append("$");

        if (ownerType instanceof ParameterizedType) {
          // Find simple name of nested type by removing the
          // shared prefix with owner.
          sb.append(rawType.getTypeName()
              .replace(((ParameterizedType) ownerType).getRawType().getTypeName() + "$", ""));
        } else if (rawType instanceof Class){
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
            // Instead of recursing into this argument, which would overflow the stack,
            // print three dots to indicate "self-loop structure is here". Note
            // that if the full string examined is some other type that contains this
            // instance, then the notation is ambiguous: We don't know which type
            // is self-loop where.
            sb.append("...");
          } else {
            sb.append(t.getTypeName());
          }
        }
        sb.append(">");
      }

      return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof ParameterizedType)) {
        return false;
      }

      ParameterizedType that = (ParameterizedType) o;
      return equals(getRawType(), that.getRawType())
                && equals(getOwnerType(), that.getOwnerType())
                && selfReferentialArrayEquals(this, reifiedTypeArguments, that, that.getActualTypeArguments());
    }

    private static boolean equals(Object a, Object b) {
      // Replace with Objects.equals
      return (a == b) || (a != null && a.equals(b));
    }

    private static boolean selfReferentialArrayEquals(Type a, Type[] aValues, Type b, Type[] bValues) {
      if (aValues.length != bValues.length) {
        return false;
      }

      for (int i = 0; i < aValues.length; i++) {
        Type aValue = aValues[i];
        Type bValue = bValues[i];
        // both must loop, or not loop
        if ((aValue == a) != (bValue == b)) {
          return false;
        }
        // skip loops
        if (aValue == a) {
          continue;
        }
        if (!equals(aValue, bValue)) {
          return false;
        }
      }
      return true;
    }

    @Override
    public int hashCode() {
      return selfReferentialArrayHashCode(this, reifiedTypeArguments) ^ hashCode(getRawType()) ^ hashCode(getOwnerType());
    }

    static int hashCode(Object o) {
      // Replace with Objects.hashCode
      return o != null ? o.hashCode() : 0;
    }

    private static int selfReferentialArrayHashCode(Type a, Type[] aValues) {
      int result = 1;
      for (Type aValue : aValues) {
        // skip loops
        result = 31 * result + (aValue == a ? 0 : hashCode(aValue));
      }
      return result;
    }
}

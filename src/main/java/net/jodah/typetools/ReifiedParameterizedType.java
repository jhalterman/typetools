package net.jodah.typetools;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

class ReifiedParameterizedType implements ParameterizedType {
    private final ParameterizedType original;
    private final Type[] reifiedTypeArguments;

    ReifiedParameterizedType(ParameterizedType original) {
      this.original = original;
      this.reifiedTypeArguments = new Type[original.getActualTypeArguments().length];
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
     * NOTE: This method should only be called once per instance.
     */
    void setReifiedTypeArguments(Type[] reifiedTypeArguments) {
      System.arraycopy(reifiedTypeArguments, 0, this.reifiedTypeArguments, 0, this.reifiedTypeArguments.length);
    }

    /** Keep this consistent with {@link sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl#toString} */
    @Override
    public String toString() {
      final Type ownerType = getOwnerType();
      final Type rawType = getRawType();
      final Type[] actualTypeArguments = getActualTypeArguments();

      StringBuilder sb = new StringBuilder();

      if (ownerType != null) {
        if (ownerType instanceof Class) {
          sb.append(((Class) ownerType).getName());
        } else {
          sb.append(ownerType.toString());
        }

        sb.append(".");

        if (ownerType instanceof ParameterizedType) {
          // Find simple name of nested type by removing the
          // shared prefix with owner.
          sb.append(rawType.getTypeName()
              .replace(((ParameterizedType) ownerType).getRawType().getTypeName() + "$", ""));
        } else {
          sb.append(rawType.getTypeName());
        }
      } else {
        sb.append(rawType.getTypeName());
      }

      if (actualTypeArguments != null && actualTypeArguments.length > 0) {
        sb.append("<");

        boolean first = true;
        for (Type t: actualTypeArguments) {
          if (!first) {
            sb.append(", ");
          }
          sb.append(t == null ? "null" : t.getTypeName());
          first = false;
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
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      ReifiedParameterizedType that = (ReifiedParameterizedType) o;
      return original.equals(that.original) &&
          Arrays.equals(reifiedTypeArguments, that.reifiedTypeArguments);
    }

    @Override
    public int hashCode() {
      int result = original.hashCode();
      result = 31 * result + Arrays.hashCode(reifiedTypeArguments);
      return result;
    }
  }
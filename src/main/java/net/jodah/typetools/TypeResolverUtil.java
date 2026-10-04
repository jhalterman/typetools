package net.jodah.typetools;

import java.lang.reflect.AccessibleObject;

final class TypeResolverUtil {

  private TypeResolverUtil() {
    /* no-op */
  }

  static Class<?> getConstantPoolClass() throws Throwable {
    return Class.forName("sun.reflect.ConstantPool");
  }

  static Class<?> getSharedSecretsClass() throws Throwable {
    return Class.forName("sun.misc.SharedSecrets");
  }

  static AccessMaker createAccessMaker() throws Throwable {
    // Java 8 and lower can simply call setAccessible
    return new AccessMaker() {
      @Override
      public void makeAccessible(AccessibleObject accessibleObject) {
        accessibleObject.setAccessible(true);
      }
    };
  }

}

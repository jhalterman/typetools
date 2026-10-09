package net.jodah.typetools;

import sun.misc.Unsafe;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;

/**
 * Unique name so Jacoco can see the coverage
 */
final class TypeResolverUtil9 {

  private TypeResolverUtil9() {
    /* no-op */
  }

  static Class<?> getConstantPoolClass() throws Throwable {
    return Class.forName("jdk.internal.reflect.ConstantPool");
  }

  static Class<?> getSharedSecretsClass() throws Throwable {
    try {
      return Class.forName("jdk.internal.misc.SharedSecrets");
    } catch (ClassNotFoundException e) {
      // In Oracle JDK 11.0.6, SharedSecrets was moved from jdk.internal.misc to jdk.internal.access.
      return Class.forName("jdk.internal.access.SharedSecrets");
    }
  }

  static AccessMaker createAccessMaker() throws Throwable {
    return createAccessMakerUsingUnsafe();
  }

  private static AccessMaker createAccessMakerUsingUnsafe() throws Throwable {
    Unsafe unsafe = AccessController.doPrivileged((PrivilegedExceptionAction<Unsafe>) () -> {
      Field f = Unsafe.class.getDeclaredField("theUnsafe");
      f.setAccessible(true);
      return (Unsafe) f.get(null);
    });

    // access control got strengthened in Java 9, but can be circumvented with Unsafe.
    Field overrideField = AccessibleObject.class.getDeclaredField("override");
    long overrideFieldOffset = unsafe.objectFieldOffset(overrideField);
    return accessibleObject -> unsafe.putBoolean(accessibleObject, overrideFieldOffset, true);
  }

}

package net.jodah.typetools;

import sun.misc.Unsafe;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

final class TypeResolverUtil {

  private TypeResolverUtil() {
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

  private static AccessMaker createAccessMakerUsingUnsafe() throws PrivilegedActionException, NoSuchFieldException {
    final Unsafe unsafe = AccessController.doPrivileged((PrivilegedExceptionAction<Unsafe>) () -> {
      final Field f = Unsafe.class.getDeclaredField("theUnsafe");
      f.setAccessible(true);
      return (Unsafe) f.get(null);
    });
    // access control got strengthened in Java 9, but can be circumvented with Unsafe.
    Field overrideField = AccessibleObject.class.getDeclaredField("override");
    final long overrideFieldOffset = unsafe.objectFieldOffset(overrideField);
    return accessibleObject -> unsafe.putBoolean(accessibleObject, overrideFieldOffset, true);
  }

}

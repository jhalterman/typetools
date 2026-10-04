package net.jodah.typetools;

import sun.misc.Unsafe;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

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
    try {
      // If --add-opens flags are present we can use MethodHandles.privateLookupIn()
      return createAccessMakerUsingMethodHandle();
    } catch (IllegalAccessException ignored) {
      // Fall back to sun.misc.Unsafe
      return createAccessMakerUsingUnsafe();
    }
  }

  private static AccessMaker createAccessMakerUsingMethodHandle() throws IllegalAccessException, NoSuchFieldException {
    MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(AccessibleObject.class, MethodHandles.lookup());
    MethodHandle overrideSetter = lookup.findSetter(AccessibleObject.class, "override", boolean.class);
    return object -> overrideSetter.invokeWithArguments(new Object[]{object, true});
  }

  private static AccessMaker createAccessMakerUsingUnsafe() throws PrivilegedActionException, NoSuchFieldException {
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

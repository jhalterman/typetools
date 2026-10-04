package net.jodah.typetools;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

final class TypeResolverUtil {

  private TypeResolverUtil() {
    /* no-op */
  }

  static Class<?> getConstantPoolClass() throws Throwable {
    return Class.forName("jdk.internal.reflect.ConstantPool");
  }

  static Class<?> getSharedSecretsClass() throws Throwable {
      return Class.forName("jdk.internal.access.SharedSecrets");
  }

  static AccessMaker createAccessMaker() throws Throwable {
      try {
        return createAccessMakerUsingMethodHandle();
    } catch (IllegalAccessException ignored) {
        return createAccessMakerUsingUnsafe();
    }
  }

  private static AccessMaker createAccessMakerUsingMethodHandle() throws IllegalAccessException, NoSuchFieldException {
    // If --add-opens flags are present we can use MethodHandles.privateLookupIn()
    MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(AccessibleObject.class, MethodHandles.lookup());
    MethodHandle overrideSetter = lookup.findSetter(AccessibleObject.class, "override", boolean.class);
    return object -> overrideSetter.invokeWithArguments(new Object[]{object, true});
  }

  private static AccessMaker createAccessMakerUsingUnsafe() throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
    // Fall back to IMPL_LOOKUP via sun.misc.Unsafe
    // On Java 22+ this will warn that sun.misc.Unsafe::staticFieldOffset has been terminally deprecated
    Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
    Field theUnsafeField = unsafeClass.getDeclaredField("theUnsafe");
    theUnsafeField.setAccessible(true);
    Object unsafe = theUnsafeField.get(null);
    // In Java 12, AccessibleObject.override was added to the reflection blacklist.
    // Access checking can still be circumvented by using the Unsafe technique to get the implementation lookup from MethodHandles.
    Field implLookupField = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
    long implLookupFieldOffset = (Long) unsafeClass.getMethod("staticFieldOffset", Field.class)
            .invoke(unsafe, implLookupField);
    Object lookupStaticFieldBase = unsafeClass.getMethod("staticFieldBase", Field.class)
            .invoke(unsafe, implLookupField);
    final MethodHandles.Lookup implLookup = (MethodHandles.Lookup) unsafeClass
            .getMethod("getObject", Object.class, long.class)
            .invoke(unsafe, lookupStaticFieldBase, implLookupFieldOffset);
    final MethodHandle overrideSetter = implLookup.findSetter(AccessibleObject.class, "override", boolean.class);
    return object -> overrideSetter.invokeWithArguments(new Object[]{object, true});
  }

}

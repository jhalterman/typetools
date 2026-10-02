package net.jodah.typetools;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

final class LambdaConstantsResolver {

  private LambdaConstantsResolver() {
    /* no-op */
  }

  private interface AccessMaker {
    void makeAccessible(AccessibleObject object) throws Throwable;
  }

  static LambdaConstants resolve() {
    try {
      AccessMaker accessSetter;
      try {
        // If --add-opens flags are present we can use MethodHandles.privateLookupIn()
        MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(AccessibleObject.class, MethodHandles.lookup());
        MethodHandle overrideSetter = lookup.findSetter(AccessibleObject.class, "override", boolean.class);
        accessSetter = new AccessMaker() {
          @Override
          public void makeAccessible(AccessibleObject object) throws Throwable {
            overrideSetter.invokeWithArguments(new Object[]{object, true});
          }
        };
      } catch (IllegalAccessException ignored) {
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
        accessSetter = new AccessMaker() {
          @Override
          public void makeAccessible(AccessibleObject object) throws Throwable {
            overrideSetter.invokeWithArguments(new Object[]{object, true});
          }
        };
      }

      Class<?> sharedSecretsClass = Class.forName("jdk.internal.access.SharedSecrets");
      Method javaLangAccessGetter = sharedSecretsClass.getMethod("getJavaLangAccess");
      accessSetter.makeAccessible(javaLangAccessGetter);
      Object javaLangAccess = javaLangAccessGetter.invoke(null);
      Method getConstantPool = javaLangAccess.getClass().getMethod("getConstantPool", Class.class);

      String constantPoolName = "jdk.internal.reflect.ConstantPool";
      Class<?> constantPoolClass = Class.forName(constantPoolName);
      Method getConstantPoolSize = constantPoolClass.getDeclaredMethod("getSize");
      Method getConstantPoolMethodAt = constantPoolClass.getDeclaredMethod("getMethodAt", int.class);

      // setting the methods as accessible
      accessSetter.makeAccessible(getConstantPool);
      accessSetter.makeAccessible(getConstantPoolSize);
      accessSetter.makeAccessible(getConstantPoolMethodAt);

      // additional checks - make sure we get a result when invoking the Class::getConstantPool and
      // ConstantPool::getSize on a class
      Object constantPool = getConstantPool.invoke(javaLangAccess, Object.class);
      getConstantPoolSize.invoke(constantPool);

      return new LambdaConstants(
          javaLangAccess,
          getConstantPool,
          getConstantPoolSize,
          getConstantPoolMethodAt
      );
    } catch (Throwable ignore) {
    }
    return null;
  }

}

package net.jodah.typetools;

import sun.misc.Unsafe;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

final class LambdaConstantsResolver {

  private LambdaConstantsResolver() {
    /* no-op */
  }

  private interface AccessMaker {
    void makeAccessible(AccessibleObject object) throws Throwable;
  }

  static LambdaConstants resolve() {
    try {
      AccessMaker accessSetter = createAccessMakerUsingUnsafe();

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

  private static AccessMaker createAccessMakerUsingUnsafe() throws PrivilegedActionException, NoSuchFieldException, IllegalAccessException {
    final Unsafe unsafe = AccessController.doPrivileged((PrivilegedExceptionAction<Unsafe>) () -> {
      final Field f = Unsafe.class.getDeclaredField("theUnsafe");
      f.setAccessible(true);
      return (Unsafe) f.get(null);
    });

    // In Java 12, AccessibleObject.override was added to the reflection blacklist.
    // Access checking can still be circumvented by using the Unsafe technique to get the implementation lookup from MethodHandles.
    Field implLookupField = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
    long implLookupFieldOffset = unsafe.staticFieldOffset(implLookupField);
    Object lookupStaticFieldBase = unsafe.staticFieldBase(implLookupField);
    MethodHandles.Lookup implLookup = (MethodHandles.Lookup) unsafe.getObject(lookupStaticFieldBase, implLookupFieldOffset);
    final MethodHandle overrideSetter = implLookup.findSetter(AccessibleObject.class, "override", boolean.class);
    return object -> overrideSetter.invokeWithArguments(new Object[]{object, true});
  }
}

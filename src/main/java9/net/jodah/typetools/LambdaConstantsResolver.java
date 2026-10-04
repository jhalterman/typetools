package net.jodah.typetools;

import sun.misc.Unsafe;

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

  static LambdaConstants resolve() {
    try {
      AccessMaker accessSetter = createAccessMaker();

      Class<?> sharedSecretsClass = getSharedSecretsClass();
      Method javaLangAccessGetter = sharedSecretsClass.getMethod("getJavaLangAccess");
      accessSetter.makeAccessible(javaLangAccessGetter);
      Object javaLangAccess = javaLangAccessGetter.invoke(null);
      Method getConstantPool = javaLangAccess.getClass().getMethod("getConstantPool", Class.class);

      Class<?> constantPoolClass = getConstantPoolClass();
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

  private static Class<?> getConstantPoolClass() throws ClassNotFoundException {
    return Class.forName("jdk.internal.reflect.ConstantPool");
  }

  private static AccessMaker createAccessMaker() throws PrivilegedActionException, NoSuchFieldException {
    return createAccessMakerUsingUnsafe();
  }

  private static Class<?> getSharedSecretsClass() throws ClassNotFoundException {
      try {
        return Class.forName("jdk.internal.misc.SharedSecrets");
    } catch (ClassNotFoundException e) {
      // In Oracle JDK 11.0.6, SharedSecrets was moved from jdk.internal.misc to jdk.internal.access.
        return Class.forName("jdk.internal.access.SharedSecrets");
    }
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

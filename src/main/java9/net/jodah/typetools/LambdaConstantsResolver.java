package net.jodah.typetools;

import sun.misc.Unsafe;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.AccessController;
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
      final Unsafe unsafe = AccessController.doPrivileged(new PrivilegedExceptionAction<Unsafe>() {
        @Override
        public Unsafe run() throws Exception {
          final Field f = Unsafe.class.getDeclaredField("theUnsafe");
          f.setAccessible(true);

          return (Unsafe) f.get(null);
        }
      });

      Class<?> sharedSecretsClass;
      try {
        sharedSecretsClass = Class.forName("jdk.internal.misc.SharedSecrets");
      } catch (ClassNotFoundException e) {
        // In Oracle JDK 11.0.6, SharedSecrets was moved from jdk.internal.misc to jdk.internal.access.
        sharedSecretsClass = Class.forName("jdk.internal.access.SharedSecrets");
      }
      // access control got strengthed in Java 9, but can be circumvented with Unsafe.
      Field overrideField = AccessibleObject.class.getDeclaredField("override");
      final long overrideFieldOffset = unsafe.objectFieldOffset(overrideField);
      AccessMaker accessSetter = new AccessMaker() {
        @Override
        public void makeAccessible(AccessibleObject accessibleObject) {
          unsafe.putBoolean(accessibleObject, overrideFieldOffset, true);
        }
      };

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

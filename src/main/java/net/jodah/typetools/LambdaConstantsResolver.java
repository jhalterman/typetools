package net.jodah.typetools;

import java.lang.reflect.AccessibleObject;
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
    return Class.forName("sun.reflect.ConstantPool");
  }

  private static Class<?> getSharedSecretsClass() throws ClassNotFoundException {
      return Class.forName("sun.misc.SharedSecrets");
  }

  private static AccessMaker createAccessMaker() {
    // Java 8 and lower can simply call setAccessible
    return new AccessMaker() {
      @Override
      public void makeAccessible(AccessibleObject accessibleObject) {
        accessibleObject.setAccessible(true);
      }
    };
  }
}

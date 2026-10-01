package net.jodah.typetools;

import sun.misc.Unsafe;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.HashMap;
import java.util.Map;

final class LambdaConstantsResolver {

  private LambdaConstantsResolver() {
    /* no-op */
  }

  private interface AccessMaker {
    void makeAccessible(AccessibleObject object) throws Throwable;
  }

  static LambdaConstants resolve(Double JAVA_VERSION) {
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
      AccessMaker accessSetter;
      if (JAVA_VERSION < 9) {
        sharedSecretsClass = Class.forName("sun.misc.SharedSecrets");
        // Java 8 and lower can simply call setAccessible
        accessSetter = new AccessMaker() {
          @Override
          public void makeAccessible(AccessibleObject accessibleObject) {
            accessibleObject.setAccessible(true);
          }
        };
      } else if (JAVA_VERSION < 12) {
        try {
          sharedSecretsClass = Class.forName("jdk.internal.misc.SharedSecrets");
        } catch (ClassNotFoundException e) {
          // In Oracle JDK 11.0.6, SharedSecrets was moved from jdk.internal.misc to jdk.internal.access.
          sharedSecretsClass = Class.forName("jdk.internal.access.SharedSecrets");
        }
        // access control got strengthed in Java 9, but can be circumvented with Unsafe.
        Field overrideField = AccessibleObject.class.getDeclaredField("override");
        final long overrideFieldOffset = unsafe.objectFieldOffset(overrideField);
        accessSetter = new AccessMaker() {
          @Override
          public void makeAccessible(AccessibleObject accessibleObject) {
            unsafe.putBoolean(accessibleObject, overrideFieldOffset, true);
          }
        };
      } else {
        sharedSecretsClass = Class.forName("jdk.internal.access.SharedSecrets");
        // In Java 12, AccessibleObject.override was added to the reflection blacklist.
        // Access checking can still be circumvented by using the Unsafe technique to get the implementation lookup from MethodHandles.
        Field implLookupField = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
        long implLookupFieldOffset = unsafe.staticFieldOffset(implLookupField);
        Object lookupStaticFieldBase = unsafe.staticFieldBase(implLookupField);
        MethodHandles.Lookup implLookup = (MethodHandles.Lookup) unsafe.getObject(lookupStaticFieldBase, implLookupFieldOffset);
        final MethodHandle overrideSetter = implLookup.findSetter(AccessibleObject.class, "override", boolean.class);
        accessSetter = new AccessMaker() {
          @Override
          public void makeAccessible(AccessibleObject object) throws Throwable {
            overrideSetter.invokeWithArguments(new Object[]{object, true});
          }
        };
      }
      Method javaLangAccessGetter = sharedSecretsClass.getMethod("getJavaLangAccess");
      accessSetter.makeAccessible(javaLangAccessGetter);
      Object javaLangAccess = javaLangAccessGetter.invoke(null);
      Method getConstantPool = javaLangAccess.getClass().getMethod("getConstantPool", Class.class);

      String constantPoolName = JAVA_VERSION < 9 ? "sun.reflect.ConstantPool" : "jdk.internal.reflect.ConstantPool";
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
      Map<String, Method> objectMethods = new HashMap<String, Method>();
      for (Method method : Object.class.getDeclaredMethods())
        objectMethods.put(method.getName(), method);

      return new LambdaConstants(
          javaLangAccess,
          getConstantPool,
          getConstantPoolSize,
          getConstantPoolMethodAt,
          objectMethods
      );
    } catch (Throwable ignore) {
    }
    return null;
  }
}

package net.jodah.typetools;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;

public class ConstantPools {

  public static Result resolveConstantPoolMethods() {
    double javaVersion = Double.parseDouble(System.getProperty("java.specification.version", "0"));
    if (javaVersion < 9) {
      return resolveByReflection();
    } else {
      return resolveByLookup();
    }
  }

  public static Result resolveByReflection() {
    try {
      Class<?> constantPoolClass = Class.forName("sun.reflect.ConstantPool");
      Method getConstantPool = Class.class.getDeclaredMethod("getConstantPool");
      getConstantPool.setAccessible(true);
      Method getConstantPoolSize = constantPoolClass.getDeclaredMethod("getSize");
      getConstantPoolSize.setAccessible(true);
      Method getConstantPoolMethodAt = constantPoolClass
          .getDeclaredMethod("getMethodAt", int.class);
      getConstantPoolMethodAt.setAccessible(true);
      return new Result(getConstantPool, getConstantPoolSize, getConstantPoolMethodAt);
    } catch (Exception e) {
      return new Result(e);
    }
  }

  public static Result resolveByLookup() {
    try {
      Method privateLookupIn = MethodHandles.class.getMethod("privateLookupIn", Class.class, Lookup.class);
      Lookup lookup = (Lookup) privateLookupIn.invoke(null, AccessibleObject.class, MethodHandles.lookup());
      MethodHandle overrideSetter = lookup.findSetter(AccessibleObject.class, "override", boolean.class);

      Class<?> constantPoolClass = Class.forName("jdk.internal.reflect.ConstantPool");
      Method getConstantPool = Class.class.getDeclaredMethod("getConstantPool");
      overrideSetter.invoke(getConstantPool, true);
      Method getConstantPoolSize = constantPoolClass.getDeclaredMethod("getSize");
      overrideSetter.invoke(getConstantPoolSize, true);
      Method getConstantPoolMethodAt = constantPoolClass.getDeclaredMethod("getMethodAt", int.class);
      overrideSetter.invoke(getConstantPoolMethodAt, true);
      return new Result(getConstantPool, getConstantPoolSize, getConstantPoolMethodAt);
    } catch (Throwable e) {
      return new Result(e);
    }
  }

  public static class Result {
    private final Throwable failure;
    private final Method getConstantPool;
    private final Method getConstantPoolSize;
    private final Method getConstantPoolMethodAt;

    public Result(Throwable failure) {
      this.failure = failure;
      this.getConstantPool = null;
      this.getConstantPoolSize = null;
      this.getConstantPoolMethodAt = null;
    }

    public Result(Method getConstantPool, Method getConstantPoolSize,
        Method getConstantPoolMethodAt) {
      this.failure = null;
      this.getConstantPool = getConstantPool;
      this.getConstantPoolSize = getConstantPoolSize;
      this.getConstantPoolMethodAt = getConstantPoolMethodAt;
    }

    public boolean resolved() {
      return failure == null;
    }

    public Method getGetConstantPool() {
      return getConstantPool;
    }

    public Method getGetConstantPoolSize() {
      return getConstantPoolSize;
    }

    public Method getGetConstantPoolMethodAt() {
      return getConstantPoolMethodAt;
    }
  }
}

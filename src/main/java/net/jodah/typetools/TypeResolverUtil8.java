package net.jodah.typetools;

import java.lang.reflect.AccessibleObject;

final class TypeResolverUtil8 implements TypeResolverUtil {

  @Override
  public Class<?> getConstantPoolClass() throws Throwable {
    return Class.forName("sun.reflect.ConstantPool");
  }

  @Override
  public Class<?> getSharedSecretsClass() throws Throwable {
    return Class.forName("sun.misc.SharedSecrets");
  }

  @Override
  public AccessMaker createAccessMaker() throws Throwable {
    // Java 8 and lower can simply call setAccessible
    return new AccessMaker() {
      @Override
      public void makeAccessible(AccessibleObject accessibleObject) {
        accessibleObject.setAccessible(true);
      }
    };
  }

}

package net.jodah.typetools;

final class TypeResolverUtil {

  private TypeResolverUtil() {
    /* no-op */
  }

  static Class<?> getConstantPoolClass() throws Throwable {
    return TypeResolverUtil22.getConstantPoolClass();
  }

  static Class<?> getSharedSecretsClass() throws Throwable {
   return TypeResolverUtil22.getSharedSecretsClass();
  }

  static AccessMaker createAccessMaker() throws Throwable {
    return TypeResolverUtil22.createAccessMaker();
  }

}

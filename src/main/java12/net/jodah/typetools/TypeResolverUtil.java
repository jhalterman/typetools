package net.jodah.typetools;

final class TypeResolverUtil {

  private TypeResolverUtil() {
    /* no-op */
  }

  static Class<?> getConstantPoolClass() throws Throwable {
    return TypeResolverUtil12.getConstantPoolClass();
  }

  static Class<?> getSharedSecretsClass() throws Throwable {
   return TypeResolverUtil12.getSharedSecretsClass();
  }

  static AccessMaker createAccessMaker() throws Throwable {
    return TypeResolverUtil12.createAccessMaker();
  }

}

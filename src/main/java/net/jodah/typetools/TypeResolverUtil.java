package net.jodah.typetools;

interface TypeResolverUtil {

  Class<?> getConstantPoolClass() throws Throwable;

  Class<?> getSharedSecretsClass() throws Throwable;

  AccessMaker createAccessMaker() throws Throwable;

}

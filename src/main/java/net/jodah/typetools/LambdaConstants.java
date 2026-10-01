package net.jodah.typetools;

import java.lang.reflect.Method;

final class LambdaConstants {
  final Object JAVA_LANG_ACCESS;
  final Method GET_CONSTANT_POOL;
  final Method GET_CONSTANT_POOL_SIZE;
  final Method GET_CONSTANT_POOL_METHOD_AT;

  LambdaConstants(Object javaLangAccess, Method getConstantPool, Method getConstantPoolSize, Method getConstantPoolMethodAt) {
    JAVA_LANG_ACCESS = javaLangAccess;
    GET_CONSTANT_POOL = getConstantPool;
    GET_CONSTANT_POOL_SIZE = getConstantPoolSize;
    GET_CONSTANT_POOL_METHOD_AT = getConstantPoolMethodAt;
  }
}

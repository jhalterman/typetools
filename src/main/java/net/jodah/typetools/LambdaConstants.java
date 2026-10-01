package net.jodah.typetools;

import java.lang.reflect.Method;
import java.util.Map;

final class LambdaConstants {
  final Object JAVA_LANG_ACCESS;
  final Method GET_CONSTANT_POOL;
  final Method GET_CONSTANT_POOL_SIZE;
  final Method GET_CONSTANT_POOL_METHOD_AT;
  final Map<String, Method> OBJECT_METHODS;

  LambdaConstants(Object javaLangAccess, Method getConstantPool, Method getConstantPoolSize, Method getConstantPoolMethodAt, Map<String, Method> objectMethods) {
    JAVA_LANG_ACCESS = javaLangAccess;
    GET_CONSTANT_POOL = getConstantPool;
    GET_CONSTANT_POOL_SIZE = getConstantPoolSize;
    GET_CONSTANT_POOL_METHOD_AT = getConstantPoolMethodAt;
    OBJECT_METHODS = objectMethods;
  }
}

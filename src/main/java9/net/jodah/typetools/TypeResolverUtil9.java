package net.jodah.typetools;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.AccessibleObject;

final class TypeResolverUtil9 {

  private TypeResolverUtil9() {
    /* no-op */
  }

  static Class<?> getConstantPoolClass() throws Throwable {
    return Class.forName("jdk.internal.reflect.ConstantPool");
  }

  static Class<?> getSharedSecretsClass() throws Throwable {
    try {
      return Class.forName("jdk.internal.misc.SharedSecrets");
    } catch (ClassNotFoundException e) {
      // In Oracle JDK 11.0.6, SharedSecrets was moved from jdk.internal.misc to jdk.internal.access.
      return Class.forName("jdk.internal.access.SharedSecrets");
    }
  }

  static AccessMaker createAccessMaker() throws Throwable {
    return createAccessMakerUsingMethodHandle();
  }

  private static AccessMaker createAccessMakerUsingMethodHandle() throws IllegalAccessException, NoSuchFieldException {
    MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(AccessibleObject.class, MethodHandles.lookup());
    MethodHandle overrideSetter = lookup.findSetter(AccessibleObject.class, "override", boolean.class);
    return object -> overrideSetter.invokeWithArguments(new Object[]{object, true});
  }

}

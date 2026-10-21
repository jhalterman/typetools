package net.jodah.typetools;

import sun.misc.Unsafe;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.util.logging.Logger;

final class TypeResolverUtil22 implements TypeResolverUtil {

  @Override
  public Class<?> getConstantPoolClass() throws Throwable {
    return Class.forName("jdk.internal.reflect.ConstantPool");
  }

  @Override
  public Class<?> getSharedSecretsClass() throws Throwable {
    return Class.forName("jdk.internal.access.SharedSecrets");
  }

  @Override
  public AccessMaker createAccessMaker() throws Throwable {
    try {
      // If --add-opens flags are present we can use MethodHandles.privateLookupIn()
      return createAccessMakerUsingMethodHandle();
    } catch (IllegalAccessException ignored) {
      logAddOpensWarnings();
      // Fall back to IMPL_LOOKUP via sun.misc.Unsafe
      // On Java 22+ this will warn that sun.misc.Unsafe::staticFieldOffset has been
      // terminally deprecated
      return createAccessMakerUsingUnsafe();
    }
  }

  private static AccessMaker createAccessMakerUsingMethodHandle() throws IllegalAccessException, NoSuchFieldException {
    MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(AccessibleObject.class, MethodHandles.lookup());
    MethodHandle overrideSetter = lookup.findSetter(AccessibleObject.class, "override", boolean.class);
    return object -> overrideSetter.invokeWithArguments(new Object[] { object, true });
  }

  private static void logAddOpensWarnings() {
    var logger = Logger.getLogger(TypeResolverUtil22.class.getName());
    logger.warning(() -> {
      var module = TypeResolverUtil22.class.getModule();
      var moduleName = module.isNamed() ? module.getName() : "ALL-UNNAMED";
      return """
          typetools needs to access the constant pool to resolve lambda arguments.

          This warning can be avoided by using these JVM flags:

          ```
          --add-opens java.base/java.lang.reflect=%s
          --add-opens java.base/jdk.internal.access=%s
          --add-opens java.base/jdk.internal.reflect=%s
          ```
          """.formatted(moduleName, moduleName, moduleName);
    });
  }

  @SuppressWarnings("removal")
  private static AccessMaker createAccessMakerUsingUnsafe() throws NoSuchFieldException, IllegalAccessException {
    // AccessController.doPrivileged deprecated and now a no-op
    Field theUnsafeField = Unsafe.class.getDeclaredField("theUnsafe");
    theUnsafeField.setAccessible(true);
    Unsafe unsafe = (Unsafe) theUnsafeField.get(null);

    // In Java 12, AccessibleObject.override was added to the reflection blacklist.
    // Access checking can still be circumvented by using the Unsafe technique to
    // get the implementation lookup from MethodHandles.
    Field implLookupField = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
    long implLookupFieldOffset = unsafe.staticFieldOffset(implLookupField);
    Object lookupStaticFieldBase = unsafe.staticFieldBase(implLookupField);
    MethodHandles.Lookup implLookup = (MethodHandles.Lookup) unsafe.getObject(lookupStaticFieldBase,
        implLookupFieldOffset);
    MethodHandle overrideSetter = implLookup.findSetter(AccessibleObject.class, "override", boolean.class);
    return object -> overrideSetter.invokeWithArguments(new Object[] { object, true });
  }

}

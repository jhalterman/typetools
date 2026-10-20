package net.jodah.typetools.issues;

import net.jodah.typetools.TypeResolver;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.function.Predicate;

import static org.testng.Assert.assertEquals;

@Test
public class Issue19 {
  public static Predicate<Method> withModifiers(int modifier) {
    return m -> m.getModifiers() == modifier;
  }

  public void test() {
    Predicate<Method> a = withModifiers(1);
    Class<?> type = TypeResolver.resolveRawArgument(Predicate.class, a.getClass());

    assertEquals(type, Method.class);
  }
}

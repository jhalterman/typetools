package net.jodah.typetools.issues;

import net.jodah.typetools.TypeResolver;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

class Issue19 {

  public static Predicate<Method> withModifiers(int modifier) {
    return m -> m.getModifiers() == modifier;
  }

  @Test
  void test() {
    Predicate<Method> a = withModifiers(1);
    Class<?> type = TypeResolver.resolveRawArgument(Predicate.class, a.getClass());

    assertThat(type).isEqualTo(Method.class);
  }
}

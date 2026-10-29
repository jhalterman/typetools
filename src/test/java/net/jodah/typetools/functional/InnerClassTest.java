package net.jodah.typetools.functional;

import net.jodah.typetools.TypeResolver;
import net.jodah.typetools.TypeResolverCacheTest;
import net.jodah.typetools.functional.InnerClassTest.FooPrime.BarPrime;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests that type arguments on inner classes are supported.
 */
@TypeResolverCacheTest
class InnerClassTest {

  public static class Foo<T extends Number> {
    public class Bar extends ArrayList<T> {
    }
  }

  public static class FooPrime extends Foo<Integer> {
    public class BarPrime extends Bar {
    }
  }

  @Test
  void shouldResolveTypeArgumentOnInnerClass() {
    assertThat(TypeResolver.resolveRawArgument(List.class, BarPrime.class)).isEqualTo(Integer.class);
  }
}

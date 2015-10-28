package net.jodah.typetools.functional;

import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import net.jodah.typetools.AbstractTypeResolverTest;
import net.jodah.typetools.TypeResolver;
import net.jodah.typetools.functional.InnerClassTest.FooPrime.BarPrime;

import org.testng.annotations.Factory;
import org.testng.annotations.Test;

/**
 * Tests that type arguments on inner classes are supported.
 */
@Test
public class InnerClassTest extends AbstractTypeResolverTest {
  @Factory(dataProvider = "cacheDataProvider")
  public InnerClassTest(boolean cacheEnabled) {
    super(cacheEnabled);
  }

  public static class Foo<T extends Number> {
    @SuppressWarnings("serial")
    public class Bar extends ArrayList<T> {
    }
  }

  public static class FooPrime extends Foo<Integer> {
    @SuppressWarnings("serial")
    public class BarPrime extends Bar {
    }
  }

  @Test
  public void shouldResolveTypeArgumentOnInnerClass() {
    assertEquals(TypeResolver.resolveRawArgument(List.class, BarPrime.class), Integer.class);
  }
}

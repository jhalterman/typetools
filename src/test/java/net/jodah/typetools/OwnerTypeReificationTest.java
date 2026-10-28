package net.jodah.typetools;

import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertSame;

@Test
public class OwnerTypeReificationTest extends AbstractTypeResolverTest {
  static class Outer<T> {
    class Inner<U> {
      class Deep<V> {
      }
    }

    class Plain {
    }

    static class Static<U> {
    }
  }

  static class Holder<T> {
    Outer<T>.Inner<Integer> owner;
    Outer<T>.Inner<T> shared;
    Outer<T>.Plain plain;
    Outer<T>.Inner<Integer>.Deep<T> deep;
    Outer<? extends Number>.Inner<Integer> wildcard;
    Outer<String>.Inner<T> concrete;
    Outer.Static<T> staticMember;
    Outer.Plain raw;
  }

  static class Concrete extends Holder<String> {
  }

  static class Expected {
    Outer<String>.Inner<Integer> owner;
    Outer<String>.Inner<String> shared;
    Outer<String>.Plain plain;
    Outer<String>.Inner<Integer>.Deep<String> deep;
    Outer<Number>.Inner<Integer> wildcard;
    Outer<String>.Inner<String> concrete;
    Outer.Static<String> staticMember;
    Outer.Plain raw;
  }

  static class Recursive<T extends Comparable<T>> {
    Outer<T>.Inner<T> member;
  }

  static class RecursiveOwner<T extends Outer<T>.Inner<Integer>> {
    T member;
  }

  static class DifferentRecursiveOwner<T extends Outer<T>.Inner<String>> {
    T member;
  }

  static class RecursivePlainOwner<T extends Outer<T>.Plain> {
    T member;
  }

  @Factory(dataProvider = "cacheDataProvider")
  public OwnerTypeReificationTest(boolean cacheEnabled) {
    super(cacheEnabled);
  }

  public void shouldReifyOwnersAndPreserveStructuralSemantics() throws Exception {
    for (String name : new String[] { "owner", "shared", "plain", "deep", "wildcard", "concrete", "staticMember",
        "raw" }) {
      Type input = Holder.class.getDeclaredField(name).getGenericType();
      Type expected = Expected.class.getDeclaredField(name).getGenericType();
      Type actual = TypeResolver.reify(input, Concrete.class);
      assertFalse(actual.equals(null));
      assertFalse(actual.equals(String.class));
      assertEquals(actual, expected, name);
      assertEquals(expected, actual, name);
      assertEquals(actual.hashCode(), expected.hashCode(), name);
      assertEquals(actual.toString(), expected.toString(), name);
    }
  }

  public void shouldReifyWildcardOwnerWithoutContext() throws Exception {
    Type input = Holder.class.getDeclaredField("wildcard").getGenericType();
    Type expected = Expected.class.getDeclaredField("wildcard").getGenericType();
    assertEquals(TypeResolver.reify(input), expected);
  }

  public void shouldShareRecursiveBoundsBetweenOwnerAndArguments() throws Exception {
    Type input = Recursive.class.getDeclaredField("member").getGenericType();
    ParameterizedType actual = (ParameterizedType) TypeResolver.reify(input);
    Type ownerArgument = ((ParameterizedType) actual.getOwnerType()).getActualTypeArguments()[0];
    assertSame(actual.getActualTypeArguments()[0], ownerArgument);
    assertSame(((ParameterizedType) ownerArgument).getActualTypeArguments()[0], ownerArgument);
    Type second = TypeResolver.reify(input);
    assertEquals(actual, second);
    assertEquals(actual.hashCode(), second.hashCode());
    assertEquals(actual.toString(), second.toString());
  }

  public void shouldHandleCyclesThroughOwners() throws Exception {
    for (Class<?> fixture : new Class<?>[] { RecursiveOwner.class, RecursivePlainOwner.class }) {
      Type input = fixture.getDeclaredField("member").getGenericType();
      ParameterizedType first = (ParameterizedType) TypeResolver.reify(input);
      Type second = TypeResolver.reify(input);
      assertSame(((ParameterizedType) first.getOwnerType()).getActualTypeArguments()[0], first);
      assertEquals(first, second);
      assertEquals(second, first);
      assertEquals(first.hashCode(), second.hashCode());
      assertEquals(first.toString(), second.toString());
      assertFalse(first.equals(((java.lang.reflect.TypeVariable<?>) input).getBounds()[0]));
      assertFalse(((java.lang.reflect.TypeVariable<?>) input).getBounds()[0].equals(first));
    }
    ParameterizedType first = (ParameterizedType) TypeResolver
        .reify(RecursiveOwner.class.getDeclaredField("member").getGenericType());
    Type different = TypeResolver.reify(DifferentRecursiveOwner.class.getDeclaredField("member").getGenericType());
    assertSame(first.getActualTypeArguments()[0], Integer.class);
    assertFalse(first.equals(different));
    assertFalse(different.equals(first));
    assertEquals(first.toString(), OwnerTypeReificationTest.class.getName() + "$Outer<...>$Inner<java.lang.Integer>");
  }

}

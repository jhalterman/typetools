package net.jodah.typetools;

import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertSame;

@Test
public class ReifiedParameterizedTypeTest extends AbstractTypeResolverTest {

  static class EqualityTypes<T> {
    List<List<String>> nested;
    List<T> generic;
    List<String> strings;
    List<Integer> integers;
  }

  static class StringEqualityTypes extends EqualityTypes<String> {

  }

  static class RecursiveEqualityTypes<T extends Comparable<T>> {
    T recursive;
    Comparable<String> nonRecursive;
  }

  @Factory(dataProvider = "cacheDataProvider")
  public ReifiedParameterizedTypeTest(boolean cacheEnabled) {
    super(cacheEnabled);
  }

  public void shouldCompareReifiedNestedTypesStructurally() throws Exception {
    Type declared = EqualityTypes.class.getDeclaredField("nested").getGenericType();
    Type first = TypeResolver.reify(declared);
    Type second = TypeResolver.reify(declared);

    assertEquals(first, second);
    assertEquals(first.hashCode(), second.hashCode());
  }

  public void shouldCompareReifiedAndDeclaredTypesSymmetrically() throws Exception {
    Type declared = EqualityTypes.class.getDeclaredField("nested").getGenericType();
    Type reified = TypeResolver.reify(declared);

    assertEquals(reified, declared);
    assertEquals(declared, reified);
    assertEquals(reified.hashCode(), declared.hashCode());
  }

  public void shouldCompareResolvedArgumentsInsteadOfOriginalVariables() throws Exception {
    Type generic = EqualityTypes.class.getDeclaredField("generic").getGenericType();
    Type strings = EqualityTypes.class.getDeclaredField("strings").getGenericType();
    Type integers = EqualityTypes.class.getDeclaredField("integers").getGenericType();
    Type reified = TypeResolver.reify(generic, StringEqualityTypes.class);

    assertEquals(reified, TypeResolver.reify(strings));
    assertEquals(reified, strings);
    assertEquals(reified.hashCode(), strings.hashCode());
    assertFalse(reified.equals(TypeResolver.reify(integers)));
  }


  public void shouldCompareIndependentRecursiveTypes() throws Exception {
    Type declared = RecursiveEqualityTypes.class.getDeclaredField("recursive").getGenericType();
    ParameterizedType first = (ParameterizedType) TypeResolver.reify(declared);
    ParameterizedType second = (ParameterizedType) TypeResolver.reify(declared);

    assertNotSame(first, second);
    assertSame(first.getActualTypeArguments()[0], first);
    assertSame(second.getActualTypeArguments()[0], second);
    assertEquals(first, second);
    assertEquals(second, first);
    assertEquals(first.hashCode(), second.hashCode());
  }

  public void shouldDistinguishRecursiveAndNonRecursiveArguments() throws Exception {
    Type declared = RecursiveEqualityTypes.class.getDeclaredField("recursive").getGenericType();
    ParameterizedType recursive = (ParameterizedType) TypeResolver.reify(declared);
    ParameterizedType nonRecursive = (ParameterizedType)
        RecursiveEqualityTypes.class.getDeclaredField("nonRecursive").getGenericType();
    Type reifiedNonRecursive = TypeResolver.reify(nonRecursive);

    assertEquals(recursive.getRawType(), nonRecursive.getRawType());
    assertEquals(recursive.getOwnerType(), nonRecursive.getOwnerType());
    assertFalse(recursive.equals(nonRecursive));
    assertFalse(nonRecursive.equals(recursive));
    assertFalse(recursive.equals(reifiedNonRecursive));
    assertFalse(reifiedNonRecursive.equals(recursive));
  }
}

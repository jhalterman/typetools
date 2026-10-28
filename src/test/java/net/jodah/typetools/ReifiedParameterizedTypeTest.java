package net.jodah.typetools;

import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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

    assertThat(first).isEqualTo(second);
    assertThat(first).hasSameHashCodeAs(second);
  }

  public void shouldCompareReifiedAndDeclaredTypesSymmetrically() throws Exception {
    Type declared = EqualityTypes.class.getDeclaredField("nested").getGenericType();
    Type reified = TypeResolver.reify(declared);

    assertThat(reified).isEqualTo(declared);
    assertThat(declared).isEqualTo(reified);
    assertThat(reified).hasSameHashCodeAs(declared);
  }

  public void shouldCompareResolvedArgumentsInsteadOfOriginalVariables() throws Exception {
    Type generic = EqualityTypes.class.getDeclaredField("generic").getGenericType();
    Type strings = EqualityTypes.class.getDeclaredField("strings").getGenericType();
    Type integers = EqualityTypes.class.getDeclaredField("integers").getGenericType();
    Type reified = TypeResolver.reify(generic, StringEqualityTypes.class);

    assertThat(reified).isEqualTo(TypeResolver.reify(strings));
    assertThat(reified).isEqualTo(strings);
    assertThat(reified).hasSameHashCodeAs(strings);
    assertThat(reified).isNotEqualTo(TypeResolver.reify(integers));
  }

  public void shouldCompareIndependentRecursiveTypes() throws Exception {
    Type declared = RecursiveEqualityTypes.class.getDeclaredField("recursive").getGenericType();
    ParameterizedType first = (ParameterizedType) TypeResolver.reify(declared);
    ParameterizedType second = (ParameterizedType) TypeResolver.reify(declared);

    assertThat(first).isNotSameAs(second);
    assertThat(first.getActualTypeArguments()[0]).isSameAs(first);
    assertThat(second.getActualTypeArguments()[0]).isSameAs(second);
    assertThat(first).isEqualTo(second);
    assertThat(second).isEqualTo(first);
    assertThat(first).hasSameHashCodeAs(second);
  }

  public void shouldDistinguishRecursiveAndNonRecursiveArguments() throws Exception {
    Type declared = RecursiveEqualityTypes.class.getDeclaredField("recursive").getGenericType();
    ParameterizedType recursive = (ParameterizedType) TypeResolver.reify(declared);
    ParameterizedType nonRecursive = (ParameterizedType) RecursiveEqualityTypes.class.getDeclaredField("nonRecursive")
        .getGenericType();
    Type reifiedNonRecursive = TypeResolver.reify(nonRecursive);

    assertThat(recursive.getRawType()).isEqualTo(nonRecursive.getRawType());
    assertThat(recursive.getOwnerType()).isEqualTo(nonRecursive.getOwnerType());
    assertThat(recursive).isNotEqualTo(nonRecursive);
    assertThat(nonRecursive).isNotEqualTo(recursive);
    assertThat(recursive).isNotEqualTo(reifiedNonRecursive);
    assertThat(reifiedNonRecursive).isNotEqualTo(recursive);
  }
}

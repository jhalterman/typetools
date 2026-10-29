package net.jodah.typetools;

import net.jodah.typetools.TypeResolver.Unknown;
import org.junit.jupiter.api.Test;

import java.io.Closeable;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Jonathan Halterman
 */
@TypeResolverCacheTest
class TypeResolverTest {

  static class RepoImplA<A1, A2 extends Map<?, ?>> extends RepoImplB<A2, ArrayList<?>> {
  }

  static class RepoImplB<B1 extends Map<?, ?>, B2 extends List<?>> extends RepoImplC<B2, HashSet<?>, B1> {
  }

  static class RepoImplC<C1 extends List<?>, C2 extends Set<?>, C3 extends Map<?, ?>>
      implements IRepo<C3, C1, Vector<?>, C2> {
  }

  interface IRepo<I1, I2, I3, I4> extends Serializable, IIRepo<I1, I3> {
  }

  interface IIRepo<II1, II2> {
  }

  static class Foo extends Bar<ArrayList<?>> {
  }

  static class Bar<B extends List<?>> implements Baz<HashSet<?>, B> {
  }

  interface Baz<C1 extends Set<?>, C2 extends List<?>> {
  }

  static class SimpleRepo implements IIRepo<String, List<?>> {
  }

  static class Entity<ID extends Serializable> {
    ID id;

    void setId(List<ID> id) {
    }
  }

  static class SomeList extends ArrayList<Integer> {
  }

  static class SomeEntity extends Entity<Long> {
  }

  @Test
  void shouldResolveClass() throws Exception {
    Field field = Entity.class.getDeclaredField("id");
    assertThat(TypeResolver.resolveRawClass(field.getGenericType(), SomeEntity.class)).isEqualTo(Long.class);
  }

  @Test
  void shouldResolveArgumentForGenericType() throws Exception {
    Method mutator = Entity.class.getDeclaredMethod("setId", List.class);
    assertThat(TypeResolver.resolveRawArgument(mutator.getGenericParameterTypes()[0], SomeEntity.class))
        .isEqualTo(Long.class);
  }

  @Test
  void shouldResolveArgumentForList() {
    assertThat(TypeResolver.resolveRawArgument(List.class, SomeList.class)).isEqualTo(Integer.class);
  }

  @Test
  void shouldResolveTypeForList() {
    Type resolvedType = TypeResolver.reify(List.class, SomeList.class);
    assertThat(resolvedType).isInstanceOf(ParameterizedType.class);
    assertThat(((ParameterizedType) resolvedType).getActualTypeArguments()[0]).isEqualTo(Integer.class);
  }

  @Test
  void shouldResolveArgumentsForBazFromFoo() {
    Class<?>[] typeArguments = TypeResolver.resolveRawArguments(Baz.class, Foo.class);
    assertThat(typeArguments[0]).isEqualTo(HashSet.class);
    assertThat(typeArguments[1]).isEqualTo(ArrayList.class);
  }

  @Test
  void shouldResolveParameterizedTypeForBazFromFoo() {
    Type type = TypeResolver.reify(Baz.class, Foo.class);

    // Now we walk the type hierarchy:
    assertThat(type).isInstanceOf(ParameterizedType.class);
    Type[] typeArguments = ((ParameterizedType) type).getActualTypeArguments();

    assertThat(typeArguments[0]).isInstanceOf(ParameterizedType.class);
    ParameterizedType firstTypeArgument = (ParameterizedType) typeArguments[0];
    assertThat(firstTypeArgument.getRawType()).isEqualTo(HashSet.class);
    assertThat(firstTypeArgument.getActualTypeArguments()[0]).isEqualTo(Object.class);

    assertThat(typeArguments[1]).isInstanceOf(ParameterizedType.class);
    ParameterizedType secondTypeArgument = (ParameterizedType) typeArguments[1];
    assertThat(secondTypeArgument.getRawType()).isEqualTo(ArrayList.class);
    assertThat(secondTypeArgument.getActualTypeArguments()[0]).isEqualTo(Object.class);
  }

  @Test
  void shouldResolveTypeVariable() {
    TypeVariable<Class<Baz>> typeVariable = Baz.class.getTypeParameters()[0];
    Type type = TypeResolver.reify(typeVariable, Bar.class);
    assertThat(type).isInstanceOf(ParameterizedType.class);
    assertThat(((ParameterizedType) type).getRawType()).isEqualTo(HashSet.class);
  }

  @Test
  void shouldResolvePartialParameterizedTypeForBazFromBar() {
    Type type = TypeResolver.reify(Baz.class, Bar.class);

    assertThat(type).isInstanceOf(ParameterizedType.class);
    Type[] typeArguments = ((ParameterizedType) type).getActualTypeArguments();

    assertThat(typeArguments[0]).isInstanceOf(ParameterizedType.class);
    ParameterizedType firstTypeArgument = (ParameterizedType) typeArguments[0];
    assertThat(firstTypeArgument.getRawType()).isEqualTo(HashSet.class);
    assertThat(firstTypeArgument.getActualTypeArguments()[0]).isEqualTo(Object.class);

    assertThat(typeArguments[1]).isInstanceOf(ParameterizedType.class);
    ParameterizedType secondTypeArgument = (ParameterizedType) typeArguments[1];
    assertThat(secondTypeArgument.getRawType()).isEqualTo(List.class);
    assertThat(secondTypeArgument.getActualTypeArguments()[0]).isEqualTo(Object.class);
  }

  @Test
  void shouldResolveArgumentsForIRepoFromRepoImplA() {
    Class<?>[] types = TypeResolver.resolveRawArguments(IRepo.class, RepoImplA.class);
    assertThat(types[0]).isEqualTo(Map.class);
    assertThat(types[1]).isEqualTo(ArrayList.class);
    assertThat(types[2]).isEqualTo(Vector.class);
    assertThat(types[3]).isEqualTo(HashSet.class);
  }

  @Test
  void shouldResolveArgumentsForRepoImplCFromRepoImplA() {
    Class<?>[] types = TypeResolver.resolveRawArguments(RepoImplC.class, RepoImplA.class);
    assertThat(types[0]).isEqualTo(ArrayList.class);
    assertThat(types[1]).isEqualTo(HashSet.class);
    assertThat(types[2]).isEqualTo(Map.class);
  }

  @Test
  void shouldResolveArgumentsForRepoImplCFromRepoImplB() {
    Class<?>[] types = TypeResolver.resolveRawArguments(RepoImplC.class, RepoImplB.class);
    assertThat(types[0]).isEqualTo(Unknown.class);
    assertThat(types[1]).isEqualTo(HashSet.class);
    assertThat(types[2]).isEqualTo(Unknown.class);
  }

  @Test
  void shouldResolveArgumentsForIRepoFromRepoImplB() {
    Class<?>[] types = TypeResolver.resolveRawArguments(IRepo.class, RepoImplB.class);
    assertThat(types[0]).isEqualTo(Map.class);
    assertThat(types[1]).isEqualTo(List.class);
    assertThat(types[2]).isEqualTo(Vector.class);
    assertThat(types[3]).isEqualTo(HashSet.class);
  }

  @Test
  void shouldResolveArgumentsForIRepoFromRepoImplC() {
    Class<?>[] types = TypeResolver.resolveRawArguments(IRepo.class, RepoImplC.class);
    assertThat(types[0]).isEqualTo(Unknown.class);
    assertThat(types[1]).isEqualTo(Unknown.class);
    assertThat(types[2]).isEqualTo(Vector.class);
    assertThat(types[3]).isEqualTo(Unknown.class);
  }

  @Test
  void shouldResolveArgumentsForIIRepoFromRepoImplA() {
    Class<?>[] types = TypeResolver.resolveRawArguments(IIRepo.class, RepoImplA.class);
    assertThat(types[0]).isEqualTo(Map.class);
    assertThat(types[1]).isEqualTo(Vector.class);
  }

  @Test
  void shouldResolveArgumentsForRepoImplBFromRepoImplA() {
    Class<?>[] types = TypeResolver.resolveRawArguments(RepoImplB.class, RepoImplA.class);
    assertThat(types[0]).isEqualTo(Unknown.class);
    assertThat(types[1]).isEqualTo(ArrayList.class);
  }

  @Test
  void shouldResolveArgumentsForSimpleType() {
    Class<?>[] args = TypeResolver.resolveRawArguments(IIRepo.class, SimpleRepo.class);
    assertThat(args[0]).isEqualTo(String.class);
    assertThat(args[1]).isEqualTo(List.class);
  }

  static class TypeArrayFixture<T> {
    T[] test;
  }

  static class TypeArrayImpl extends TypeArrayFixture<String> {
  }

  static class TypeArrayNonClassImpl extends TypeArrayFixture<Closeable> {
  }

  static class TypeListFixture<T> {
    List<T> testList;

    T testPlain;

    public void testMethod(List<? super T> arg) {
    }
  }

  static class TypeListImpl extends TypeListFixture<String> {
  }

  static abstract class GenericMethodHolder {
    public abstract <T extends Number> T genericMethod();
  }

  @Test
  void shouldResolveGenericClassTypeArray() throws Throwable {
    Type arrayField = TypeArrayFixture.class.getDeclaredField("test").getGenericType();

    Class<?> arg = TypeResolver.resolveRawClass(arrayField, TypeArrayImpl.class);
    assertThat(arg).isEqualTo(String[].class);
  }

  @Test
  void shouldReifyGenericArrayTypeWithInterface() throws Throwable {
    Type arrayField = TypeArrayFixture.class.getDeclaredField("test").getGenericType();

    Type arg = TypeResolver.reify(arrayField, TypeArrayNonClassImpl.class);
    assertThat(arg).isEqualTo(Closeable[].class);
  }

  @Test
  void shouldResolveRawTypeList() throws Throwable {
    Type listField = TypeListFixture.class.getDeclaredField("testList").getGenericType();

    Class<?> arg = TypeResolver.resolveRawClass(listField, TypeListImpl.class);
    assertThat(arg).isEqualTo(List.class);
  }

  @Test
  void shouldReifyGenericArrayTypeWithClass() throws Throwable {
    Type arrayField = TypeArrayFixture.class.getDeclaredField("test").getGenericType();

    Type arg = TypeResolver.reify(arrayField, TypeArrayImpl.class);
    assertThat(arg).isEqualTo(String[].class);
  }

  @Test
  void shouldReifyList() throws Throwable {
    Type listField = TypeListFixture.class.getDeclaredField("testList").getGenericType();

    Type arg = TypeResolver.reify(listField, TypeListImpl.class);
    assertThat(arg).isInstanceOf(ParameterizedType.class);
    ParameterizedType parameterizedType = (ParameterizedType) arg;
    assertThat(parameterizedType.getRawType()).isEqualTo(List.class);
    assertThat(parameterizedType.getActualTypeArguments()[0]).isEqualTo(String.class);
  }

  @Test
  void shouldReifyTypeVariable() throws Throwable {
    Type plainField = TypeListFixture.class.getDeclaredField("testPlain").getGenericType();

    Type arg = TypeResolver.reify(plainField, TypeListImpl.class);
    assertThat(arg).isEqualTo(String.class);
  }

  @Test
  void shouldReifyWildcardFromGenericMethod() throws Exception {
    Type type = TypeResolver.reify( //
        GenericMethodHolder.class.getMethod("genericMethod").getGenericReturnType() //
    );
    assertThat(type).isEqualTo(Number.class);
  }

  @Test
  void shouldReturnNullOnResolveArgumentsForNonParameterizedType() {
    assertThat(TypeResolver.resolveRawArguments(Object.class, String.class)).isNull();
  }

  @Test
  void shouldReturnUnknownOnResolveArgumentForNonParameterizedType() {
    assertThat(TypeResolver.resolveRawArgument(Object.class, String.class)).isEqualTo(Unknown.class);
  }

  private static abstract class WildcardWithBoundFixture {

    public abstract List<? extends Number> getNumberList();

    public abstract List<? extends Collection<List<? extends Number>>> collect();

  }

  @Test
  void shouldReifyWildcardToUpperBound() throws Exception {
    Type type = TypeResolver.reify( //
        WildcardWithBoundFixture.class.getMethod("getNumberList").getGenericReturnType(), //
        WildcardWithBoundFixture.class //
    );

    assertThat(type).isInstanceOf(ParameterizedType.class);
    ParameterizedType parameterizedType = (ParameterizedType) type;
    assertThat(parameterizedType.getRawType()).isEqualTo(List.class);
    assertThat(parameterizedType.getActualTypeArguments()[0]).isEqualTo(Number.class);
  }

  @Test
  void shouldReifyWildcardWithComplexUpperBound() throws Exception {
    Type type = TypeResolver.reify( //
        WildcardWithBoundFixture.class.getMethod("collect").getGenericReturnType(), //
        WildcardWithBoundFixture.class //
    );

    assertThat(type.toString()).isEqualTo("java.util.List<java.util.Collection<java.util.List<java.lang.Number>>>");

    assertThat(type).isInstanceOf(ParameterizedType.class);
    ParameterizedType parameterizedType = (ParameterizedType) type;
    assertThat(parameterizedType.getRawType()).isEqualTo(List.class);

    assertThat(parameterizedType.getActualTypeArguments()[0]).isInstanceOf(ParameterizedType.class);
    parameterizedType = (ParameterizedType) parameterizedType.getActualTypeArguments()[0];

    assertThat(parameterizedType.getRawType()).isEqualTo(Collection.class);

    assertThat(parameterizedType.getActualTypeArguments()[0]).isInstanceOf(ParameterizedType.class);
    parameterizedType = (ParameterizedType) parameterizedType.getActualTypeArguments()[0];

    assertThat(parameterizedType.getRawType()).isEqualTo(List.class);
    assertThat(parameterizedType.getActualTypeArguments()[0]).isEqualTo(Number.class);
  }

  static abstract class EnumBound<S extends Enum<S>> {
    public S enumField;
  }

  static abstract class SubEnumBound<S extends Enum<S>> extends EnumBound<S> {
  }

  static abstract class MutuallyRecursiveBase<T, U> {
  }

  static abstract class MutuallyRecursive<T extends List<T>, U extends List<T>> extends MutuallyRecursiveBase<T, U> {
  }

  static abstract class RecursiveOnSecondBase<S, T> {
  }

  static abstract class RecursiveOnSecond<S, T extends RecursiveOnSecond<S, T>> extends RecursiveOnSecondBase<S, T> {
  }

  static abstract class RecursiveLongBase<T> {
  }

  static abstract class RecursiveLong<T extends List<Set<T>>> extends RecursiveLongBase<T> {
  }

  @Test
  void shouldReifyRecursiveBound() {
    Type result = TypeResolver.reify(EnumBound.class, SubEnumBound.class);
    assertThat(result).isInstanceOf(ParameterizedType.class);
    ParameterizedType parameterizedType = (ParameterizedType) result;

    // Navigate into enum parameter.
    assertThat(parameterizedType.getActualTypeArguments()[0]).isInstanceOf(ParameterizedType.class);
    ParameterizedType parameterizedType2 = (ParameterizedType) parameterizedType.getActualTypeArguments()[0];
    assertThat(parameterizedType.getActualTypeArguments()[0]).isEqualTo(parameterizedType2);
    // Assert existence of loop
    assertThat(parameterizedType2.getActualTypeArguments()[0]).isEqualTo(parameterizedType2);

    assertThat(parameterizedType).isNotEqualTo(parameterizedType2);
  }

  @Test
  void shouldReifyEnumBound() throws NoSuchFieldException {
    Type result = TypeResolver.reify(SubEnumBound.class.getField("enumField").getGenericType(), SubEnumBound.class);
    assertThat(result).isInstanceOf(ParameterizedType.class);
    ParameterizedType parameterizedType = (ParameterizedType) result;
    assertThat(parameterizedType.toString()).isEqualTo("java.lang.Enum<...>");

    // Navigate into enum parameter.
    assertThat(parameterizedType.getActualTypeArguments()[0]).isInstanceOf(ParameterizedType.class);
    ParameterizedType parameterizedType2 = (ParameterizedType) parameterizedType.getActualTypeArguments()[0];
    assertThat(parameterizedType.getActualTypeArguments()[0]).isEqualTo(parameterizedType2);

    // Assert existence of loop
    assertThat(parameterizedType2.getActualTypeArguments()[0]).isEqualTo(parameterizedType2);

    // Reify the same type again, which will create new instances of
    // ReifiedParameterizedType that are not referentially equal, but
    // equal to the previous result.
    Type same = TypeResolver.reify(SubEnumBound.class.getField("enumField").getGenericType(), SubEnumBound.class);
    assertThat(same).isEqualTo(result);

    // Just to call hashCode() and equals() a bit more.
    Set<Type> types = new HashSet<>();
    types.add(parameterizedType);
    types.add(parameterizedType2);
    types.add(same);
    assertThat(types.size()).isEqualTo(1);
    assertThat(types).containsExactly(same);
  }

  @Test
  void shouldReifyMutuallyRecursiveBound() {
    Type result = TypeResolver.reify(MutuallyRecursiveBase.class, MutuallyRecursive.class);
    assertThat(result).isInstanceOf(ParameterizedType.class);
    ParameterizedType parent = (ParameterizedType) result;
    assertThat(result.toString()).isEqualTo(
        "net.jodah.typetools.TypeResolverTest$MutuallyRecursiveBase<java.util.List<...>, java.util.List<...>>");
    assertThat(parent.getActualTypeArguments()[0]).isInstanceOf(ParameterizedType.class);
    ParameterizedType parameterizedType = (ParameterizedType) parent.getActualTypeArguments()[0];
    assertThat(parameterizedType.getActualTypeArguments()[0]).isEqualTo(parameterizedType);
    assertThat(parameterizedType.getActualTypeArguments()[0]).isEqualTo(parameterizedType);

    assertThat(parent.getActualTypeArguments()[1]).isInstanceOf(ParameterizedType.class);
    ParameterizedType parameterizedType2 = (ParameterizedType) parent.getActualTypeArguments()[1];
    assertThat(parameterizedType2.getActualTypeArguments()[0]).isEqualTo(parameterizedType2);

    assertThat(parameterizedType).isEqualTo(parameterizedType2);
  }

  @Test
  void shouldReifyRecursiveOnSecondBound() {
    Type result = TypeResolver.reify(RecursiveOnSecondBase.class, RecursiveOnSecond.class);
    assertThat(result).isInstanceOf(ParameterizedType.class);
    ParameterizedType parameterizedType = (ParameterizedType) result;

    assertThat(parameterizedType.getActualTypeArguments()[0]).isEqualTo(Object.class);

    assertThat(parameterizedType.getActualTypeArguments()[1]).isInstanceOf(ParameterizedType.class);
    parameterizedType = (ParameterizedType) parameterizedType.getActualTypeArguments()[1];
    // Assert existence of loop
    assertThat(parameterizedType.getActualTypeArguments()[1]).isEqualTo(parameterizedType);
  }

  @Test
  void shouldThrowOnResolveArgumentForTypeWithMultipleArguments() {
    assertThrows(IllegalArgumentException.class, () -> {
      TypeResolver.resolveRawArgument(Map.class, new HashMap<String, String>() {
      }.getClass());
    });
  }

  @Test
  void shouldThrowOnReifyForUnknownImplementation() {
    assertThrows(UnsupportedOperationException.class, () -> {
      TypeResolver.reify(new Type() {
        @Override
        public String getTypeName() {
          return "unknown";
        }
      }, Bar.class);
    });
  }

  @Test
  void shouldThrowOnReifyForNontrivialBounds() {
    assertThrows(UnsupportedOperationException.class, () -> {
      Type toResolve = TypeListImpl.class.getMethod("testMethod", List.class).getGenericParameterTypes()[0];
      TypeResolver.reify(toResolve, TypeListImpl.class);
    });
  }

  @Test
  void shouldResolveTypeParamFromAnonymousClass() {
    List<String> stringList = new ArrayList<String>() {
    };
    Class<?> stringType = TypeResolver.resolveRawArgument(List.class, stringList.getClass());
    assertThat(stringType).isEqualTo(String.class);
  }
}

package net.jodah.typetools;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

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

import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import net.jodah.typetools.TypeResolver.Unknown;

/**
 * @author Jonathan Halterman
 */
@Test
@SuppressWarnings("serial")
public class TypeResolverTest extends AbstractTypeResolverTest {
  @Factory(dataProvider = "cacheDataProvider")
  public TypeResolverTest(boolean cacheEnabled) {
    super(cacheEnabled);
  }

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

  public void shouldResolveClass() throws Exception {
    Field field = Entity.class.getDeclaredField("id");
    assertEquals(TypeResolver.resolveRawClass(field.getGenericType(), SomeEntity.class), Long.class);
  }

  public void shouldResolveArgumentForGenericType() throws Exception {
    Method mutator = Entity.class.getDeclaredMethod("setId", List.class);
    assertEquals(TypeResolver.resolveRawArgument(mutator.getGenericParameterTypes()[0], SomeEntity.class), Long.class);
  }

  public void shouldResolveArgumentForList() {
    assertEquals(TypeResolver.resolveRawArgument(List.class, SomeList.class), Integer.class);
  }

  public void shouldResolveTypeForList() {
    Type resolvedType = TypeResolver.reify(List.class, SomeList.class);
    assert resolvedType instanceof ParameterizedType;
    assertEquals(((ParameterizedType) resolvedType).getActualTypeArguments()[0], Integer.class);
  }

  public void shouldResolveArgumentsForBazFromFoo() {
    Class<?>[] typeArguments = TypeResolver.resolveRawArguments(Baz.class, Foo.class);
    assert typeArguments[0] == HashSet.class;
    assert typeArguments[1] == ArrayList.class;
  }

  public void shouldResolveParameterizedTypeForBazFromFoo() {
    Type type = TypeResolver.reify(Baz.class, Foo.class);

    // Now we walk the type hierarchy:
    assert type instanceof ParameterizedType;
    Type[] typeArguments = ((ParameterizedType) type).getActualTypeArguments();

    assert typeArguments[0] instanceof ParameterizedType;
    ParameterizedType firstTypeArgument = (ParameterizedType) typeArguments[0];
    assert firstTypeArgument.getRawType() == HashSet.class;
    assert firstTypeArgument.getActualTypeArguments()[0] == Object.class;

    assert typeArguments[1] instanceof ParameterizedType;
    ParameterizedType secondTypeArgument = (ParameterizedType) typeArguments[1];
    assert secondTypeArgument.getRawType() == ArrayList.class;
    assert secondTypeArgument.getActualTypeArguments()[0] == Object.class;
  }

  public void shouldResolveTypeVariable() {
    TypeVariable<Class<Baz>> typeVariable = Baz.class.getTypeParameters()[0];
    Type type = TypeResolver.reify(typeVariable, Bar.class);
    assert type instanceof ParameterizedType;
    assert ((ParameterizedType) type).getRawType().equals(HashSet.class);
  }

  public void shouldResolvePartialParameterizedTypeForBazFromBar() {
    Type type = TypeResolver.reify(Baz.class, Bar.class);

    assert type instanceof ParameterizedType;
    Type[] typeArguments = ((ParameterizedType) type).getActualTypeArguments();

    assert typeArguments[0] instanceof ParameterizedType;
    ParameterizedType firstTypeArgument = (ParameterizedType) typeArguments[0];
    assert firstTypeArgument.getRawType() == HashSet.class;
    assert firstTypeArgument.getActualTypeArguments()[0] == Object.class;

    assert typeArguments[1] instanceof ParameterizedType;
    ParameterizedType secondTypeArgument = (ParameterizedType) typeArguments[1];
    assert secondTypeArgument.getRawType() == List.class;
    assert secondTypeArgument.getActualTypeArguments()[0] == Object.class;
  }

  public void shouldResolveArgumentsForIRepoFromRepoImplA() {
    Class<?>[] types = TypeResolver.resolveRawArguments(IRepo.class, RepoImplA.class);
    assertEquals(types[0], Map.class);
    assertEquals(types[1], ArrayList.class);
    assertEquals(types[2], Vector.class);
    assertEquals(types[3], HashSet.class);
  }

  public void shouldResolveArgumentsForRepoImplCFromRepoImplA() {
    Class<?>[] types = TypeResolver.resolveRawArguments(RepoImplC.class, RepoImplA.class);
    assertEquals(types[0], ArrayList.class);
    assertEquals(types[1], HashSet.class);
    assertEquals(types[2], Map.class);
  }

  public void shouldResolveArgumentsForRepoImplCFromRepoImplB() {
    Class<?>[] types = TypeResolver.resolveRawArguments(RepoImplC.class, RepoImplB.class);
    assertEquals(types[0], Unknown.class);
    assertEquals(types[1], HashSet.class);
    assertEquals(types[2], Unknown.class);
  }

  public void shouldResolveArgumentsForIRepoFromRepoImplB() {
    Class<?>[] types = TypeResolver.resolveRawArguments(IRepo.class, RepoImplB.class);
    assertEquals(types[0], Map.class);
    assertEquals(types[1], List.class);
    assertEquals(types[2], Vector.class);
    assertEquals(types[3], HashSet.class);
  }

  public void shouldResolveArgumentsForIRepoFromRepoImplC() {
    Class<?>[] types = TypeResolver.resolveRawArguments(IRepo.class, RepoImplC.class);
    assertEquals(types[0], Unknown.class);
    assertEquals(types[1], Unknown.class);
    assertEquals(types[2], Vector.class);
    assertEquals(types[3], Unknown.class);
  }

  public void shouldResolveArgumentsForIIRepoFromRepoImplA() {
    Class<?>[] types = TypeResolver.resolveRawArguments(IIRepo.class, RepoImplA.class);
    assertEquals(types[0], Map.class);
    assertEquals(types[1], Vector.class);
  }

  public void shouldResolveArgumentsForRepoImplBFromRepoImplA() {
    Class<?>[] types = TypeResolver.resolveRawArguments(RepoImplB.class, RepoImplA.class);
    assertEquals(types[0], Unknown.class);
    assertEquals(types[1], ArrayList.class);
  }

  public void shouldResolveArgumentsForSimpleType() {
    Class<?>[] args = TypeResolver.resolveRawArguments(IIRepo.class, SimpleRepo.class);
    assertEquals(args[0], String.class);
    assertEquals(args[1], List.class);
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

  public void shouldResolveGenericClassTypeArray() throws Throwable {
    Type arrayField = TypeArrayFixture.class.getDeclaredField("test").getGenericType();

    Class<?> arg = TypeResolver.resolveRawClass(arrayField, TypeArrayImpl.class);
    assertEquals(arg, String[].class);
  }

  public void shouldReifyGenericArrayTypeWithInterface() throws Throwable {
    Type arrayField = TypeArrayFixture.class.getDeclaredField("test").getGenericType();

    Type arg = TypeResolver.reify(arrayField, TypeArrayNonClassImpl.class);
    assertEquals(arg, Closeable[].class);
  }

  public void shouldResolveRawTypeList() throws Throwable {
    Type listField = TypeListFixture.class.getDeclaredField("testList").getGenericType();

    Class<?> arg = TypeResolver.resolveRawClass(listField, TypeListImpl.class);
    assertEquals(arg, List.class);
  }

  public void shouldReifyGenericArrayTypeWithClass() throws Throwable {
    Type arrayField = TypeArrayFixture.class.getDeclaredField("test").getGenericType();

    Type arg = TypeResolver.reify(arrayField, TypeArrayImpl.class);
    assertEquals(arg, String[].class);
  }

  public void shouldReifyList() throws Throwable {
    Type listField = TypeListFixture.class.getDeclaredField("testList").getGenericType();

    Type arg = TypeResolver.reify(listField, TypeListImpl.class);
    assert arg instanceof ParameterizedType;
    ParameterizedType parameterizedType = (ParameterizedType) arg;
    assertEquals(parameterizedType.getRawType(), List.class);
    assertEquals(parameterizedType.getActualTypeArguments()[0], String.class);
  }

  public void shouldReifyTypeVariable() throws Throwable {
    Type plainField  = TypeListFixture.class.getDeclaredField("testPlain").getGenericType();

    Type arg = TypeResolver.reify(plainField, TypeListImpl.class);
    assert arg == String.class;
  }

  public void shouldReifyWildcardFromGenericMethod() throws Exception {
    Type type = TypeResolver.reify(
        GenericMethodHolder.class.getMethod("genericMethod").getGenericReturnType());
    assert type == Number.class;
  }

  public void shouldReturnNullOnResolveArgumentsForNonParameterizedType() {
    assertNull(TypeResolver.resolveRawArguments(Object.class, String.class));
  }

  public void shouldReturnUnknownOnResolveArgumentForNonParameterizedType() {
    assertEquals(TypeResolver.resolveRawArgument(Object.class, String.class), Unknown.class);
  }

  private static abstract class WildcardWithBoundFixture {
    public abstract List<? extends Number> getNumberList();
    public abstract List<? extends Collection<List<? extends Number>>> collect();
  }

  public void shouldReifyWildcardToUpperBound() throws Exception {
    Type type = TypeResolver.reify(
        WildcardWithBoundFixture.class.getMethod("getNumberList").getGenericReturnType(),
        WildcardWithBoundFixture.class
    );

    assert type instanceof ParameterizedType;
    ParameterizedType parameterizedType = (ParameterizedType) type;
    assert parameterizedType.getRawType() == List.class;
    assert parameterizedType.getActualTypeArguments()[0] == Number.class;
  }

  public void shouldReifyWildcardWithComplexUpperBound() throws Exception {
    Type type = TypeResolver.reify(
        WildcardWithBoundFixture.class.getMethod("collect").getGenericReturnType(),
        WildcardWithBoundFixture.class
    );

    assertEquals(type.toString(), "java.util.List<java.util.Collection<java.util.List<java.lang.Number>>>");

    assert type instanceof ParameterizedType;
    ParameterizedType parameterizedType = (ParameterizedType) type;
    assert parameterizedType.getRawType() == List.class;

    assert parameterizedType.getActualTypeArguments()[0] instanceof ParameterizedType;
    parameterizedType = (ParameterizedType) parameterizedType.getActualTypeArguments()[0];

    assert parameterizedType.getRawType() == Collection.class;

    assert parameterizedType.getActualTypeArguments()[0] instanceof ParameterizedType;
    parameterizedType = (ParameterizedType) parameterizedType.getActualTypeArguments()[0];

    assert parameterizedType.getRawType() == List.class;
    assert parameterizedType.getActualTypeArguments()[0] == Number.class;
  }

  static abstract class EnumBound<S extends Enum<S>> {
    public S enumField;
  }

  static abstract class SubEnumBound<S extends Enum<S>> extends EnumBound<S>  {
  }

  static abstract class MutuallyRecursiveBase<T, U> {}

  static abstract class MutuallyRecursive<T extends List<T>, U extends List<T>> extends MutuallyRecursiveBase<T, U> {}

  static abstract class RecursiveOnSecondBase<S, T> {}

  static abstract class RecursiveOnSecond<S, T extends RecursiveOnSecond<S, T>> extends RecursiveOnSecondBase<S, T> {}

  static abstract class RecursiveLongBase<T> {}

  static abstract class RecursiveLong<T extends List<Set<T>>> extends RecursiveLongBase<T> {}

  public void shouldReifyRecursiveBound() {
    Type result = TypeResolver.reify(EnumBound.class, SubEnumBound.class);
    assert result instanceof ParameterizedType;
    ParameterizedType parameterizedType = (ParameterizedType) result;

    // Navigate into enum parameter.
    assert parameterizedType.getActualTypeArguments()[0] instanceof ParameterizedType;
    ParameterizedType parameterizedType2 = (ParameterizedType)parameterizedType.getActualTypeArguments()[0];
    assert parameterizedType.getActualTypeArguments()[0].equals(parameterizedType2);
    // Assert existence of loop
    assert parameterizedType2.getActualTypeArguments()[0] == parameterizedType2;

    assert !parameterizedType.equals(parameterizedType2);
  }

  public void shouldReifyEnumBound() throws NoSuchFieldException {
    Type result = TypeResolver.reify(SubEnumBound.class.getField("enumField").getGenericType(), SubEnumBound.class);
    assert result instanceof ParameterizedType;
    ParameterizedType parameterizedType = (ParameterizedType) result;
    assert parameterizedType.toString().equals("java.lang.Enum<...>");

    // Navigate into enum parameter.
    assert parameterizedType.getActualTypeArguments()[0] instanceof ParameterizedType;
    ParameterizedType parameterizedType2 = (ParameterizedType)parameterizedType.getActualTypeArguments()[0];
    assert parameterizedType.getActualTypeArguments()[0].equals(parameterizedType2);

    // Assert existence of loop
    assert parameterizedType2.getActualTypeArguments()[0] == parameterizedType2;

    // Reify the same type again, which will create new instances of
    // ReifiedParameterizedType that are not referentially equal, but
    // equal to the previous result.
    Type same = TypeResolver.reify(SubEnumBound.class.getField("enumField").getGenericType(), SubEnumBound.class);
    assert same.equals(result);

    // Just to call hashCode() and equals() a bit more.
    Set<Type> types = new HashSet<>();
    types.add(parameterizedType);
    types.add(parameterizedType2);
    types.add(same);
    assert types.size() == 1;
    assert types.contains(same);
  }

  public void shouldReifyMutuallyRecursiveBound() {
    Type result = TypeResolver.reify(MutuallyRecursiveBase.class, MutuallyRecursive.class);
    assert result instanceof ParameterizedType;
    ParameterizedType parent = (ParameterizedType) result;
    assert result.toString().equals("net.jodah.typetools.TypeResolverTest$MutuallyRecursiveBase<java.util.List<...>, java.util.List<...>>");
    assert parent.getActualTypeArguments()[0] instanceof ParameterizedType;
    ParameterizedType parameterizedType = (ParameterizedType)parent.getActualTypeArguments()[0];
    assert parameterizedType.getActualTypeArguments()[0] == parameterizedType;
    assert parameterizedType.getActualTypeArguments()[0].equals(parameterizedType);

    assert parent.getActualTypeArguments()[1] instanceof ParameterizedType;
    ParameterizedType parameterizedType2 = (ParameterizedType)parent.getActualTypeArguments()[1];
    assert parameterizedType2.getActualTypeArguments()[0] == parameterizedType2;

    assert parameterizedType.equals(parameterizedType2);
  }

  public void shouldReifyRecursiveOnSecondBound() {
    Type result = TypeResolver.reify(RecursiveOnSecondBase.class, RecursiveOnSecond.class);
    assert result instanceof ParameterizedType;
    ParameterizedType parameterizedType = (ParameterizedType) result;

    assert parameterizedType.getActualTypeArguments()[0] == Object.class;

    assert parameterizedType.getActualTypeArguments()[1] instanceof ParameterizedType;
    parameterizedType = (ParameterizedType)parameterizedType.getActualTypeArguments()[1];
    // Assert existence of loop
    assert parameterizedType.getActualTypeArguments()[1] == parameterizedType;
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void shouldThrowOnResolveArgumentForTypeWithMultipleArguments() {
    TypeResolver.resolveRawArgument(Map.class, new HashMap<String, String>() {
    }.getClass());
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void shouldThrowOnReifyForUnknownImplementation() {
    TypeResolver.reify(new Type() {
      @Override
      public String getTypeName() {
        return "unknown";
      }
    }, Bar.class);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void shouldThrowOnReifyForNontrivialBounds() throws Exception {
    Type toResolve = TypeListImpl.class.getMethod("testMethod", List.class).getGenericParameterTypes()[0];
    TypeResolver.reify(toResolve, TypeListImpl.class);
  }

  public void shouldResolveTypeParamFromAnonymousClass() {
    List<String> stringList = new ArrayList<String>() {
    };
    Class<?> stringType = TypeResolver.resolveRawArgument(List.class, stringList.getClass());
    assertEquals(stringType, String.class);
  }
}

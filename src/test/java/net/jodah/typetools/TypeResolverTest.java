package net.jodah.typetools;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
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

  public void shouldResolveArgumentsForBazFromFoo() {
    Class<?>[] typeArguments = TypeResolver.resolveRawArguments(Baz.class, Foo.class);
    assert typeArguments[0] == HashSet.class;
    assert typeArguments[1] == ArrayList.class;
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

  public void shouldResolveGenericTypeArray() throws Throwable {
    Type arrayField = TypeArrayFixture.class.getDeclaredField("test").getGenericType();

    Class<?> arg = TypeResolver.resolveRawClass(arrayField, TypeArrayImpl.class);
    assertEquals(arg, String[].class);
  }

  public void shouldReturnNullOnResolveArgumentsForNonParameterizedType() {
    assertNull(TypeResolver.resolveRawArguments(Object.class, String.class));
  }

  public void shouldReturnUnknownOnResolveArgumentForNonParameterizedType() {
    assertEquals(TypeResolver.resolveRawArgument(Object.class, String.class), Unknown.class);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void shouldThrowOnResolveArgumentForTypeWithMultipleArguments() {
    TypeResolver.resolveRawArgument(Map.class, new HashMap<String, String>() {
    }.getClass());
  }

  public void shouldResolveTypeParamFromAnonymousClass() {
    List<String> stringList = new ArrayList<String>() {
    };
    Class<?> stringType = TypeResolver.resolveRawArgument(List.class, stringList.getClass());
    assertEquals(stringType, String.class);
  }
}

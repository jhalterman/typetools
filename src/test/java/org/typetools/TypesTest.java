package org.typetools;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.testng.annotations.Test;

/**
 * Tests Types.
 */
@Test
public class TypesTest {
  static class RepoImplA<A1, A2 extends Map<?, ?>> extends RepoImplB<A2, ArrayList<?>> {
  }

  static class RepoImplB<B1 extends Map<?, ?>, B2 extends List<?>> extends
      RepoImplC<B2, HashSet<?>, B1> {
  }

  static class RepoImplC<C1 extends List<?>, C2 extends Set<?>, C3 extends Map<?, ?>> implements
      IRepo<C3, C1, Vector<?>, C2> {
  }

  interface IRepo<I1, I2, I3, I4> extends Serializable, IIRepo<I1, I3> {
  }

  interface IIRepo<II1, II2> {
  }

  static class NonParameterizedType {
  }

  static class Foo extends Bar<ArrayList<?>> {
  }

  static class Bar<B extends List<?>> implements Baz<HashSet<?>, B> {
  }

  interface Baz<C1 extends Set<?>, C2 extends List<?>> {
  }

  static class MultiBounded<T extends List<?> & Comparable<?> & Serializable> {
  }

  @Test
  public void testResolveArgumentsForBazFromFoo() {
    Class<?>[] typeArguments = Types.resolveArguments(Baz.class, Foo.class);
    assert typeArguments[0] == HashSet.class;
    assert typeArguments[1] == ArrayList.class;
  }

  @Test
  public void resolveArgumentsAsTypesForNonParameterizedTypeShouldBeNull() {
    assertNull(Types
        .resolveArgumentsAsTypes(NonParameterizedType.class, NonParameterizedType.class));
  }

  @Test
  public void testResolveBoundForRepoImplA() {
    assertEquals(Types.resolveBound(RepoImplA.class.getTypeParameters()[1], 0), Map.class);
  }

  @Test
  public void testResolveBoundForRepoImplC() {
    assertEquals(Types.resolveBound(RepoImplC.class.getTypeParameters()[0], 0), List.class);
    assertEquals(Types.resolveBound(RepoImplC.class.getTypeParameters()[2], 0), Map.class);
  }

  @Test
  public void testResolveArgumentsAsTypesForIRepoFromRepoImplA() {
    Type[] types = Types.resolveArgumentsAsTypes(IRepo.class, RepoImplA.class);
    assertTrue(types[0] instanceof TypeVariable);
    assertEquals(((ParameterizedType) types[1]).getRawType(), ArrayList.class);
    assertEquals(((ParameterizedType) types[2]).getRawType(), Vector.class);
    assertEquals(((ParameterizedType) types[3]).getRawType(), HashSet.class);
  }

  @Test
  public void testResolveArgumentsForRepoImplCFromRepoImplA() {
    Class<?>[] types = Types.resolveArguments(RepoImplC.class, RepoImplA.class);
    assertEquals(types[0], ArrayList.class);
    assertEquals(types[1], HashSet.class);
    assertNull(types[2]);
  }

  @Test
  public void testResolveArgumentsForRepoImplCFromRepoImplB() {
    Class<?>[] types = Types.resolveArguments(RepoImplC.class, RepoImplB.class);
    assertNull(types[0]);
    assertEquals(types[1], HashSet.class);
    assertNull(types[2]);
  }

  @Test
  public void testResolveArgumentsForIRepoFromRepoImplA() {
    Class<?>[] types = Types.resolveArguments(IRepo.class, RepoImplA.class);
    assertNull(types[0]);
    assertEquals(types[1], ArrayList.class);
    assertEquals(types[2], Vector.class);
    assertEquals(types[3], HashSet.class);
  }

  @Test
  public void testResolveArgumentsForIRepoFromRepoImplB() {
    Class<?>[] types = Types.resolveArguments(IRepo.class, RepoImplB.class);
    assertNull(types[0]);
    assertNull(types[1]);
    assertEquals(types[2], Vector.class);
    assertEquals(types[3], HashSet.class);
  }

  @Test
  public void testResolveArgumentsForIRepoFromRepoImplC() {
    Class<?>[] types = Types.resolveArguments(IRepo.class, RepoImplC.class);
    assertNull(types[0]);
    assertNull(types[1]);
    assertEquals(types[2], Vector.class);
    assertNull(types[3]);
  }

  @Test
  public void testResolveArgumentsForIIRepoFromRepoImplA() {
    Class<?>[] types = Types.resolveArguments(IIRepo.class, RepoImplA.class);
    assertNull(types[0]);
    assertEquals(types[1], Vector.class);
  }

  @Test
  public void testResolveArgumentsForRepoImplBFromRepoImplA() {
    Class<?>[] types = Types.resolveArguments(RepoImplB.class, RepoImplA.class);
    assertNull(types[0]);
    assertEquals(types[1], ArrayList.class);
  }

  @Test
  public void testResolveBoundsForComplexBounds() {
    Type typeParameter = MultiBounded.class.getTypeParameters()[0];
    assertEquals(Types.resolveBound(typeParameter, 0), List.class);
    assertEquals(Types.resolveBound(typeParameter, 1), Comparable.class);
    assertEquals(Types.resolveBound(typeParameter, 2), Serializable.class);
  }
}

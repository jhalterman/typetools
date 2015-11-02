package net.jodah.typetools.functional;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import net.jodah.typetools.AbstractTypeResolverTest;
import net.jodah.typetools.TypeResolver;
import net.jodah.typetools.TypeResolver.Unknown;

/**
 * Tests the resolution of type arguments defined on lambda expressions.
 * 
 * @author Jonathan Halterman
 */
@Test
public class LambdaTest extends AbstractTypeResolverTest {
  @Factory(dataProvider = "cacheDataProvider")
  public LambdaTest(boolean cacheEnabled) {
    super(cacheEnabled);
  }

  interface FnSubclass<T, V> extends Function<T, V> {
  }

  interface ReverseFn<D, E> extends Function<E, D> {
  }

  interface SelectingFn<A, B, C> extends ReverseFn<A, C> {
  };

  interface StrToInt extends SelectingFn<Integer, Long, String> {
  };

  public static interface Function3<T, U, V, R> {
    R apply(T t, U u, V v);
  }

  interface Foo<A, B, C, D> {
    D apply(A a, B b, C c);
  }

  interface Bar<A, B, C, D> {
    void apply(A a, B b, C c);
  }

  static class Baz {
    boolean evaluate(String s) {
      return false;
    }

    static boolean eval(String s) {
      return false;
    }

    Integer apply(String a, Long b) {
      return 0;
    }

    static Integer applyStatic(String a, Long b) {
      return 0;
    }

    static <T> int convert(String test, T t) {
      return 0;
    }
  }

  @FunctionalInterface
  interface I1<F, T> {
    T apply(F f1, F f2);

    int hashCode();

    boolean equals(Object other);
  }

  /**
   * Asserts that arguments can be resolved from lambda expressions for simple functional interfaces.
   */
  public void shouldResolveArguments() {
    Predicate<String> predicate = str -> true;
    Function<String, Integer> fn = str -> Integer.valueOf(str);
    Supplier<String> supplier = () -> "test";
    Consumer<String> consumer = s -> {
    };

    assertEquals(TypeResolver.resolveRawArgument(Predicate.class, predicate.getClass()), String.class);
    assertEquals(TypeResolver.resolveRawArguments(Function.class, fn.getClass()),
        new Class<?>[] { String.class, Integer.class });
    assertEquals(TypeResolver.resolveRawArgument(Supplier.class, supplier.getClass()), String.class);
    assertEquals(TypeResolver.resolveRawArgument(Consumer.class, consumer.getClass()), String.class);
  }

  /**
   * Asserts that arguments can be resolved from method references for simple functional interfaces.
   */
  public void shouldResolveArgumentsFromMethodRefs() {
    Baz baz = new Baz();
    Predicate<String> p1 = baz::evaluate;
    Predicate<String> p2 = Baz::eval;
    BiPredicate<Baz, String> p3 = Baz::evaluate;
    Comparator<String> c = String::compareToIgnoreCase;

    assertEquals(TypeResolver.resolveRawArgument(Predicate.class, p1.getClass()), String.class);
    assertEquals(TypeResolver.resolveRawArgument(Predicate.class, p2.getClass()), String.class);
    assertEquals(TypeResolver.resolveRawArguments(BiPredicate.class, p3.getClass()),
        new Class<?>[] { Baz.class, String.class });
    assertEquals(TypeResolver.resolveRawArgument(Comparator.class, c.getClass()), String.class);
  }

  /**
   * Asserts that arguments can be resolved for interfaces that contain additional Object.class overriding methods.
   */
  public void shouldResolveArgumentsFromNonSamMethodRef() throws Throwable {
    I1<String, Integer> fn = String::compareToIgnoreCase;
    assertEquals(TypeResolver.resolveRawArguments(I1.class, fn.getClass()),
        new Class<?>[] { String.class, Integer.class });
  }

  /**
   * Asserts that method references with primitive type arguments that are auto boxed to primitive wrappers are properly
   * handled.
   * 
   * Note: disabled since method signature exposed via constant pool contains convert(String, Object). Subsequent
   * bytecode contains convert(String, String). May need ASM to read.
   */
  @Test(enabled = false)
  public void shouldResolveArgumentsForAutoBoxedMethodRefArgument() throws Throwable {
    I1<String, Integer> fn = Baz::convert;
    assertEquals(TypeResolver.resolveRawArguments(I1.class, fn.getClass()),
        new Class<?>[] { String.class, Integer.class });
  }

  /**
   * Asserts that arguments can be resolved from lambda expressions for simple functional interfaces that contain
   * multiple type parameters.
   */
  public void shouldResolveMultiArguments() {
    BiFunction<String, Long, Integer> biFn = (str1, str2) -> Integer.valueOf(str1 + str2);
    BiConsumer<String, String> consumer1 = (s1, s2) -> {
    };
    BiConsumer<String, Long> consumer2 = (s1, s2) -> {
    };
    Foo<String, Long, Integer, Double> foo = (a, b, c) -> 2.0;
    Bar<String, Long, Integer, Double> bar = (a, b, c) -> {
    };

    assertEquals(TypeResolver.resolveRawArguments(BiFunction.class, biFn.getClass()),
        new Class<?>[] { String.class, Long.class, Integer.class });
    assertEquals(TypeResolver.resolveRawArguments(BiConsumer.class, consumer1.getClass()),
        new Class<?>[] { String.class, String.class });
    assertEquals(TypeResolver.resolveRawArguments(BiConsumer.class, consumer2.getClass()),
        new Class<?>[] { String.class, Long.class });
    assertEquals(TypeResolver.resolveRawArguments(Foo.class, foo.getClass()),
        new Class<?>[] { String.class, Long.class, Integer.class, Double.class });
    assertEquals(TypeResolver.resolveRawArguments(Bar.class, bar.getClass()),
        new Class<?>[] { String.class, Long.class, Integer.class, Unknown.class });
  }

  /**
   * Asserts that arguments can be resolved from method references for simple functional interfaces that contain
   * multiple type parameters.
   */
  public void shouldResolveMultiArgumentsForMethodRefs() {
    Baz baz = new Baz();
    BiFunction<String, Long, Integer> f1 = baz::apply;
    BiFunction<String, Long, Integer> f2 = Baz::applyStatic;
    Function3<Baz, String, Long, Integer> f3 = Baz::apply;

    assertEquals(TypeResolver.resolveRawArguments(BiFunction.class, f1.getClass()),
        new Class<?>[] { String.class, Long.class, Integer.class });
    assertEquals(TypeResolver.resolveRawArguments(BiFunction.class, f2.getClass()),
        new Class<?>[] { String.class, Long.class, Integer.class });
    assertEquals(TypeResolver.resolveRawArguments(Function3.class, f3.getClass()),
        new Class<?>[] { Baz.class, String.class, Long.class, Integer.class });
  }

  /**
   * Asserts that arguments can be resolved from a lambda expression when declared on a subclass of some type.
   */
  public void shouldResolveSubclassArguments() {
    FnSubclass<String, Integer> fn = str -> Integer.valueOf(str);
    assertEquals(TypeResolver.resolveRawArguments(Function.class, fn.getClass()),
        new Class<?>[] { String.class, Integer.class });
  }

  /**
   * Asserts that arguments can be resolved from a method reference when declared on a subclass of some type.
   */
  public void shouldResolveSubclassArgumentsForMethodRefs() {
    FnSubclass<String, Integer> fn = Integer::valueOf;
    assertEquals(TypeResolver.resolveRawArguments(Function.class, fn.getClass()),
        new Class<?>[] { String.class, Integer.class });
  }

  public void shouldResolveTransposedSubclassArguments() {
    SelectingFn<Integer, Long, String> fn = (String str) -> Integer.valueOf(str);
    assertEquals(TypeResolver.resolveRawArguments(SelectingFn.class, fn.getClass()),
        new Class<?>[] { Integer.class, Unknown.class, String.class });
    assertEquals(TypeResolver.resolveRawArguments(ReverseFn.class, fn.getClass()),
        new Class<?>[] { Integer.class, String.class });
    assertEquals(TypeResolver.resolveRawArguments(Function.class, fn.getClass()),
        new Class<?>[] { String.class, Integer.class });

    StrToInt fn1 = (String str) -> Integer.valueOf(str);
    assertEquals(TypeResolver.resolveRawArguments(StrToInt.class, fn1.getClass()), new Class<?>[] {});
    assertEquals(TypeResolver.resolveRawArguments(SelectingFn.class, fn1.getClass()),
        new Class<?>[] { Integer.class, Long.class, String.class });
    assertEquals(TypeResolver.resolveRawArguments(ReverseFn.class, fn1.getClass()),
        new Class<?>[] { Integer.class, String.class });
    assertEquals(TypeResolver.resolveRawArguments(Function.class, fn1.getClass()),
        new Class<?>[] { String.class, Integer.class });
  }

  /**
   * Asserts that lambdas passed into methods can be resolved.
   */
  public void shouldHandlePassedLambda() {
    handlePassedFunction((UUID i) -> i.toString());
  }

  /**
   * Asserts that method references passed into methods can be resolved.
   */
  public void shouldHandlePassedMethodRef() {
    handlePassedFunction(UUID::toString);
  }

  private <T, R> void handlePassedFunction(Function<T, R> fn) {
    Class<?>[] typeArgs = TypeResolver.resolveRawArguments(Function.class, fn.getClass());
    assertEquals(typeArgs[0], UUID.class);
    assertEquals(typeArgs[1], String.class);
  }

  /**
   * Asserts that when resolving type argument variables for a method ref, the correct ParameterizedType is returned.
   */
  public void shouldResolveMethodRefGenericArguments() {
    Consumer<Optional<String>> consumer = this::foo;

    Type[] arguments = TypeResolver.resolveTypeArgumentVariables(Consumer.class, consumer.getClass());
    assertEquals(arguments.length, 1);
    Type argument = arguments[0];
    assertTrue(argument instanceof ParameterizedType);
    ParameterizedType type = (ParameterizedType) argument;
    assertEquals(type.getRawType(), Optional.class);
    assertEquals(type.getActualTypeArguments()[0], String.class);
  }

  private void foo(Optional<String> foo) {
  }

  /**
   * Asserts that when resolving type argument variables for a lambda, the correct ParameterizedType is returned.
   *
   * Note: disabled since the Java compiler doesn't include generic information in synthetic lambda method signatures.
   */
  @Test(enabled = false)
  public void shouldResolveLambdaGenericArguments() {
    Consumer<Optional<String>> consumer = (Optional<String> optString) -> System.out.println(optString);

    Type[] arguments = TypeResolver.resolveTypeArgumentVariables(Consumer.class, consumer.getClass());
    assertEquals(arguments.length, 1);
    Type argument = arguments[0];
    assertTrue(argument instanceof ParameterizedType);
    ParameterizedType type = (ParameterizedType) argument;
    assertEquals(type.getRawType(), Optional.class);
    assertEquals(type.getActualTypeArguments()[0], String.class);
  }

  public static class Foo2 {
    public Foo2(Optional<String> arg) {}
  }

  /**
   * Asserts that when resolving type argument variables for a constructor ref, the correct ParameterizedType is
   * returned.
   */
  public void shouldResolveConstructorRefArguments() {
    Consumer<Optional<String>> consumer = Foo2::new;

    Type[] arguments = TypeResolver.resolveTypeArgumentVariables(Consumer.class, consumer.getClass());
    assertEquals(arguments.length, 1);
    Type argument = arguments[0];
    assertTrue(argument instanceof ParameterizedType);
    ParameterizedType type = (ParameterizedType) argument;
    assertEquals(type.getRawType(), Optional.class);
    assertEquals(type.getActualTypeArguments()[0], String.class);
  }

}

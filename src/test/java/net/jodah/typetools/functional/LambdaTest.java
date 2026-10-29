package net.jodah.typetools.functional;

import net.jodah.typetools.TypeResolver;
import net.jodah.typetools.TypeResolver.Unknown;
import net.jodah.typetools.TypeResolverCacheTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.Comparator;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.DoubleFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the resolution of type arguments defined on lambda expressions.
 *
 * @author Jonathan Halterman
 */
@TypeResolverCacheTest
class LambdaTest {

  interface TriPredicate<A, B, C> {
    boolean test(A a, B b, C c);
  }

  interface FnSubclass<T, V> extends Function<T, V> {
  }

  interface ReverseFn<D, E> extends Function<E, D> {
  }

  interface SelectingFn<A, B, C> extends ReverseFn<A, C> {
  }

  interface StrToInt extends SelectingFn<Integer, Long, String> {
  }

  interface SerializableFn<T, V> extends Function<T, V>, Serializable {
  }

  public interface Function3<T, U, V, R> {
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

    boolean evaluate2(String s, int i) {
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

    String toString();
  }

  /**
   * Asserts that arguments can be resolved from lambda expressions for simple
   * functional interfaces.
   */
  @Test
  void shouldResolveArguments() {
    Predicate<String> predicate = str -> true;
    Function<String, Integer> fn = str -> Integer.valueOf(str);
    Supplier<String> supplier = () -> "test";
    Consumer<String> consumer = s -> {
    };

    assertThat(TypeResolver.resolveRawArgument(Predicate.class, predicate.getClass())).isEqualTo(String.class);
    assertThat(TypeResolver.resolveRawArguments(Function.class, fn.getClass())) //
        .containsExactly(String.class, Integer.class);
    assertThat(TypeResolver.resolveRawArgument(Supplier.class, supplier.getClass())).isEqualTo(String.class);
    assertThat(TypeResolver.resolveRawArgument(Consumer.class, consumer.getClass())).isEqualTo(String.class);
  }

  /**
   * Asserts that arguments captured from the enclosing scope can be resolved.
   */
  @Test
  void shouldResolveCapturedArguments() {
    final AtomicLong a = new AtomicLong(0);
    Function<String, Long> func = (s) -> {
      a.incrementAndGet();
      return (long) s.hashCode();
    };
    assertThat(TypeResolver.resolveRawArguments(Function.class, func.getClass())) //
        .containsExactly(String.class, Long.class);
  }

  /**
   * Asserts that arguments can be resolved from instance method references for
   * simple functional interfaces.
   */
  @Test
  void shouldResolveArgumentsFromInstanceMethodRefs() {
    Baz baz = new Baz();
    Predicate<String> p1 = baz::evaluate;

    assertThat(TypeResolver.resolveRawArgument(Predicate.class, p1.getClass())).isEqualTo(String.class);
  }

  /**
   * Asserts that arguments can be resolved from static method references for
   * simple functional interfaces.
   */
  @Test
  void shouldResolveArgumentsFromStaticMethodRefs() {
    Comparator<String> c = String::compareToIgnoreCase;

    assertThat(TypeResolver.resolveRawArgument(Comparator.class, c.getClass())).isEqualTo(String.class);
  }

  /**
   * Asserts that arguments can be resolved from arbitrary object method
   * references for simple functional interfaces.
   */
  @Test
  void shouldResolveArgumentsFromArbitraryObjectMethodRefs() {
    Predicate<String> p1 = Baz::eval;
    BiPredicate<Baz, String> p2 = Baz::evaluate;
    TriPredicate<Baz, String, Integer> p3 = Baz::evaluate2;

    assertThat(TypeResolver.resolveRawArgument(Predicate.class, p1.getClass())).isEqualTo(String.class);
    assertThat(TypeResolver.resolveRawArguments(BiPredicate.class, p2.getClass())) //
        .containsExactly(Baz.class, String.class);
    assertThat(TypeResolver.resolveRawArguments(TriPredicate.class, p3.getClass())) //
        .containsExactly(Baz.class, String.class, Integer.class);
  }

  /**
   * Asserts that arguments can be resolved for interfaces that contain additional
   * Object.class overriding methods.
   */
  @Test
  void shouldResolveArgumentsFromNonSamMethodRef() {
    I1<String, Integer> fn = String::compareToIgnoreCase;
    assertThat(TypeResolver.resolveRawArguments(I1.class, fn.getClass())) //
        .containsExactly(String.class, Integer.class);
  }

  /**
   * Asserts that method references with primitive type arguments that are auto
   * boxed to primitive wrappers are properly handled. Note: disabled since method
   * signature exposed via constant pool contains convert(String, Object).
   * Subsequent bytecode contains convert(String, String). May need ASM to read.
   */
  @Test
  @Disabled("method signature exposed via constant pool contains convert(String, Object). Subsequent bytecode contains convert(String, String)")
  void shouldResolveArgumentsForAutoBoxedMethodRefArgument() {
    BiFunction<String, Boolean, Integer> fn1 = Baz::convert;
    assertThat(TypeResolver.resolveRawArguments(BiFunction.class, fn1.getClass())) //
        .containsExactly(String.class, Boolean.class, Integer.class);
  }

  /**
   * Asserts that arguments can be resolved from lambda expressions for simple
   * functional interfaces that contain multiple type parameters.
   */
  @Test
  void shouldResolveMultiArguments() {
    BiFunction<String, Long, Integer> biFn = (str1, str2) -> Integer.valueOf(str1 + str2);
    BiConsumer<String, String> consumer1 = (s1, s2) -> {
    };
    BiConsumer<String, Long> consumer2 = (s1, s2) -> {
    };
    Foo<String, Long, Integer, Double> foo = (a, b, c) -> 2.0;
    Bar<String, Long, Integer, Double> bar = (a, b, c) -> {
    };

    assertThat(TypeResolver.resolveRawArguments(BiFunction.class, biFn.getClass())) //
        .containsExactly(String.class, Long.class, Integer.class);
    assertThat(TypeResolver.resolveRawArguments(BiConsumer.class, consumer1.getClass())) //
        .containsExactly(String.class, String.class);
    assertThat(TypeResolver.resolveRawArguments(BiConsumer.class, consumer2.getClass())) //
        .containsExactly(String.class, Long.class);
    assertThat(TypeResolver.resolveRawArguments(Foo.class, foo.getClass())) //
        .containsExactly(String.class, Long.class, Integer.class, Double.class);
    assertThat(TypeResolver.resolveRawArguments(Bar.class, bar.getClass())) //
        .containsExactly(String.class, Long.class, Integer.class, Unknown.class);
  }

  /**
   * Asserts that arguments can be resolved from method references for simple
   * functional interfaces that contain multiple type parameters.
   */
  @Test
  void shouldResolveMultiArgumentsForMethodRefs() {
    Baz baz = new Baz();
    BiFunction<String, Long, Integer> f1 = baz::apply;
    BiFunction<String, Long, Integer> f2 = Baz::applyStatic;
    Function3<Baz, String, Long, Integer> f3 = Baz::apply;

    assertThat(TypeResolver.resolveRawArguments(BiFunction.class, f1.getClass())) //
        .containsExactly(String.class, Long.class, Integer.class);
    assertThat(TypeResolver.resolveRawArguments(BiFunction.class, f2.getClass())) //
        .containsExactly(String.class, Long.class, Integer.class);
    assertThat(TypeResolver.resolveRawArguments(Function3.class, f3.getClass())) //
        .containsExactly(Baz.class, String.class, Long.class, Integer.class);
  }

  /**
   * Asserts that arguments can be resolved from a lambda expression when declared
   * on a subclass of some type.
   */
  @Test
  void shouldResolveSubclassArguments() {
    FnSubclass<String, Integer> fn = str -> Integer.valueOf(str);
    assertThat(TypeResolver.resolveRawArguments(Function.class, fn.getClass())) //
        .containsExactly(String.class, Integer.class);
  }

  @Test
  void shouldResolveSubclassArgumentsForConstructor() {
    FnSubclass<String, Integer> fn = str -> new Integer(str);
    assertThat(TypeResolver.resolveRawArguments(Function.class, fn.getClass())) //
        .containsExactly(String.class, Integer.class);
  }

  /**
   * Asserts that arguments can be resolved from a method reference when declared
   * on a subclass of some type.
   */
  @Test
  void shouldResolveSubclassArgumentsForMethodRefs() {
    FnSubclass<String, Integer> fn = Integer::valueOf;
    assertThat(TypeResolver.resolveRawArguments(Function.class, fn.getClass())) //
        .containsExactly(String.class, Integer.class);
  }

  @Test
  void shouldResolveSubclassArgumentsForConstructorRef() {
    FnSubclass<String, Integer> fn = Integer::new;
    assertThat(TypeResolver.resolveRawArguments(Function.class, fn.getClass())) //
        .containsExactly(String.class, Integer.class);
  }

  @Test
  void shouldResolveObjectClassMethodRef() {
    Function<?, String> fn = Object::toString;
    assertThat(TypeResolver.resolveRawArguments(Function.class, fn.getClass())) //
        .containsExactly(Object.class, String.class);
  }

  @Test
  void shouldResolveObjectClassConstructorRef() {
    Supplier<?> fn = Object::new;
    assertThat(TypeResolver.resolveRawArguments(Supplier.class, fn.getClass())) //
        .containsExactly(Object.class);
  }

  @Test
  void shouldResolveUnboxedReturnValue() {
    ToDoubleFunction<String> fn = Double::new; // this method returns Double, thus unboxing takes place
    assertThat(TypeResolver.resolveRawArguments(ToDoubleFunction.class, fn.getClass())) //
        .containsExactly(String.class);
  }

  @Test
  void shouldResolveUnboxedReturnValue2() {
    ToDoubleFunction<String> fn = Double::valueOf; // this method returns Double, thus unboxing takes place
    assertThat(TypeResolver.resolveRawArguments(ToDoubleFunction.class, fn.getClass())) //
        .containsExactly(String.class);
  }

  @Test
  void shouldResolveUnboxedReturnValue3() {
    ToDoubleFunction<Double> fn = Double::doubleValue;
    assertThat(TypeResolver.resolveRawArguments(ToDoubleFunction.class, fn.getClass())) //
        .containsExactly(Double.class);
  }

  @Test
  void shouldResolveBoxedReturnValue() {
    DoubleFunction<Double> fn = Double::valueOf; // this method returns Double, thus boxing takes place
    assertThat(TypeResolver.resolveRawArguments(DoubleFunction.class, fn.getClass())) //
        .containsExactly(Double.class);
  }

  @Test
  void shouldResolveTransposedSubclassArguments() {
    SelectingFn<Integer, Long, String> fn = (String str) -> Integer.valueOf(str);
    assertThat(TypeResolver.resolveRawArguments(SelectingFn.class, fn.getClass())) //
        .containsExactly(Integer.class, Unknown.class, String.class);
    assertThat(TypeResolver.resolveRawArguments(ReverseFn.class, fn.getClass())) //
        .containsExactly(Integer.class, String.class);
    assertThat(TypeResolver.resolveRawArguments(Function.class, fn.getClass())) //
        .containsExactly(String.class, Integer.class);

    StrToInt fn1 = (String str) -> Integer.valueOf(str);
    assertThat(TypeResolver.resolveRawArguments(StrToInt.class, fn1.getClass())).isEmpty();
    assertThat(TypeResolver.resolveRawArguments(SelectingFn.class, fn1.getClass())) //
        .containsExactly(Integer.class, Long.class, String.class);
    assertThat(TypeResolver.resolveRawArguments(ReverseFn.class, fn1.getClass())) //
        .containsExactly(Integer.class, String.class);
    assertThat(TypeResolver.resolveRawArguments(Function.class, fn1.getClass())) //
        .containsExactly(String.class, Integer.class);
  }

  /**
   * Asserts that lambdas passed into methods can be resolved.
   */
  @Test
  void shouldHandlePassedLambda() {
    handlePassedFunction((UUID i) -> i.toString());
  }

  @Test
  void shouldHandlePassedSerializableLambda() {
    handlePassedFunction((SerializableFn<UUID, String>) i -> i.toString());
  }

  /**
   * Asserts that method references passed into methods can be resolved.
   */
  @Test
  void shouldHandlePassedMethodRef() {
    handlePassedFunction(UUID::toString);
  }

  private <T, R> void handlePassedFunction(Function<T, R> fn) {
    Class<?>[] typeArgs = TypeResolver.resolveRawArguments(Function.class, fn.getClass());
    assertThat(typeArgs[0]).isEqualTo(UUID.class);
    assertThat(typeArgs[1]).isEqualTo(String.class);
  }
}

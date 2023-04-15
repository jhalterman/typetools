package net.jodah.typetools.functional;

import static org.testng.Assert.assertEquals;

import java.io.Serializable;
import java.util.Comparator;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;

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
   * Asserts that arguments captured from the enclosing scope can be resolved.
   */
  public void shouldResolveCapturedArguments() {
    final AtomicLong a = new AtomicLong(0);
    Function<String, Long> func = (s) -> {
      a.incrementAndGet();
      return (long) s.hashCode();
    };

    assertEquals(new Class<?>[] { String.class, Long.class },
        TypeResolver.resolveRawArguments(Function.class, func.getClass()));
  }

  /**
   * Asserts that arguments can be resolved from instance method references for simple functional interfaces.
   */
  public void shouldResolveArgumentsFromInstanceMethodRefs() {
    Baz baz = new Baz();
    Predicate<String> p1 = baz::evaluate;

    assertEquals(TypeResolver.resolveRawArgument(Predicate.class, p1.getClass()), String.class);
  }

  /**
   * Asserts that arguments can be resolved from static method references for simple functional interfaces.
   */
  public void shouldResolveArgumentsFromStaticMethodRefs() {
    Comparator<String> c = String::compareToIgnoreCase;

    assertEquals(TypeResolver.resolveRawArgument(Comparator.class, c.getClass()), String.class);
  }

  /**
   * Asserts that arguments can be resolved from arbitrary object method references for simple functional interfaces.
   */
  public void shouldResolveArgumentsFromArbitraryObjectMethodRefs() {
    Predicate<String> p1 = Baz::eval;
    BiPredicate<Baz, String> p2 = Baz::evaluate;
    TriPredicate<Baz, String, Integer> p3 = Baz::evaluate2;

    assertEquals(TypeResolver.resolveRawArgument(Predicate.class, p1.getClass()), String.class);
    assertEquals(TypeResolver.resolveRawArguments(BiPredicate.class, p2.getClass()),
        new Class<?>[] { Baz.class, String.class });
    assertEquals(TypeResolver.resolveRawArguments(TriPredicate.class, p3.getClass()),
        new Class<?>[] { Baz.class, String.class, Integer.class });
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
    BiFunction<String, Boolean, Integer> fn1 = Baz::convert;
    assertEquals(TypeResolver.resolveRawArguments(BiFunction.class, fn1.getClass()),
        new Class<?>[] { String.class, Boolean.class, Integer.class });
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

  public void shouldResolveSubclassArgumentsForConstructor() {
    FnSubclass<String, Integer> fn = str -> new Integer(str);
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

  public void shouldResolveSubclassArgumentsForConstructorRef() {
    FnSubclass<String, Integer> fn = Integer::new;
    assertEquals(TypeResolver.resolveRawArguments(Function.class, fn.getClass()),
        new Class<?>[] { String.class, Integer.class });
  }

  public void shouldResolveObjectClassMethodRef() {
    Function<?, String> fn = Object::toString;
    assertEquals(TypeResolver.resolveRawArguments(Function.class, fn.getClass()),
        new Class<?>[] { Object.class, String.class });
  }

  public void shouldResolveObjectClassConstructorRef() {
    Supplier<?> fn = Object::new;
    assertEquals(TypeResolver.resolveRawArguments(Supplier.class, fn.getClass()),
        new Class<?>[] { Object.class });
  }

  public void shouldResolvePrimitiveReturnValue() {
    ToDoubleFunction<String> fn = Double::new; //this method returns Double, thus unboxing takes place
    assertEquals(TypeResolver.resolveRawArguments(ToDoubleFunction.class, fn.getClass()),
        new Class<?>[] { String.class });
  }

  public void shouldResolvePrimitiveReturnValue2() {
    ToDoubleFunction<String> fn = Double::valueOf; //this method returns Double, thus unboxing takes place
    assertEquals(TypeResolver.resolveRawArguments(ToDoubleFunction.class, fn.getClass()),
        new Class<?>[] { String.class });
  }

  public void shouldResolvePrimitiveReturnValue3() {
    ToDoubleFunction<Double> fn = Double::doubleValue;
    assertEquals(TypeResolver.resolveRawArguments(ToDoubleFunction.class, fn.getClass()),
        new Class<?>[] { Double.class });
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

  public void shouldHandlePassedSerializableLambda() {
    handlePassedFunction((SerializableFn<UUID, String>) i -> i.toString());
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
}

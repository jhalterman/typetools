package net.jodah.typetools;

import static org.testng.Assert.assertEquals;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import net.jodah.typetools.TypeResolver.Unknown;

import org.testng.annotations.Test;

/**
 * Tests the resolution of type arguments defined on lambda expressions.
 * 
 * @author Jonathan Halterman
 */
@Test
public class LambdaTest {
  interface FnSubclass<T, V> extends Function<T, V> {
  }

  interface ReverseFn<D, E> extends Function<E, D> {
  }

  interface SelectingFn<A, B, C> extends ReverseFn<A, C> {
  };

  interface StrToInt extends SelectingFn<Integer, Long, String> {
  };

  interface Foo<A, B, C, D> {
    D apply(A a, B b, C c);
  }

  interface Bar<A, B, C, D> {
    void apply(A a, B b, C c);
  }

  public void shouldResolveArguments() throws Throwable {
    Predicate<String> predicate = str -> true;
    assertEquals(TypeResolver.resolveRawArgument(Predicate.class, predicate.getClass()),
      String.class);

    Function<String, Integer> fn = str -> Integer.valueOf(str);
    assertEquals(TypeResolver.resolveRawArguments(Function.class, fn.getClass()), new Class<?>[] {
        String.class, Integer.class });

    Supplier<String> supplier = () -> "test";
    assertEquals(TypeResolver.resolveRawArgument(Supplier.class, supplier.getClass()), String.class);

    Consumer<String> consumer = s -> {
    };
    assertEquals(TypeResolver.resolveRawArgument(Consumer.class, consumer.getClass()), String.class);
  }

  public void shouldResolveMultiArguments() {
    BiFunction<String, Long, Integer> biFn = (str1, str2) -> Integer.valueOf(str1 + str2);
    assertEquals(TypeResolver.resolveRawArguments(BiFunction.class, biFn.getClass()),
      new Class<?>[] { String.class, Long.class, Integer.class });

    BiConsumer<String, String> consumer1 = (s1, s2) -> {
    };
    assertEquals(TypeResolver.resolveRawArguments(BiConsumer.class, consumer1.getClass()),
      new Class<?>[] { String.class, String.class });

    BiConsumer<String, Long> consumer2 = (s1, s2) -> {
    };
    assertEquals(TypeResolver.resolveRawArguments(BiConsumer.class, consumer2.getClass()),
      new Class<?>[] { String.class, Long.class });

    Foo<String, Long, Integer, Double> foo = (a, b, c) -> 2.0;
    assertEquals(TypeResolver.resolveRawArguments(Foo.class, foo.getClass()), new Class<?>[] {
        String.class, Long.class, Integer.class, Double.class });

    Bar<String, Long, Integer, Double> bar = (a, b, c) -> {
    };
    assertEquals(TypeResolver.resolveRawArguments(Bar.class, bar.getClass()), new Class<?>[] {
        String.class, Long.class, Integer.class, Unknown.class });
  }

  public void shouldResolveSubclassArguments() {
    FnSubclass<String, Integer> fn = str -> Integer.valueOf(str);
    assertEquals(TypeResolver.resolveRawArguments(Function.class, fn.getClass()), new Class<?>[] {
        String.class, Integer.class });
  }

  public void shouldResolveTransposedSubclassArguments() {
    SelectingFn<Integer, Long, String> fn = (String str) -> Integer.valueOf(str);
    assertEquals(TypeResolver.resolveRawArguments(SelectingFn.class, fn.getClass()),
      new Class<?>[] { Integer.class, Unknown.class, String.class });
    assertEquals(TypeResolver.resolveRawArguments(ReverseFn.class, fn.getClass()), new Class<?>[] {
        Integer.class, String.class });
    assertEquals(TypeResolver.resolveRawArguments(Function.class, fn.getClass()), new Class<?>[] {
        String.class, Integer.class });

    StrToInt fn1 = (String str) -> Integer.valueOf(str);
    assertEquals(TypeResolver.resolveRawArguments(StrToInt.class, fn1.getClass()),
      new Class<?>[] {});
    assertEquals(TypeResolver.resolveRawArguments(SelectingFn.class, fn1.getClass()),
      new Class<?>[] { Integer.class, Long.class, String.class });
    assertEquals(TypeResolver.resolveRawArguments(ReverseFn.class, fn1.getClass()), new Class<?>[] {
        Integer.class, String.class });
    assertEquals(TypeResolver.resolveRawArguments(Function.class, fn1.getClass()), new Class<?>[] {
        String.class, Integer.class });
  }
}

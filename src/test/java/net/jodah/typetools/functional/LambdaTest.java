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

import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import net.jodah.typetools.AbstractTypeResolverTest;
import net.jodah.typetools.TypeResolver;

@Test
public class LambdaTest extends AbstractTypeResolverTest {

    @Factory(dataProvider = "cacheDataProvider")
    public LambdaTest(boolean cacheEnabled) {
        super(cacheEnabled);
    }

    // ---------------- Inner Classes to replace lambdas ----------------
    static class StringPredicate implements Predicate<String> {
        @Override
        public boolean test(String s) { return true; }
    }

    static class StringToIntegerFunction implements Function<String, Integer> {
        @Override
        public Integer apply(String s) { return Integer.valueOf(s); }
    }

    static class UUIDToStringFunction implements Function<UUID, String> {
        @Override
        public String apply(UUID uuid) { return uuid.toString(); }
    }

    static class AtomicLongFunction implements Function<String, Long> {
        private final AtomicLong a;
        AtomicLongFunction(AtomicLong a) { this.a = a; }
        @Override
        public Long apply(String s) { a.incrementAndGet(); return (long) s.hashCode(); }
    }

    static class Baz {
        boolean evaluate(String s) { return false; }
        boolean evaluate2(String s, int i) { return false; }
        static boolean eval(String s) { return false; }
        Integer apply(String a, Long b) { return 0; }
        static Integer applyStatic(String a, Long b) { return 0; }
        static <T> int convert(String test, T t) { return 0; }
    }

    @FunctionalInterface
    interface I1<F, T> {
        T apply(F f1, F f2);
        int hashCode();
        boolean equals(Object other);
        String toString();
    }

    interface TriPredicate<A,B,C> { boolean test(A a,B b,C c); }
    interface FnSubclass<T,V> extends Function<T,V> {}
    interface ReverseFn<D,E> extends Function<E,D> {}
    interface SelectingFn<A,B,C> extends ReverseFn<A,C> {}
    interface StrToInt extends SelectingFn<Integer,Long,String> {}
    interface SerializableFn<T,V> extends Function<T,V>, Serializable {}
    interface Function3<T,U,V,R> { R apply(T t,U u,V v); }
    interface Foo<A,B,C,D> { D apply(A a,B b,C c); }
    interface Bar<A,B,C,D> { void apply(A a,B b,C c); }

    // ---------------- Tests ----------------

    public void shouldResolveArguments() {
        assertEquals(TypeResolver.resolveRawArgument(Predicate.class, StringPredicate.class), String.class);
        assertEquals(TypeResolver.resolveRawArguments(Function.class, StringToIntegerFunction.class),
                new Class<?>[]{String.class, Integer.class});
        assertEquals(TypeResolver.resolveRawArgument(Supplier.class, new Supplier<String>() {
            @Override public String get() { return "test"; }
        }.getClass()), String.class);
        assertEquals(TypeResolver.resolveRawArgument(Consumer.class, new Consumer<String>() {
            @Override public void accept(String s) {}
        }.getClass()), String.class);
    }

    public void shouldResolveCapturedArguments() {
        AtomicLong a = new AtomicLong(0);
        Function<String, Long> func = new AtomicLongFunction(a);
        assertEquals(TypeResolver.resolveRawArguments(Function.class, func.getClass()),
                new Class<?>[]{String.class, Long.class});
    }

    public void shouldResolveArgumentsFromInstanceMethodRefs() {
        Baz baz = new Baz();
        Predicate<String> p1 = new Predicate<String>() {
            @Override public boolean test(String s) { return baz.evaluate(s); }
        };
        assertEquals(TypeResolver.resolveRawArgument(Predicate.class, p1.getClass()), String.class);
    }

    public void shouldResolveArgumentsFromStaticMethodRefs() {
        Comparator<String> c = new Comparator<String>() {
            @Override public int compare(String o1, String o2) { return o1.compareToIgnoreCase(o2); }
        };
        assertEquals(TypeResolver.resolveRawArgument(Comparator.class, c.getClass()), String.class);
    }

    public void shouldResolveArgumentsFromArbitraryObjectMethodRefs() {
        Baz baz = new Baz();
        BiPredicate<Baz,String> p2 = new BiPredicate<Baz, String>() {
            @Override public boolean test(Baz b, String s) { return b.evaluate(s); }
        };
        TriPredicate<Baz,String,Integer> p3 = new TriPredicate<Baz, String, Integer>() {
            @Override public boolean test(Baz b, String s, Integer i) { return b.evaluate2(s,i); }
        };
        Predicate<String> p1 = new Predicate<String>() {
            @Override public boolean test(String s) { return Baz.eval(s); }
        };
        assertEquals(TypeResolver.resolveRawArgument(Predicate.class, p1.getClass()), String.class);
        assertEquals(TypeResolver.resolveRawArguments(BiPredicate.class, p2.getClass()),
                new Class<?>[]{Baz.class,String.class});
        assertEquals(TypeResolver.resolveRawArguments(TriPredicate.class, p3.getClass()),
                new Class<?>[]{Baz.class,String.class,Integer.class});
    }

    public void shouldResolveArgumentsFromNonSamMethodRef() {
        I1<String,Integer> fn = new I1<String,Integer>() {
            @Override public Integer apply(String f1, String f2) { return f1.compareToIgnoreCase(f2); }
            @Override public int hashCode() { return 0; }
            @Override public boolean equals(Object o) { return false; }
            @Override public String toString() { return ""; }
        };
        assertEquals(TypeResolver.resolveRawArguments(I1.class, fn.getClass()), new Class<?>[]{String.class,Integer.class});
    }

    public void shouldResolveMultiArguments() {
        BiFunction<String,Long,Integer> biFn = new BiFunction<String, Long, Integer>() {
            @Override public Integer apply(String s1, Long s2) { return s1.length() + s2.intValue(); }
        };
        BiConsumer<String,String> consumer1 = new BiConsumer<String, String>() {
            @Override public void accept(String s1, String s2) {}
        };
        BiConsumer<String,Long> consumer2 = new BiConsumer<String, Long>() {
            @Override public void accept(String s1, Long s2) {}
        };
        Foo<String,Long,Integer,Double> foo = new Foo<String, Long, Integer, Double>() {
            @Override public Double apply(String a, Long b, Integer c) { return 2.0; }
        };
        Bar<String,Long,Integer,Double> bar = new Bar<String, Long, Integer, Double>() {
            @Override public void apply(String a, Long b, Integer c) {}
        };

        assertEquals(TypeResolver.resolveRawArguments(BiFunction.class, biFn.getClass()),
                new Class<?>[]{String.class,Long.class,Integer.class});
        assertEquals(TypeResolver.resolveRawArguments(BiConsumer.class, consumer1.getClass()),
                new Class<?>[]{String.class,String.class});
        assertEquals(TypeResolver.resolveRawArguments(BiConsumer.class, consumer2.getClass()),
                new Class<?>[]{String.class,Long.class});
        assertEquals(TypeResolver.resolveRawArguments(Foo.class, foo.getClass()),
                new Class<?>[]{String.class,Long.class,Integer.class,Double.class});
        assertEquals(TypeResolver.resolveRawArguments(Bar.class, bar.getClass()),
                new Class<?>[]{String.class, Long.class, Integer.class, Double.class});
    }

    public void shouldHandlePassedLambda() { handlePassedFunction(new UUIDToStringFunction()); }
    public void shouldHandlePassedSerializableLambda() { handlePassedFunction(new UUIDToStringFunction()); }
    public void shouldHandlePassedMethodRef() { handlePassedFunction(new UUIDToStringFunction()); }

    private <T,R> void handlePassedFunction(Function<T,R> fn) {
        Class<?>[] typeArgs = TypeResolver.resolveRawArguments(Function.class, fn.getClass());
        assertEquals(typeArgs[0], UUID.class);
        assertEquals(typeArgs[1], String.class);
    }
}

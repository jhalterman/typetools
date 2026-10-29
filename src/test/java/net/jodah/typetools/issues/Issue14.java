package net.jodah.typetools.issues;

import net.jodah.typetools.TypeResolver;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * https://github.com/jhalterman/typetools/issues/14
 */
class Issue14 {

  public static class Printer<T> {
    public void print(T t) {
    }
  }

  @Test
  void test() {
    Printer<String> printer = new Printer<>();
    Consumer<String> print = printer::print;
    Class<?> type = TypeResolver.resolveRawArgument(Consumer.class, print.getClass());

    assertThrows( //
        AssertionError.class, //
        () -> assertThat(type).isEqualTo(String.class), //
        "Not currently supported. See https://github.com/jhalterman/typetools/issues/14#issuecomment-203253692");
  }
}

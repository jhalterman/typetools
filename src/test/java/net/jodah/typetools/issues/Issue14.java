package net.jodah.typetools.issues;

import net.jodah.typetools.TypeResolver;
import org.testng.annotations.Test;

import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * https://github.com/jhalterman/typetools/issues/14
 */
public class Issue14 {
  public static class Printer<T> {
    public void print(T t) {
    }
  }

  /**
   * Not currently supported. See
   * https://github.com/jhalterman/typetools/issues/14#issuecomment-203253692
   */
  @Test(enabled = false)
  public void test() {
    Printer<String> printer = new Printer<>();
    Consumer<String> print = printer::print;
    Class<?> type = TypeResolver.resolveRawArgument(Consumer.class, print.getClass());

    assertThat(type).isEqualTo(String.class);
  }
}

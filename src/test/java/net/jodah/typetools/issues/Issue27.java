package net.jodah.typetools.issues;

import net.jodah.typetools.TypeResolver;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * https://github.com/jhalterman/typetools/issues/27
 */
class Issue27 {

  @Test
  void test() {
    Function<String[], String> fn = (String[] strings) -> strings.toString();

    Class<?>[] args = TypeResolver.resolveRawArguments(Function.class, fn.getClass());

    assertThat(args[0]).isEqualTo(String[].class);
    assertThat(args[1]).isEqualTo(String.class);
  }
}

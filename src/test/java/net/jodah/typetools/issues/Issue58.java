package net.jodah.typetools.issues;

import net.jodah.typetools.TypeResolver;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * https://github.com/jhalterman/typetools/issues/58
 */
class Issue58 {

  interface Callback<T> {
    void handle(List<T> result);
  }

  @Test
  void test() {
    Callback<String> fn = result -> {
      /* do something */
    };
    Class<?>[] args = TypeResolver.resolveRawArguments(Callback.class, fn.getClass());
    assertThrows( //
        AssertionError.class, //
        () -> assertThat(args).containsExactly(String.class), //
        "Not currently supported. See https://github.com/jhalterman/typetools/issues/58#issuecomment-963987880");
  }

}

package net.jodah.typetools.issues;

import net.jodah.typetools.TypeResolver;
import org.testng.annotations.Test;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * https://github.com/jhalterman/typetools/issues/27
 */
@Test
public class Issue27 {
  public void test() throws Throwable {
    Function<String[], String> fn = (String[] strings) -> strings.toString();

    Class<?>[] args = TypeResolver.resolveRawArguments(Function.class, fn.getClass());

    assertThat(args[0]).isEqualTo(String[].class);
    assertThat(args[1]).isEqualTo(String.class);
  }
}

package net.jodah.typetools.issues;

import net.jodah.typetools.TypeResolver;
import org.testng.annotations.Test;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * https://github.com/jhalterman/typetools/issues/11
 */
@Test
public class Issue11 {
  public void test() {
    TypeResolver.disableCache();

    Function<String, Integer> strToInt = s -> Integer.valueOf(s);
    Class<?>[] typeArgs = TypeResolver.resolveRawArguments(Function.class, strToInt.getClass());
    assertThat(typeArgs).containsExactly(String.class, Integer.class);
  }
}

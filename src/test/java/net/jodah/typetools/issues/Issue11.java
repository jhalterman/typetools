package net.jodah.typetools.issues;

import net.jodah.typetools.TypeResolver;
import net.jodah.typetools.TypeResolverCacheTest;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * https://github.com/jhalterman/typetools/issues/11
 */
@TypeResolverCacheTest
class Issue11 {

  @Test
  void test() {
    Function<String, Integer> strToInt = s -> Integer.valueOf(s);
    Class<?>[] typeArgs = TypeResolver.resolveRawArguments(Function.class, strToInt.getClass());
    assertThat(typeArgs).containsExactly(String.class, Integer.class);
  }
}

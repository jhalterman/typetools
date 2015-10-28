package net.jodah.typetools.issues;

import static org.testng.Assert.assertEquals;

import java.util.function.Function;

import org.testng.annotations.Test;

import net.jodah.typetools.TypeResolver;

/**
 * https://github.com/jhalterman/typetools/issues/11
 */
@Test
public class Issue11 {
  public void test() {
    TypeResolver.disableCache();

    Function<String, Integer> strToInt = s -> Integer.valueOf(s);
    Class<?>[] typeArgs = TypeResolver.resolveRawArguments(Function.class, strToInt.getClass());
    assertEquals(typeArgs, new Class<?>[] { String.class, Integer.class });
  }
}

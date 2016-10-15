package net.jodah.typetools.issues;

import static org.testng.Assert.assertEquals;

import java.util.function.Function;

import org.testng.annotations.Test;

import net.jodah.typetools.TypeResolver;

/**
 * https://github.com/jhalterman/typetools/issues/27
 */
@Test
public class Issue27 {
  public void test() throws Throwable {
    Function<String[], String> fn = (String[] strings) -> strings.toString();

    Class<?>[] args = TypeResolver.resolveRawArguments(Function.class, fn.getClass());

    assertEquals(args[0], String[].class);
    assertEquals(args[1], String.class);
  }
}

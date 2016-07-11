package net.jodah.typetools.issues;

import java.util.function.Function;

import org.testng.annotations.Test;

import net.jodah.typetools.TypeResolver;

/**
 * https://github.com/jhalterman/typetools/issues/27
 */
@Test
public class Issue27 {
  public void test() {
    Function<String[], String> fn = (String[] strings) -> strings.toString();
    TypeResolver.resolveRawArguments(Function.class, fn.getClass());
  }
}

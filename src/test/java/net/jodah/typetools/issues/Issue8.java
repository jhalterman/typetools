package net.jodah.typetools.issues;

import net.jodah.typetools.TypeResolver;
import org.testng.annotations.Test;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * https://github.com/jhalterman/typetools/issues/8
 */
@Test
public class Issue8 {
  interface Foo<T1, T2> {
  }

  class Bar implements Foo<List<Integer>, List<String>> {
  }

  public void test() {
    Type typeArgs = TypeResolver.reify(Foo.class, Bar.class);
    assertEquals(
        typeArgs.toString(),
        "net.jodah.typetools.issues.Issue8$Foo<java.util.List<java.lang.Integer>, java.util.List<java.lang.String>>");
    assertTrue(typeArgs instanceof ParameterizedType);
    ParameterizedType par = (ParameterizedType) typeArgs;
    assertEquals(par.getRawType(), Foo.class);
    assertEquals(par.getActualTypeArguments().length, 2);
    assertTrue(par.getActualTypeArguments()[0] instanceof ParameterizedType);
    ParameterizedType firstArg = (ParameterizedType) par.getActualTypeArguments()[0];
    assertEquals(firstArg.getRawType(), List.class);
    assertEquals(firstArg.getActualTypeArguments()[0], Integer.class);
  }
}

package net.jodah.typetools.issues;

import net.jodah.typetools.TypeResolver;
import org.junit.jupiter.api.Test;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * https://github.com/jhalterman/typetools/issues/8
 */
class Issue8 {

  interface Foo<T1, T2> {
  }

  class Bar implements Foo<List<Integer>, List<String>> {
  }

  @Test
  void test() {
    Type typeArgs = TypeResolver.reify(Foo.class, Bar.class);
    assertThat(typeArgs.toString()).isEqualTo(
        "net.jodah.typetools.issues.Issue8$Foo<java.util.List<java.lang.Integer>, java.util.List<java.lang.String>>");
    assertThat(typeArgs).isInstanceOf(ParameterizedType.class);
    ParameterizedType par = (ParameterizedType) typeArgs;
    assertThat(par.getRawType()).isEqualTo(Foo.class);
    assertThat(par.getActualTypeArguments().length).isEqualTo(2);
    assertThat(par.getActualTypeArguments()[0]).isInstanceOf(ParameterizedType.class);
    ParameterizedType firstArg = (ParameterizedType) par.getActualTypeArguments()[0];
    assertThat(firstArg.getRawType()).isEqualTo(List.class);
    assertThat(firstArg.getActualTypeArguments()[0]).isEqualTo(Integer.class);
  }
}

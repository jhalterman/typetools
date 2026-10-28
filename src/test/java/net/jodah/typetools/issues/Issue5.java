package net.jodah.typetools.issues;

import net.jodah.typetools.TypeResolver;
import org.testng.annotations.Test;

import java.util.function.BiConsumer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * https://github.com/jhalterman/typetools/issues/5
 */
@Test
public class Issue5 {
  interface Service {
  }

  static class ServiceImpl implements Service {
  }

  static class Client {
    void bind(Service impl) {
    }
  }

  public void test() {
    BiConsumer<Client, Service> bc = Client::bind;
    assertThat(TypeResolver.resolveRawArguments(BiConsumer.class, bc.getClass())) //
        .containsExactly(Client.class, Service.class);
  }
}

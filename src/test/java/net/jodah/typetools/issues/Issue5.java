package net.jodah.typetools.issues;

import static org.testng.Assert.assertEquals;

import java.util.function.BiConsumer;

import net.jodah.typetools.TypeResolver;

import org.testng.annotations.Test;

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
    assertEquals(TypeResolver.resolveRawArguments(BiConsumer.class, bc.getClass()), new Class<?>[] { Client.class,
        Service.class });
  }
}

package net.jodah.typetools;

import org.junit.jupiter.api.ClassTemplate;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ClassTemplateInvocationContext;
import org.junit.jupiter.api.extension.ClassTemplateInvocationContextProvider;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.parallel.ResourceAccessMode;
import org.junit.jupiter.api.parallel.ResourceLock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Runs all tests twice. Once with {@link TypeResolver#disableCache()} and once
 * with {@link TypeResolver#enableCache()}.
 */
@ClassTemplate
@ExtendWith(TypeResolverCacheTest.TemplateInvocationContextProvider.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ResourceLock(mode = ResourceAccessMode.READ_WRITE, value = "net.jodah.typetools.TypeResolver.cache")
public @interface TypeResolverCacheTest {

  final class TemplateInvocationContextProvider implements ClassTemplateInvocationContextProvider {

    @Override
    public boolean supportsClassTemplate(ExtensionContext context) {
      return true;
    }

    @Override
    public Stream<ClassTemplateInvocationContext> provideClassTemplateInvocationContexts(ExtensionContext context) {
      return Stream.of(invocationContext(true), invocationContext(false));
    }

    private ClassTemplateInvocationContext invocationContext(boolean enableCache) {
      return new ClassTemplateInvocationContext() {

        private final BeforeEachCallback beforeEachCallback = context -> {
          if (enableCache) {
            TypeResolver.enableCache();
          } else {
            TypeResolver.disableCache();
          }
        };

        private final AfterEachCallback afterEachCallback = context -> TypeResolver.enableCache();

        @Override
        public String getDisplayName(int invocationIndex) {
          return enableCache ? "with cache enabled" : "with cache disabled";
        }

        @Override
        public List<Extension> getAdditionalExtensions() {
          return Arrays.asList(beforeEachCallback, afterEachCallback);
        }
      };
    }
  }
}
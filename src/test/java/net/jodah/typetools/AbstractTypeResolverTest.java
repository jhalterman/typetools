package net.jodah.typetools;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@Test
public abstract class AbstractTypeResolverTest {
  boolean cacheEnabled;

  protected AbstractTypeResolverTest(boolean cacheEnabled) {
    this.cacheEnabled = cacheEnabled;
  }

  @DataProvider
  public static Object[][] cacheDataProvider() {
    return new Object[][] { { true }, { false } };
  }

  @BeforeMethod
  protected void setCache() {
    if (cacheEnabled)
      TypeResolver.enableCache();
    else
      TypeResolver.disableCache();
  }
}

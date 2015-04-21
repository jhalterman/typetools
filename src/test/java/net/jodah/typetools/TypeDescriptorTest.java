package net.jodah.typetools;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

@Test
public class TypeDescriptorTest {
  public void shouldGetType() {
    assertEquals(TypeDescriptor.getInternalType("V").getType(), Void.class);
    assertEquals(TypeDescriptor.getInternalType("B").getType(), Byte.class);
    assertEquals(TypeDescriptor.getInternalType("[B").getType(), Byte[].class);
    assertEquals(TypeDescriptor.getInternalType("[[Ljava.lang.String;").getType(), String[][].class);
    assertEquals(TypeDescriptor.getInternalType("Ljava.lang.String;").getType(), String.class);
  }
}

package net.jodah.typetools;

import org.testng.Assert;
import org.testng.annotations.Test;

public class AssertTest {

    @Test
    void shouldRunTestsWithAssertEnabled(){
        // Sanity check.
        try {
            assert false;
        } catch (AssertionError expected) {
            return;
        }
        Assert.fail("tests should be run with assert enabled");
    }
}

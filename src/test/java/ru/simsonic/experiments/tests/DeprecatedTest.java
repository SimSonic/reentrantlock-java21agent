package ru.simsonic.experiments.tests;

import jdk.jfr.Description;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.simsonic.experiments.BaseSynchronizedToReentrantLockClassFileTransformerTest;

class DeprecatedTest extends BaseSynchronizedToReentrantLockClassFileTransformerTest {

    @Description("Old tests, need to rethink")
    @Test
    void testTransformedClass() {
        try {
            myClassModifiedInstance.oldTestingMethod();
            Assertions.fail();
        } catch (IllegalCallerException expectedException) {
            String message = expectedException.getMessage();
            Assertions.assertEquals(message, "Value of X = 0");
        } catch (Exception ex) {
            Assertions.fail();
        }
    }
}

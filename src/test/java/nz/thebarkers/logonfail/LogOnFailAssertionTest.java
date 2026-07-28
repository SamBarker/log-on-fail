package nz.thebarkers.logonfail;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class LogOnFailAssertionTest {

    @Test
    void extensionCanBeInjectedAsParameter(LogOnFailExtension ext) {
        // Given

        // When

        // Then
        assertNotNull(ext);
    }
}

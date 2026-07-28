package nz.thebarkers.logonfail;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import static org.junit.jupiter.api.Assertions.*;

class LogOnFailAssertionTest {

    private static final Logger LOG = LoggerFactory.getLogger(LogOnFailAssertionTest.class);

    @Test
    void extensionCanBeInjectedAsParameter(LogOnFailExtension ext) {
        // Given

        // When

        // Then
        assertNotNull(ext);
    }

    @Test
    void assertLoggedMatchesEventByLoggerAndLevel(LogOnFailExtension ext) {
        // Given
        LOG.warn("expected message");

        // When / Then
        assertDoesNotThrow(() -> ext.assertLogged(LogOnFailAssertionTest.class, Level.WARN,
                msg -> assertTrue(msg.contains("expected message"))));
    }

    @Test
    void assertLoggedFormatsMessageArguments(LogOnFailExtension ext) {
        // Given
        LOG.warn("hello {}", "world");

        // When / Then
        assertDoesNotThrow(() -> ext.assertLogged(LogOnFailAssertionTest.class, Level.WARN,
                msg -> assertEquals("hello world", msg)));
    }

    @Test
    void assertLoggedIgnoresEventsFromOtherLoggers(LogOnFailExtension ext) {
        // Given
        LOG.warn("from this class");

        // When / Then
        assertThrows(AssertionError.class, () -> ext.assertLogged(String.class, Level.WARN,
                msg -> {}));
    }

    @Test
    void assertLoggedIgnoresEventsAtWrongLevel(LogOnFailExtension ext) {
        // Given
        LOG.info("info message");

        // When / Then
        assertThrows(AssertionError.class, () -> ext.assertLogged(LogOnFailAssertionTest.class, Level.WARN,
                msg -> {}));
    }

    @Test
    void assertLoggedFailsWhenNoEventsCaptured(LogOnFailExtension ext) {
        // Given

        // When / Then
        assertThrows(AssertionError.class, () -> ext.assertLogged(LogOnFailAssertionTest.class, Level.WARN,
                msg -> {}));
    }

    @Test
    void assertLoggedWithoutLevelMatchesAnyLevel(LogOnFailExtension ext) {
        // Given
        LOG.debug("debug message");

        // When / Then
        assertDoesNotThrow(() -> ext.assertLogged(LogOnFailAssertionTest.class,
                msg -> assertTrue(msg.contains("debug message"))));
    }

    @Test
    void assertNotLoggedPassesWhenNothingLogged(LogOnFailExtension ext) {
        // Given

        // When / Then
        assertDoesNotThrow(() -> ext.assertNotLogged(LogOnFailAssertionTest.class, Level.WARN));
    }

    @Test
    void assertNotLoggedPassesForDifferentLogger(LogOnFailExtension ext) {
        // Given
        LOG.warn("from this class");

        // When / Then
        assertDoesNotThrow(() -> ext.assertNotLogged(String.class, Level.WARN));
    }

    @Test
    void assertNotLoggedPassesForDifferentLevel(LogOnFailExtension ext) {
        // Given
        LOG.info("info message");

        // When / Then
        assertDoesNotThrow(() -> ext.assertNotLogged(LogOnFailAssertionTest.class, Level.WARN));
    }

    @Test
    void assertNotLoggedFailsWhenMatchingEventExists(LogOnFailExtension ext) {
        // Given
        LOG.warn("unexpected warning");

        // When / Then
        assertThrows(AssertionError.class, () -> ext.assertNotLogged(LogOnFailAssertionTest.class, Level.WARN));
    }

    @Test
    void assertLoggedFailureMessageListsCapturedEvents(LogOnFailExtension ext) {
        // Given
        LOG.info("something else was logged");

        // When
        AssertionError err = assertThrows(AssertionError.class, () ->
                ext.assertLogged(LogOnFailAssertionTest.class, Level.WARN, msg -> {}));

        // Then
        assertTrue(err.getMessage().contains("something else was logged"),
                "failure message should list captured events, got: " + err.getMessage());
    }
}

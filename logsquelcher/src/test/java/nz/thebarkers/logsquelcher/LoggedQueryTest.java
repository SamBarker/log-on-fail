package nz.thebarkers.logsquelcher;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.slf4j.event.LoggingEvent;

import static org.junit.jupiter.api.Assertions.*;

class LoggedQueryTest {

    private static final Logger LOG = LoggerFactory.getLogger(LoggedQueryTest.class);

    @Test
    void loggedReturnsFirstMatchingEventByLoggerAndLevel(LogSquelcherExtension ext) {
        // Given
        LOG.warn("expected warning");

        // When
        LoggingEvent event = ext.logged(LoggedQueryTest.class, Level.WARN);

        // Then
        assertEquals("expected warning", event.getMessage());
        assertEquals(Level.WARN, event.getLevel());
    }

    @Test
    void loggedThrowsWhenNoEventsMatchLevel(LogSquelcherExtension ext) {
        // Given
        LOG.info("info only");

        // When / Then
        assertThrows(AssertionError.class, () -> ext.logged(LoggedQueryTest.class, Level.WARN));
    }

    @Test
    void loggedThrowsWhenNoEventsMatchLogger(LogSquelcherExtension ext) {
        // Given
        LOG.warn("from this logger");

        // When / Then
        assertThrows(AssertionError.class, () -> ext.logged(String.class, Level.WARN));
    }

    @Test
    void loggedThrowsWhenNothingCaptured(LogSquelcherExtension ext) {
        // Given

        // When / Then
        AssertionError err = assertThrows(AssertionError.class,
                () -> ext.logged(LoggedQueryTest.class, Level.WARN));
        assertTrue(err.getMessage().contains("(none)"),
                "failure message should say (none) when nothing captured, got: " + err.getMessage());
    }

    @Test
    void loggedFailureMessageListsCapturedEvents(LogSquelcherExtension ext) {
        // Given
        LOG.info("something else was logged");

        // When
        AssertionError err = assertThrows(AssertionError.class,
                () -> ext.logged(LoggedQueryTest.class, Level.WARN));

        // Then
        assertTrue(err.getMessage().contains("something else was logged"),
                "failure message should list captured events, got: " + err.getMessage());
    }

    @Test
    void loggedWithoutLevelReturnsFirstMatchingEventAtAnyLevel(LogSquelcherExtension ext) {
        // Given
        LOG.debug("debug message");

        // When
        LoggingEvent event = ext.logged(LoggedQueryTest.class);

        // Then
        assertEquals("debug message", event.getMessage());
    }

    @Test
    void loggedWithoutLevelThrowsWhenNothingCaptured(LogSquelcherExtension ext) {
        // Given

        // When / Then
        assertThrows(AssertionError.class, () -> ext.logged(LoggedQueryTest.class));
    }

    @Test
    void loggedReturnsFirstWhenMultipleEventsMatch(LogSquelcherExtension ext) {
        // Given
        LOG.warn("first warning");
        LOG.warn("second warning");

        // When
        LoggingEvent event = ext.logged(LoggedQueryTest.class, Level.WARN);

        // Then
        assertEquals("first warning", event.getMessage());
    }
}

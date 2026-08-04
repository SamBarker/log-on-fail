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
    void loggedReturnsFirstMatchingEventByLoggerAndLevel(CapturedLogs logs) {
        // Given
        LOG.warn("expected warning");

        // When
        LoggingEvent event = logs.logged(LoggedQueryTest.class, Level.WARN);

        // Then
        assertEquals("expected warning", event.getMessage());
        assertEquals(Level.WARN, event.getLevel());
    }

    @Test
    void loggedThrowsWhenNoEventsMatchLevel(CapturedLogs logs) {
        // Given
        LOG.info("info only");

        // When / Then
        assertThrows(AssertionError.class, () -> logs.logged(LoggedQueryTest.class, Level.WARN));
    }

    @Test
    void loggedThrowsWhenNoEventsMatchLogger(CapturedLogs logs) {
        // Given
        LOG.warn("from this logger");

        // When / Then
        assertThrows(AssertionError.class, () -> logs.logged(String.class, Level.WARN));
    }

    @Test
    void loggedThrowsWhenNothingCaptured(CapturedLogs logs) {
        // Given

        // When / Then
        AssertionError err = assertThrows(AssertionError.class,
                () -> logs.logged(LoggedQueryTest.class, Level.WARN));
        assertTrue(err.getMessage().contains("(none)"),
                "failure message should say (none) when nothing captured, got: " + err.getMessage());
    }

    @Test
    void loggedFailureMessageListsCapturedEvents(CapturedLogs logs) {
        // Given
        LOG.info("something else was logged");

        // When
        AssertionError err = assertThrows(AssertionError.class,
                () -> logs.logged(LoggedQueryTest.class, Level.WARN));

        // Then
        assertTrue(err.getMessage().contains("something else was logged"),
                "failure message should list captured events, got: " + err.getMessage());
    }

    @Test
    void loggedWithoutLevelReturnsFirstMatchingEventAtAnyLevel(CapturedLogs logs) {
        // Given
        LOG.debug("debug message");

        // When
        LoggingEvent event = logs.logged(LoggedQueryTest.class);

        // Then
        assertEquals("debug message", event.getMessage());
    }

    @Test
    void loggedWithoutLevelThrowsWhenNothingCaptured(CapturedLogs logs) {
        // Given

        // When / Then
        assertThrows(AssertionError.class, () -> logs.logged(LoggedQueryTest.class));
    }

    @Test
    void loggedReturnsFirstWhenMultipleEventsMatch(CapturedLogs logs) {
        // Given
        LOG.warn("first warning");
        LOG.warn("second warning");

        // When
        LoggingEvent event = logs.logged(LoggedQueryTest.class, Level.WARN);

        // Then
        assertEquals("first warning", event.getMessage());
    }
}

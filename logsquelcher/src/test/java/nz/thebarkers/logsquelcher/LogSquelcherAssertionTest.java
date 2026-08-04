package nz.thebarkers.logsquelcher;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import static nz.thebarkers.logsquelcher.LoggingEventAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class LogSquelcherAssertionTest {

    private static final Logger LOG = LoggerFactory.getLogger(LogSquelcherAssertionTest.class);

    @Test
    void capturedLogsCanBeInjectedAsParameter(CapturedLogs logs) {
        // Given

        // When

        // Then
        assertNotNull(logs);
    }

    @Test
    void loggedReturnsEventMatchingLoggerAndLevel(CapturedLogs logs) {
        // Given
        LOG.warn("expected message");

        // When / Then
        assertDoesNotThrow(() ->
                assertThat(logs.logged(LogSquelcherAssertionTest.class, Level.WARN))
                        .hasFormattedMessage("expected message"));
    }

    @Test
    void loggedFormatsMessageArguments(CapturedLogs logs) {
        // Given
        LOG.warn("hello {}", "world");

        // When / Then
        assertThat(logs.logged(LogSquelcherAssertionTest.class, Level.WARN))
                .hasFormattedMessage("hello world");
    }

    @Test
    void loggedThrowsWhenNoEventsMatchLogger(CapturedLogs logs) {
        // Given
        LOG.warn("from this class");

        // When / Then
        assertThrows(AssertionError.class, () -> logs.logged(String.class, Level.WARN));
    }

    @Test
    void loggedThrowsWhenNoEventsMatchLevel(CapturedLogs logs) {
        // Given
        LOG.info("info message");

        // When / Then
        assertThrows(AssertionError.class, () -> logs.logged(LogSquelcherAssertionTest.class, Level.WARN));
    }

    @Test
    void loggedThrowsWhenNothingCaptured(CapturedLogs logs) {
        // Given

        // When / Then
        assertThrows(AssertionError.class, () -> logs.logged(LogSquelcherAssertionTest.class, Level.WARN));
    }

    @Test
    void loggedWithoutLevelMatchesAnyLevel(CapturedLogs logs) {
        // Given
        LOG.debug("debug message");

        // When / Then
        assertThat(logs.logged(LogSquelcherAssertionTest.class))
                .hasFormattedMessage("debug message");
    }

    @Test
    void assertNotLoggedPassesWhenNothingLogged(CapturedLogs logs) {
        // Given

        // When / Then
        assertDoesNotThrow(() -> logs.assertNotLogged(LogSquelcherAssertionTest.class, Level.WARN));
    }

    @Test
    void assertNotLoggedPassesForDifferentLogger(CapturedLogs logs) {
        // Given
        LOG.warn("from this class");

        // When / Then
        assertDoesNotThrow(() -> logs.assertNotLogged(String.class, Level.WARN));
    }

    @Test
    void assertNotLoggedPassesForDifferentLevel(CapturedLogs logs) {
        // Given
        LOG.info("info message");

        // When / Then
        assertDoesNotThrow(() -> logs.assertNotLogged(LogSquelcherAssertionTest.class, Level.WARN));
    }

    @Test
    void assertNotLoggedFailsWhenMatchingEventExists(CapturedLogs logs) {
        // Given
        LOG.warn("unexpected warning");

        // When / Then
        assertThrows(AssertionError.class, () -> logs.assertNotLogged(LogSquelcherAssertionTest.class, Level.WARN));
    }

    @Test
    void loggedFailureMessageListsCapturedEvents(CapturedLogs logs) {
        // Given
        LOG.info("something else was logged");

        // When
        AssertionError err = assertThrows(AssertionError.class,
                () -> logs.logged(LogSquelcherAssertionTest.class, Level.WARN));

        // Then
        assertTrue(err.getMessage().contains("something else was logged"),
                "failure message should list captured events, got: " + err.getMessage());
    }

    @Test
    void loggedExposeKeyValuePairsFromFluentApi(CapturedLogs logs) {
        // Given
        LOG.atWarn().addKeyValue("filterName", "myFilterDef").log("Plugin is deprecated");

        // When / Then
        assertThat(logs.logged(LogSquelcherAssertionTest.class, Level.WARN))
                .hasFormattedMessage("Plugin is deprecated")
                .containsKeyValue("filterName", "myFilterDef");
    }
}

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
    void extensionCanBeInjectedAsParameter(LogSquelcherExtension ext) {
        // Given

        // When

        // Then
        assertNotNull(ext);
    }

    @Test
    void loggedReturnsEventMatchingLoggerAndLevel(LogSquelcherExtension ext) {
        // Given
        LOG.warn("expected message");

        // When / Then
        assertDoesNotThrow(() ->
                assertThat(ext.logged(LogSquelcherAssertionTest.class, Level.WARN))
                        .hasFormattedMessage("expected message"));
    }

    @Test
    void loggedFormatsMessageArguments(LogSquelcherExtension ext) {
        // Given
        LOG.warn("hello {}", "world");

        // When / Then
        assertThat(ext.logged(LogSquelcherAssertionTest.class, Level.WARN))
                .hasFormattedMessage("hello world");
    }

    @Test
    void loggedThrowsWhenNoEventsMatchLogger(LogSquelcherExtension ext) {
        // Given
        LOG.warn("from this class");

        // When / Then
        assertThrows(AssertionError.class, () -> ext.logged(String.class, Level.WARN));
    }

    @Test
    void loggedThrowsWhenNoEventsMatchLevel(LogSquelcherExtension ext) {
        // Given
        LOG.info("info message");

        // When / Then
        assertThrows(AssertionError.class, () -> ext.logged(LogSquelcherAssertionTest.class, Level.WARN));
    }

    @Test
    void loggedThrowsWhenNothingCaptured(LogSquelcherExtension ext) {
        // Given

        // When / Then
        assertThrows(AssertionError.class, () -> ext.logged(LogSquelcherAssertionTest.class, Level.WARN));
    }

    @Test
    void loggedWithoutLevelMatchesAnyLevel(LogSquelcherExtension ext) {
        // Given
        LOG.debug("debug message");

        // When / Then
        assertThat(ext.logged(LogSquelcherAssertionTest.class))
                .hasFormattedMessage("debug message");
    }

    @Test
    void assertNotLoggedPassesWhenNothingLogged(LogSquelcherExtension ext) {
        // Given

        // When / Then
        assertDoesNotThrow(() -> ext.assertNotLogged(LogSquelcherAssertionTest.class, Level.WARN));
    }

    @Test
    void assertNotLoggedPassesForDifferentLogger(LogSquelcherExtension ext) {
        // Given
        LOG.warn("from this class");

        // When / Then
        assertDoesNotThrow(() -> ext.assertNotLogged(String.class, Level.WARN));
    }

    @Test
    void assertNotLoggedPassesForDifferentLevel(LogSquelcherExtension ext) {
        // Given
        LOG.info("info message");

        // When / Then
        assertDoesNotThrow(() -> ext.assertNotLogged(LogSquelcherAssertionTest.class, Level.WARN));
    }

    @Test
    void assertNotLoggedFailsWhenMatchingEventExists(LogSquelcherExtension ext) {
        // Given
        LOG.warn("unexpected warning");

        // When / Then
        assertThrows(AssertionError.class, () -> ext.assertNotLogged(LogSquelcherAssertionTest.class, Level.WARN));
    }

    @Test
    void loggedFailureMessageListsCapturedEvents(LogSquelcherExtension ext) {
        // Given
        LOG.info("something else was logged");

        // When
        AssertionError err = assertThrows(AssertionError.class,
                () -> ext.logged(LogSquelcherAssertionTest.class, Level.WARN));

        // Then
        assertTrue(err.getMessage().contains("something else was logged"),
                "failure message should list captured events, got: " + err.getMessage());
    }

    @Test
    void loggedExposeKeyValuePairsFromFluentApi(LogSquelcherExtension ext) {
        // Given
        LOG.atWarn().addKeyValue("filterName", "myFilterDef").log("Plugin is deprecated");

        // When / Then
        assertThat(ext.logged(LogSquelcherAssertionTest.class, Level.WARN))
                .hasFormattedMessage("Plugin is deprecated")
                .containsKeyValue("filterName", "myFilterDef");
    }
}

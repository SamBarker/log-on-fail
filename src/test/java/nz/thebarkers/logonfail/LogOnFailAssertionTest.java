package nz.thebarkers.logonfail;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import static nz.thebarkers.logonfail.LoggingEventAssert.assertThat;
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
    void loggedReturnsEventMatchingLoggerAndLevel(LogOnFailExtension ext) {
        // Given
        LOG.warn("expected message");

        // When / Then
        assertDoesNotThrow(() ->
                assertThat(ext.logged(LogOnFailAssertionTest.class, Level.WARN))
                        .hasFormattedMessage("expected message"));
    }

    @Test
    void loggedFormatsMessageArguments(LogOnFailExtension ext) {
        // Given
        LOG.warn("hello {}", "world");

        // When / Then
        assertThat(ext.logged(LogOnFailAssertionTest.class, Level.WARN))
                .hasFormattedMessage("hello world");
    }

    @Test
    void loggedThrowsWhenNoEventsMatchLogger(LogOnFailExtension ext) {
        // Given
        LOG.warn("from this class");

        // When / Then
        assertThrows(AssertionError.class, () -> ext.logged(String.class, Level.WARN));
    }

    @Test
    void loggedThrowsWhenNoEventsMatchLevel(LogOnFailExtension ext) {
        // Given
        LOG.info("info message");

        // When / Then
        assertThrows(AssertionError.class, () -> ext.logged(LogOnFailAssertionTest.class, Level.WARN));
    }

    @Test
    void loggedThrowsWhenNothingCaptured(LogOnFailExtension ext) {
        // Given

        // When / Then
        assertThrows(AssertionError.class, () -> ext.logged(LogOnFailAssertionTest.class, Level.WARN));
    }

    @Test
    void loggedWithoutLevelMatchesAnyLevel(LogOnFailExtension ext) {
        // Given
        LOG.debug("debug message");

        // When / Then
        assertThat(ext.logged(LogOnFailAssertionTest.class))
                .hasFormattedMessage("debug message");
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
    void loggedFailureMessageListsCapturedEvents(LogOnFailExtension ext) {
        // Given
        LOG.info("something else was logged");

        // When
        AssertionError err = assertThrows(AssertionError.class,
                () -> ext.logged(LogOnFailAssertionTest.class, Level.WARN));

        // Then
        assertTrue(err.getMessage().contains("something else was logged"),
                "failure message should list captured events, got: " + err.getMessage());
    }

    @Test
    void loggedExposeKeyValuePairsFromFluentApi(LogOnFailExtension ext) {
        // Given
        LOG.atWarn().addKeyValue("filterName", "myFilterDef").log("Plugin is deprecated");

        // When / Then
        assertThat(ext.logged(LogOnFailAssertionTest.class, Level.WARN))
                .hasFormattedMessage("Plugin is deprecated")
                .containsKeyValue("filterName", "myFilterDef");
    }
}

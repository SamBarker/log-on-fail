package nz.thebarkers.logonfail;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import static nz.thebarkers.logonfail.LoggingEventAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class LoggingEventAssertTest {

    private static final Logger LOG = LoggerFactory.getLogger(LoggingEventAssertTest.class);

    @Test
    void hasFormattedMessagePassesWhenMessageMatches(LogOnFailExtension ext) {
        // Given
        LOG.warn("plugin {} is deprecated", "myPlugin");

        // When / Then
        assertDoesNotThrow(() ->
                assertThat(ext.logged(LoggingEventAssertTest.class, Level.WARN))
                        .hasFormattedMessage("plugin myPlugin is deprecated"));
    }

    @Test
    void hasFormattedMessageFailsWhenMessageDoesNotMatch(LogOnFailExtension ext) {
        // Given
        LOG.warn("actual message");

        // When / Then
        assertThrows(AssertionError.class, () ->
                assertThat(ext.logged(LoggingEventAssertTest.class, Level.WARN))
                        .hasFormattedMessage("different message"));
    }

    @Test
    void containsKeyValuePassesWhenKvPairPresent(LogOnFailExtension ext) {
        // Given
        LOG.atWarn().addKeyValue("filterName", "myFilterDef").log("Plugin is deprecated");

        // When / Then
        assertDoesNotThrow(() ->
                assertThat(ext.logged(LoggingEventAssertTest.class, Level.WARN))
                        .containsKeyValue("filterName", "myFilterDef"));
    }

    @Test
    void containsKeyValueFailsWhenKvPairAbsent(LogOnFailExtension ext) {
        // Given
        LOG.atWarn().addKeyValue("filterName", "myFilterDef").log("Plugin is deprecated");

        // When / Then
        assertThrows(AssertionError.class, () ->
                assertThat(ext.logged(LoggingEventAssertTest.class, Level.WARN))
                        .containsKeyValue("filterName", "wrongValue"));
    }

    @Test
    void containsKeyValueFailsWhenNoKvPairsPresent(LogOnFailExtension ext) {
        // Given
        LOG.warn("plain message");

        // When / Then
        assertThrows(AssertionError.class, () ->
                assertThat(ext.logged(LoggingEventAssertTest.class, Level.WARN))
                        .containsKeyValue("filterName", "anything"));
    }

    @Test
    void assertionsAreChainable(LogOnFailExtension ext) {
        // Given
        LOG.atWarn().addKeyValue("k", "v").log("hello {}","world");

        // When / Then
        assertDoesNotThrow(() ->
                assertThat(ext.logged(LoggingEventAssertTest.class, Level.WARN))
                        .hasFormattedMessage("hello world")
                        .containsKeyValue("k", "v"));
    }
}

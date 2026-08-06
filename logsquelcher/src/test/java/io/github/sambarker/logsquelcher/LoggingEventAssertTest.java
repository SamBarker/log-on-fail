package io.github.sambarker.logsquelcher;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.util.List;

import static io.github.sambarker.logsquelcher.LoggingEventAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class LoggingEventAssertTest {

    private static final Logger LOG = LoggerFactory.getLogger(LoggingEventAssertTest.class);

    @Test
    void hasFormattedMessagePassesWhenMessageMatches(CapturedLogs logs) {
        LOG.warn("plugin {} is deprecated", "myPlugin");

        assertDoesNotThrow(() ->
                assertThat(logs.logged(LoggingEventAssertTest.class, Level.WARN).get(0))
                        .hasFormattedMessage("plugin myPlugin is deprecated"));
    }

    @Test
    void hasFormattedMessageFailsWhenMessageDoesNotMatch(CapturedLogs logs) {
        LOG.warn("actual message");

        assertThrows(AssertionError.class, () ->
                assertThat(logs.logged(LoggingEventAssertTest.class, Level.WARN).get(0))
                        .hasFormattedMessage("different message"));
    }

    @Test
    void containsKeyValuePassesWhenKvPairPresent(CapturedLogs logs) {
        LOG.atWarn().addKeyValue("filterName", "myFilterDef").log("Plugin is deprecated");

        assertDoesNotThrow(() ->
                assertThat(logs.logged(LoggingEventAssertTest.class, Level.WARN).get(0))
                        .containsKeyValue("filterName", "myFilterDef"));
    }

    @Test
    void containsKeyValueFailsWhenKvPairAbsent(CapturedLogs logs) {
        LOG.atWarn().addKeyValue("filterName", "myFilterDef").log("Plugin is deprecated");

        assertThrows(AssertionError.class, () ->
                assertThat(logs.logged(LoggingEventAssertTest.class, Level.WARN).get(0))
                        .containsKeyValue("filterName", "wrongValue"));
    }

    @Test
    void containsKeyValueFailsWhenNoKvPairsPresent(CapturedLogs logs) {
        LOG.warn("plain message");

        assertThrows(AssertionError.class, () ->
                assertThat(logs.logged(LoggingEventAssertTest.class, Level.WARN).get(0))
                        .containsKeyValue("filterName", "anything"));
    }

    @Test
    void assertionsAreChainable(CapturedLogs logs) {
        LOG.atWarn().addKeyValue("k", "v").log("hello {}", "world");

        assertDoesNotThrow(() ->
                assertThat(logs.logged(LoggingEventAssertTest.class, Level.WARN).get(0))
                        .hasFormattedMessage("hello world")
                        .containsKeyValue("k", "v"));
    }

    @Test
    void assertThatListSupportsCollectionAssertions(CapturedLogs logs) {
        LOG.warn("first");
        LOG.warn("second");

        assertThat(logs.logged(LoggingEventAssertTest.class, Level.WARN))
                .isNotEmpty()
                .hasSize(2);
    }

    @Test
    void assertThatListAllSatisfyReceivesLoggingEvents(CapturedLogs logs) {
        LOG.warn("msg one");
        LOG.warn("msg two");

        assertThat(logs.logged(LoggingEventAssertTest.class, Level.WARN))
                .isNotEmpty()
                .allSatisfy(event -> assertEquals(Level.WARN, event.getLevel()));
    }

    @Test
    void assertThatEmptyListPassesIsEmpty(CapturedLogs logs) {
        assertThat(logs.logged(LoggingEventAssertTest.class, Level.WARN))
                .isEmpty();
    }
}

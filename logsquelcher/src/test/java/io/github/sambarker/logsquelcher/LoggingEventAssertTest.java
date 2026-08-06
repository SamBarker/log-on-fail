package io.github.sambarker.logsquelcher;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.util.Map;

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
    void assertThatListDoesNotAssertOnConstruction(CapturedLogs logs) {
        assertDoesNotThrow(() -> assertThat(logs.logged(LoggingEventAssertTest.class, Level.WARN)));
    }

    @Test
    void assertThatListIsEmptyPassesWhenNothingLogged(CapturedLogs logs) {
        assertThat(logs.logged(LoggingEventAssertTest.class, Level.WARN)).isEmpty();
    }

    @Test
    void assertThatListIsNotEmptyPassesWhenEventsPresent(CapturedLogs logs) {
        LOG.warn("something");

        assertThat(logs.logged(LoggingEventAssertTest.class, Level.WARN)).isNotEmpty();
    }

    @Test
    void assertThatListAllSatisfyReceivesLoggingEvents(CapturedLogs logs) {
        LOG.warn("first");
        LOG.warn("second");

        assertThat(logs.logged(LoggingEventAssertTest.class, Level.WARN))
                .hasSize(2)
                .allSatisfy(event -> assertEquals(Level.WARN, event.getLevel()));
    }

    @Test
    void hasKeyValuesPassesWhenAllEntriesPresent(CapturedLogs logs) {
        LOG.atWarn().addKeyValue("a", "1").addKeyValue("b", "2").log("msg");

        assertDoesNotThrow(() ->
                assertThat(logs.logged(LoggingEventAssertTest.class, Level.WARN).get(0))
                        .hasKeyValues(Map.of("a", "1", "b", "2")));
    }

    @Test
    void hasKeyValuesPassesWhenMapIsSubsetOfPairs(CapturedLogs logs) {
        LOG.atWarn().addKeyValue("a", "1").addKeyValue("b", "2").log("msg");

        assertDoesNotThrow(() ->
                assertThat(logs.logged(LoggingEventAssertTest.class, Level.WARN).get(0))
                        .hasKeyValues(Map.of("a", "1")));
    }

    @Test
    void hasKeyValuesFailsWhenAnyEntryMissing(CapturedLogs logs) {
        LOG.atWarn().addKeyValue("a", "1").log("msg");

        assertThrows(AssertionError.class, () ->
                assertThat(logs.logged(LoggingEventAssertTest.class, Level.WARN).get(0))
                        .hasKeyValues(Map.of("a", "1", "b", "2")));
    }

    @Test
    void assertThatListSingleElementReturnsLoggingEventAssert(CapturedLogs logs) {
        LOG.warn("only event");

        assertThat(logs.logged(LoggingEventAssertTest.class, Level.WARN))
                .singleElement()
                .hasFormattedMessage("only event");
    }
}

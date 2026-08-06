package io.github.sambarker.logsquelcher;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.slf4j.event.LoggingEvent;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LoggedQueryTest {

    private static final Logger LOG = LoggerFactory.getLogger(LoggedQueryTest.class);
    private static final Logger OTHER_LOG = LoggerFactory.getLogger("other.logger");

    @Test
    void loggedReturnsSingleMatchingEventByLoggerAndLevel(CapturedLogs logs) {
        LOG.warn("expected warning");

        List<LoggingEvent> events = logs.logged(LoggedQueryTest.class, Level.WARN);

        assertThat(events).hasSize(1);
        assertThat(events.get(0).getMessage()).isEqualTo("expected warning");
        assertThat(events.get(0).getLevel()).isEqualTo(Level.WARN);
    }

    @Test
    void loggedReturnsAllMatchingEventsByLoggerAndLevel(CapturedLogs logs) {
        LOG.warn("first warning");
        LOG.warn("second warning");

        List<LoggingEvent> events = logs.logged(LoggedQueryTest.class, Level.WARN);

        assertThat(events).hasSize(2);
        assertThat(events.get(0).getMessage()).isEqualTo("first warning");
        assertThat(events.get(1).getMessage()).isEqualTo("second warning");
    }

    @Test
    void loggedReturnsEmptyListWhenNoEventsMatchLevel(CapturedLogs logs) {
        LOG.info("info only");

        List<LoggingEvent> events = logs.logged(LoggedQueryTest.class, Level.WARN);

        assertThat(events).isEmpty();
    }

    @Test
    void loggedReturnsEmptyListWhenNoEventsMatchLogger(CapturedLogs logs) {
        LOG.warn("from this logger");

        List<LoggingEvent> events = logs.logged(String.class, Level.WARN);

        assertThat(events).isEmpty();
    }

    @Test
    void loggedReturnsEmptyListWhenNothingCaptured(CapturedLogs logs) {
        List<LoggingEvent> events = logs.logged(LoggedQueryTest.class, Level.WARN);

        assertThat(events).isEmpty();
    }

    @Test
    void loggedWithoutLevelReturnsAllEventsFromLoggerAtAnyLevel(CapturedLogs logs) {
        LOG.debug("debug message");
        LOG.info("info message");
        LOG.warn("warn message");

        List<LoggingEvent> events = logs.logged(LoggedQueryTest.class);

        assertThat(events).hasSize(3);
    }

    @Test
    void loggedWithoutLevelReturnsEmptyListWhenNothingCaptured(CapturedLogs logs) {
        List<LoggingEvent> events = logs.logged(LoggedQueryTest.class);

        assertThat(events).isEmpty();
    }

    @Test
    void loggedWithoutLevelFiltersToSpecifiedLogger(CapturedLogs logs) {
        LOG.warn("from this logger");
        OTHER_LOG.warn("from other logger");

        List<LoggingEvent> events = logs.logged(LoggedQueryTest.class);

        assertThat(events).hasSize(1);
        assertThat(events.get(0).getMessage()).isEqualTo("from this logger");
    }

    @Test
    void loggedWithNoArgsReturnsAllCapturedEventsFromAllLoggers(CapturedLogs logs) {
        LOG.warn("from this logger");
        OTHER_LOG.info("from other logger");

        List<LoggingEvent> events = logs.logged();

        assertThat(events).hasSize(2);
    }

    @Test
    void loggedWithNoArgsReturnsEmptyListWhenNothingCaptured(CapturedLogs logs) {
        List<LoggingEvent> events = logs.logged();

        assertThat(events).isEmpty();
    }
}

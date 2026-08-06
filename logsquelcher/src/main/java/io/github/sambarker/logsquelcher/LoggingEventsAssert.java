package io.github.sambarker.logsquelcher;

import org.assertj.core.api.AbstractIterableAssert;
import org.slf4j.event.LoggingEvent;

import java.util.List;
import java.util.stream.StreamSupport;

public class LoggingEventsAssert
        extends AbstractIterableAssert<LoggingEventsAssert, List<LoggingEvent>, LoggingEvent, LoggingEventAssert> {

    LoggingEventsAssert(List<LoggingEvent> actual) {
        super(actual, LoggingEventsAssert.class);
    }

    @Override
    protected LoggingEventAssert toAssert(LoggingEvent value, String description) {
        return LoggingEventAssert.assertThat(value).as(description);
    }

    @Override
    protected LoggingEventsAssert newAbstractIterableAssert(Iterable<? extends LoggingEvent> iterable) {
        List<LoggingEvent> list = StreamSupport.stream(iterable.spliterator(), false)
                .map(LoggingEvent.class::cast)
                .toList();
        return new LoggingEventsAssert(list);
    }
}

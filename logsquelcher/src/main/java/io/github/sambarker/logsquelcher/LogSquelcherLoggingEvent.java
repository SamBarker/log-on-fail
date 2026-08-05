package io.github.sambarker.logsquelcher;

import org.slf4j.Marker;
import org.slf4j.event.KeyValuePair;
import org.slf4j.event.Level;
import org.slf4j.event.LoggingEvent;

import java.util.Arrays;
import java.util.List;

record LogSquelcherLoggingEvent(Level level, String loggerName, String message,
                             Object[] arguments, Throwable throwable, long timeStamp,
                             String threadName, List<KeyValuePair> keyValuePairs) implements LoggingEvent {

    @Override public Level getLevel() { return level; }
    @Override public String getLoggerName() { return loggerName; }
    @Override public String getMessage() { return message; }
    @Override public Object[] getArgumentArray() { return arguments; }
    @Override public List<Object> getArguments() { return arguments != null ? Arrays.asList(arguments) : null; }
    @Override public Throwable getThrowable() { return throwable; }
    @Override public long getTimeStamp() { return timeStamp; }
    @Override public String getThreadName() { return threadName; }
    @Override public List<Marker> getMarkers() { return null; }
    @Override public List<KeyValuePair> getKeyValuePairs() { return keyValuePairs; }
}

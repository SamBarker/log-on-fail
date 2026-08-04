package nz.thebarkers.logsquelcher.fixture;

import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.event.LoggingEvent;
import org.slf4j.helpers.AbstractLogger;
import org.slf4j.spi.LoggingEventAware;

import java.util.ArrayList;
import java.util.List;

public class SpyLogger extends AbstractLogger implements LoggingEventAware {

    public final List<LoggingEvent> received = new ArrayList<>();

    public SpyLogger() {
        this.name = "spy";
    }

    @Override
    public void log(LoggingEvent event) {
        received.add(event);
    }

    @Override
    protected void handleNormalizedLoggingCall(Level level, Marker marker,
            String messagePattern, Object[] arguments, Throwable throwable) {
        // non-fluent path — not expected in tests but required by AbstractLogger
    }

    @Override
    public String getFullyQualifiedCallerName() {
        return null;
    }

    @Override public boolean isTraceEnabled() { return true; }
    @Override public boolean isTraceEnabled(Marker marker) { return true; }
    @Override public boolean isDebugEnabled() { return true; }
    @Override public boolean isDebugEnabled(Marker marker) { return true; }
    @Override public boolean isInfoEnabled() { return true; }
    @Override public boolean isInfoEnabled(Marker marker) { return true; }
    @Override public boolean isWarnEnabled() { return true; }
    @Override public boolean isWarnEnabled(Marker marker) { return true; }
    @Override public boolean isErrorEnabled() { return true; }
    @Override public boolean isErrorEnabled(Marker marker) { return true; }
}

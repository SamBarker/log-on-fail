package nz.thebarkers.logsquelcher;

import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.event.LoggingEvent;
import org.slf4j.helpers.AbstractLogger;
import org.slf4j.spi.LoggingEventAware;

import java.util.Optional;

class CapturingLogger extends AbstractLogger implements LoggingEventAware {

    private final Optional<Logger> delegate;

    CapturingLogger(String name, Logger realLogger) {
        this.name = name;
        this.delegate = Optional.ofNullable(realLogger);
    }

    @Override
    public void log(LoggingEvent event) {
        var captured = new LogSquelcherLoggingEvent(
                event.getLevel(), name, event.getMessage(), event.getArgumentArray(),
                event.getThrowable(), event.getTimeStamp() > 0 ? event.getTimeStamp() : System.currentTimeMillis(),
                event.getThreadName() != null ? event.getThreadName() : Thread.currentThread().getName(),
                event.getKeyValuePairs());
        EventBuffer.capture(System.nanoTime(), captured);
        if (LogSquelcherConfig.REALTIME_LOGGING) {
            delegate.ifPresent(d -> CapturingLoggerFactory.forward(d, captured));
        }
    }

    @Override
    protected void handleNormalizedLoggingCall(Level level, Marker marker,
            String messagePattern, Object[] arguments, Throwable throwable) {
        var captured = new LogSquelcherLoggingEvent(
                level, name, messagePattern, arguments, throwable,
                System.currentTimeMillis(), Thread.currentThread().getName(), null);
        EventBuffer.capture(System.nanoTime(), captured);
        if (LogSquelcherConfig.REALTIME_LOGGING) {
            delegate.ifPresent(d -> CapturingLoggerFactory.forward(d, captured));
        }
    }

    @Override
    public String getFullyQualifiedCallerName() {
        return CapturingLogger.class.getName();
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

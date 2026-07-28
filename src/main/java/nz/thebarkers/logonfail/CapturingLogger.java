package nz.thebarkers.logonfail;

import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.helpers.AbstractLogger;
import org.slf4j.helpers.MessageFormatter;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;

class CapturingLogger extends AbstractLogger {

    CapturingLogger(String name) {
        this.name = name;
    }

    @Override
    protected void handleNormalizedLoggingCall(Level level, Marker marker,
            String messagePattern, Object[] arguments, Throwable throwable) {
        String msg = MessageFormatter.arrayFormat(messagePattern, arguments, throwable).getMessage();
        String line = String.format("%s %-5s [%s] %s - %s",
                Instant.now(),
                level.name(),
                Thread.currentThread().getName(),
                name,
                msg);
        if (throwable != null) {
            StringWriter sw = new StringWriter();
            throwable.printStackTrace(new PrintWriter(sw));
            line = line + '\n' + sw;
        }
        EventBuffer.capture(System.nanoTime(), line);
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

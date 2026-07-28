package nz.thebarkers.logonfail;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.spi.FilterReply;
import org.slf4j.Marker;

class CapturingTurboFilter extends TurboFilter {

    @Override
    public FilterReply decide(Marker marker, Logger logger, Level level, String format, Object[] params, Throwable t) {
        org.slf4j.event.Level slf4jLevel;
        try {
            slf4jLevel = org.slf4j.event.Level.valueOf(level.toString());
        } catch (IllegalArgumentException e) {
            slf4jLevel = org.slf4j.event.Level.TRACE;
        }
        EventBuffer.capture(System.nanoTime(), new LogOnFailLoggingEvent(
                slf4jLevel, logger.getName(), format, params, t,
                System.currentTimeMillis(), Thread.currentThread().getName(), null));
        return FilterReply.DENY;
    }
}

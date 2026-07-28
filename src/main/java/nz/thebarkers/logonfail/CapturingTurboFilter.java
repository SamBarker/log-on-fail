package nz.thebarkers.logonfail;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.spi.FilterReply;
import org.slf4j.Marker;
import org.slf4j.helpers.MessageFormatter;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;

class CapturingTurboFilter extends TurboFilter {

    @Override
    public FilterReply decide(Marker marker, Logger logger, Level level, String format, Object[] params, Throwable t) {
        String msg = format != null ? MessageFormatter.arrayFormat(format, params, t).getMessage() : "";
        String line = String.format("%s %-5s [%s] %s - %s",
                Instant.now(), level.toString(), Thread.currentThread().getName(), logger.getName(), msg);
        if (t != null) {
            StringWriter sw = new StringWriter();
            t.printStackTrace(new PrintWriter(sw));
            line = line + '\n' + sw;
        }
        EventBuffer.capture(System.nanoTime(), line);
        return FilterReply.DENY;
    }
}

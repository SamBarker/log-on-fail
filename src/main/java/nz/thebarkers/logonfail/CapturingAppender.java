package nz.thebarkers.logonfail;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Property;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.LongSupplier;

public class CapturingAppender extends AbstractAppender {

    static final long DEFAULT_TTL_NANOS = 300_000_000_000L; // 300 s

    private static final ConcurrentLinkedDeque<CapturedEvent> EVENTS = new ConcurrentLinkedDeque<>();
    private static volatile CapturingAppender INSTANCE;
    private static final Object LOCK = new Object();

    private final long ttlNanos;
    private final LongSupplier clock;

    CapturingAppender(long ttlNanos, LongSupplier clock) {
        super("CapturingAppender", null, null, true, Property.EMPTY_ARRAY);
        this.ttlNanos = ttlNanos;
        this.clock = clock;
    }

    public static void ensureRegistered() {
        if (INSTANCE != null) {
            return;
        }
        synchronized (LOCK) {
            if (INSTANCE != null) {
                return;
            }
            LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
            Configuration config = ctx.getConfiguration();
            CapturingAppender appender = new CapturingAppender(DEFAULT_TTL_NANOS, System::nanoTime);
            appender.start();
            config.addAppender(appender);
            config.getRootLogger().addAppender(appender, Level.TRACE, null);
            ctx.updateLoggers();
            INSTANCE = appender;
        }
    }

    @Override
    public void append(LogEvent event) {
        long now = clock.getAsLong();
        EVENTS.addLast(new CapturedEvent(now, formatLine(event)));
        trim(now);
    }

    private static String formatLine(LogEvent event) {
        String msg = event.getMessage().getFormattedMessage();
        String timestamp = Instant.ofEpochMilli(event.getTimeMillis()).toString();
        StringBuilder sb = new StringBuilder()
                .append(timestamp).append(' ')
                .append(String.format("%-5s", event.getLevel().name())).append(' ')
                .append('[').append(event.getThreadName()).append("] ")
                .append(event.getLoggerName()).append(" - ")
                .append(msg);
        if (event.getThrown() != null) {
            StringWriter sw = new StringWriter();
            event.getThrown().printStackTrace(new PrintWriter(sw));
            sb.append('\n').append(sw);
        }
        return sb.toString();
    }

    private void trim(long now) {
        long cutoff = now - ttlNanos;
        CapturedEvent head;
        while ((head = EVENTS.peekFirst()) != null && head.nanoTime() < cutoff) {
            EVENTS.pollFirst();
        }
    }

    public static List<CapturedEvent> extractWindow(long startNanos, long endNanos) {
        List<CapturedEvent> result = new ArrayList<>();
        for (CapturedEvent e : EVENTS) {
            if (e.nanoTime() >= startNanos && e.nanoTime() <= endNanos) {
                result.add(e);
            }
        }
        return result;
    }

    static void reset() {
        synchronized (LOCK) {
            EVENTS.clear();
            if (INSTANCE != null) {
                LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
                Configuration config = ctx.getConfiguration();
                config.getRootLogger().removeAppender(INSTANCE.getName());
                ctx.updateLoggers();
                INSTANCE.stop();
                INSTANCE = null;
            }
        }
    }
}

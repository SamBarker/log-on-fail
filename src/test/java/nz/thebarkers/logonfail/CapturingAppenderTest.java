package nz.thebarkers.logonfail;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.message.SimpleMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

class CapturingAppenderTest {

    @BeforeEach
    void reset() {
        CapturingAppender.reset();
    }

    private static Log4jLogEvent event(String message) {
        return Log4jLogEvent.newBuilder()
                .setLoggerName("test")
                .setLevel(Level.WARN)
                .setMessage(new SimpleMessage(message))
                .build();
    }

    @Test
    void appendStampsEventWithClockTime() {
        var clock = new AtomicLong(1_000L);
        var appender = new CapturingAppender(CapturingAppender.DEFAULT_TTL_NANOS, clock::get);

        appender.append(event("hello"));

        List<CapturedEvent> all = CapturingAppender.extractWindow(0, Long.MAX_VALUE);
        assertEquals(1, all.size());
        assertEquals(1_000L, all.get(0).nanoTime());
    }

    @Test
    void appendTrimsEventsOlderThanTtl() {
        var clock = new AtomicLong(0L);
        long ttl = 100L;
        var appender = new CapturingAppender(ttl, clock::get);

        appender.append(event("old"));   // t=0

        clock.set(200L);
        appender.append(event("new"));   // t=200, cutoff=100 → "old" trimmed

        List<CapturedEvent> all = CapturingAppender.extractWindow(0, Long.MAX_VALUE);
        assertEquals(1, all.size());
        assertEquals("new", all.get(0).event().getMessage().getFormattedMessage());
    }

    @Test
    void extractWindowReturnsOnlyEventsInRange() {
        var clock = new AtomicLong(100L);
        var appender = new CapturingAppender(CapturingAppender.DEFAULT_TTL_NANOS, clock::get);

        appender.append(event("before"));   // t=100
        clock.set(200L);
        appender.append(event("inside"));   // t=200
        clock.set(300L);
        appender.append(event("after"));    // t=300

        List<CapturedEvent> windowed = CapturingAppender.extractWindow(150L, 250L);
        assertEquals(1, windowed.size());
        assertEquals("inside", windowed.get(0).event().getMessage().getFormattedMessage());
    }

    @Test
    void extractWindowBoundariesAreInclusive() {
        var clock = new AtomicLong(100L);
        var appender = new CapturingAppender(CapturingAppender.DEFAULT_TTL_NANOS, clock::get);

        appender.append(event("start"));   // t=100
        clock.set(200L);
        appender.append(event("end"));     // t=200

        List<CapturedEvent> windowed = CapturingAppender.extractWindow(100L, 200L);
        assertEquals(2, windowed.size());
    }

    @Test
    void resetClearsBuffer() {
        var appender = new CapturingAppender(CapturingAppender.DEFAULT_TTL_NANOS, System::nanoTime);
        appender.append(event("something"));

        CapturingAppender.reset();

        assertEquals(0, CapturingAppender.extractWindow(0, Long.MAX_VALUE).size());
    }
}

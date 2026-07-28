package nz.thebarkers.logonfail;

import org.slf4j.event.LoggingEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

class EventBuffer {

    static final long DEFAULT_TTL_NANOS = 300_000_000_000L; // 300 s

    private static final ConcurrentLinkedDeque<CapturedEvent> EVENTS = new ConcurrentLinkedDeque<>();
    private static volatile long ttlNanos = DEFAULT_TTL_NANOS;

    private EventBuffer() {
    }

    static void setTtlNanos(long ttl) {
        ttlNanos = ttl;
    }

    static void capture(long nanoTime, LoggingEvent event) {
        EVENTS.addLast(new CapturedEvent(nanoTime, event));
        trim(nanoTime);
    }

    private static void trim(long now) {
        long cutoff = now - ttlNanos;
        CapturedEvent head;
        while ((head = EVENTS.peekFirst()) != null && head.nanoTime() < cutoff) {
            EVENTS.pollFirst();
        }
    }

    static List<CapturedEvent> extractWindow(long startNanos, long endNanos) {
        List<CapturedEvent> result = new ArrayList<>();
        for (CapturedEvent e : EVENTS) {
            if (e.nanoTime() >= startNanos && e.nanoTime() <= endNanos) {
                result.add(e);
            }
        }
        return result;
    }

    static void reset() {
        EVENTS.clear();
    }
}

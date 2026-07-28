package nz.thebarkers.logonfail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EventBufferTest {

    @BeforeEach
    void reset() {
        EventBuffer.reset();
        EventBuffer.setTtlNanos(EventBuffer.DEFAULT_TTL_NANOS);
    }

    @Test
    void captureStampsEventWithProvidedNanoTime() {
        EventBuffer.capture(1_000L, "hello");

        List<CapturedEvent> all = EventBuffer.extractWindow(0, Long.MAX_VALUE);
        assertEquals(1, all.size());
        assertEquals(1_000L, all.get(0).nanoTime());
    }

    @Test
    void captureTrimsEventsOlderThanTtl() {
        EventBuffer.setTtlNanos(100L);

        EventBuffer.capture(0L, "old");
        EventBuffer.capture(200L, "new");   // cutoff = 200-100 = 100 → "old" (t=0) trimmed

        List<CapturedEvent> all = EventBuffer.extractWindow(0, Long.MAX_VALUE);
        assertEquals(1, all.size());
        assertTrue(all.get(0).formattedLine().contains("new"));
    }

    @Test
    void extractWindowReturnsOnlyEventsInRange() {
        EventBuffer.capture(100L, "before");
        EventBuffer.capture(200L, "inside");
        EventBuffer.capture(300L, "after");

        List<CapturedEvent> windowed = EventBuffer.extractWindow(150L, 250L);
        assertEquals(1, windowed.size());
        assertTrue(windowed.get(0).formattedLine().contains("inside"));
    }

    @Test
    void extractWindowBoundariesAreInclusive() {
        EventBuffer.capture(100L, "start");
        EventBuffer.capture(200L, "end");

        List<CapturedEvent> windowed = EventBuffer.extractWindow(100L, 200L);
        assertEquals(2, windowed.size());
    }

    @Test
    void resetClearsBuffer() {
        EventBuffer.capture(1L, "something");
        EventBuffer.reset();

        assertEquals(0, EventBuffer.extractWindow(0, Long.MAX_VALUE).size());
    }
}

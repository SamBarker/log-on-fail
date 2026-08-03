package nz.thebarkers.logsquelcher;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;
import org.slf4j.event.LoggingEvent;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EventBufferTest {

    @BeforeEach
    void reset() {
        EventBuffer.reset();
        EventBuffer.setTtlNanos(EventBuffer.DEFAULT_TTL_NANOS);
    }

    private static LoggingEvent event(String message) {
        return new LogSquelcherLoggingEvent(Level.WARN, "test", message, null, null,
                System.currentTimeMillis(), "main", null);
    }

    @Test
    void captureStampsEventWithProvidedNanoTime() {
        // Given

        // When
        EventBuffer.capture(1_000L, event("hello"));

        // Then
        List<CapturedEvent> all = EventBuffer.extractWindow(0, Long.MAX_VALUE);
        assertEquals(1, all.size());
        assertEquals(1_000L, all.get(0).nanoTime());
    }

    @Test
    void captureTrimsEventsOlderThanTtl() {
        // Given
        EventBuffer.setTtlNanos(100L);
        EventBuffer.capture(0L, event("old"));

        // When
        EventBuffer.capture(200L, event("new"));   // cutoff = 200-100 = 100 → "old" (t=0) trimmed

        // Then
        List<CapturedEvent> all = EventBuffer.extractWindow(0, Long.MAX_VALUE);
        assertEquals(1, all.size());
        assertEquals("new", all.get(0).loggingEvent().getMessage());
    }

    @Test
    void extractWindowReturnsOnlyEventsInRange() {
        // Given
        EventBuffer.capture(100L, event("before"));
        EventBuffer.capture(200L, event("inside"));
        EventBuffer.capture(300L, event("after"));

        // When
        List<CapturedEvent> windowed = EventBuffer.extractWindow(150L, 250L);

        // Then
        assertEquals(1, windowed.size());
        assertEquals("inside", windowed.get(0).loggingEvent().getMessage());
    }

    @Test
    void extractWindowBoundariesAreInclusive() {
        // Given
        EventBuffer.capture(100L, event("start"));
        EventBuffer.capture(200L, event("end"));

        // When
        List<CapturedEvent> windowed = EventBuffer.extractWindow(100L, 200L);

        // Then
        assertEquals(2, windowed.size());
    }

    @Test
    void resetClearsBuffer() {
        // Given
        EventBuffer.capture(1L, event("something"));

        // When
        EventBuffer.reset();

        // Then
        assertEquals(0, EventBuffer.extractWindow(0, Long.MAX_VALUE).size());
    }
}

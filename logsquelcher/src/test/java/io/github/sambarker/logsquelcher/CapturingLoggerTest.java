package io.github.sambarker.logsquelcher;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.event.KeyValuePair;
import org.slf4j.event.Level;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CapturingLoggerTest {

    @BeforeEach
    void reset() {
        EventBuffer.reset();
    }

    @Test
    void warnCapturesMessagePatternAndArguments() {
        // Given
        var logger = new CapturingLogger("com.example.Foo", null);

        // When
        logger.warn("hello {}", "world");

        // Then
        List<CapturedEvent> all = EventBuffer.extractWindow(0, Long.MAX_VALUE);
        assertEquals(1, all.size());
        assertEquals("hello {}", all.get(0).loggingEvent().getMessage());
        assertArrayEquals(new Object[]{"world"}, all.get(0).loggingEvent().getArgumentArray());
    }

    @Test
    void loggerNameIsCaptured() {
        // Given
        var logger = new CapturingLogger("com.example.Foo", null);

        // When
        logger.info("msg");

        // Then
        assertEquals("com.example.Foo", EventBuffer.extractWindow(0, Long.MAX_VALUE).get(0).loggingEvent().getLoggerName());
    }

    @Test
    void levelIsCaptured() {
        // Given
        var logger = new CapturingLogger("com.example.Foo", null);

        // When
        logger.error("boom");

        // Then
        assertEquals(Level.ERROR, EventBuffer.extractWindow(0, Long.MAX_VALUE).get(0).loggingEvent().getLevel());
    }

    @Test
    void keyValuePairsAreCaptured() {
        // Given
        var logger = new CapturingLogger("com.example.Foo", null);

        // When
        logger.atWarn().addKeyValue("requestId", "abc123").log("something happened");

        // Then
        List<CapturedEvent> all = EventBuffer.extractWindow(0, Long.MAX_VALUE);
        assertEquals(1, all.size());
        List<KeyValuePair> kvps = all.get(0).loggingEvent().getKeyValuePairs();
        assertNotNull(kvps);
        assertEquals(1, kvps.size());
        assertEquals("requestId", kvps.get(0).key);
        assertEquals("abc123", kvps.get(0).value);
    }

    @Test
    void throwableIsCaptured() {
        // Given
        var logger = new CapturingLogger("com.example.Foo", null);
        var ex = new RuntimeException("bad things");

        // When
        logger.warn("oops", ex);

        // Then
        assertSame(ex, EventBuffer.extractWindow(0, Long.MAX_VALUE).get(0).loggingEvent().getThrowable());
    }
}

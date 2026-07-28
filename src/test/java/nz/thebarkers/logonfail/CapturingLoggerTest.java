package nz.thebarkers.logonfail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CapturingLoggerTest {

    @BeforeEach
    void reset() {
        EventBuffer.reset();
    }

    @Test
    void warnCapturesFormattedMessage() {
        // Given
        var logger = new CapturingLogger("test.Logger");

        // When
        logger.warn("hello {}", "world");

        // Then
        List<CapturedEvent> all = EventBuffer.extractWindow(0, Long.MAX_VALUE);
        assertEquals(1, all.size());
        assertTrue(all.get(0).formattedLine().contains("hello world"));
    }

    @Test
    void loggerNameAppearsInFormattedLine() {
        // Given
        var logger = new CapturingLogger("com.example.Foo");

        // When
        logger.info("msg");

        // Then
        String line = EventBuffer.extractWindow(0, Long.MAX_VALUE).get(0).formattedLine();
        assertTrue(line.contains("com.example.Foo"));
    }

    @Test
    void levelAppearsInFormattedLine() {
        // Given
        var logger = new CapturingLogger("log");

        // When
        logger.error("boom");

        // Then
        String line = EventBuffer.extractWindow(0, Long.MAX_VALUE).get(0).formattedLine();
        assertTrue(line.contains("ERROR"));
    }

    @Test
    void thrownStackTraceAppearsInFormattedLine() {
        // Given
        var logger = new CapturingLogger("log");

        // When
        logger.warn("oops", new RuntimeException("bad things"));

        // Then
        String line = EventBuffer.extractWindow(0, Long.MAX_VALUE).get(0).formattedLine();
        assertTrue(line.contains("bad things"));
        assertTrue(line.contains("RuntimeException"));
    }
}

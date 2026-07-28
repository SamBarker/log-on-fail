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
        new CapturingLogger("test.Logger").warn("hello {}", "world");

        List<CapturedEvent> all = EventBuffer.extractWindow(0, Long.MAX_VALUE);
        assertEquals(1, all.size());
        assertTrue(all.get(0).formattedLine().contains("hello world"));
    }

    @Test
    void loggerNameAppearsInFormattedLine() {
        new CapturingLogger("com.example.Foo").info("msg");

        String line = EventBuffer.extractWindow(0, Long.MAX_VALUE).get(0).formattedLine();
        assertTrue(line.contains("com.example.Foo"));
    }

    @Test
    void levelAppearsInFormattedLine() {
        new CapturingLogger("log").error("boom");

        String line = EventBuffer.extractWindow(0, Long.MAX_VALUE).get(0).formattedLine();
        assertTrue(line.contains("ERROR"));
    }

    @Test
    void thrownStackTraceAppearsInFormattedLine() {
        new CapturingLogger("log").warn("oops", new RuntimeException("bad things"));

        String line = EventBuffer.extractWindow(0, Long.MAX_VALUE).get(0).formattedLine();
        assertTrue(line.contains("bad things"));
        assertTrue(line.contains("RuntimeException"));
    }
}

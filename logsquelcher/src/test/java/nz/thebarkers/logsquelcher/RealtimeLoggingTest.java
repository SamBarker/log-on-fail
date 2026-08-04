package nz.thebarkers.logsquelcher;

import nz.thebarkers.logsquelcher.fixture.SpyLogger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RealtimeLoggingTest {

    @BeforeEach
    void setUp() {
        EventBuffer.reset();
        LogSquelcherConfig.REALTIME_LOGGING = true;
    }

    @AfterEach
    void tearDown() {
        LogSquelcherConfig.REALTIME_LOGGING = false;
    }

    @Test
    void delegateReceivesEventImmediatelyWhenRealtimeEnabled() {
        var spy = new SpyLogger();
        var logger = new CapturingLogger("com.example.Foo", spy);

        logger.warn("hello realtime");

        assertEquals(1, spy.received.size());
        assertEquals("hello realtime", spy.received.get(0).getMessage());
    }

    @Test
    void eventIsStillBufferedInRealtimeMode() {
        var spy = new SpyLogger();
        var logger = new CapturingLogger("com.example.Foo", spy);

        logger.warn("buffered too");

        List<CapturedEvent> buffered = EventBuffer.extractWindow(0, Long.MAX_VALUE);
        assertEquals(1, buffered.size());
    }

    @Test
    void delegateNotCalledAtLogTimeWhenRealtimeDisabled() {
        LogSquelcherConfig.REALTIME_LOGGING = false;
        var spy = new SpyLogger();
        var logger = new CapturingLogger("com.example.Foo", spy);

        logger.warn("no live forward");

        assertTrue(spy.received.isEmpty());
    }

    @Test
    void fluentApiAlsoForwardsLiveInRealtimeMode() {
        var spy = new SpyLogger();
        var logger = new CapturingLogger("com.example.Foo", spy);

        logger.atWarn().addKeyValue("requestId", "abc").log("fluent message");

        assertEquals(1, spy.received.size());
        assertEquals("fluent message", spy.received.get(0).getMessage());
    }
}

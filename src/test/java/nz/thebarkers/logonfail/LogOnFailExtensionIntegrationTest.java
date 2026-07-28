package nz.thebarkers.logonfail;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LogOnFailExtensionIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(LogOnFailExtensionIntegrationTest.class);

    @BeforeEach
    void setUp() {
        EventBuffer.reset();
    }

    @AfterEach
    void tearDown() {
        EventBuffer.reset();
    }

    @Test
    void slf4jEventsAreCapturedWithinWindow() {
        // Given
        long start = System.nanoTime();

        // When
        LOG.warn("captured warning");
        long end = System.nanoTime();

        // Then
        List<CapturedEvent> events = EventBuffer.extractWindow(start, end);
        assertTrue(events.stream().anyMatch(e -> "captured warning".equals(e.loggingEvent().getMessage())));
    }

    @Test
    void eventsLoggedBeforeWindowAreExcluded() {
        // Given
        LOG.warn("before the window");
        long start = System.nanoTime();
        long end = System.nanoTime();

        // When
        List<CapturedEvent> events = EventBuffer.extractWindow(start, end);

        // Then
        assertTrue(events.stream().noneMatch(e -> "before the window".equals(e.loggingEvent().getMessage())));
    }

    @Test
    void passingTestWindowContainsNoEvents() {
        // Given
        long start = System.nanoTime();
        long end = System.nanoTime();

        // When
        List<CapturedEvent> events = EventBuffer.extractWindow(start, end);

        // Then
        assertTrue(events.isEmpty());
    }
}

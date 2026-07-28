package nz.thebarkers.logonfail;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
        assertTrue(events.stream().anyMatch(e -> e.formattedLine().contains("captured warning")));
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
        assertTrue(events.stream().noneMatch(e -> e.formattedLine().contains("before the window")));
    }

    @Test
    void fileLogSinkWritesCapturedEventsToExpectedPath(@TempDir Path tmp) throws IOException {
        // Given
        long start = System.nanoTime();
        LOG.error("full end-to-end message");
        long end = System.nanoTime();
        List<CapturedEvent> events = EventBuffer.extractWindow(start, end);
        assertFalse(events.isEmpty(), "Expected at least one captured event");

        // When
        new FileLogSink(tmp).report("SmokeTest#fileOutput", events);

        // Then
        Path file = tmp.resolve("SmokeTest").resolve("fileOutput.log");
        assertTrue(Files.exists(file));
        assertTrue(Files.readString(file).contains("full end-to-end message"));
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

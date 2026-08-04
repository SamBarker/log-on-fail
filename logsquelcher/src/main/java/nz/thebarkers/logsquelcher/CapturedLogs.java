package nz.thebarkers.logsquelcher;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.event.Level;
import org.slf4j.event.LoggingEvent;
import org.slf4j.helpers.MessageFormatter;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Per-test view of the logs captured by logSquelcher.
 *
 * <p>Inject it as a test parameter to make assertions about log output:
 * <pre>{@code
 * @Test
 * void myTest(CapturedLogs logs) {
 *     myService.doSomething();
 *     assertThat(logs.logged(MyService.class, Level.WARN))
 *         .hasFormattedMessage("something went wrong");
 * }
 * }</pre>
 */
public class CapturedLogs implements ExtensionContext.Store.CloseableResource, AutoCloseable {

    private final long startNanos;

    CapturedLogs(long startNanos) {
        this.startNanos = startNanos;
    }

    long startNanos() {
        return startNanos;
    }

    /**
     * Asserts that no log events from {@code logger} at {@code level} were captured since this
     * test started.
     *
     * <p>The window covers all threads — not just the JUnit test thread.
     *
     * @param logger the logger class to check
     * @param level  the log level to check
     * @throws AssertionError if any matching event was captured, listing the offending messages
     */
    public void assertNotLogged(Class<?> logger, Level level) {
        List<CapturedEvent> window = EventBuffer.extractWindow(startNanos, System.nanoTime());
        List<String> matching = window.stream()
                .filter(e -> logger.getName().equals(e.loggingEvent().getLoggerName()))
                .filter(e -> level == e.loggingEvent().getLevel())
                .map(e -> format(e.loggingEvent()))
                .toList();
        if (!matching.isEmpty()) {
            throw new AssertionError("Expected no log events from [" + logger.getName() + "] at ["
                    + level + "] but found " + matching.size()
                    + " (events from all threads are included):\n  "
                    + String.join("\n  ", matching));
        }
    }

    /**
     * Returns the first log event from {@code logger} at {@code level} captured since this test
     * started, or throws {@link AssertionError} if none was captured.
     *
     * <p>The window covers all threads — not just the JUnit test thread.
     *
     * <p>Typical usage with AssertJ:
     * <pre>{@code
     * assertThat(logs.logged(MyService.class, Level.WARN))
     *     .hasFormattedMessage("Plugin is deprecated")
     *     .containsKeyValue("filterName", "myFilterDef");
     * }</pre>
     *
     * @param logger the logger class whose events to inspect
     * @param level  the required log level
     * @return the first matching event
     * @throws AssertionError if no matching event was captured
     */
    public LoggingEvent logged(Class<?> logger, Level level) {
        return loggedInternal(logger, level);
    }

    /**
     * Returns the first log event from {@code logger} at any level captured since this test
     * started, or throws {@link AssertionError} if none was captured.
     *
     * <p>Convenience overload of {@link #logged(Class, Level)} that matches any level.
     *
     * @param logger the logger class whose events to inspect
     * @return the first matching event
     * @throws AssertionError if no matching event was captured
     */
    public LoggingEvent logged(Class<?> logger) {
        return loggedInternal(logger, null);
    }

    @Override
    public void close() {
        // EventBuffer is global; nothing per-test to release here.
        // Subclasses or future APIs that register per-test resources should override this.
    }

    private LoggingEvent loggedInternal(Class<?> logger, Level level) {
        List<CapturedEvent> window = EventBuffer.extractWindow(startNanos, System.nanoTime());
        String levelClause = level != null ? " at [" + level + "]" : "";
        return window.stream()
                .map(CapturedEvent::loggingEvent)
                .filter(e -> logger.getName().equals(e.getLoggerName()))
                .filter(e -> level == null || level == e.getLevel())
                .findFirst()
                .orElseThrow(() -> {
                    String captured = window.stream()
                            .map(e -> format(e.loggingEvent()))
                            .collect(Collectors.joining("\n  ", "  ", ""));
                    return new AssertionError("No log events from [" + logger.getName() + "]" + levelClause
                            + " captured since this test started (events from all threads are included)."
                            + " All events captured this test:\n" + (window.isEmpty() ? "  (none)" : captured));
                });
    }

    private static String format(LoggingEvent event) {
        return MessageFormatter.arrayFormat(event.getMessage(), event.getArgumentArray(),
                event.getThrowable()).getMessage();
    }
}

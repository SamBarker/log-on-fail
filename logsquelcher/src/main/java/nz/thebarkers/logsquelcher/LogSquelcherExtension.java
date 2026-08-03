package nz.thebarkers.logsquelcher;

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestWatcher;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.slf4j.event.LoggingEvent;
import org.slf4j.helpers.MessageFormatter;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class LogSquelcherExtension implements BeforeEachCallback, TestWatcher, ParameterResolver {

    private static final ExtensionContext.Namespace NAMESPACE =
            ExtensionContext.Namespace.create(LogSquelcherExtension.class);
    private static final String SELF_KEY = "self";

    private final ThreadLocal<Long> startNanos = new ThreadLocal<>();

    @Override
    public void beforeEach(ExtensionContext context) {
        startNanos.set(System.nanoTime());
        store(context).put(SELF_KEY, this);
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        long start = startNanos.get();
        long end = System.nanoTime();
        startNanos.remove();
        if (!LogSquelcherConfig.REALTIME_LOGGING) {
            EventBuffer.extractWindow(start, end).forEach(e -> replay(e.loggingEvent()));
        }
    }

    @Override
    public void testSuccessful(ExtensionContext context) {
        startNanos.remove();
    }

    @Override
    public void testAborted(ExtensionContext context, Throwable cause) {
        startNanos.remove();
    }

    @Override
    public void testDisabled(ExtensionContext context, Optional<String> reason) {
        startNanos.remove();
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return parameterContext.getParameter().getType() == LogSquelcherExtension.class;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return store(extensionContext).get(SELF_KEY, LogSquelcherExtension.class);
    }

    /**
     * Asserts that no log events from {@code logger} at {@code level} were captured since this
     * test started.
     *
     * <p>The window covers all threads — not just the JUnit test thread.
     *
     * <p>Typical usage:
     * <pre>{@code
     * ext.assertNotLogged(MyService.class, Level.ERROR);
     * }</pre>
     *
     * @param logger the logger class to check
     * @param level  the log level to check
     * @throws AssertionError if any matching event was captured, listing the offending messages
     */
    public void assertNotLogged(Class<?> logger, Level level) {
        List<CapturedEvent> window = EventBuffer.extractWindow(startNanos.get(), System.nanoTime());
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
     * assertThat(ext.logged(MyService.class, Level.WARN))
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

    private LoggingEvent loggedInternal(Class<?> logger, Level level) {
        List<CapturedEvent> window = EventBuffer.extractWindow(startNanos.get(), System.nanoTime());
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

    private static void replay(LoggingEvent event) {
        if (LoggerFactory.getILoggerFactory() instanceof CapturingLoggerFactory factory) {
            factory.replay(event);
        }
    }

    private static ExtensionContext.Store store(ExtensionContext context) {
        return context.getStore(NAMESPACE);
    }
}

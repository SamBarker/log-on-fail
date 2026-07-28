package nz.thebarkers.logonfail;

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestWatcher;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.slf4j.event.LoggingEvent;
import org.slf4j.helpers.MessageFormatter;
import org.slf4j.spi.LoggingEventAware;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class LogOnFailExtension implements BeforeEachCallback, TestWatcher, ParameterResolver {

    private static final ExtensionContext.Namespace NAMESPACE =
            ExtensionContext.Namespace.create(LogOnFailExtension.class);
    private static final String SELF_KEY = "self";

    private static final boolean LOGBACK_PRESENT;

    static {
        boolean present;
        try {
            Class.forName("ch.qos.logback.classic.LoggerContext");
            present = true;
        } catch (ClassNotFoundException e) {
            present = false;
        }
        LOGBACK_PRESENT = present;
    }

    private final ThreadLocal<Long> startNanos = new ThreadLocal<>();

    @Override
    public void beforeEach(ExtensionContext context) {
        if (LOGBACK_PRESENT) {
            LogbackCapture.install();
        }
        startNanos.set(System.nanoTime());
        store(context).put(SELF_KEY, this);
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        long start = startNanos.get();
        long end = System.nanoTime();
        startNanos.remove();
        if (LOGBACK_PRESENT) {
            LogbackCapture.uninstall();
        }
        List<CapturedEvent> events = EventBuffer.extractWindow(start, end);
        events.forEach(e -> replay(e.loggingEvent()));
    }

    @Override
    public void testSuccessful(ExtensionContext context) {
        startNanos.remove();
        if (LOGBACK_PRESENT) {
            LogbackCapture.uninstall();
        }
    }

    @Override
    public void testAborted(ExtensionContext context, Throwable cause) {
        startNanos.remove();
        if (LOGBACK_PRESENT) {
            LogbackCapture.uninstall();
        }
    }

    @Override
    public void testDisabled(ExtensionContext context, Optional<String> reason) {
        startNanos.remove();
        if (LOGBACK_PRESENT) {
            LogbackCapture.uninstall();
        }
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return parameterContext.getParameter().getType() == LogOnFailExtension.class;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return store(extensionContext).get(SELF_KEY, LogOnFailExtension.class);
    }

    /**
     * Asserts that at least one log event from {@code logger} at {@code level} was captured since
     * this test started, and that the formatted message satisfies {@code assertion}.
     *
     * <p>The window covers all threads — not just the JUnit test thread. If the system under test
     * logs from a background thread, those events are included.
     *
     * <p>The {@code assertion} consumer receives the fully formatted message (SLF4J placeholders
     * resolved). Use any assertion library inside it; an {@link AssertionError} thrown by the
     * consumer is treated as a failed match. If no captured event satisfies the consumer, this
     * method throws an {@link AssertionError} listing all events that were captured.
     *
     * <p>Typical usage with AssertJ:
     * <pre>{@code
     * ext.assertLogged(MyService.class, Level.WARN,
     *     msg -> assertThat(msg).contains("plugin not found"));
     * }</pre>
     *
     * @param logger    the logger class whose events to inspect
     * @param level     the required log level
     * @param assertion a consumer that asserts on the formatted message; throws AssertionError on mismatch
     * @throws AssertionError if no matching event was captured, or none satisfied the assertion
     */
    public void assertLogged(Class<?> logger, Level level, Consumer<String> assertion) {
        assertLoggedInternal(logger, level, assertion);
    }

    /**
     * Asserts that at least one log event from {@code logger} at any level was captured since this
     * test started, and that the formatted message satisfies {@code assertion}.
     *
     * <p>Convenience overload of {@link #assertLogged(Class, Level, Consumer)} that matches any level.
     *
     * @param logger    the logger class whose events to inspect
     * @param assertion a consumer that asserts on the formatted message; throws AssertionError on mismatch
     * @throws AssertionError if no matching event was captured, or none satisfied the assertion
     */
    public void assertLogged(Class<?> logger, Consumer<String> assertion) {
        assertLoggedInternal(logger, null, assertion);
    }

    private void assertLoggedInternal(Class<?> logger, Level level, Consumer<String> assertion) {
        List<CapturedEvent> window = EventBuffer.extractWindow(startNanos.get(), System.nanoTime());
        List<CapturedEvent> matching = window.stream()
                .filter(e -> logger.getName().equals(e.loggingEvent().getLoggerName()))
                .filter(e -> level == null || level == e.loggingEvent().getLevel())
                .toList();
        if (matching.isEmpty()) {
            String levelClause = level != null ? " at [" + level + "]" : "";
            String captured = window.stream()
                    .map(e -> format(e.loggingEvent()))
                    .collect(Collectors.joining("\n  ", "  ", ""));
            throw new AssertionError("No log events from [" + logger.getName() + "]" + levelClause
                    + " captured since this test started (events from all threads are included)."
                    + " All events captured this test:\n" + (window.isEmpty() ? "  (none)" : captured));
        }
        AssertionError last = null;
        for (CapturedEvent event : matching) {
            try {
                assertion.accept(format(event.loggingEvent()));
                return;
            } catch (AssertionError e) {
                last = e;
            }
        }
        String levelClause = level != null ? " at [" + level + "]" : "";
        throw new AssertionError("None of the " + matching.size() + " log event(s) from ["
                + logger.getName() + "]" + levelClause + " satisfied the assertion.", last);
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

    private static String format(LoggingEvent event) {
        return MessageFormatter.arrayFormat(event.getMessage(), event.getArgumentArray(),
                event.getThrowable()).getMessage();
    }

    private static void replay(LoggingEvent event) {
        org.slf4j.Logger logger = LoggerFactory.getLogger(event.getLoggerName());
        if (logger instanceof LoggingEventAware lea) {
            lea.log(event);
        }
    }

    private static ExtensionContext.Store store(ExtensionContext context) {
        return context.getStore(NAMESPACE);
    }
}

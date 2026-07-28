package nz.thebarkers.logonfail;

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class LogOnFailExtension implements BeforeEachCallback, TestWatcher {

    private static final ExtensionContext.Namespace NAMESPACE =
            ExtensionContext.Namespace.create(LogOnFailExtension.class);
    private static final String START_KEY = "startNanos";

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

    private final LogSink sink;

    public LogOnFailExtension(LogSink sink) {
        this.sink = sink;
    }

    /** Used by {@code @ExtendWith(LogOnFailExtension.class)}. */
    public LogOnFailExtension() {
        this(auto().sink);
    }

    public static LogOnFailExtension stdout() {
        return new LogOnFailExtension(new StdoutLogSink());
    }

    public static LogOnFailExtension devNull() {
        return new LogOnFailExtension((testId, events) -> {

        });
    }

    public static LogOnFailExtension file(Path dir) {
        return new LogOnFailExtension(new FileLogSink(dir));
    }

    public static LogOnFailExtension auto() {
        boolean ci = System.getenv("CI") != null && !System.getenv("CI").isBlank();
        return ci
                ? file(Path.of("target/log-capture"))
                : file(Path.of("target/log-capture"));
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        if (LOGBACK_PRESENT) {
            LogbackCapture.install();
        }
        store(context).put(START_KEY, System.nanoTime());
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        long start = (long) store(context).get(START_KEY);
        long end = System.nanoTime();
        if (LOGBACK_PRESENT) {
            LogbackCapture.uninstall();
        }
        List<CapturedEvent> events = EventBuffer.extractWindow(start, end);
        sink.report(testId(context), events);
    }

    @Override
    public void testSuccessful(ExtensionContext context) {
        if (LOGBACK_PRESENT) {
            LogbackCapture.uninstall();
        }
    }

    @Override
    public void testAborted(ExtensionContext context, Throwable cause) {
        if (LOGBACK_PRESENT) {
            LogbackCapture.uninstall();
        }
    }

    @Override
    public void testDisabled(ExtensionContext context, Optional<String> reason) {
        if (LOGBACK_PRESENT) {
            LogbackCapture.uninstall();
        }
    }

    private static ExtensionContext.Store store(ExtensionContext context) {
        return context.getStore(NAMESPACE);
    }

    private static String testId(ExtensionContext context) {
        String cls = context.getTestClass().map(Class::getSimpleName).orElse("?");
        String method = context.getTestMethod().map(Method::getName).orElse("?");
        return cls + "#" + method;
    }
}

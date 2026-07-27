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

    public static LogOnFailExtension file(Path dir) {
        return new LogOnFailExtension(new FileLogSink(dir));
    }

    public static LogOnFailExtension auto() {
        boolean ci = System.getenv("CI") != null && !System.getenv("CI").isBlank();
        return ci
                ? file(Path.of("target/log-capture"))
                : stdout();
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        CapturingAppender.ensureRegistered();
        store(context).put(START_KEY, System.nanoTime());
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        long start = (long) store(context).get(START_KEY);
        long end = System.nanoTime();
        List<CapturedEvent> events = CapturingAppender.extractWindow(start, end);
        sink.report(testId(context), events);
    }

    @Override
    public void testSuccessful(ExtensionContext context) {}

    @Override
    public void testAborted(ExtensionContext context, Throwable cause) {}

    @Override
    public void testDisabled(ExtensionContext context, Optional<String> reason) {}

    private static ExtensionContext.Store store(ExtensionContext context) {
        return context.getStore(NAMESPACE);
    }

    private static String testId(ExtensionContext context) {
        String cls = context.getTestClass().map(Class::getSimpleName).orElse("?");
        String method = context.getTestMethod().map(Method::getName).orElse("?");
        return cls + "#" + method;
    }
}

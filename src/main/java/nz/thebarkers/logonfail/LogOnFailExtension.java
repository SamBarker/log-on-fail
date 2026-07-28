package nz.thebarkers.logonfail;

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestWatcher;
import org.slf4j.LoggerFactory;
import org.slf4j.event.LoggingEvent;
import org.slf4j.spi.LoggingEventAware;

import java.util.List;
import java.util.Optional;

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

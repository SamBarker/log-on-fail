package nz.thebarkers.logonfail;

import org.slf4j.LoggerFactory;
import org.slf4j.event.LoggingEvent;
import org.slf4j.spi.LoggingEventAware;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;

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
        events.forEach(e -> replay(e.loggingEvent()));
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

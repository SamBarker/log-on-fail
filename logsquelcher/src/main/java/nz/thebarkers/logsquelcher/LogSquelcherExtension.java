package nz.thebarkers.logsquelcher;

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestWatcher;
import org.slf4j.LoggerFactory;
import org.slf4j.event.LoggingEvent;

public class LogSquelcherExtension implements BeforeEachCallback, TestWatcher, ParameterResolver {

    private static final ExtensionContext.Namespace NAMESPACE =
            ExtensionContext.Namespace.create(LogSquelcherExtension.class);
    private static final String CAPTURED_LOGS_KEY = "capturedLogs";

    @Override
    public void beforeEach(ExtensionContext context) {
        store(context).put(CAPTURED_LOGS_KEY, new CapturedLogs(System.nanoTime()));
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        if (LogSquelcherConfig.REALTIME_LOGGING) {
            return;
        }
        CapturedLogs logs = store(context).get(CAPTURED_LOGS_KEY, CapturedLogs.class);
        if (logs != null) {
            EventBuffer.extractWindow(logs.startNanos(), System.nanoTime())
                    .forEach(e -> replay(e.loggingEvent()));
        }
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return parameterContext.getParameter().getType() == CapturedLogs.class;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return store(extensionContext).get(CAPTURED_LOGS_KEY, CapturedLogs.class);
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

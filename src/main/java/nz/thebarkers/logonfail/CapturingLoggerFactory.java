package nz.thebarkers.logonfail;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.event.LoggingEvent;
import org.slf4j.spi.LoggingEventAware;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

class CapturingLoggerFactory implements ILoggerFactory {

    private final ILoggerFactory delegate;
    private final ConcurrentMap<String, CapturingLogger> loggers = new ConcurrentHashMap<>();

    CapturingLoggerFactory(ILoggerFactory delegate) {
        this.delegate = delegate;
    }

    @Override
    public Logger getLogger(String name) {
        return loggers.computeIfAbsent(name, n -> {
            Logger real = delegate != null ? delegate.getLogger(n) : null;
            return new CapturingLogger(n, real);
        });
    }

    void replay(LoggingEvent event) {
        if (delegate == null) {
            return;
        }
        Logger real = delegate.getLogger(event.getLoggerName());
        if (real instanceof LoggingEventAware lea) {
            lea.log(event);
        }
    }
}

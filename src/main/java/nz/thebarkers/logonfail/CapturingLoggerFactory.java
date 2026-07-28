package nz.thebarkers.logonfail;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

class CapturingLoggerFactory implements ILoggerFactory {

    private final ConcurrentMap<String, CapturingLogger> loggers = new ConcurrentHashMap<>();

    @Override
    public Logger getLogger(String name) {
        return loggers.computeIfAbsent(name, CapturingLogger::new);
    }
}

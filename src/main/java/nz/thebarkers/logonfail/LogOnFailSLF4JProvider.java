package nz.thebarkers.logonfail;

import org.slf4j.ILoggerFactory;
import org.slf4j.IMarkerFactory;
import org.slf4j.helpers.BasicMarkerFactory;
import org.slf4j.helpers.NOPMDCAdapter;
import org.slf4j.spi.MDCAdapter;
import org.slf4j.spi.SLF4JServiceProvider;

import java.util.ServiceLoader;

public class LogOnFailSLF4JProvider implements SLF4JServiceProvider {

    private CapturingLoggerFactory loggerFactory;
    private final IMarkerFactory markerFactory = new BasicMarkerFactory();
    private final MDCAdapter mdcAdapter = new NOPMDCAdapter();

    @Override
    public ILoggerFactory getLoggerFactory() {
        return loggerFactory;
    }

    @Override
    public IMarkerFactory getMarkerFactory() {
        return markerFactory;
    }

    @Override
    public MDCAdapter getMDCAdapter() {
        return mdcAdapter;
    }

    @Override
    public String getRequestedApiVersion() {
        return "2.0.99";
    }

    @Override
    public void initialize() {
        ILoggerFactory delegate = findAndInitializeDelegate();
        loggerFactory = new CapturingLoggerFactory(delegate);
    }

    private ILoggerFactory findAndInitializeDelegate() {
        for (SLF4JServiceProvider provider : ServiceLoader.load(SLF4JServiceProvider.class)) {
            if (provider instanceof LogOnFailSLF4JProvider) {
                continue;
            }
            provider.initialize();
            return provider.getLoggerFactory();
        }
        return null;
    }
}

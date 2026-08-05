package io.github.sambarker.logsquelcher;

import org.slf4j.ILoggerFactory;
import org.slf4j.IMarkerFactory;
import org.slf4j.helpers.BasicMarkerFactory;
import org.slf4j.helpers.NOPMDCAdapter;
import org.slf4j.spi.MDCAdapter;
import org.slf4j.spi.SLF4JServiceProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class LogSquelcherSLF4JProvider implements SLF4JServiceProvider {

    private static final String SIMPLE_PROVIDER_CLASS = "org.slf4j.simple.SimpleServiceProvider";

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
        List<SLF4JServiceProvider> providers = new ArrayList<>();
        ServiceLoader.load(SLF4JServiceProvider.class).forEach(providers::add);
        loggerFactory = new CapturingLoggerFactory(selectDelegate(providers));
    }

    ILoggerFactory selectDelegate(Iterable<SLF4JServiceProvider> providers) {
        SLF4JServiceProvider fallback = null;
        for (SLF4JServiceProvider provider : providers) {
            if (provider instanceof LogSquelcherSLF4JProvider) {
                continue;
            }
            if (SIMPLE_PROVIDER_CLASS.equals(provider.getClass().getName())) {
                if (fallback == null) fallback = provider;
                continue;
            }
            provider.initialize();
            return provider.getLoggerFactory();
        }
        if (fallback != null) {
            fallback.initialize();
            return fallback.getLoggerFactory();
        }
        return null;
    }
}

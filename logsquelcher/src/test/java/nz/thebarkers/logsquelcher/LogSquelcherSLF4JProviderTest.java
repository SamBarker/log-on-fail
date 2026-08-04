package nz.thebarkers.logsquelcher;

import org.junit.jupiter.api.Test;
import org.slf4j.ILoggerFactory;
import org.slf4j.IMarkerFactory;
import org.slf4j.helpers.NOPLoggerFactory;
import org.slf4j.simple.SimpleServiceProvider;
import org.slf4j.spi.MDCAdapter;
import org.slf4j.spi.SLF4JServiceProvider;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LogSquelcherSLF4JProviderTest {

    @Test
    void selectDelegateReturnsRealBackendOverSimple() {
        var provider = new LogSquelcherSLF4JProvider();
        var simple = new SimpleServiceProvider();
        var real = new StubBackendProvider();

        ILoggerFactory result = provider.selectDelegate(List.of(provider, simple, real));

        assertSame(real.factory, result);
    }

    @Test
    void selectDelegateFallsBackToSimpleWhenNoRealBackendPresent() {
        var provider = new LogSquelcherSLF4JProvider();
        var simple = new SimpleServiceProvider();

        ILoggerFactory result = provider.selectDelegate(List.of(provider, simple));

        assertNotNull(result);
        assertSame(simple.getLoggerFactory(), result);
    }

    @Test
    void selectDelegateReturnsNullWhenOnlySelfPresent() {
        var provider = new LogSquelcherSLF4JProvider();

        ILoggerFactory result = provider.selectDelegate(List.of(provider));

        assertNull(result);
    }

    @Test
    void selectDelegateReturnsFirstRealBackendWhenMultiplePresent() {
        var provider = new LogSquelcherSLF4JProvider();
        var simple = new SimpleServiceProvider();
        var first = new StubBackendProvider();
        var second = new StubBackendProvider();

        ILoggerFactory result = provider.selectDelegate(List.of(provider, simple, first, second));

        assertSame(first.factory, result);
    }

    @Test
    void selectDelegateSkipsSelf() {
        var provider = new LogSquelcherSLF4JProvider();
        var real = new StubBackendProvider();

        ILoggerFactory result = provider.selectDelegate(List.of(provider, real));

        assertSame(real.factory, result);
    }

    // --- test doubles ---

    private static class StubBackendProvider implements SLF4JServiceProvider {
        final ILoggerFactory factory = new NOPLoggerFactory();

        @Override
        public ILoggerFactory getLoggerFactory() {
            return factory;
        }

        @Override
        public IMarkerFactory getMarkerFactory() {
            return null;
        }

        @Override
        public MDCAdapter getMDCAdapter() {
            return null;
        }

        @Override
        public String getRequestedApiVersion() {
            return "2.0.99";
        }

        @Override
        public void initialize() {
        }
    }
}

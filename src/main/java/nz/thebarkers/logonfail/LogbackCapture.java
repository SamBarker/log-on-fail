package nz.thebarkers.logonfail;

import ch.qos.logback.classic.LoggerContext;
import org.slf4j.LoggerFactory;

class LogbackCapture {

    private static volatile CapturingTurboFilter installed;
    private static final Object LOCK = new Object();

    static void install() {
        synchronized (LOCK) {
            if (installed != null) {
                return;
            }
            if (!(LoggerFactory.getILoggerFactory() instanceof LoggerContext ctx)) {
                return;
            }
            installed = new CapturingTurboFilter();
            installed.start();
            ctx.addTurboFilter(installed);
        }
    }

    static void uninstall() {
        synchronized (LOCK) {
            if (installed == null) {
                return;
            }
            if (!(LoggerFactory.getILoggerFactory() instanceof LoggerContext ctx)) {
                return;
            }
            ctx.getTurboFilterList().remove(installed);
            installed.stop();
            installed = null;
        }
    }
}

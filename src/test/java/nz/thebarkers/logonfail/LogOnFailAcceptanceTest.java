package nz.thebarkers.logonfail;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import nz.thebarkers.logonfail.fixture.LoggingFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;

class LogOnFailAcceptanceTest {

    private ListAppender<ILoggingEvent> appender;

    @BeforeEach
    void attachAppender() {
        appender = new ListAppender<>();
        logbackContext().ifPresent(ctx -> {
            appender.setContext(ctx);
            appender.start();
            ctx.getLogger(Logger.ROOT_LOGGER_NAME).addAppender(appender);
        });
    }

    @AfterEach
    void detachAppender() {
        logbackContext().ifPresent(ctx ->
                ctx.getLogger(Logger.ROOT_LOGGER_NAME).detachAppender(appender));
    }

    @Test
    void logsFromFailingTestAreReplayedToOutput() {
        EngineTestKit.engine("junit-jupiter")
                .selectors(selectMethod(LoggingFixture.class, "failingTestThatLogs"))
                .execute()
                .testEvents()
                .assertStatistics(stats -> stats.started(1).failed(1));

        assertThat(appender.list)
                .anyMatch(e -> e.getFormattedMessage().contains(LoggingFixture.REPLAYED_MESSAGE));
    }

    @Test
    void logsFromPassingTestAreSuppressed() {
        EngineTestKit.engine("junit-jupiter")
                .selectors(selectMethod(LoggingFixture.class, "passingTestThatLogs"))
                .execute()
                .testEvents()
                .assertStatistics(stats -> stats.started(1).succeeded(1));

        assertThat(appender.list)
                .noneMatch(e -> e.getFormattedMessage().contains(LoggingFixture.SUPPRESSED_MESSAGE));
    }

    private java.util.Optional<LoggerContext> logbackContext() {
        if (LoggerFactory.getILoggerFactory() instanceof CapturingLoggerFactory capturing
                && capturing.getDelegate() instanceof LoggerContext ctx) {
            return java.util.Optional.of(ctx);
        }
        return java.util.Optional.empty();
    }
}

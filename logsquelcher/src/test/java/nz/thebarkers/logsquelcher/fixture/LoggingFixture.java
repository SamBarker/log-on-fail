package nz.thebarkers.logsquelcher.fixture;

import nz.thebarkers.logsquelcher.LogSquelcherExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test fixture for acceptance tests. Not named *Test so Surefire does not discover it directly.
 * Run via EngineTestKit in LogSquelcherAcceptanceTest.
 */
@ExtendWith(LogSquelcherExtension.class)
public class LoggingFixture {

    public static final String REPLAYED_MESSAGE = "this should appear on failure";
    public static final String SUPPRESSED_MESSAGE = "this should be silenced on success";

    private static final Logger LOG = LoggerFactory.getLogger(LoggingFixture.class);

    @Test
    public void failingTestThatLogs() {
        LOG.warn(REPLAYED_MESSAGE);
        throw new AssertionError("deliberate failure");
    }

    @Test
    public void passingTestThatLogs() {
        LOG.warn(SUPPRESSED_MESSAGE);
    }
}

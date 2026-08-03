package example;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RealtimePassingTest {
    static final String LIVE_MESSAGE = "live log from a passing test in realtime mode";
    private static final Logger LOG = LoggerFactory.getLogger(RealtimePassingTest.class);

    @Test
    void logsAndPasses() {
        LOG.warn(LIVE_MESSAGE);
    }
}

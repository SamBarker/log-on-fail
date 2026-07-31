package example;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NoBackendFailingTest {
    static final String REPLAYED_MESSAGE = "this message should appear even without a logging backend";
    private static final Logger LOG = LoggerFactory.getLogger(NoBackendFailingTest.class);

    @Test
    void logsAndFails() {
        LOG.warn(REPLAYED_MESSAGE);
        throw new AssertionError("deliberate failure to trigger log replay");
    }
}

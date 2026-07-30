package example;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FailingTest {

    static final String REPLAYED_MESSAGE = "this message should appear when the test fails";

    private static final Logger LOG = LoggerFactory.getLogger(FailingTest.class);

    @Test
    void logsAndFails() {
        LOG.warn(REPLAYED_MESSAGE);
        throw new AssertionError("deliberate failure to trigger log replay");
    }
}

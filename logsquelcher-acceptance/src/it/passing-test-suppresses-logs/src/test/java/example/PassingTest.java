package example;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PassingTest {

    static final String SUPPRESSED_MESSAGE = "this message should not appear when the test passes";

    private static final Logger LOG = LoggerFactory.getLogger(PassingTest.class);

    @Test
    void logsAndPasses() {
        LOG.warn(SUPPRESSED_MESSAGE);
    }
}

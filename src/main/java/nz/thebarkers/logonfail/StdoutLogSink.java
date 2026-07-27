package nz.thebarkers.logonfail;

import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.util.List;

class StdoutLogSink implements LogSink {

    private static final String BORDER = "═".repeat(58);

    @Override
    public void report(String testId, List<CapturedEvent> events) {
        ConsoleAppender appender = ConsoleAppender.newBuilder()
                .setName("log-on-fail-console")
                .setTarget(ConsoleAppender.Target.SYSTEM_OUT)
                .setLayout(PatternLayout.createDefaultLayout())
                .build();
        appender.start();
        System.out.println(BORDER);
        System.out.println("LOG CAPTURE — " + testId + " [FAILED]");
        System.out.println(BORDER);
        events.forEach(e -> appender.append(e.event()));
        System.out.println(BORDER);
        appender.stop();
    }
}

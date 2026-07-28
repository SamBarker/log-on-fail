package nz.thebarkers.logonfail;

import java.util.List;

class StdoutLogSink implements LogSink {

    private static final String BORDER = "═".repeat(58);

    @Override
    public void report(String testId, List<CapturedEvent> events) {
        System.out.println('\n' + BORDER);
        System.out.println("LOG CAPTURE — " + testId + " [FAILED]");
        System.out.println(BORDER);
        events.forEach(e -> System.out.println(e.formattedLine()));
        System.out.println(BORDER + '\n');
    }
}

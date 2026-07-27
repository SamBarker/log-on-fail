package nz.thebarkers.logonfail;

import java.util.List;

public interface LogSink {
    void report(String testId, List<CapturedEvent> events);
}

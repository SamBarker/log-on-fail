package nz.thebarkers.logsquelcher;

import org.slf4j.event.LoggingEvent;

public record CapturedEvent(long nanoTime, LoggingEvent loggingEvent) {
}

package nz.thebarkers.logonfail;

import org.slf4j.event.LoggingEvent;

public record CapturedEvent(long nanoTime, LoggingEvent loggingEvent) {
}

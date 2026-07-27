package nz.thebarkers.logonfail;

import org.apache.logging.log4j.core.LogEvent;

public record CapturedEvent(long nanoTime, LogEvent event) {
}

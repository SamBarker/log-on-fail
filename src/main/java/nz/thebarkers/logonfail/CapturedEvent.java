package nz.thebarkers.logonfail;

import org.slf4j.event.LoggingEvent;
import org.slf4j.helpers.MessageFormatter;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;

public record CapturedEvent(long nanoTime, LoggingEvent loggingEvent) {

    String formattedLine() {
        String msg = MessageFormatter.arrayFormat(
                loggingEvent.getMessage(),
                loggingEvent.getArgumentArray(),
                loggingEvent.getThrowable()).getMessage();
        StringBuilder sb = new StringBuilder()
                .append(Instant.ofEpochMilli(loggingEvent.getTimeStamp())).append(' ')
                .append(String.format("%-5s", loggingEvent.getLevel().name())).append(' ')
                .append('[').append(loggingEvent.getThreadName()).append("] ")
                .append(loggingEvent.getLoggerName()).append(" - ")
                .append(msg);
        if (loggingEvent.getThrowable() != null) {
            StringWriter sw = new StringWriter();
            loggingEvent.getThrowable().printStackTrace(new PrintWriter(sw));
            sb.append('\n').append(sw);
        }
        return sb.toString();
    }
}

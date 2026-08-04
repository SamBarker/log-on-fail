package nz.thebarkers.logsquelcher;

import org.assertj.core.api.AbstractAssert;
import org.slf4j.event.KeyValuePair;
import org.slf4j.event.Level;
import org.slf4j.event.LoggingEvent;
import org.slf4j.helpers.MessageFormatter;

/**
 * AssertJ assertion for {@link LoggingEvent} instances returned by
 * {@link CapturedLogs#logged(Class, Level)} and {@link CapturedLogs#logged(Class)}.
 *
 * <p>Typical usage:
 * <pre>{@code
 * import static nz.thebarkers.logsquelcher.LoggingEventAssert.assertThat;
 *
 * assertThat(ext.logged(MyService.class, Level.WARN))
 *     .hasFormattedMessage("Plugin is deprecated")
 *     .containsKeyValue("filterName", "myFilterDef");
 * }</pre>
 */
public class LoggingEventAssert extends AbstractAssert<LoggingEventAssert, LoggingEvent> {

    private LoggingEventAssert(LoggingEvent actual) {
        super(actual, LoggingEventAssert.class);
    }

    public static LoggingEventAssert assertThat(LoggingEvent actual) {
        return new LoggingEventAssert(actual);
    }

    /**
     * Verifies that the SLF4J-formatted message (placeholders resolved) equals {@code expected}.
     */
    public LoggingEventAssert hasFormattedMessage(String expected) {
        isNotNull();
        String formatted = MessageFormatter.arrayFormat(actual.getMessage(), actual.getArgumentArray(),
                actual.getThrowable()).getMessage();
        if (!formatted.equals(expected)) {
            failWithMessage("Expected formatted message to be <%s> but was <%s>", expected, formatted);
        }
        return this;
    }

    /**
     * Verifies that the event's key-value pairs contain an entry with the given key and value.
     */
    public LoggingEventAssert containsKeyValue(String key, Object value) {
        isNotNull();
        if (actual.getKeyValuePairs() == null) {
            failWithMessage("Expected key-value pairs to contain <%s=%s> but getKeyValuePairs() returned null", key, value);
            return this;
        }
        boolean found = actual.getKeyValuePairs().stream()
                .anyMatch(kv -> key.equals(kv.key) && String.valueOf(value).equals(String.valueOf(kv.value)));
        if (!found) {
            failWithMessage("Expected key-value pairs to contain <%s=%s> but was <%s>",
                    key, value, actual.getKeyValuePairs());
        }
        return this;
    }
}

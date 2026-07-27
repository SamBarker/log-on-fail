package nz.thebarkers.logonfail;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.message.SimpleMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileLogSinkTest {

    @TempDir
    Path tmp;

    private static CapturedEvent event(String logger, String message) {
        return new CapturedEvent(0L, Log4jLogEvent.newBuilder()
                .setLoggerName(logger)
                .setLevel(Level.WARN)
                .setMessage(new SimpleMessage(message))
                .build()
                .toImmutable());
    }

    @Test
    void resolveFileProducesClassMethodPath() {
        var sink = new FileLogSink(tmp);
        Path file = sink.resolveFile("MyTest#myMethod");
        assertEquals(tmp.resolve("MyTest").resolve("myMethod.log"), file);
    }

    @Test
    void reportCreatesFileWithHeader() throws IOException {
        var sink = new FileLogSink(tmp);
        sink.report("MyTest#myMethod", List.of());

        Path file = tmp.resolve("MyTest").resolve("myMethod.log");
        assertTrue(Files.exists(file));
        String content = Files.readString(file);
        assertTrue(content.contains("LOG CAPTURE — MyTest#myMethod [FAILED]"));
    }

    @Test
    void reportWritesEventLinesToFile() throws IOException {
        var sink = new FileLogSink(tmp);
        sink.report("MyTest#myMethod", List.of(
                event("io.kroxylicious.Foo", "something bad")));

        String content = Files.readString(tmp.resolve("MyTest").resolve("myMethod.log"));
        assertTrue(content.contains("something bad"));
    }

    @Test
    void reportCreatesIntermediateDirectories() {
        var sink = new FileLogSink(tmp.resolve("nested").resolve("dirs"));
        assertDoesNotThrow(() -> sink.report("T#m", List.of()));
    }
}

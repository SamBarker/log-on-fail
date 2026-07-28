package nz.thebarkers.logonfail;

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
        return new CapturedEvent(0L, "WARN  [main] " + logger + " - " + message);
    }

    @Test
    void resolveFileProducesClassMethodPath() {
        // Given
        var sink = new FileLogSink(tmp);

        // When
        Path file = sink.resolveFile("MyTest#myMethod");

        // Then
        assertEquals(tmp.resolve("MyTest").resolve("myMethod.log"), file);
    }

    @Test
    void reportCreatesFileWithHeader() throws IOException {
        // Given
        var sink = new FileLogSink(tmp);

        // When
        sink.report("MyTest#myMethod", List.of());

        // Then
        Path file = tmp.resolve("MyTest").resolve("myMethod.log");
        assertTrue(Files.exists(file));
        assertTrue(Files.readString(file).contains("LOG CAPTURE — MyTest#myMethod [FAILED]"));
    }

    @Test
    void reportWritesEventLinesToFile() throws IOException {
        // Given
        var sink = new FileLogSink(tmp);

        // When
        sink.report("MyTest#myMethod", List.of(event("io.kroxylicious.Foo", "something bad")));

        // Then
        String content = Files.readString(tmp.resolve("MyTest").resolve("myMethod.log"));
        assertTrue(content.contains("something bad"));
    }

    @Test
    void reportCreatesIntermediateDirectories() {
        // Given
        var sink = new FileLogSink(tmp.resolve("nested").resolve("dirs"));

        // When / Then
        assertDoesNotThrow(() -> sink.report("T#m", List.of()));
    }
}

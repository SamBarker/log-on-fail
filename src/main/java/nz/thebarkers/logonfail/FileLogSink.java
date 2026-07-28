package nz.thebarkers.logonfail;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

class FileLogSink implements LogSink {

    private final Path dir;

    FileLogSink(Path dir) {
        this.dir = dir;
    }

    @Override
    public void report(String testId, List<CapturedEvent> events) {
        Path file = resolveFile(testId);
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, "LOG CAPTURE — " + testId + " [FAILED]\n",
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            for (CapturedEvent e : events) {
                Files.writeString(file, e.formattedLine() + '\n', StandardOpenOption.APPEND);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    Path resolveFile(String testId) {
        int hash = testId.indexOf('#');
        String cls = hash >= 0 ? testId.substring(0, hash) : testId;
        String method = hash >= 0 ? testId.substring(hash + 1) : "unknown";
        return dir.resolve(cls).resolve(method + ".log");
    }
}

package dev.cloud.master.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import dev.cloud.master.console.ConsoleFormatter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;

/**
 * Custom Logback appender that writes formatted log lines to a daily rolling log file
 * under {@code logs/YYYY-MM-DD.log} and prints them to stdout with ANSI colors.
 */
public class LogAppender extends AppenderBase<ILoggingEvent> {

    private static final Path LOG_DIR = Path.of("logs");
    private final ConsoleFormatter formatter = new ConsoleFormatter();

    @Override
    protected void append(ILoggingEvent event) {
        String level = event.getLevel().levelStr;
        String message = event.getFormattedMessage();
        String colored = formatter.format(level, message);

        System.out.println(colored);
        writeToFile(ConsoleFormatter.stripAnsi(colored));
    }

    private void writeToFile(String line) {
        try {
            Files.createDirectories(LOG_DIR);
            Path logFile = LOG_DIR.resolve(LocalDate.now() + ".log");
            Files.writeString(logFile, line + System.lineSeparator(),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Failed to write log: " + e.getMessage());
        }
    }
}
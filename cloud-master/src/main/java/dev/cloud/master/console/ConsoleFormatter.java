package dev.cloud.master.console;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Formats console output with timestamps and ANSI color codes.
 */
public class ConsoleFormatter {

    // ANSI codes
    public static final String RESET = "\u001B[0m";
    public static final String GRAY = "\u001B[90m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String RED = "\u001B[31m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    /**
     * Strips all ANSI escape codes from a string.
     * Used when writing to log files.
     *
     * @param text the text to strip
     * @return plain text without ANSI codes
     */
    public static String stripAnsi(String text) {
        return text.replaceAll("\u001B\\[[;\\d]*m", "");
    }

    /**
     * Formats a log line with timestamp and level color.
     *
     * @param level   the log level label (e.g. {@code "INFO"})
     * @param message the message to format
     * @return the formatted string with ANSI codes
     */
    public String format(String level, String message) {
        String time = GRAY + "[" + LocalTime.now().format(TIME_FMT) + "]" + RESET;
        String lvl = levelColor(level) + "[" + level + "]" + RESET;
        return time + " " + lvl + " " + WHITE + message + RESET;
    }

    /**
     * Returns the ANSI color for a given log level.
     */
    private String levelColor(String level) {
        return switch (level.toUpperCase()) {
            case "INFO" -> GREEN;
            case "WARN" -> YELLOW;
            case "ERROR" -> RED;
            case "DEBUG" -> GRAY;
            default -> CYAN;
        };
    }
}
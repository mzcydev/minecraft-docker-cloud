package dev.cloud.master.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Central logger for master-level cloud events.
 * Wraps SLF4J and prefixes messages with the cloud context.
 */
public class CloudLogger {

    private static final Logger log = LoggerFactory.getLogger("CloudMaster");

    public static void info(String message, Object... args) {
        log.info(message, args);
    }

    public static void warn(String message, Object... args) {
        log.warn(message, args);
    }

    public static void error(String message, Object... args) {
        log.error(message, args);
    }

    public static void debug(String message, Object... args) {
        log.debug(message, args);
    }
}
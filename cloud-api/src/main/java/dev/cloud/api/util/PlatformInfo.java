package dev.cloud.api.util;

import java.lang.management.ManagementFactory;

/**
 * Provides static information about the current host machine at runtime.
 */
public final class PlatformInfo {

    private PlatformInfo() {
    }

    /**
     * Returns the total physical memory of the machine in megabytes.
     */
    public static int getTotalMemoryMb() {
        long bytes = Runtime.getRuntime().totalMemory();
        return (int) (bytes / (1024 * 1024));
    }

    /**
     * Returns the number of logical CPU cores available to the JVM.
     */
    public static int getCpuCores() {
        return Runtime.getRuntime().availableProcessors();
    }

    /**
     * Returns the name of the operating system (e.g. {@code "Linux"}).
     */
    public static String getOperatingSystem() {
        return System.getProperty("os.name");
    }

    /**
     * Returns the current Java version string (e.g. {@code "21.0.1"}).
     */
    public static String getJavaVersion() {
        return System.getProperty("java.version");
    }

    /**
     * Returns the current JVM uptime in milliseconds.
     */
    public static long getUptimeMs() {
        return ManagementFactory.getRuntimeMXBean().getUptime();
    }
}
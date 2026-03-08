package dev.cloud.api.util;

/**
 * Parses and compares Java version strings.
 */
public record JavaVersion(int major, int minor, int patch) {

    /**
     * Parses a Java version string (e.g. {@code "21.0.1"} or {@code "17"}).
     *
     * @param version the version string to parse
     * @return the parsed {@link JavaVersion}
     * @throws IllegalArgumentException if the string cannot be parsed
     */
    public static JavaVersion parse(String version) {
        String[] parts = version.split("\\.");
        int major = parts.length > 0 ? Integer.parseInt(parts[0]) : 0;
        int minor = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
        int patch = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;
        return new JavaVersion(major, minor, patch);
    }

    /**
     * Returns the current JVM's version.
     */
    public static JavaVersion current() {
        return parse(System.getProperty("java.version"));
    }

    /**
     * Returns {@code true} if this version is at least the given major version.
     *
     * @param requiredMajor the minimum required major version (e.g. {@code 21})
     */
    public boolean isAtLeast(int requiredMajor) {
        return this.major >= requiredMajor;
    }
}
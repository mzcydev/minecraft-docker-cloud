package dev.cloud.api.util;

/**
 * Utility enum for converting between common byte units.
 */
public enum ByteUnit {
    BYTES(1L),
    KILOBYTES(1024L),
    MEGABYTES(1024L * 1024),
    GIGABYTES(1024L * 1024 * 1024),
    TERABYTES(1024L * 1024 * 1024 * 1024);

    private final long factor;

    ByteUnit(long factor) {
        this.factor = factor;
    }

    /**
     * Converts the given value from this unit to bytes.
     *
     * @param value the value in this unit
     * @return the equivalent number of bytes
     */
    public long toBytes(long value) {
        return value * factor;
    }

    /**
     * Converts the given value from this unit to megabytes.
     *
     * @param value the value in this unit
     * @return the equivalent number of megabytes
     */
    public long toMegabytes(long value) {
        return toBytes(value) / MEGABYTES.factor;
    }
}
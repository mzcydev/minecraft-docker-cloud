package dev.cloud.api.util;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe port allocator that hands out ports sequentially within a defined range.
 * Wraps around to the start when the end of the range is reached.
 */
public class PortRange {

    private final int min;
    private final int max;
    private final AtomicInteger current;

    /**
     * Creates a port range.
     *
     * @param min the lowest port number (inclusive)
     * @param max the highest port number (inclusive)
     */
    public PortRange(int min, int max) {
        if (min > max) throw new IllegalArgumentException("min must be <= max");
        this.min = min;
        this.max = max;
        this.current = new AtomicInteger(min);
    }

    /**
     * Returns the next available port, wrapping around if the end of the range is reached.
     */
    public int next() {
        return current.getAndUpdate(port -> port >= max ? min : port + 1);
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }
}
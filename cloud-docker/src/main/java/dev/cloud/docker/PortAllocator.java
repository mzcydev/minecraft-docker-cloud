package dev.cloud.docker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.NavigableSet;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Thread-safe port allocator that manages a pool of available ports.
 * Ports are reserved when a service starts and released when it stops.
 */
public class PortAllocator {

    private static final Logger log = LoggerFactory.getLogger(PortAllocator.class);

    private final NavigableSet<Integer> available = new ConcurrentSkipListSet<>();
    private final NavigableSet<Integer> reserved = new ConcurrentSkipListSet<>();

    /**
     * Initializes the allocator with a contiguous range of ports.
     *
     * @param from the first port in the range (inclusive)
     * @param to   the last port in the range (inclusive)
     */
    public PortAllocator(int from, int to) {
        for (int port = from; port <= to; port++) {
            available.add(port);
        }
        log.info("PortAllocator initialized with {} ports ({}-{})", available.size(), from, to);
    }

    /**
     * Reserves and returns the lowest available port.
     *
     * @return an {@link Optional} containing the reserved port,
     * or empty if no ports are available
     */
    public Optional<Integer> acquire() {
        Integer port = available.pollFirst();
        if (port == null) {
            log.warn("No ports available in the pool.");
            return Optional.empty();
        }
        reserved.add(port);
        log.debug("Port {} acquired ({} remaining)", port, available.size());
        return Optional.of(port);
    }

    /**
     * Releases a previously reserved port back into the available pool.
     *
     * @param port the port to release
     */
    public void release(int port) {
        if (reserved.remove(port)) {
            available.add(port);
            log.debug("Port {} released ({} available)", port, available.size());
        } else {
            log.warn("Attempted to release port {} which was not reserved.", port);
        }
    }

    /**
     * Returns the number of currently available ports.
     */
    public int availableCount() {
        return available.size();
    }

    /**
     * Returns the number of currently reserved ports.
     */
    public int reservedCount() {
        return reserved.size();
    }
}
package dev.cloud.api.group;

/**
 * Describes the deployment strategy for a {@link ServiceGroup},
 * controlling how the scheduler keeps services alive and balanced.
 */
public record GroupDeployment(
        int minOnline,
        int maxOnline,
        int startThresholdPercent
) {
    /**
     * Creates a {@link GroupDeployment} from a group's configuration.
     *
     * @param group the source group
     * @return the corresponding deployment descriptor
     */
    public static GroupDeployment from(ServiceGroup group) {
        return new GroupDeployment(
                group.getMinOnlineCount(),
                group.getMaxOnlineCount(),
                80
        );
    }

    /**
     * Returns {@code true} if an additional service should be started
     * given the current fill percentage across all online services.
     *
     * @param currentOnline  number of services currently online
     * @param avgFillPercent average fill percentage across all online services (0–100)
     */
    public boolean shouldStartAdditional(int currentOnline, int avgFillPercent) {
        return currentOnline < maxOnline && avgFillPercent >= startThresholdPercent;
    }

    /**
     * Returns {@code true} if the minimum service count is not yet satisfied.
     *
     * @param currentOnline number of services currently online or starting
     */
    public boolean isBelowMinimum(int currentOnline) {
        return currentOnline < minOnline;
    }
}
package dev.cloud.api.group;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.cloud.api.service.ServiceLifecycle;

/**
 * Default Jackson-serializable implementation of {@link ServiceGroup}.
 * Instances are persisted as YAML files in the master's {@code groups/} directory.
 */
public class ServiceGroupImpl implements ServiceGroup {

    private final String name;
    private final ServiceType serviceType;
    private final String templateName;
    private final int minOnlineCount;
    private final int maxOnlineCount;
    private final int maxPlayers;
    private final int memory;
    private final ServiceLifecycle lifecycle;
    private boolean maintenance;
    private final String jvmFlags;
    private final int startPort;

    @JsonCreator
    public ServiceGroupImpl(
            @JsonProperty("name")           String name,
            @JsonProperty("serviceType")    ServiceType serviceType,
            @JsonProperty("templateName")   String templateName,
            @JsonProperty("minOnlineCount") int minOnlineCount,
            @JsonProperty("maxOnlineCount") int maxOnlineCount,
            @JsonProperty("maxPlayers")     int maxPlayers,
            @JsonProperty("memory")         int memory,
            @JsonProperty("lifecycle")      ServiceLifecycle lifecycle,
            @JsonProperty("maintenance")    boolean maintenance,
            @JsonProperty("jvmFlags")       String jvmFlags,
            @JsonProperty("startPort")      int startPort
    ) {
        this.name = name;
        this.serviceType = serviceType;
        this.templateName = templateName;
        this.minOnlineCount = minOnlineCount;
        this.maxOnlineCount = maxOnlineCount;
        this.maxPlayers = maxPlayers;
        this.memory = memory;
        this.lifecycle = lifecycle;
        this.maintenance = maintenance;
        this.jvmFlags = jvmFlags != null ? jvmFlags : "-Xmx" + memory + "M -Xms" + memory + "M";
        this.startPort = startPort;
    }

    @Override public String getName()                { return name; }
    @Override public ServiceType getServiceType()    { return serviceType; }
    @Override public ServiceEnvironment getEnvironment() { return ServiceEnvironment.of(serviceType); }
    @Override public String getTemplateName()        { return templateName; }
    @Override public int getMinOnlineCount()         { return minOnlineCount; }
    @Override public int getMaxOnlineCount()         { return maxOnlineCount; }
    @Override public int getMaxPlayers()             { return maxPlayers; }
    @Override public int getMemory()                 { return memory; }
    @Override public ServiceLifecycle getLifecycle() { return lifecycle; }
    @Override public boolean isMaintenance()         { return maintenance; }
    @Override public String getJvmFlags()            { return jvmFlags; }
    @Override public int getStartPort()              { return startPort; }

    /**
     * Toggles maintenance mode for this group.
     *
     * @param maintenance {@code true} to enable, {@code false} to disable
     */
    public void setMaintenance(boolean maintenance) {
        this.maintenance = maintenance;
    }
}
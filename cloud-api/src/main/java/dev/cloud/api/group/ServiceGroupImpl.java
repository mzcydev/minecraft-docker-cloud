package dev.cloud.api.group;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Default Jackson-serializable implementation of {@link ServiceGroup}.
 */
public class ServiceGroupImpl implements ServiceGroup {

    private final String name;
    private final ServiceType type;
    private final String templateName;
    private final int minServices;
    private final int maxServices;
    private final int maxPlayers;
    private final int memory;
    private final String jvmFlags;
    private final int startPort;
    private final boolean isStatic;
    private boolean maintenance;

    @JsonCreator
    public ServiceGroupImpl(
            @JsonProperty("name")        String name,
            @JsonProperty("type")        ServiceType type,
            @JsonProperty("templateName") String templateName,
            @JsonProperty("minServices") int minServices,
            @JsonProperty("maxServices") int maxServices,
            @JsonProperty("maxPlayers")  int maxPlayers,
            @JsonProperty("memory")      int memory,
            @JsonProperty("jvmFlags")    String jvmFlags,
            @JsonProperty("startPort")   int startPort,
            @JsonProperty("static")      boolean isStatic,
            @JsonProperty("maintenance") boolean maintenance
    ) {
        this.name        = name;
        this.type        = type;
        this.templateName = templateName;
        this.minServices = minServices;
        this.maxServices = maxServices;
        this.maxPlayers  = maxPlayers;
        this.memory      = memory;
        this.jvmFlags    = jvmFlags != null ? jvmFlags : "-Xmx" + memory + "M -Xms" + memory + "M";
        this.startPort   = startPort;
        this.isStatic    = isStatic;
        this.maintenance = maintenance;
    }

    /** Convenience constructor without startPort/maintenance */
    public ServiceGroupImpl(String name, ServiceType type, String templateName,
                            int memory, int maxPlayers,
                            int minServices, int maxServices,
                            String jvmFlags, boolean isStatic) {
        this(name, type, templateName, minServices, maxServices,
             maxPlayers, memory, jvmFlags, 25565, isStatic, false);
    }

    @Override public String getName()            { return name; }
    @Override public ServiceType getType()       { return type; }
    @Override public ServiceEnvironment getEnvironment() { return ServiceEnvironment.of(type); }
    @Override public String getTemplateName()    { return templateName; }
    @Override public int getMinServices()        { return minServices; }
    @Override public int getMaxServices()        { return maxServices; }
    @Override public int getMaxPlayers()         { return maxPlayers; }
    @Override public int getMemory()             { return memory; }
    @Override public boolean isStatic()          { return isStatic; }
    @Override public boolean isMaintenance()     { return maintenance; }
    @Override public String getJvmFlags()        { return jvmFlags; }
    @Override public int getStartPort()          { return startPort; }

    public void setMaintenance(boolean maintenance) { this.maintenance = maintenance; }
}

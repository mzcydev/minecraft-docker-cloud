package dev.cloud.api.template;

/**
 * Represents a template — a directory of files that is copied into every
 * new service directory before the container is started.
 */
public interface Template {

    /**
     * Returns the unique name of this template (e.g. {@code "lobby"}, {@code "bedwars"}).
     */
    String getName();

    /**
     * Returns the backend storage type this template is stored in.
     */
    TemplateStorage getStorage();

    /**
     * Returns the path to the template's root directory relative to the storage backend's root.
     * Example: {@code "templates/lobby"}
     */
    String getPath();
}
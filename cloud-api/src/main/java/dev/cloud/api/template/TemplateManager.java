package dev.cloud.api.template;

import java.util.Collection;
import java.util.Optional;

/**
 * Manages template metadata and coordinates template distribution to nodes.
 */
public interface TemplateManager {

    /**
     * Registers a new template.
     *
     * @param template the template to register
     */
    void createTemplate(Template template);

    /**
     * Removes a template registration. Does not delete the underlying files.
     *
     * @param name the name of the template to remove
     */
    void deleteTemplate(String name);

    /**
     * Looks up a template by name.
     *
     * @param name the template name
     * @return an {@link Optional} containing the template, or empty if not found
     */
    Optional<Template> getTemplate(String name);

    /**
     * Returns all registered templates.
     */
    Collection<Template> getAllTemplates();

    /**
     * Returns the current version info for the given template.
     *
     * @param templateName the template to get the version for
     * @return an {@link Optional} containing the version, or empty if not versioned yet
     */
    Optional<TemplateVersion> getVersion(String templateName);
}
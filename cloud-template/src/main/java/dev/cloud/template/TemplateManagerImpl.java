package dev.cloud.template;

import dev.cloud.api.template.Template;
import dev.cloud.api.template.TemplateManager;
import dev.cloud.api.template.TemplateVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of {@link TemplateManager}.
 * Stores templates in memory and delegates file operations to the configured storage backend.
 */
public class TemplateManagerImpl implements TemplateManager {

    private static final Logger log = LoggerFactory.getLogger(TemplateManagerImpl.class);

    private final Map<String, Template> templates = new ConcurrentHashMap<>();
    private final Map<String, TemplateVersion> versions = new ConcurrentHashMap<>();

    @Override
    public void createTemplate(Template template) {
        if (templates.containsKey(template.getName())) {
            throw new IllegalArgumentException("Template already exists: " + template.getName());
        }
        templates.put(template.getName(), template);
        log.info("Template '{}' registered.", template.getName());
    }

    @Override
    public void deleteTemplate(String name) {
        templates.remove(name);
        versions.remove(name);
        log.info("Template '{}' deleted.", name);
    }

    @Override
    public Optional<Template> getTemplate(String name) {
        return Optional.ofNullable(templates.get(name));
    }

    @Override
    public Collection<Template> getAllTemplates() {
        return templates.values();
    }

    @Override
    public Optional<TemplateVersion> getVersion(String templateName) {
        return Optional.ofNullable(versions.get(templateName));
    }

    /**
     * Updates the stored version metadata for a template.
     * Called after a template is synced or modified.
     *
     * @param version the new version to store
     */
    public void updateVersion(TemplateVersion version) {
        versions.put(version.templateName(), version);
        log.debug("Version updated for template '{}': {}", version.templateName(), version.checksum());
    }
}
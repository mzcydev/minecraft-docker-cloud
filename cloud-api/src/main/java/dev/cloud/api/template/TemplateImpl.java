package dev.cloud.api.template;

/**
 * Default implementation of {@link Template}.
 */
public record TemplateImpl(
        String name,
        TemplateStorage storage,
        String path
) implements Template {

    @Override
    public String getName() {
        return name;
    }

    @Override
    public TemplateStorage getStorage() {
        return storage;
    }

    @Override
    public String getPath() {
        return path;
    }
}
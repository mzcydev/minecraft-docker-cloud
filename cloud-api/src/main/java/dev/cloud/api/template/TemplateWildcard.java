package dev.cloud.api.template;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.List;

/**
 * Applies glob-style include and exclude patterns to filter files within a template directory.
 * Useful for excluding log files or world data from being synced.
 */
public class TemplateWildcard {

    private final List<PathMatcher> includes;
    private final List<PathMatcher> excludes;

    /**
     * Creates a new wildcard filter.
     *
     * @param includePatterns glob patterns for files to include (e.g. {@code "**.yml"})
     * @param excludePatterns glob patterns for files to exclude (e.g. {@code "logs/**"})
     */
    public TemplateWildcard(List<String> includePatterns, List<String> excludePatterns) {
        this.includes = toMatchers(includePatterns);
        this.excludes = toMatchers(excludePatterns);
    }

    /**
     * Returns {@code true} if the given path should be included in the template sync.
     * A file is included if it matches at least one include pattern and no exclude patterns.
     * If no include patterns are defined, all files are included by default.
     *
     * @param path the file path to check
     */
    public boolean matches(Path path) {
        boolean included = includes.isEmpty() || includes.stream().anyMatch(m -> m.matches(path));
        boolean excluded = excludes.stream().anyMatch(m -> m.matches(path));
        return included && !excluded;
    }

    private List<PathMatcher> toMatchers(List<String> patterns) {
        return patterns.stream()
                .map(p -> FileSystems.getDefault().getPathMatcher("glob:" + p))
                .toList();
    }
}
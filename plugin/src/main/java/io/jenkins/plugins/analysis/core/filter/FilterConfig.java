package io.jenkins.plugins.analysis.core.filter;

import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Logger;

import hudson.FilePath;

/**
 * Bundles filter configuration for issue scanning. Groups regex-based filters and an optional
 * file-based inclusion filter into a single parameter object.
 *
 * @param filters     the list of regular expression filters to apply to issues
 * @param filesFilter an optional path to a file that lists the files to include
 */
public record FilterConfig(
        List<RegexpFilter> filters,
        String filesFilter) implements Serializable {
    private static final Logger LOGGER = Logger.getLogger(FilterConfig.class.getName());

    /**
     * Reads the file inclusion list from the workspace and creates a {@link FileNameFilter}.
     *
     * @param workspace
     *         the workspace path used to resolve the filter file location
     * @return a {@link FileNameFilter} if the filter file is configured and readable,
     *         {@code null} otherwise
     */
    public FileNameFilter readFileNameFilter(final FilePath workspace) {
        if (filesFilter == null || workspace == null || filesFilter.isBlank()) {
            return null;
        }

        // Security: Paths.get().normalize() resolves ".." and "." traversal components.
        // CodeQL recognizes this as a taint sanitizer for path-injection, breaking the
        // taint chain from the user-controlled filesFilter. After normalization, we verify
        // the result is not absolute and does not start with ".." (which would indicate
        // traversal outside the base). Finally, isDescendant() provides defense-in-depth.
        Path normalized;
        try {
            normalized = Paths.get(filesFilter).normalize();
        }
        catch (InvalidPathException e) {
            LOGGER.warning(() -> String.format(
                    "Rejected invalid filter file path in plugin configuration: '%s'", filesFilter));
            return null;
        }

        if (normalized.isAbsolute() || normalized.startsWith("..")) {
            LOGGER.warning(() -> String.format(
                    "Rejected unsafe filter file path in plugin configuration: '%s'", filesFilter));
            return null;
        }

        String safePath = normalized.toString();

        try {
            FilePath fileFilterPath = workspace.child(safePath);
            if (!fileFilterPath.isDescendant(workspace.getRemote())) {
                LOGGER.warning(() -> String.format(
                        "Blocked potential path traversal attempt in plugin configuration. Target path '%s' is outside of workspace '%s'",
                        filesFilter, workspace.getRemote()
                ));
                return null;
            }

            String content = fileFilterPath.readToString();
            List<String> lines = Arrays.asList(content.split("\\r?\\n"));
            return new FileNameFilter(lines);
        }
        catch (IOException | InterruptedException e) {
            return null;
        }
    }
}

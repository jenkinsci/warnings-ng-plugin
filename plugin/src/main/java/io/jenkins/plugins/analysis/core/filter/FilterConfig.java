package io.jenkins.plugins.analysis.core.filter;

import org.apache.commons.io.FilenameUtils;

import edu.hm.hafner.util.FilteredLog;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
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
     * <p>
     * Reading the list fails open: if the filter file is missing, unreadable, empty, or outside of the
     * workspace, then an error is reported to {@code log} and a {@link NullFileNameFilter} is returned so
     * that no issues are removed. Publishing all issues is the safer failure mode, since a filter that
     * silently removes everything would hide warnings rather than show too many. Builds that should fail
     * on such a misconfiguration can enable the {@code failOnError} option of the recorder, which reacts
     * to the errors reported here.
     * </p>
     *
     * @param workspace
     *         the workspace path used to resolve the filter file location
     * @param log
     *         the log to report problems to
     *
     * @return a {@link FileNameFilter} for the configured files, or a {@link NullFileNameFilter} if no
     *         filter file has been configured or if it cannot be read
     */
    public FileNameFilter readFileNameFilter(final FilePath workspace, final FilteredLog log) {
        if (filesFilter == null || filesFilter.isBlank()) {
            return new NullFileNameFilter();
        }
        if (workspace == null) {
            log.logError("Cannot read the filter file '%s': no workspace available", filesFilter);

            return new NullFileNameFilter();
        }

        // Resolve "." and ".." segments ourselves, treating both "/" and "\\" as separators. java.nio.file
        // Path normalizes against the JVM this code runs on, which differs between controller and agent and
        // across operating systems; a manual normalization keeps both sides in agreement. The workspace is
        // still the parent below: isDescendant() provides the defense-in-depth against paths that escape it,
        // including through symlinks.
        String safePath = normalizePath(filesFilter);
        if (safePath == null) {
            // Also reported to the system log: an administrator should see rejected paths even if the
            // build (and with it the console log) is discarded later on.
            LOGGER.warning(() -> String.format(
                    "Rejected unsafe filter file path in plugin configuration: '%s'", filesFilter));
            log.logError("Rejected the filter file path '%s': only relative paths within the workspace are supported",
                    filesFilter);

            return new NullFileNameFilter();
        }

        try {
            // The workspace is the parent and safePath the relative child: isDescendant() resolves symlinks
            // segment by segment, so it also rejects a path that escapes the workspace through a link.
            if (!workspace.isDescendant(safePath)) {
                LOGGER.warning(() -> String.format(
                        "Blocked potential path traversal attempt in plugin configuration. Target path '%s' is outside of workspace '%s'",
                        filesFilter, workspace.getRemote()
                ));
                log.logError("Blocked the filter file '%s': it resolves to a path outside of the workspace '%s'",
                        filesFilter, workspace.getRemote());

                return new NullFileNameFilter();
            }

            var files = readFileNames(workspace.child(safePath));
            if (files.isEmpty()) {
                log.logInfo("The filter file '%s' does not list any file name, no issues will be removed",
                        filesFilter);

                return new NullFileNameFilter();
            }

            log.logInfo("Restricting issues to the %d files listed in '%s'", files.size(), filesFilter);

            return new FileNameFilter(files);
        }
        catch (IOException | InterruptedException exception) {
            log.logException(exception, "Cannot read the filter file '%s'", filesFilter);

            return new NullFileNameFilter();
        }
    }

    private List<String> readFileNames(final FilePath file) throws IOException, InterruptedException {
        return Arrays.stream(file.readToString().split("\\r?\\n"))
                .map(String::strip)
                .filter(line -> !line.isEmpty())
                .toList();
    }

    /**
     * Normalizes the configured filter file path into a workspace-relative path that uses {@code /} as the
     * separator. Both {@code /} and {@code \} are treated as separators, and {@code .} as well as {@code ..}
     * segments are resolved, so the result is identical on the controller and the agent regardless of their
     * operating systems.
     *
     * @param path
     *         the raw path from the configuration
     *
     * @return the normalized relative path, or {@code null} if the path is absolute or resolves to a location
     *         outside of the workspace
     */
    private String normalizePath(final String path) {
        if (isAbsolute(path)) {
            return null;
        }

        return FilenameUtils.normalize(path, true);
    }

    private boolean isAbsolute(final String path) {
        return path.startsWith("/")
                || path.startsWith("\\")
                || path.matches("^[A-Za-z]:[/\\\\].*");
    }
}

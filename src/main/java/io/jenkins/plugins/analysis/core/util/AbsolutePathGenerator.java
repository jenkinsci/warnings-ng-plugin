package io.jenkins.plugins.analysis.core.util;

import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.Issues;
import edu.hm.hafner.util.VisibleForTesting;

import hudson.FilePath;

/**
 * Resolves absolute paths of the affected files of a set of issues.
 *
 * @author Ullrich Hafner
 */
// TODO: if this class is called on the master then a remote call is initiated for each affected file
public class AbsolutePathGenerator {
    static final String NOTHING_TO_DO = "Affected files for all issues already have absolute paths";

    private final FileSystem fileSystem;

    /**
     * Creates a new instance of {@link AbsolutePathGenerator}.
     */
    public AbsolutePathGenerator() {
        this(new FileSystem());
    }

    @VisibleForTesting
    AbsolutePathGenerator(final FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }

    /**
     * Resolves absolute paths of the affected files of the specified set of issues.
     *
     * @param issues
     *         the issues to resolve the paths
     * @param workspace
     *         the workspace containing the affected files
     */
    public void run(final Issues<?> issues, final FilePath workspace) {
        Set<String> relativeFileNames = issues.getFiles()
                .stream()
                .filter(fileName -> fileSystem.isRelative(fileName) && !IssueParser.SELF.equals(fileName))
                .collect(Collectors.toSet());

        if (relativeFileNames.isEmpty()) {
            issues.logInfo(NOTHING_TO_DO);

            return;
        }

        Map<String, String> relativeToAbsoluteMapping = resolveAbsoluteNames(relativeFileNames, workspace);

        int resolvedCount = 0;
        int unchangedCount = 0;
        int unresolvedCount = 0;
        for (Issue issue : issues) {
            if (relativeToAbsoluteMapping.containsKey(issue.getFileName())) {
                String absoluteFileName = relativeToAbsoluteMapping.get(issue.getFileName());
                issue.setFileName(absoluteFileName);
                resolvedCount++;
            }
            else {
                if (relativeFileNames.contains(issue.getFileName())) {
                    unresolvedCount++;
                }
                else {
                    unchangedCount++;
                }
            }
        }

        String message = String.format(
                "Resolved absolute paths for %d files (Issues %d resolved, %d unresolved, %d already absolute)",
                relativeToAbsoluteMapping.size(), resolvedCount, unresolvedCount, unchangedCount);
        if (unresolvedCount > 0) {
            issues.logError(message);
        }
        else {
            issues.logInfo(message);
        }
    }

    private Map<String, String> resolveAbsoluteNames(final Set<String> relativeFileNames, final FilePath workspace) {
        Map<String, String> relativeToAbsoluteMapping = new HashMap<>();
        for (String fileName : relativeFileNames) {
            String absolute = fileSystem.resolveFile(fileName, workspace);
            if (!absolute.equals(fileName)) {
                relativeToAbsoluteMapping.put(fileName, absolute);
            }
        }
        return relativeToAbsoluteMapping;
    }

    /**
     * File system facade for test cases.
     */
    @VisibleForTesting
    static class FileSystem {
        String resolveFile(final String fileName, final FilePath workspace) {
            try {
                FilePath remoteFile = workspace.child(fileName);
                if (remoteFile.exists()) {
                    return remoteFile.getRemote();
                }
            }
            catch (IOException | InterruptedException ignored) {
                // ignore
            }
            return fileName;
        }

        boolean isRelative(final String fileName) {
            try {
                return !Paths.get(fileName).isAbsolute();
            }
            catch (InvalidPathException ignored) {
                return false; // do not try to resolve illegal paths
            }
        }
    }
}

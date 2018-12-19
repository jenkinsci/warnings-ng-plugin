package io.jenkins.plugins.analysis.core.util;

import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import edu.hm.hafner.analysis.FilteredLog;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.util.PathUtil;
import edu.hm.hafner.util.VisibleForTesting;

/**
 * Resolves absolute paths of the affected files of a set of issues.
 *
 * @author Ullrich Hafner
 */
public class AbsolutePathGenerator {
    static final String NOTHING_TO_DO = "-> none of the issues requires resolving of absolute path";
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
     * @param report
     *         the issues to resolve the paths
     * @param workspace
     *         the workspace containing the affected files
     */
    public void run(final Report report, final Path workspace) {
        Set<String> filesToProcess = report.getFiles()
                .stream()
                .filter(this::isInterestingFileName)
                .collect(Collectors.toSet());

        if (filesToProcess.isEmpty()) {
            report.logInfo(NOTHING_TO_DO);
            report.stream().forEach(issue -> issue.setFileName(new PathUtil().getAbsolutePath(issue.getFileName())));
            return;
        }

        FilteredLog log = new FilteredLog(report, "Can't resolve absolute paths for some files:");

        Map<String, String> pathMapping = resolveAbsoluteNames(filesToProcess, workspace, log);
        report.stream()
                .filter(issue -> pathMapping.containsKey(issue.getFileName()))
                .forEach(issue -> issue.setFileName(pathMapping.get(issue.getFileName())));

        log.logSummary();
    }

    private boolean isInterestingFileName(final String fileName) {
        return !"-".equals(fileName) && !ConsoleLogHandler.isInConsoleLog(fileName);
    }

    private Map<String, String> resolveAbsoluteNames(final Set<String> affectedFiles, final Path workspace,
            final FilteredLog log) {
        Map<String, String> pathMapping = new HashMap<>();
        int errors = 0;
        int unchanged = 0;
        int changed = 0;

        for (String fileName : affectedFiles) {
            Optional<String> absolutePath = fileSystem.resolveAbsolutePath(workspace, fileName);
            if (absolutePath.isPresent()) {
                String resolved = absolutePath.get();
                pathMapping.put(fileName, resolved);
                if (fileName.equals(resolved)) {
                    unchanged++;
                }
                else {
                    changed++;
                }
            }
            else {
                log.logError("- %s", fileName);
                errors++;
            }
        }
        log.logInfo("-> %d resolved, %d unresolved, %d already resolved", changed, errors, unchanged);

        return pathMapping;
    }

    /**
     * File system facade for test cases.
     */
    @VisibleForTesting
    static class FileSystem {
        Optional<String> resolveAbsolutePath(final Path parent, final String fileName) {
            try {
                return Optional.of(new PathUtil().toString(parent.resolve(fileName)));
            }
            catch (IOException | InvalidPathException ignored) {
                return Optional.empty();
            }
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

package io.jenkins.plugins.analysis.core.filter;

import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import edu.hm.hafner.analysis.Issue;

/**
 * A {@link Predicate} that filters {@link Issue} instances by checking their file names against
 * a provided collection of allowed paths.
 *
 * <p>
 * This filter bridges the difference between the absolute paths that static analysis tools often report
 * and the workspace relative paths that SCM tools (like {@code git diff}) produce: an entry matches an
 * issue if it is either the complete file name of that issue, or a suffix of it that starts at a path
 * segment boundary. So {@code src/File.ts} matches an issue in {@code /var/jenkins/job/src/File.ts},
 * while {@code Test.java} does not match an issue in {@code src/MyTest.java}.
 * </p>
 *
 * <p>
 * Because the match does not depend on whether the file names of the issues have already been resolved
 * to absolute paths, the filter yields the same result before and after post processing.
 * </p>
 *
 * @author Michael Trimarchi
 */
public class FileNameFilter implements Predicate<Issue> {
    /** The set of normalized file paths that are permitted to remain in the report. */
    private final Set<String> allowedFiles;

    /**
     * Creates a new instance of {@link FileNameFilter}.
     *
     * @param files
     *         the collection of file paths to include in the filter. These are typically retrieved from a
     *         version control system's diff output. Blank entries are ignored: an entry that matches every
     *         file would silently disable the filter.
     */
    public FileNameFilter(final Collection<String> files) {
        allowedFiles = files.stream()
                .map(FileNameFilter::normalize)
                .filter(file -> !file.isEmpty())
                .collect(Collectors.toSet());
    }

    /**
     * Evaluates this predicate on the given issue.
     *
     * @param issue
     *         the issue to test
     *
     * @return {@code true} if the file name of the issue is allowed, {@code false} otherwise
     */
    @Override
    public boolean test(final Issue issue) {
        var fileName = normalize(issue.getFileName());

        // Linear search over the allowed files: for extremely large lists, looking up all segment
        // boundary suffixes of the file name in the set would scale better.
        return allowedFiles.stream().anyMatch(allowed -> matches(fileName, allowed));
    }

    private static boolean matches(final String fileName, final String allowed) {
        return fileName.equals(allowed) || fileName.endsWith("/" + allowed);
    }

    /**
     * Converts a path into a comparable form: Windows separators are replaced by {@code /} and a leading
     * {@code ./} is dropped, so that entries and file names of issues can be compared segment wise.
     *
     * @param path
     *         the path to normalize
     *
     * @return the normalized path
     */
    private static String normalize(final String path) {
        var normalized = path.strip().replace('\\', '/');
        while (normalized.startsWith("./")) {
            normalized = normalized.substring(2);
        }
        return normalized;
    }
}

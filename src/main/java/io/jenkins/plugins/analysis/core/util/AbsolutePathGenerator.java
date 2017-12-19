package io.jenkins.plugins.analysis.core.util;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Issues;

import hudson.FilePath;

/**
 * Creates a fingerprint of an issue. A fingerprint is a digest of the affected source code of an issue. Using this
 * fingerprint an issue can be tracked in the source code even after some minor refactorings.
 *
 * @author Ullrich Hafner
 */
public class AbsolutePathGenerator {
    public Issues<Issue> run(final Issues<Issue> issues, final IssueBuilder builder, final FilePath workspace) {
        Set<String> relativeFileNames = issues.getFiles()
                .stream()
                .filter(fileName -> !Paths.get(fileName).isAbsolute())
                .collect(Collectors.toSet());

        if (relativeFileNames.isEmpty()) {
            Issues<Issue> unchanged = issues.copy();
            unchanged.log("Affected files for all issues already have absolute paths");

            return unchanged;
        }

        Map<String, String> relativeToAbsoluteMapping = resolveAbsoluteNames(relativeFileNames, workspace);
        Issues<Issue> resolved = new Issues<>();
        int resolvedCount = 0;
        int unchangedCount = 0;
        int unresolvedCount = 0;
        for (Issue issue : issues) {
            if (relativeToAbsoluteMapping.containsKey(issue.getFileName())) {
                String absoluteFileName = relativeToAbsoluteMapping.get(issue.getFileName());
                resolved.add(builder.copy(issue).setFileName(absoluteFileName).build());
                resolvedCount++;
            }
            else {
                if (relativeFileNames.contains(issue.getFileName())) {
                    unresolvedCount++;
                }
                else {
                    unchangedCount++;
                }
                resolved.add(issue);
            }
        }

        resolved.log("Resolved absolute paths for %d files (Issues %d resolved, %d unresolved, %d already absolute)",
                relativeToAbsoluteMapping.size(), resolvedCount, unresolvedCount, unchangedCount);

        return resolved;
    }

    private Map<String, String> resolveAbsoluteNames(final Set<String> relativeFileNames, final FilePath workspace) {
        Map<String, String> relativeToAbsoluteMapping = new HashMap<>();
        for (String fileName : relativeFileNames) {
            String absolute = resolveFile(fileName, workspace);
            if (!absolute.equals(fileName)) {
                relativeToAbsoluteMapping.put(fileName, absolute);
            }
        }
        return relativeToAbsoluteMapping;
    }

    private String resolveFile(final String fileName, final FilePath workspace) {
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
}

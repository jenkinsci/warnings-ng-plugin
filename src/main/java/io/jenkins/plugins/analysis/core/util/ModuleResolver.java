package io.jenkins.plugins.analysis.core.util;

import java.util.List;
import java.util.stream.Collectors;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.ModuleDetector;

/**
 * Resolves module names by reading and mapping module definitions (build.xml, pom.xml, or Manifest.mf files).
 *
 * @author Ullrich Hafner
 */
public class ModuleResolver {
    /**
     * Resolves absolute paths of the affected files of the specified set of issues.
     *
     * @param report
     *         the issues to resolve the paths
     * @param detector
     *         the module detector to use
     */
    public void run(final Report report, final ModuleDetector detector) {
        List<Issue> issuesWithoutModule = report.stream()
                .filter(issue -> !issue.hasModuleName())
                .collect(Collectors.toList());

        if (issuesWithoutModule.isEmpty()) {
            report.logInfo("All issues already have a valid module name");

            return;
        }

        // FIXME: plural should be in a base/utility class?
        issuesWithoutModule.forEach(issue -> issue.setModuleName(detector.guessModuleName(issue.getFileName())));
        report.logInfo("Resolved module names for %d issues", issuesWithoutModule.size());
    }
}

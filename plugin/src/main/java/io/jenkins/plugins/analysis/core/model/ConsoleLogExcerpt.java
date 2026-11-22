package io.jenkins.plugins.analysis.core.model;

import edu.hm.hafner.analysis.Issue;
import hudson.console.ConsoleNote;
import hudson.model.ModelObject;
import hudson.model.Run;
import io.jenkins.plugins.analysis.core.util.BuildFolderFacade;
import io.jenkins.plugins.prism.Marker;
import io.jenkins.plugins.prism.SourceCodeViewModel;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * Creates the source code view of issues that reference the console log of a build. The view shows an excerpt of the
 * console log: the lines of all issues plus some context lines before and after. The console log is shown as plain
 * text, and the console notes (e.g., hyperlinks or colors) are removed. The console log is not part of the workspace,
 * so the permission to view the source code is not required: every user who can see the build can see its console
 * log.
 *
 * @author Ullrich Hafner
 */
class ConsoleLogExcerpt {
    private static final int CONTEXT = 10;

    private final BuildFolderFacade buildFolder;

    /**
     * Creates a new instance of {@link ConsoleLogExcerpt}.
     *
     * @param buildFolder
     *         facade to read the console log of a build
     */
    ConsoleLogExcerpt(final BuildFolderFacade buildFolder) {
        this.buildFolder = buildFolder;
    }

    /**
     * Creates the view of the console log of the specified build.
     *
     * @param build
     *         the build that contains the console log
     * @param issues
     *         the issues to show
     * @param markers
     *         the markers of the issues
     *
     * @return the view model of the console log
     */
    ModelObject create(final Run<?, ?> build, final Collection<Issue> issues, final List<Marker> markers) {
        int first = issues.stream().mapToInt(Issue::getLineStart).min().orElse(1);
        int last = issues.stream()
                .mapToInt(issue -> Math.max(issue.getLineStart(), issue.getLineEnd()))
                .max()
                .orElse(1);
        try (Stream<String> consoleLog = buildFolder.readConsoleLog(build)) {
            return new SourceCodeViewModel.Builder(build, Messages.ConsoleLog_Name())
                    .withMarkers(markers)
                    .withPlainText()
                    .withExcerpt(first - CONTEXT, last + CONTEXT)
                    .withoutPermissionCheck()
                    .build(consoleLog.map(ConsoleNote::removeNotes));
        }
    }
}

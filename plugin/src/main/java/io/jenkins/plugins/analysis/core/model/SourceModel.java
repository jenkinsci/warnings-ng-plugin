package io.jenkins.plugins.analysis.core.model;

import edu.hm.hafner.analysis.Issue;
import hudson.model.ModelObject;
import hudson.model.Run;
import io.jenkins.plugins.analysis.core.util.BuildFolderFacade;
import io.jenkins.plugins.prism.Marker;
import io.jenkins.plugins.prism.Marker.MarkerBuilder;
import io.jenkins.plugins.prism.SourceCodeViewModel;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.Comparator;
import java.util.List;
import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * Server side model that provides the data for the source code page of the Warnings tab of a run. The page answers
 * the question: "Where in the source code are the warnings located?". It shows the affected file of the selected
 * issue with all issues of the same file: the lines of each issue are highlighted, and the message and description
 * of each issue are shown in a box below these lines. In contrast to the classic source code view, the page is part
 * of the Warnings tab, so the side panel of the tab is still visible.
 *
 * <p>
 * The source code is rendered by the {@link SourceCodeViewModel} of the Prism API plugin and embedded into the page
 * using the fragment {@code content.jelly} of that model. Each issue is shown as a {@link Marker} with the ID
 * {@code issue-<UUID>}, so a specific issue can be selected using the fragment of the URL. The client side script
 * {@code source-model.js} selects the issue of the link if no other issue has been selected in the URL yet.
 * </p>
 *
 * <p>
 * Fixed issues are not part of the source code of the current build anymore. So the source code of the reference
 * build is shown for fixed issues, together with all issues of the same file that have been fixed since then.
 * </p>
 *
 * @author Ullrich Hafner
 */
public class SourceModel implements ModelObject {
    private static final String MARKER_ID_PREFIX = "issue-";

    private final Run<?, ?> owner;
    private final Run<?, ?> sourceBuild;
    private final Issue selectedIssue;
    private final List<ToolIssue> issues;
    private final Charset sourceEncoding;
    private final String issuesUrl;
    private final BuildFolderFacade buildFolder;

    /**
     * Creates a new instance of {@link SourceModel}.
     *
     * @param owner
     *         the run that owns the Warnings tab
     * @param sourceBuild
     *         the run that contains the source code of the affected file: the owner for outstanding and new issues,
     *         the reference build for fixed issues
     * @param selectedIssue
     *         the issue that has been selected by the link
     * @param issues
     *         all issues of the affected file (including the selected issue) together with the tools that reported
     *         them
     * @param sourceEncoding
     *         the encoding of the affected file
     * @param issuesUrl
     *         the URL of the issues page that lists the selected issue, relative to the Warnings tab
     * @param buildFolder
     *         the facade to read the affected file from the build folder
     */
    @SuppressWarnings("checkstyle:ParameterNumber")
    SourceModel(
            final Run<?, ?> owner,
            final Run<?, ?> sourceBuild,
            final Issue selectedIssue,
            final List<ToolIssue> issues,
            final Charset sourceEncoding,
            final String issuesUrl,
            final BuildFolderFacade buildFolder) {
        this.owner = owner;
        this.sourceBuild = sourceBuild;
        this.selectedIssue = selectedIssue;
        this.issues = issues.stream()
                .sorted(Comparator.comparingInt(
                                (ToolIssue toolIssue) -> toolIssue.issue().getLineStart())
                        .thenComparingInt(toolIssue -> toolIssue.issue().getColumnStart()))
                .toList();
        this.sourceEncoding = sourceEncoding;
        this.issuesUrl = issuesUrl;
        this.buildFolder = buildFolder;
    }

    @Override
    public String getDisplayName() {
        return selectedIssue.getBaseName();
    }

    /**
     * Returns the full path of the affected file.
     *
     * @return the file name
     */
    public String getFileName() {
        return selectedIssue.getFileName();
    }

    /**
     * Returns the run that owns the Warnings tab. Required by {@code l:run-subpage}.
     *
     * @return the run
     */
    public Run<?, ?> getObject() {
        return owner;
    }

    /**
     * Returns the Warnings tab of the run. Used to render the side panel.
     *
     * @return the tab
     */
    public RunTab getTab() {
        return new RunTab(owner);
    }

    /**
     * Returns the issue that has been selected by the link.
     *
     * @return the selected issue
     */
    public Issue getSelectedIssue() {
        return selectedIssue;
    }

    /**
     * Returns the ID of the marker of the selected issue in the source code view.
     *
     * @return the ID of the marker
     */
    public String getSelectedMarkerId() {
        return createMarkerId(selectedIssue);
    }

    /**
     * Returns all issues of the affected file, sorted by their position in the file.
     *
     * @return the issues of the affected file
     */
    public List<Issue> getIssues() {
        return issues.stream().map(ToolIssue::issue).toList();
    }

    /**
     * Returns whether the source code of the reference build is shown, since the selected issue has been fixed.
     *
     * @return {@code true} if the selected issue has been fixed, {@code false} otherwise
     */
    public boolean isFixed() {
        return !sourceBuild.equals(owner);
    }

    /**
     * Returns the name of the run that contains the source code of the affected file.
     *
     * @return the name of the run, e.g. {@code #36}
     */
    public String getSourceBuildName() {
        return sourceBuild.getDisplayName();
    }

    /**
     * Returns the URL of the run that contains the source code of the affected file.
     *
     * @return the URL of the run, relative to the context path of Jenkins
     */
    public String getSourceBuildUrl() {
        return sourceBuild.getUrl();
    }

    /**
     * Returns the URL of the issues page that lists the selected issue.
     *
     * @return the URL, relative to the Warnings tab
     */
    public String getIssuesUrl() {
        return issuesUrl;
    }

    /**
     * Returns the view model that renders the source code of the affected file with all issues. If the current user
     * has no permission to see the source code, a view model with a corresponding message is returned instead. Both
     * models provide the fragment {@code content.jelly}.
     *
     * @return the view model of the source code
     */
    public ModelObject getSourceCode() {
        var markers = createMarkers();
        try (var affectedFile = buildFolder.readFile(sourceBuild, getFileName(), sourceEncoding)) {
            return render(affectedFile, markers);
        } catch (IOException exception) {
            try (var fallback = new StringReader("%s%n%s"
                    .formatted(ExceptionUtils.getMessage(exception), ExceptionUtils.getStackTrace(exception)))) {
                return render(fallback, markers);
            }
        }
    }

    private ModelObject render(final Reader reader, final List<Marker> markers) {
        return SourceCodeViewModel.create(sourceBuild, getDisplayName(), reader, markers);
    }

    /**
     * Creates the markers of all issues of the affected file.
     *
     * @return the markers
     */
    List<Marker> createMarkers() {
        return issues.stream().map(this::asMarker).toList();
    }

    private Marker asMarker(final ToolIssue toolIssue) {
        var issue = toolIssue.issue();
        var labelProvider = toolIssue.labelProvider();
        return new MarkerBuilder()
                .withId(createMarkerId(issue))
                .withTitle(issue.getMessage())
                .withDescription(labelProvider.getSourceCodeDescription(sourceBuild, issue))
                .withIcon(labelProvider.getSmallIconUrl())
                .withLineStart(issue.getLineStart())
                .withLineEnd(issue.getLineEnd())
                .withColumnStart(issue.getColumnStart())
                .withColumnEnd(issue.getColumnEnd())
                .build();
    }

    private static String createMarkerId(final Issue issue) {
        return MARKER_ID_PREFIX + issue.getId();
    }

    /**
     * An issue together with the label provider of the tool that reported the issue.
     *
     * @param issue
     *         the issue
     * @param labelProvider
     *         the label provider of the tool that reported the issue
     */
    record ToolIssue(Issue issue, StaticAnalysisLabelProvider labelProvider) {}
}

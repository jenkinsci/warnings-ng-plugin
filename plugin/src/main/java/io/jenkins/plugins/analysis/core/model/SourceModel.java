package io.jenkins.plugins.analysis.core.model;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Severity;
import hudson.model.ModelObject;
import hudson.model.Run;
import io.jenkins.plugins.analysis.core.util.Blame;
import io.jenkins.plugins.analysis.core.util.BuildFolderFacade;
import io.jenkins.plugins.analysis.core.util.LocalizedSeverity;
import io.jenkins.plugins.forensics.blame.Blames;
import io.jenkins.plugins.prism.Marker;
import io.jenkins.plugins.prism.Marker.MarkerBuilder;
import io.jenkins.plugins.prism.SourceCodeViewModel;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * Server side model that provides the data for the source code page of the Warnings tab of a run. The page answers
 * the question: "Where in the source code are the warnings located?". It shows the affected file of the selected
 * issue with all issues of the same file: the lines of each issue are highlighted, and the message and description
 * of each issue are shown in a box below these lines. The box shows the severity, the state (new or fixed), the
 * tool, the category and type, and the author and commit of the affected line (if blames are available) as badges.
 * The severity, the state, and the tool of an issue are additionally provided as data of the marker, so the client
 * side script {@code source-model.js} can filter the issues by the selected settings of the app bar: all issues of
 * the file or only the selected one, the minimum severity, and the baseline (new or outstanding issues).
 * The color of the box and of the marked lines is derived from the severity of the issue, see
 * {@code asMarkerSeverity}. In contrast to the classic source code view, the page is part
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
    private static final String UNDEFINED = "-";
    private static final int COMMIT_LENGTH = 7;

    private final Run<?, ?> owner;
    private final Run<?, ?> sourceBuild;
    private final Issue selectedIssue;
    private final List<ToolIssue> issues;
    private final Charset sourceEncoding;
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
     * @param buildFolder
     *         the facade to read the affected file from the build folder
     */
    SourceModel(
            final Run<?, ?> owner,
            final Run<?, ?> sourceBuild,
            final Issue selectedIssue,
            final List<ToolIssue> issues,
            final Charset sourceEncoding,
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
        var labelProvider = toolIssue.action().getLabelProvider();
        var builder = new MarkerBuilder()
                .withId(createMarkerId(issue))
                .withTitle(issue.getMessage())
                .withDescription(labelProvider.getSourceCodeDescription(sourceBuild, issue))
                .withIcon(labelProvider.getSmallIconUrl())
                .withSeverity(asMarkerSeverity(issue.getSeverity()))
                .withLineStart(issue.getLineStart())
                .withLineEnd(issue.getLineEnd())
                .withColumnStart(issue.getColumnStart())
                .withColumnEnd(issue.getColumnEnd())
                .withData("severity", issue.getSeverity().getName().toLowerCase(Locale.ENGLISH))
                .withData("state", toolIssue.state().name().toLowerCase(Locale.ENGLISH))
                .withData("tool", toolIssue.action().getId());

        builder.withBadge(
                LocalizedSeverity.getLocalizedString(issue.getSeverity()),
                LocalizedSeverity.getLongLocalizedString(issue.getSeverity()));
        if (toolIssue.state() == IssueState.NEW) {
            builder.withBadge(Messages.SourceModel_New(), Messages.SourceModel_New_Tooltip());
        } else if (toolIssue.state() == IssueState.FIXED) {
            builder.withBadge(Messages.SourceModel_Fixed(), Messages.SourceModel_Fixed_Tooltip());
        }
        builder.withBadge(labelProvider.getName(), Messages.SourceModel_Tool_Tooltip());
        var categoryAndType = Stream.of(issue.getCategory(), issue.getType())
                .filter(SourceModel::isDefined)
                .collect(Collectors.joining(" · "));
        if (!categoryAndType.isEmpty()) {
            builder.withBadge(categoryAndType, Messages.SourceModel_CategoryAndType_Tooltip());
        }
        addBlame(builder, issue, toolIssue.action().getResult().getBlames());

        return builder.build();
    }

    private void addBlame(final MarkerBuilder builder, final Issue issue, final Blames blames) {
        var blame = new Blame(issue, blames);
        if (isDefined(blame.getCommit())) {
            var commit = StringUtils.left(blame.getCommit(), COMMIT_LENGTH);
            builder.withBadge(
                    Messages.SourceModel_Blame(blame.getAuthorName(), commit),
                    Messages.SourceModel_Blame_Tooltip(
                            blame.getCommit(), blame.getAuthorName(), blame.getAuthorEmail()));
        }
    }

    private static boolean isDefined(final String value) {
        return StringUtils.isNotBlank(value) && !UNDEFINED.equals(value);
    }

    /**
     * Maps the severity of an issue to the severity of a marker: errors and warnings with high severity are shown
     * as errors, warnings with low severity as information.
     *
     * @param severity
     *         the severity of the issue
     *
     * @return the severity of the marker
     */
    static Marker.Severity asMarkerSeverity(final Severity severity) {
        if (Severity.ERROR.equals(severity) || Severity.WARNING_HIGH.equals(severity)) {
            return Marker.Severity.ERROR;
        }
        if (Severity.WARNING_LOW.equals(severity)) {
            return Marker.Severity.INFO;
        }
        return Marker.Severity.WARNING;
    }

    private static String createMarkerId(final Issue issue) {
        return MARKER_ID_PREFIX + issue.getId();
    }

    /**
     * The state of an issue in the source code view.
     */
    enum IssueState {
        /** The issue has been introduced in the current build. */
        NEW,
        /** The issue has been reported in the reference build as well. */
        OUTSTANDING,
        /** The issue has been reported in the reference build but has been fixed in the current build. */
        FIXED
    }

    /**
     * An issue together with the results of the tool that reported the issue.
     *
     * @param issue
     *         the issue
     * @param action
     *         the results of the tool that reported the issue: the results of the current build for new and
     *         outstanding issues, the results of the reference build for fixed issues
     * @param state
     *         the state of the issue
     */
    record ToolIssue(Issue issue, ResultAction action, IssueState state) {}
}

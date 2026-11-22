package io.jenkins.plugins.analysis.core.model;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Severity;
import hudson.model.Run;
import io.jenkins.plugins.analysis.core.model.SourceModel.IssueState;
import io.jenkins.plugins.analysis.core.util.Blame;
import io.jenkins.plugins.analysis.core.util.LocalizedSeverity;
import io.jenkins.plugins.forensics.blame.Blames;
import io.jenkins.plugins.prism.Marker;
import io.jenkins.plugins.prism.Marker.MarkerBuilder;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;

/**
 * Creates the {@link Marker markers} of issues for the source code view of the Prism API plugin. A marker shows the
 * message and the description of the issue. The severity, the state (new or fixed), the tool, the category and type,
 * and the author and commit of the affected line (if blames are available) are shown as badges. The severity, the
 * state, and the tool are additionally provided as data of the marker, so client side scripts can filter the markers.
 * The color of the marker is derived from the severity of the issue: errors and warnings with high severity are shown
 * as errors, warnings with low severity as information.
 *
 * @author Ullrich Hafner
 */
class IssueMarkerFactory {
    private static final String MARKER_ID_PREFIX = "issue-";
    private static final String UNDEFINED = "-";
    private static final int COMMIT_LENGTH = 7;

    /**
     * Creates the marker for the specified issue.
     *
     * @param build
     *         the build that contains the affected file of the issue
     * @param issue
     *         the issue to create the marker for
     * @param state
     *         the state of the issue
     * @param toolId
     *         the ID of the tool that reported the issue
     * @param labelProvider
     *         the label provider of the tool that reported the issue
     * @param blames
     *         the blames of the affected files
     *
     * @return the marker
     */
    Marker create(
            final Run<?, ?> build,
            final Issue issue,
            final IssueState state,
            final String toolId,
            final StaticAnalysisLabelProvider labelProvider,
            final Blames blames) {
        var builder = new MarkerBuilder()
                .withId(createMarkerId(issue))
                .withTitle(issue.getMessage())
                .withDescription(labelProvider.getSourceCodeDescription(build, issue))
                .withIcon(labelProvider.getSmallIconUrl())
                .withSeverity(asMarkerSeverity(issue.getSeverity()))
                .withLineStart(issue.getLineStart())
                .withLineEnd(issue.getLineEnd())
                .withColumnStart(issue.getColumnStart())
                .withColumnEnd(issue.getColumnEnd())
                .withData("severity", issue.getSeverity().getName().toLowerCase(Locale.ENGLISH))
                .withData("state", state.name().toLowerCase(Locale.ENGLISH))
                .withData("tool", toolId);

        builder.withBadge(
                LocalizedSeverity.getLocalizedString(issue.getSeverity()),
                LocalizedSeverity.getLongLocalizedString(issue.getSeverity()));
        if (state == IssueState.NEW) {
            builder.withBadge(Messages.SourceModel_New(), Messages.SourceModel_New_Tooltip());
        } else if (state == IssueState.FIXED) {
            builder.withBadge(Messages.SourceModel_Fixed(), Messages.SourceModel_Fixed_Tooltip());
        }
        builder.withBadge(labelProvider.getName(), Messages.SourceModel_Tool_Tooltip());
        var categoryAndType = Stream.of(issue.getCategory(), issue.getType())
                .filter(IssueMarkerFactory::isDefined)
                .collect(Collectors.joining(" · "));
        if (!categoryAndType.isEmpty()) {
            builder.withBadge(categoryAndType, Messages.SourceModel_CategoryAndType_Tooltip());
        }
        addBlame(builder, issue, blames);

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
     * Returns the ID of the marker of the specified issue.
     *
     * @param issue
     *         the issue
     *
     * @return the ID of the marker
     */
    static String createMarkerId(final Issue issue) {
        return MARKER_ID_PREFIX + issue.getId();
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
}

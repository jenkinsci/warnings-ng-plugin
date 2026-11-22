package io.jenkins.plugins.analysis.core.model;

import io.jenkins.plugins.analysis.core.model.OverviewModel.SeveritySegment;
import io.jenkins.plugins.util.QualityGateStatus;
import java.util.List;

/**
 * Server side model of the Warnings widget on the build page. The widget is a compact version of the {@link
 * OverviewModel overview} of the Warnings tab and answers the question: "Did this build make the static analysis
 * results worse, and does that block anything?". It shows (from top to bottom):
 * <ul>
 *     <li>the overall status of the quality gates (or the status of the new warnings if there are no quality
 *     gates), and a link to the log page if the tools reported errors,</li>
 *     <li>the total, new and fixed warnings of all results, and the reference build they are compared to, and</li>
 *     <li>the distribution of all warnings by severity.</li>
 * </ul>
 *
 * <p>
 * The individual tools are not listed, since the cards of the build page have a fixed height: the overview lists all
 * tools. All values are delegated to the {@link OverviewModel}, so the widget and the overview always show the same
 * numbers. The layout is defined in the Jelly view {@code RunTab/widget.jelly}.
 * </p>
 */
public class Widget {

    private final OverviewModel overview;

    /**
     * Creates a new widget for the specified overview.
     *
     * @param overview
     *         the overview of the Warnings tab that provides all values
     */
    Widget(final OverviewModel overview) {
        this.overview = overview;
    }

    /**
     * Returns the symbol that is shown in front of the summary lines if there are no quality gates. Without quality
     * gates, the status is derived from the new warnings: an unstable symbol if there are new warnings, and a success
     * symbol otherwise. The symbols are the same build status symbols that are used for the quality gates.
     *
     * @return the symbol
     */
    public String getSymbol() {
        return overview.getStatusIcon(getNewSize() > 0 ? QualityGateStatus.WARNING : QualityGateStatus.PASSED);
    }

    /**
     * Returns the summary lines that are shown if there are no quality gates. The first line states whether the build
     * introduced new warnings, the second line explains the status.
     *
     * @return the summary lines
     */
    public List<String> getLines() {
        if (getTotalSize() == 0) {
            return List.of(Messages.Widget_AllClear(), Messages.Widget_NoWarningsForThisBuild());
        }
        if (getNewSize() > 0) {
            return List.of(Messages.Widget_NewWarnings(getNewSize()), Messages.Widget_NoQualityGates());
        }
        return List.of(Messages.Widget_NoNewWarnings(), Messages.Widget_NoQualityGates());
    }

    /**
     * Returns whether at least one of the results has an active quality gate.
     *
     * @return {@code true} if there are quality gates, {@code false} otherwise
     */
    public boolean hasQualityGates() {
        return overview.hasQualityGates();
    }

    /**
     * Returns the overall status of the quality gates, i.e., the worst status of all results.
     *
     * @return the overall quality gate status
     */
    public QualityGateStatus getQualityGateStatus() {
        return overview.getQualityGateStatus();
    }

    /**
     * Returns the symbol that visualizes the overall status of the quality gates (see
     * {@link OverviewModel#getStatusIcon(QualityGateStatus)}).
     *
     * @return the symbol
     */
    public String getQualityGateIcon() {
        return overview.getStatusIcon(getQualityGateStatus());
    }

    /**
     * Returns a localized summary of the quality gates, e.g. "1 of 3 quality gates missed".
     *
     * @return the summary
     */
    public String getQualityGateSummary() {
        int missed = overview.getMissedQualityGatesSize();
        int total = overview.getQualityGatesSize();
        if (missed == 0) {
            return Messages.Widget_QualityGates_Passed(total);
        }
        return Messages.Widget_QualityGates_Missed(missed, total);
    }

    /**
     * Returns the total number of warnings of all results.
     *
     * @return the total number of warnings
     */
    public int getTotalSize() {
        return overview.getTotalSize();
    }

    /**
     * Returns the number of new warnings of all results.
     *
     * @return the number of new warnings
     */
    public int getNewSize() {
        return overview.getNewSize();
    }

    /**
     * Returns the number of fixed warnings of all results.
     *
     * @return the number of fixed warnings
     */
    public int getFixedSize() {
        return overview.getFixedSize();
    }

    /**
     * Returns whether there is a reference build. Without a reference build, the new and fixed warnings are not
     * highlighted.
     *
     * @return {@code true} if there is a reference build, {@code false} otherwise
     */
    public boolean hasReferenceBuild() {
        return overview.hasReferenceBuild();
    }

    /**
     * Returns the caption below the metrics that names the reference build, e.g. "compared to #48" or "compared to
     * #36 (CPD: #2)" if some results use a different reference build.
     *
     * @return the caption
     */
    public String getComparisonCaption() {
        return overview.getComparisonCaption();
    }

    /**
     * Returns the URL of the reference build that is used by most of the results (relative to the Jenkins root), or
     * an empty string if there is no reference build.
     *
     * @return the URL of the reference build
     */
    public String getReferenceBuildUrl() {
        return overview.getReferenceBuildUrl();
    }

    /**
     * Returns a tooltip that lists the reference build of each result.
     *
     * @return the tooltip
     */
    public String getReferenceBuildTooltip() {
        return overview.getReferenceBuildTooltip();
    }

    /**
     * Returns the distribution of all warnings by severity. Used to render the severity bar.
     *
     * @return the segments, ordered from the most to the least important severity
     */
    public List<SeveritySegment> getTotalSegments() {
        return overview.getTotalSegments();
    }

    /**
     * Returns the number of error messages that have been recorded for all results.
     *
     * @return the number of error messages
     */
    public int getErrorMessagesSize() {
        return overview.getErrorMessagesSize();
    }

    /**
     * Returns the URL of the log page that shows the error messages, relative to the Warnings tab. The first result
     * with errors is preselected.
     *
     * @return the URL of the log page
     */
    public String getLogUrl() {
        return overview.getActions().stream()
                .filter(action -> overview.getErrorMessagesSizeOf(action) > 0)
                .findFirst()
                .map(overview::getLogUrlOf)
                .orElse(LogModel.URL);
    }
}

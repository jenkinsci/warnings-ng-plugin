package io.jenkins.plugins.analysis.core.model;

import io.jenkins.plugins.analysis.core.model.OverviewModel.SeveritySegment;
import io.jenkins.plugins.util.QualityGateStatus;
import java.util.Comparator;
import java.util.List;

/**
 * Server side model of the Warnings widget on the build page. The widget is a compact version of the {@link
 * OverviewModel overview} of the Warnings tab and answers the question: "Did this build make the static analysis
 * results worse, and does that block anything?". It shows (from top to bottom):
 * <ul>
 *     <li>the overall status of the quality gates (or a summary line if there are no quality gates),</li>
 *     <li>the total, new and fixed warnings of all results,</li>
 *     <li>the distribution of all warnings by severity,</li>
 *     <li>a pill for each result that has warnings or missed its quality gates (only if there is more than one
 *     result), colored by the status of its quality gates, and</li>
 *     <li>a link to the log page if the tools reported errors.</li>
 * </ul>
 *
 * <p>
 * All values are delegated to the {@link OverviewModel}, so the widget and the overview always show the same
 * numbers. The layout is defined in the Jelly view {@code RunTab/widget.jelly}.
 * </p>
 */
public class Widget {
    private static final String WARNING_SYMBOL = "symbol-warning-outline plugin-ionicons-api";
    private static final String CLEAN_SYMBOL = "symbol-status-blue";
    private static final String ERROR_STATUS = "error";
    private static final String WARNING_STATUS = "warning";
    private static final String NO_STATUS = "none";

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
     * Returns the symbol that is shown in front of the summary lines if there are no quality gates.
     *
     * @return the symbol
     */
    public String getSymbol() {
        return hasWarnings() ? WARNING_SYMBOL : CLEAN_SYMBOL;
    }

    /**
     * Returns the summary lines that are shown if there are no quality gates.
     *
     * @return the summary lines
     */
    public List<String> getLines() {
        if (hasWarnings()) {
            return List.of(Messages.Widget_WarningsForThisBuild(getTotalSize()));
        }
        return List.of(Messages.Widget_AllClear(), Messages.Widget_NoWarningsForThisBuild());
    }

    private boolean hasWarnings() {
        return getTotalSize() > 0;
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
     * Returns the distribution of all warnings by severity. Used to render the severity bar.
     *
     * @return the segments, ordered from the most to the least important severity
     */
    public List<SeveritySegment> getTotalSegments() {
        return overview.getTotalSegments();
    }

    /**
     * Returns whether the widget shows a pill for each result. If there is only one result, the pills are skipped,
     * since the severity bar already shows the details of this result.
     *
     * @return {@code true} if the pills should be shown, {@code false} otherwise
     */
    public boolean hasPills() {
        return overview.getActions().size() > 1 && !getResults().isEmpty();
    }

    /**
     * Returns the results that get a pill: all results with warnings or with missed quality gates. The results are
     * sorted so that the most relevant results come first: results with missed quality gates (worst status first),
     * then results with the most new warnings, then results with the most warnings. Passed quality gates do not
     * change the order.
     *
     * @return the results, sorted by relevance
     */
    public List<ResultAction> getResults() {
        return overview.getActions().stream()
                .filter(action -> action.getResult().getTotalSize() > 0 || hasMissedQualityGate(action))
                .sorted(Comparator.comparingInt(this::getStatusRank)
                        .thenComparingInt(this::getNewSizeOf)
                        .thenComparingInt(action -> action.getResult().getTotalSize())
                        .reversed()
                        .thenComparing(ResultAction::getDisplayName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    /**
     * Returns the number of new warnings of the specified result.
     *
     * @param action
     *         the result
     *
     * @return the number of new warnings
     */
    public int getNewSizeOf(final ResultAction action) {
        return action.getResult().getNewSize();
    }

    /**
     * Returns the CSS modifier that defines the color of the pill of the specified result: {@code error} if a
     * quality gate failed the step or the build, {@code warning} if a quality gate marked the step or the build as
     * unstable, and {@code none} otherwise.
     *
     * @param action
     *         the result
     *
     * @return the CSS modifier
     */
    public String getStatusModifier(final ResultAction action) {
        return switch (getQualityGateStatusOf(action)) {
            case ERROR, FAILED -> ERROR_STATUS;
            case NOTE, WARNING -> WARNING_STATUS;
            default -> NO_STATUS;
        };
    }

    /**
     * Returns the rank of the quality gate status of the specified result. Results without quality gates and
     * results with passed quality gates have the same rank.
     *
     * @param action
     *         the result
     *
     * @return the rank: 2 for failed, 1 for unstable, and 0 for all other results
     */
    private int getStatusRank(final ResultAction action) {
        return switch (getStatusModifier(action)) {
            case ERROR_STATUS -> 2;
            case WARNING_STATUS -> 1;
            default -> 0;
        };
    }

    private boolean hasMissedQualityGate(final ResultAction action) {
        return !NO_STATUS.equals(getStatusModifier(action));
    }

    private QualityGateStatus getQualityGateStatusOf(final ResultAction action) {
        return action.getQualityGateResult().getOverallStatus();
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

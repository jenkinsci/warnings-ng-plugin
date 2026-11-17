package io.jenkins.plugins.analysis.core.model;

import edu.hm.hafner.analysis.Severity;
import hudson.model.Actionable;
import hudson.model.ModelObject;
import io.jenkins.plugins.analysis.core.util.LocalizedSeverity;
import io.jenkins.plugins.util.QualityGate;
import io.jenkins.plugins.util.QualityGate.QualityGateCriticality;
import io.jenkins.plugins.util.QualityGateStatus;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.ToIntFunction;

/**
 * Server side model that provides the data for the "Overview" page of the Warnings tab of a run. The page answers the
 * question: "How many warnings did the static analysis tools report in this build, and did the build pass the
 * quality gates?". It shows the total number of warnings, the new and the fixed warnings of all {@link ResultAction
 * results} of the run, a table with these numbers (and the distribution by severity) for each result, and the
 * results of the quality gates of all results.
 *
 * <p>
 * The page has no client side controls yet. All values are obtained from the {@link AnalysisResult} of the
 * individual actions, nothing is stored separately. The layout is defined in the associated Jelly view
 * {@code index.jelly}.
 * </p>
 *
 * @author Ullrich Hafner
 */
public class OverviewModel implements ModelObject {
    static final String URL = "overview";

    private static final List<Severity> SEVERITIES =
            List.of(Severity.ERROR, Severity.WARNING_HIGH, Severity.WARNING_NORMAL, Severity.WARNING_LOW);

    private static final String ERROR_ICON = "symbol-close-circle-outline plugin-ionicons-api";
    private static final String WARNING_ICON = "symbol-warning-outline plugin-ionicons-api";
    private static final String ERROR_COLOR = "jenkins-!-error-color";
    private static final String WARNING_COLOR = "jenkins-!-warning-color";

    private final Actionable owner;
    private final List<ResultAction> actions;

    /**
     * Creates a new instance of {@link OverviewModel}.
     *
     * @param owner
     *         the run that owns the results
     * @param actions
     *         the results of the run, in the order they should be shown
     */
    OverviewModel(final Actionable owner, final List<ResultAction> actions) {
        this.owner = owner;
        this.actions = List.copyOf(actions);
    }

    @Override
    public String getDisplayName() {
        return Messages.OverviewModel_DisplayName();
    }

    /**
     * Returns the URL of this page, relative to the Warnings tab.
     *
     * @return the URL name
     */
    public String getUrlName() {
        return URL;
    }

    /**
     * Returns the run that owns the results. Required by {@code l:run-subpage}.
     *
     * @return the run
     */
    public Actionable getObject() {
        return owner;
    }

    /**
     * Returns the Warnings tab of the run. Used to render the app bar and the side panel.
     *
     * @return the tab
     */
    public RunTab getTab() {
        return new RunTab(owner);
    }

    /**
     * Returns the results of the run (one for each static analysis tool or aggregated result).
     *
     * @return the results
     */
    public List<ResultAction> getActions() {
        return actions;
    }

    /**
     * Returns the total number of warnings of all results.
     *
     * @return the total number of warnings
     */
    public int getTotalSize() {
        return sum(AnalysisResult::getTotalSize);
    }

    /**
     * Returns the number of new warnings of all results.
     *
     * @return the number of new warnings
     */
    public int getNewSize() {
        return sum(AnalysisResult::getNewSize);
    }

    /**
     * Returns the number of fixed warnings of all results.
     *
     * @return the number of fixed warnings
     */
    public int getFixedSize() {
        return sum(AnalysisResult::getFixedSize);
    }

    /**
     * Returns the number of warnings of all results with the specified severity.
     *
     * @param severity
     *         the severity to count
     *
     * @return the number of warnings with the specified severity
     */
    public int getTotalSizeOf(final Severity severity) {
        return sum(result -> result.getTotalSizeOf(severity));
    }

    /**
     * Returns the number of new warnings of all results with the specified severity.
     *
     * @param severity
     *         the severity to count
     *
     * @return the number of new warnings with the specified severity
     */
    public int getNewSizeOf(final Severity severity) {
        return sum(result -> result.getNewSizeOf(severity));
    }

    /**
     * Returns the distribution of all warnings by severity. Used to render the segmented severity bar of the "Total"
     * card. Severities without warnings are skipped.
     *
     * @return the segments, ordered from the most to the least important severity
     */
    public List<SeveritySegment> getTotalSegments() {
        return createSegments(this::getTotalSizeOf);
    }

    /**
     * Returns the distribution of the new warnings by severity. Used to render the segmented severity bar of the
     * "New" card. Severities without new warnings are skipped.
     *
     * @return the segments, ordered from the most to the least important severity
     */
    public List<SeveritySegment> getNewSegments() {
        return createSegments(this::getNewSizeOf);
    }

    private List<SeveritySegment> createSegments(final ToIntFunction<Severity> sizeOf) {
        return SEVERITIES.stream()
                .filter(severity -> sizeOf.applyAsInt(severity) > 0)
                .map(severity -> new SeveritySegment(severity, sizeOf.applyAsInt(severity)))
                .toList();
    }

    private int sum(final ToIntFunction<AnalysisResult> size) {
        return actions.stream().map(ResultAction::getResult).mapToInt(size).sum();
    }

    /**
     * Returns the severities that have at least one warning in one of the results. Only these severities get a
     * column in the results table.
     *
     * @return the severities with warnings, ordered from the most to the least important severity
     */
    public List<Severity> getSeverities() {
        return SEVERITIES.stream()
                .filter(severity -> getTotalSizeOf(severity) > 0)
                .toList();
    }

    /**
     * Returns the localized name of the specified severity.
     *
     * @param severity
     *         the severity
     *
     * @return the localized name
     */
    public String getSeverityLabel(final Severity severity) {
        return LocalizedSeverity.getLocalizedString(severity);
    }

    /**
     * Returns the results that have at least one active quality gate.
     *
     * @return the results with active quality gates
     */
    public List<ResultAction> getQualityGateActions() {
        return actions.stream()
                .filter(action -> !action.getQualityGateResult().isInactive())
                .toList();
    }

    /**
     * Returns whether at least one of the results has an active quality gate.
     *
     * @return {@code true} if there are active quality gates, {@code false} otherwise
     */
    public boolean hasQualityGates() {
        return !getQualityGateActions().isEmpty();
    }

    /**
     * Returns the overall quality gate status of the run, i.e., the worst status of all results.
     *
     * @return the overall quality gate status, or {@link QualityGateStatus#INACTIVE} if there are no quality gates
     */
    public QualityGateStatus getQualityGateStatus() {
        return actions.stream()
                .map(action -> action.getQualityGateResult().getOverallStatus())
                .max(Comparator.naturalOrder())
                .orElse(QualityGateStatus.INACTIVE);
    }

    /**
     * Returns the icon that visualizes the criticality of the specified quality gate: an error icon for quality gates
     * that fail the step or build, and a warning icon for quality gates that mark the step or build as unstable.
     *
     * @param qualityGate
     *         the quality gate
     *
     * @return the icon
     */
    public String getCriticalityIcon(final QualityGate qualityGate) {
        return isFailing(qualityGate) ? ERROR_ICON : WARNING_ICON;
    }

    /**
     * Returns the CSS class that defines the color of the criticality icon of the specified quality gate (see
     * {@link #getCriticalityIcon(QualityGate)}).
     *
     * @param qualityGate
     *         the quality gate
     *
     * @return the CSS class of the color
     */
    public String getCriticalityColor(final QualityGate qualityGate) {
        return isFailing(qualityGate) ? ERROR_COLOR : WARNING_COLOR;
    }

    /**
     * Returns a localized description of the criticality of the specified quality gate, i.e., what happens with the
     * step and the build if the quality gate is missed. Shown as tooltip of the criticality icon.
     *
     * @param qualityGate
     *         the quality gate
     *
     * @return the localized description
     */
    public String getCriticalityDescription(final QualityGate qualityGate) {
        return switch (qualityGate.getCriticality()) {
            case NOTE -> Messages.OverviewModel_Criticality_UnstableStage();
            case UNSTABLE -> Messages.OverviewModel_Criticality_UnstableRun();
            case ERROR -> Messages.OverviewModel_Criticality_FailureStage();
            case FAILURE -> Messages.OverviewModel_Criticality_FailureRun();
        };
    }

    private static boolean isFailing(final QualityGate qualityGate) {
        var criticality = qualityGate.getCriticality();
        return criticality == QualityGateCriticality.ERROR || criticality == QualityGateCriticality.FAILURE;
    }

    /**
     * A segment of the severity bar: the number of warnings of a given severity.
     */
    public static class SeveritySegment {
        private final Severity severity;
        private final int count;

        /**
         * Creates a new segment.
         *
         * @param severity
         *         the severity of the warnings
         * @param count
         *         the number of warnings with this severity
         */
        SeveritySegment(final Severity severity, final int count) {
            this.severity = severity;
            this.count = count;
        }

        /**
         * Returns the number of warnings with this severity. Used as relative width of the segment. Note that this
         * property must not be named {@code size}, since {@code size} is a reserved word in JEXL (Jelly expressions).
         *
         * @return the number of warnings
         */
        public int getCount() {
            return count;
        }

        /**
         * Returns the localized name of the severity.
         *
         * @return the localized name
         */
        public String getLabel() {
            return LocalizedSeverity.getLocalizedString(severity);
        }

        /**
         * Returns the BEM modifier of the CSS class that defines the color of the segment, e.g. {@code error} or
         * {@code high}.
         *
         * @return the CSS modifier
         */
        public String getModifier() {
            return severity.getName().toLowerCase(Locale.ENGLISH);
        }
    }
}

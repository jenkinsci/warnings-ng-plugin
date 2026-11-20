package io.jenkins.plugins.analysis.core.model;

import edu.hm.hafner.analysis.Severity;
import hudson.model.Actionable;
import hudson.model.ModelObject;
import hudson.model.Run;
import io.jenkins.plugins.analysis.core.util.LocalizedSeverity;
import io.jenkins.plugins.util.QualityGate;
import io.jenkins.plugins.util.QualityGate.QualityGateCriticality;
import io.jenkins.plugins.util.QualityGateResult.QualityGateResultItem;
import io.jenkins.plugins.util.QualityGateStatus;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

/**
 * Server side model that provides the data for the "Overview" page of the Warnings tab of a run. The page answers the
 * question: "How many warnings did the static analysis tools report in this build, and did the build pass the
 * quality gates?". It shows the total number of warnings, the new and the fixed warnings of all {@link ResultAction
 * results} of the run, a table with these numbers (and the distribution by severity) for each result, and the
 * results of the quality gates of all results. If a tool reported error messages (e.g., a report file that could
 * not be found or parsed), the table also shows the number of these errors and links to the corresponding tool on
 * the {@link LogModel log page}.
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
    private List<Run<?, ?>> referenceBuilds = List.of();
    private boolean isReferenceBuildResolved;

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

    /**
     * Returns the number of error messages that have been recorded for all results.
     *
     * @return the number of error messages
     */
    public int getErrorMessagesSize() {
        return actions.stream().mapToInt(this::getErrorMessagesSizeOf).sum();
    }

    /**
     * Returns the number of error messages that have been recorded for the specified result (e.g., a report file
     * that could not be found or parsed).
     *
     * @param action
     *         the result
     *
     * @return the number of error messages
     */
    public int getErrorMessagesSizeOf(final ResultAction action) {
        return action.getResult().getErrorMessages().size();
    }

    /**
     * Returns whether at least one of the results has recorded error messages. Only in this case the results table
     * gets a column with the number of errors.
     *
     * @return {@code true} if there are error messages, {@code false} otherwise
     */
    public boolean hasErrorMessages() {
        return getErrorMessagesSize() > 0;
    }

    /**
     * Returns the URL of the log page that shows the messages of the specified result, relative to the Warnings tab.
     * The tool is preselected using the fragment of the URL.
     *
     * @param action
     *         the result
     *
     * @return the URL of the log page
     */
    public String getLogUrlOf(final ResultAction action) {
        return LogModel.URL + "#tool=" + action.getId();
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
     * Returns the reference builds of all results. Usually, all results use the same reference build, so the list
     * contains at most one element. Results without a reference build are skipped. The builds are resolved only
     * once, since each lookup requires a search in Jenkins.
     *
     * @return the distinct reference builds
     */
    public List<Run<?, ?>> getReferenceBuilds() {
        if (!isReferenceBuildResolved) {
            referenceBuilds = findReferenceBuilds();
            isReferenceBuildResolved = true;
        }
        return referenceBuilds;
    }

    private List<Run<?, ?>> findReferenceBuilds() {
        return actions.stream()
                .map(action -> action.getResult().getReferenceBuild())
                .flatMap(Optional::stream)
                .distinct()
                .toList();
    }

    /**
     * Returns whether at least one of the results has a reference build. Without a reference build, the new and fixed
     * warnings are not meaningful.
     *
     * @return {@code true} if there is a reference build, {@code false} otherwise
     */
    public boolean hasReferenceBuild() {
        return !getReferenceBuilds().isEmpty();
    }

    /**
     * Returns the name of the reference build, e.g. {@code #48}. If the reference build belongs to another job (e.g.,
     * the target branch of a pull request), the full name of the build is returned, e.g. {@code main #48}. If the
     * results use different reference builds, a localized text with the number of reference builds is returned.
     *
     * @return the name of the reference build, or an empty string if there is no reference build
     */
    public String getReferenceBuildName() {
        var builds = getReferenceBuilds();
        if (builds.isEmpty()) {
            return "";
        }
        if (builds.size() > 1) {
            return Messages.OverviewModel_ReferenceBuilds(builds.size());
        }
        return getNameOf(builds.get(0));
    }

    private String getNameOf(final Run<?, ?> build) {
        if (owner instanceof Run<?, ?> run && run.getParent() == build.getParent()) {
            return build.getDisplayName();
        }
        return build.getFullDisplayName();
    }

    /**
     * Returns the text of the reference build link in the app bar, e.g. "Reference build #48".
     *
     * @return the text of the reference build link
     */
    public String getReferenceBuildText() {
        if (hasReferenceBuild()) {
            return Messages.OverviewModel_ReferenceBuild(getReferenceBuildName());
        }
        return Messages.OverviewModel_NoReferenceBuild();
    }

    /**
     * Returns the URL of the reference build (relative to the Jenkins root). If there is no reference build or if
     * the results use different reference builds, an empty string is returned, since there is no single target.
     *
     * @return the URL of the reference build, or an empty string
     */
    public String getReferenceBuildUrl() {
        var builds = getReferenceBuilds();
        if (builds.size() == 1) {
            return builds.get(0).getUrl();
        }
        return "";
    }

    /**
     * Returns a tooltip that lists the reference build of each result, e.g. "CheckStyle: #48, PMD: #47". Used if the
     * results use different reference builds.
     *
     * @return the tooltip
     */
    public String getReferenceBuildTooltip() {
        return actions.stream()
                .map(action -> action.getDisplayName() + ": "
                        + action.getResult()
                                .getReferenceBuild()
                                .map(this::getNameOf)
                                .orElse(Messages.OverviewModel_NoReferenceBuild()))
                .collect(Collectors.joining(", "));
    }

    /**
     * Returns the caption of the new and fixed warnings that names the reference build, e.g. "compared to #48".
     *
     * @return the caption
     */
    public String getComparisonCaption() {
        if (hasReferenceBuild()) {
            return Messages.OverviewModel_ComparedTo(getReferenceBuildName());
        }
        return Messages.OverviewModel_NoReferenceBuild();
    }

    /**
     * Returns the number of quality gates of all results.
     *
     * @return the number of quality gates
     */
    public int getQualityGatesSize() {
        return countQualityGates(item -> true);
    }

    /**
     * Returns the number of quality gates of all results that have been missed, i.e., whose status is not
     * successful.
     *
     * @return the number of missed quality gates
     */
    public int getMissedQualityGatesSize() {
        return countQualityGates(item -> !item.getStatus().isSuccessful());
    }

    private int countQualityGates(final Predicate<QualityGateResultItem> filter) {
        return (int) actions.stream()
                .flatMap(action -> action.getQualityGateResult().getResultItems().stream())
                .filter(filter)
                .count();
    }

    /**
     * Returns the symbol that visualizes the specified quality gate status. The symbols are the build status symbols
     * of Jenkins (see the icon legend), so that a quality gate status looks like the build result it causes: passed
     * like a successful build, note and warning like an unstable build, and error and failed like a failed build.
     *
     * @param status
     *         the quality gate status
     *
     * @return the symbol
     */
    public String getStatusIcon(final QualityGateStatus status) {
        return switch (status) {
            case PASSED -> "symbol-status-blue";
            case NOTE, WARNING -> "symbol-status-yellow";
            case ERROR, FAILED -> "symbol-status-red";
            default -> "symbol-status-nobuilt";
        };
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

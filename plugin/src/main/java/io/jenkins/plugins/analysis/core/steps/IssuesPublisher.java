package io.jenkins.plugins.analysis.core.steps;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssuesInModifiedCodeMarker;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.util.FilteredLog;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import hudson.model.Result;
import hudson.model.Run;

import io.jenkins.plugins.analysis.core.model.AggregationAction;
import io.jenkins.plugins.analysis.core.model.AnalysisHistory;
import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.ByIdResultSelector;
import io.jenkins.plugins.analysis.core.model.DeltaReport;
import io.jenkins.plugins.analysis.core.model.QualityGateEvaluationMode;
import io.jenkins.plugins.analysis.core.model.ResetReferenceAction;
import io.jenkins.plugins.analysis.core.model.ResultAction;
import io.jenkins.plugins.analysis.core.model.ResultSelector;
import io.jenkins.plugins.analysis.core.util.HealthDescriptor;
import io.jenkins.plugins.analysis.core.util.TrendChartType;
import io.jenkins.plugins.analysis.core.util.WarningsQualityGate;
import io.jenkins.plugins.analysis.core.util.WarningsQualityGateEvaluator;
import io.jenkins.plugins.forensics.delta.DeltaCalculator;
import io.jenkins.plugins.forensics.delta.FileChanges;
import io.jenkins.plugins.forensics.reference.ReferenceBuild;
import io.jenkins.plugins.forensics.reference.ReferenceFinder;
import io.jenkins.plugins.util.LogHandler;
import io.jenkins.plugins.util.QualityGateResult;
import io.jenkins.plugins.util.ResultHandler;

import static io.jenkins.plugins.analysis.core.model.QualityGateEvaluationMode.*;

/**
 * Publishes issues: Stores the created issues in an {@link AnalysisResult}. The result is attached to the {@link Run}
 * by registering a {@link ResultAction}.
 *
 * @author Ullrich Hafner
 */
@SuppressWarnings({"checkstyle:ClassFanOutComplexity", "checkstyle:ClassDataAbstractionCoupling"})
class IssuesPublisher {
    private final AnnotatedReport report;
    private final Run<?, ?> run;
    private final DeltaCalculator deltaCalculator;
    private final HealthDescriptor healthDescriptor;
    private final String name;
    private final String icon;
    private final Charset sourceCodeEncoding;
    private final List<WarningsQualityGate> qualityGates;
    private final QualityGateEvaluationMode qualityGateEvaluationMode;
    private final LogHandler logger;
    private final ResultHandler notifier;
    private final boolean failOnErrors;

    @SuppressWarnings("ParameterNumber")
    IssuesPublisher(final Run<?, ?> run, final AnnotatedReport report, final DeltaCalculator deltaCalculator,
            final HealthDescriptor healthDescriptor, final List<WarningsQualityGate> qualityGates,
            final String name, final String icon, final boolean ignoreQualityGate, final Charset sourceCodeEncoding,
            final LogHandler logger, final ResultHandler notifier, final boolean failOnErrors) {
        this.report = report;
        this.run = run;
        this.deltaCalculator = deltaCalculator;
        this.healthDescriptor = healthDescriptor;
        this.name = name;
        this.icon = icon;
        this.sourceCodeEncoding = sourceCodeEncoding;
        this.qualityGates = qualityGates;
        qualityGateEvaluationMode = ignoreQualityGate ? IGNORE_QUALITY_GATE : SUCCESSFUL_QUALITY_GATE;
        this.logger = logger;
        this.notifier = notifier;
        this.failOnErrors = failOnErrors;
    }

    private String getId() {
        return report.getId();
    }

    /**
     * Creates a new {@link AnalysisResult} and attaches the result in a {@link ResultAction} that is registered with
     * the current run.
     *
     * @param trendChartType
     *         the chart to show
     *
     * @return the created result action
     */
    ResultAction attachAction(final TrendChartType trendChartType) {
        var issues = report.getReport();
        var deltaReport = computeDelta(issues);

        var qualityGateResult = evaluateQualityGate(issues, deltaReport);
        reportHealth(issues);

        issues.logInfo("Created analysis result for %d issues (found %d new issues, fixed %d issues)",
                deltaReport.getAllIssues().size(), deltaReport.getNewIssues().size(),
                deltaReport.getFixedIssues().size());

        if (failOnErrors && issues.hasErrors()) {
            issues.logInfo("Failing build because analysis result contains errors");
            run.setResult(Result.FAILURE);
        }

        if (trendChartType == TrendChartType.AGGREGATION_TOOLS) {
            var action = run.getAction(AggregationAction.class);
            if (action == null) {
                run.addAction(new AggregationAction());
            }
        }

        issues.logInfo("Attaching ResultAction with ID '%s' to build '%s'.", getId(), run);
        logger.logInfoMessages(issues.getInfoMessages());
        logger.logErrorMessages(issues.getErrorMessages());

        var result = new AnalysisHistory(run, ensureThatIdIsUnique()).getResult()
                .map(previous -> new AnalysisResult(run, getId(), deltaReport, report.getBlames(),
                        report.getStatistics(), qualityGateResult, report.getSizeOfOrigin(),
                        previous))
                .orElseGet(() -> new AnalysisResult(run, getId(), deltaReport, report.getBlames(),
                        report.getStatistics(), qualityGateResult, report.getSizeOfOrigin()));
        var action = new ResultAction(run, result, healthDescriptor, getId(), name, icon,
                sourceCodeEncoding, trendChartType);
        run.addAction(action);

        if (trendChartType == TrendChartType.TOOLS_AGGREGATION || trendChartType == TrendChartType.AGGREGATION_ONLY) {
            run.addOrReplaceAction(new AggregationAction());
        }

        return action;
    }

    private long count(final Report issues) {
        return issues.stream().filter(Issue::isPartOfModifiedCode).count();
    }

    private DeltaReport computeDelta(final Report issues) {
        var selector = ensureThatIdIsUnique();
        var possibleReferenceBuild = findReferenceBuild(selector, issues);
        if (possibleReferenceBuild.isPresent()) {
            Run<?, ?> build = possibleReferenceBuild.get();
            var resultAction = selector.get(build)
                    .orElseThrow(() -> new IllegalStateException("Reference build does not contain a result action"));

            var deltaReport = new DeltaReport(issues, build, run.getNumber(), resultAction.getResult().getIssues());

            markIssuesInModifiedFiles(build, issues, deltaReport);

            return deltaReport;
        }
        else {
            return new DeltaReport(issues, run.getNumber());
        }
    }

    private void markIssuesInModifiedFiles(final Run<?, ?> referenceBuild, final Report issues, final DeltaReport deltaReport) {
        if (issues.isNotEmpty()) {
            report.logInfo("Detect all issues that are part of modified code");

            var log = new FilteredLog("Errors while computing delta: ");
            var delta = deltaCalculator.calculateDelta(run, referenceBuild, log);
            issues.mergeLogMessages(log);

            if (delta.isPresent()) {
                var changes = delta.get().getFileChangesMap().values().stream()
                        .collect(Collectors.toMap(
                                FileChanges::getFileName,
                                FileChanges::getModifiedLines,
                                (left, right) -> {
                                    left.addAll(right);
                                    return left;
                                }));
                var marker = new IssuesInModifiedCodeMarker();
                marker.markIssuesInModifiedCode(issues, changes);
                report.logInfo("Issues in modified code: %d (new: %d, outstanding: %d)",
                        count(deltaReport.getAllIssues()),
                        count(deltaReport.getNewIssues()),
                        count(deltaReport.getOutstandingIssues()));
            }
            else {
                report.logInfo("No relevant modified code found");
            }
        }
        else {
            report.logInfo("Skip detection of issues in modified code");
        }
    }

    private ResultSelector ensureThatIdIsUnique() {
        var selector = new ByIdResultSelector(getId());
        Optional<ResultAction> other = selector.get(run);
        if (other.isPresent()) {
            throw new IllegalStateException(
                    "ID %s is already used by another action: %s%n".formatted(getId(), other.get()));
        }
        return selector;
    }

    private void reportHealth(final Report filtered) {
        if (healthDescriptor.isEnabled()) {
            if (healthDescriptor.isValid()) {
                filtered.logInfo("Enabling health report (%s)", healthDescriptor);
            }
            else {
                filtered.logInfo("Health report is invalid (%s) - skipping", healthDescriptor);
            }
        }
        else {
            filtered.logInfo("Health report is disabled - skipping");
        }
    }

    private QualityGateResult evaluateQualityGate(final Report issues, final DeltaReport deltaReport) {
        var evaluator = new WarningsQualityGateEvaluator(qualityGates, deltaReport.getStatistics());
        var log = new FilteredLog("Errors while evaluating quality gates:");
        var qualityGateStatus = evaluator.evaluate(notifier, log);
        issues.mergeLogMessages(log);
        return qualityGateStatus;
    }

    private Optional<Run<?, ?>> findReferenceBuild(final ResultSelector selector, final Report issues) {
        Run<?, ?> previous = run.getPreviousCompletedBuild();
        if (previous != null) {
            List<ResetReferenceAction> actions = previous.getActions(ResetReferenceAction.class);
            for (ResetReferenceAction action : actions) {
                if (issues.getId().equals(action.getId())) {
                    issues.logInfo("Resetting reference build, ignoring quality gate result for one build");
                    issues.logInfo("Using reference build '%s' to compute new, fixed, and outstanding issues",
                            previous.getFullDisplayName());
                    return Optional.of(previous);
                }
            }
        }
        
        var log = new FilteredLog("Errors while resolving the reference build:");
        var reference = new ReferenceFinder().findReference(run, log);
        issues.mergeLogMessages(log);

        if (reference.isPresent()) {
            return refineReferenceBasedOnQualityGate(selector, issues, reference.get());
        }
        return Optional.empty();
    }

    @SuppressWarnings({"PMD.CognitiveComplexity", "PMD.AvoidDeeplyNestedIfStmts"})
    private Optional<Run<?, ?>> refineReferenceBasedOnQualityGate(final ResultSelector selector, final Report issues,
            final Run<?, ?> reference) {
        boolean isSkipped = false;
        var gateEvaluationMode = determineQualityGateEvaluationMode(issues);
        for (Run<?, ?> r = reference; r != null; r = r.getPreviousBuild()) {
            var result = r.getResult();
            boolean shouldConsiderBuild = result != null && (gateEvaluationMode == IGNORE_QUALITY_GATE 
                    || result.isBetterOrEqualTo(getRequiredResult()));
            if (shouldConsiderBuild) {
                var displayName = r.getFullDisplayName();
                Optional<ResultAction> action = selector.get(r);
                if (action.isPresent()) {
                    var resultAction = action.get();
                    if (resultAction.isSuccessful()) {
                        issues.logInfo(
                                "Quality gate successful for reference build '%s', using this build as reference",
                                displayName);
                        return Optional.of(r);
                    }
                    if (gateEvaluationMode == IGNORE_QUALITY_GATE) {
                        issues.logInfo(
                                "Quality gate has been missed for reference build '%s', but is configured to be ignored",
                                displayName);
                        return Optional.of(r);
                    }
                    if (!isSkipped) {
                        issues.logInfo("Quality gate failed for reference build '%s', analyzing previous builds",
                                displayName);
                        isSkipped = true;
                    }
                }
                else {
                    if (!isSkipped) {
                        issues.logInfo(
                                "Reference build '%s' does not contain a result action, analyzing previous builds",
                                displayName);
                        isSkipped = true;
                    }
                }
            }
        }
        issues.logInfo("No reference build with successful quality gate found, skipping delta computation");

        return Optional.empty();
    }

    private Result getRequiredResult() {
        var action = run.getAction(ReferenceBuild.class);
        if (action == null) {
            return Result.UNSTABLE;
        }
        return action.getRequiredResult();
    }

    private QualityGateEvaluationMode determineQualityGateEvaluationMode(final Report filtered) {
        return qualityGateEvaluationMode;
    }
}

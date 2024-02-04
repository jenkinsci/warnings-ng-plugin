package io.jenkins.plugins.analysis.core.steps;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Optional;

import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.util.FilteredLog;

import hudson.model.Result;
import hudson.model.Run;

import io.jenkins.plugins.analysis.core.model.AggregationAction;
import io.jenkins.plugins.analysis.core.model.AnalysisHistory;
import io.jenkins.plugins.analysis.core.model.AnalysisHistory.JobResultEvaluationMode;
import io.jenkins.plugins.analysis.core.model.AnalysisHistory.QualityGateEvaluationMode;
import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.ByIdResultSelector;
import io.jenkins.plugins.analysis.core.model.DeltaReport;
import io.jenkins.plugins.analysis.core.model.History;
import io.jenkins.plugins.analysis.core.model.NullAnalysisHistory;
import io.jenkins.plugins.analysis.core.model.ResetReferenceAction;
import io.jenkins.plugins.analysis.core.model.ResultAction;
import io.jenkins.plugins.analysis.core.model.ResultSelector;
import io.jenkins.plugins.analysis.core.util.HealthDescriptor;
import io.jenkins.plugins.analysis.core.util.TrendChartType;
import io.jenkins.plugins.analysis.core.util.WarningsQualityGate;
import io.jenkins.plugins.analysis.core.util.WarningsQualityGateEvaluator;
import io.jenkins.plugins.forensics.reference.ReferenceFinder;
import io.jenkins.plugins.util.LogHandler;
import io.jenkins.plugins.util.QualityGateResult;
import io.jenkins.plugins.util.ResultHandler;

import static io.jenkins.plugins.analysis.core.model.AnalysisHistory.JobResultEvaluationMode.*;
import static io.jenkins.plugins.analysis.core.model.AnalysisHistory.QualityGateEvaluationMode.*;

/**
 * Publishes issues: Stores the created issues in an {@link AnalysisResult}. The result is attached to the {@link Run}
 * by registering a {@link ResultAction}.
 *
 * @author Ullrich Hafner
 */
@SuppressWarnings({"PMD.ExcessiveImports", "checkstyle:ClassFanOutComplexity"})
class IssuesPublisher {
    private final AnnotatedReport report;
    private final Run<?, ?> run;
    private final HealthDescriptor healthDescriptor;
    private final String name;
    private final Charset sourceCodeEncoding;
    private final List<WarningsQualityGate> qualityGates;
    private final QualityGateEvaluationMode qualityGateEvaluationMode;
    private final JobResultEvaluationMode jobResultEvaluationMode;
    private final LogHandler logger;
    private final ResultHandler notifier;
    private final boolean failOnErrors;

    @SuppressWarnings("ParameterNumber")
    IssuesPublisher(final Run<?, ?> run, final AnnotatedReport report,
            final HealthDescriptor healthDescriptor, final List<WarningsQualityGate> qualityGates,
            final String name, final boolean ignoreQualityGate, final boolean ignoreFailedBuilds,
            final Charset sourceCodeEncoding, final LogHandler logger,
            final ResultHandler notifier, final boolean failOnErrors) {
        this.report = report;
        this.run = run;
        this.healthDescriptor = healthDescriptor;
        this.name = name;
        this.sourceCodeEncoding = sourceCodeEncoding;
        this.qualityGates = qualityGates;
        qualityGateEvaluationMode = ignoreQualityGate ? IGNORE_QUALITY_GATE : SUCCESSFUL_QUALITY_GATE;
        jobResultEvaluationMode = ignoreFailedBuilds ? NO_JOB_FAILURE : IGNORE_JOB_RESULT;
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
        ResultSelector selector = ensureThatIdIsUnique();

        Report issues = report.getReport();
        var history = createAnalysisHistory(selector, issues);
        DeltaReport deltaReport;
        if (history.getBuild().isPresent()) {
            deltaReport = new DeltaReport(issues, history.getBuild().get(), run.getNumber(), history.getIssues());
        }
        else {
            deltaReport = new DeltaReport(issues, run.getNumber());
        }
        QualityGateResult qualityGateResult = evaluateQualityGate(issues, deltaReport);
        reportHealth(issues);

        issues.logInfo("Created analysis result for %d issues (found %d new issues, fixed %d issues)",
                deltaReport.getAllIssues().size(), deltaReport.getNewIssues().size(),
                deltaReport.getFixedIssues().size());

        if (failOnErrors && issues.hasErrors()) {
            issues.logInfo("Failing build because analysis result contains errors");
            run.setResult(Result.FAILURE);
        }

        if (trendChartType == TrendChartType.AGGREGATION_TOOLS) {
            AggregationAction action = run.getAction(AggregationAction.class);
            if (action == null) {
                run.addAction(new AggregationAction());
            }
        }

        issues.logInfo("Attaching ResultAction with ID '%s' to build '%s'.", getId(), run);
        logger.logInfoMessages(issues.getInfoMessages());
        logger.logErrorMessages(issues.getErrorMessages());

        AnalysisResult result = new AnalysisHistory(run, selector).getResult()
                .map(previous -> new AnalysisResult(run, getId(), deltaReport, report.getBlames(),
                        report.getStatistics(), qualityGateResult, report.getSizeOfOrigin(),
                        previous))
                .orElseGet(() -> new AnalysisResult(run, getId(), deltaReport, report.getBlames(),
                        report.getStatistics(), qualityGateResult, report.getSizeOfOrigin()));
        ResultAction action
                = new ResultAction(run, result, healthDescriptor, getId(), name, sourceCodeEncoding, trendChartType);
        run.addAction(action);

        if (trendChartType == TrendChartType.TOOLS_AGGREGATION || trendChartType == TrendChartType.AGGREGATION_ONLY) {
            run.addOrReplaceAction(new AggregationAction());
        }

        return action;
    }

    private ResultSelector ensureThatIdIsUnique() {
        ResultSelector selector = new ByIdResultSelector(getId());
        Optional<ResultAction> other = selector.get(run);
        if (other.isPresent()) {
            throw new IllegalStateException(
                    String.format("ID %s is already used by another action: %s%n", getId(), other.get()));
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

    private History createAnalysisHistory(final ResultSelector selector, final Report issues) {
        var reference = findReference(issues);
        if (reference.isPresent()) {
            return new AnalysisHistory(reference.get(), selector,
                    determineQualityGateEvaluationMode(issues), jobResultEvaluationMode);
        }
        else {
            return new NullAnalysisHistory();
        }
    }

    private Optional<Run<?, ?>> findReference(final Report issues) {
        FilteredLog log = new FilteredLog("Errors while resolving the reference build:");
        Optional<Run<?, ?>> reference = new ReferenceFinder().findReference(run, log);
        issues.mergeLogMessages(log);
        return reference;
    }

    private QualityGateEvaluationMode determineQualityGateEvaluationMode(final Report filtered) {
        Run<?, ?> previous = run.getPreviousCompletedBuild();
        if (previous != null) {
            List<ResetReferenceAction> actions = previous.getActions(ResetReferenceAction.class);
            for (ResetReferenceAction action : actions) {
                if (report.getId().equals(action.getId())) {
                    filtered.logInfo("Resetting reference build, ignoring quality gate result for one build");

                    return IGNORE_QUALITY_GATE;
                }
            }
        }
        return qualityGateEvaluationMode;
    }
}

package io.jenkins.plugins.analysis.core.steps;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.analysis.Report;

import hudson.model.Job;
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
import io.jenkins.plugins.analysis.core.util.LogHandler;
import io.jenkins.plugins.analysis.core.util.QualityGateEvaluator;
import io.jenkins.plugins.analysis.core.util.QualityGateStatus;
import io.jenkins.plugins.analysis.core.util.StageResultHandler;
import io.jenkins.plugins.analysis.core.util.TrendChartType;
import io.jenkins.plugins.util.JenkinsFacade;

import static io.jenkins.plugins.analysis.core.model.AnalysisHistory.JobResultEvaluationMode.*;
import static io.jenkins.plugins.analysis.core.model.AnalysisHistory.QualityGateEvaluationMode.*;

/**
 * Publishes issues: Stores the created issues in an {@link AnalysisResult}. The result is attached to the {@link Run}
 * by registering a {@link ResultAction}.
 *
 * @author Ullrich Hafner
 */
@SuppressWarnings("PMD.ExcessiveImports")
class IssuesPublisher {
    private final AnnotatedReport report;
    private final Run<?, ?> run;
    private final HealthDescriptor healthDescriptor;
    private final String name;
    private final Charset sourceCodeEncoding;
    private final QualityGateEvaluator qualityGate;
    private final String referenceJobName;
    private final String referenceBuildId;
    private final QualityGateEvaluationMode qualityGateEvaluationMode;
    private final JobResultEvaluationMode jobResultEvaluationMode;
    private final LogHandler logger;
    private final StageResultHandler stageResultHandler;
    private final boolean failOnErrors;

    @SuppressWarnings("ParameterNumber")
    IssuesPublisher(final Run<?, ?> run, final AnnotatedReport report,
            final HealthDescriptor healthDescriptor, final QualityGateEvaluator qualityGate,
            final String name, final String referenceJobName, final String referenceBuildId,
            final boolean ignoreQualityGate,
            final boolean ignoreFailedBuilds, final Charset sourceCodeEncoding, final LogHandler logger,
            final StageResultHandler stageResultHandler, final boolean failOnErrors) {

        this.report = report;
        this.run = run;
        this.healthDescriptor = healthDescriptor;
        this.name = name;
        this.sourceCodeEncoding = sourceCodeEncoding;
        this.qualityGate = qualityGate;
        this.referenceJobName = referenceJobName;
        this.referenceBuildId = referenceBuildId;
        qualityGateEvaluationMode = ignoreQualityGate ? IGNORE_QUALITY_GATE : SUCCESSFUL_QUALITY_GATE;
        jobResultEvaluationMode = ignoreFailedBuilds ? NO_JOB_FAILURE : IGNORE_JOB_RESULT;
        this.logger = logger;
        this.stageResultHandler = stageResultHandler;
        this.failOnErrors = failOnErrors;
    }

    private String getId() {
        return report.getId();
    }

    /**
     * Creates a new {@link AnalysisResult} and attaches the result in a {@link ResultAction} that is registered with
     * the current run.
     *
     * @return the created result action
     */
    ResultAction attachAction() {
        return attachAction(TrendChartType.AGGREGATION_TOOLS);
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

        Report filtered = report.getReport();
        DeltaReport deltaReport = new DeltaReport(filtered, createAnalysisHistory(selector, filtered), run.getNumber());
        QualityGateStatus qualityGateStatus = evaluateQualityGate(filtered, deltaReport);
        reportHealth(filtered);

        report.logInfo("Created analysis result for %d issues (found %d new issues, fixed %d issues)",
                deltaReport.getAllIssues().size(), deltaReport.getNewIssues().size(),
                deltaReport.getFixedIssues().size());

        if (failOnErrors && report.getReport().hasErrors()) {
            report.logInfo("Failing build because analysis result contains errors");
            stageResultHandler.setResult(Result.FAILURE,
                    "Some errors have been logged during recording of issues");
        }

        if (trendChartType == TrendChartType.AGGREGATION_TOOLS) {
            AggregationAction action = run.getAction(AggregationAction.class);
            if (action == null) {
                run.addAction(new AggregationAction());
            }
        }

        report.logInfo("Attaching ResultAction with ID '%s' to run '%s'.", getId(), run);
        logger.log(filtered);

        AnalysisResult result = new AnalysisHistory(run, selector).getResult()
                .map(previous -> new AnalysisResult(run, getId(), deltaReport, report.getBlames(),
                        report.getStatistics(), qualityGateStatus, report.getSizeOfOrigin(),
                        previous))
                .orElseGet(() -> new AnalysisResult(run, getId(), deltaReport, report.getBlames(),
                        report.getStatistics(), qualityGateStatus, report.getSizeOfOrigin()));
        ResultAction action
                = new ResultAction(run, result, healthDescriptor, getId(), name, sourceCodeEncoding, trendChartType);
        run.addAction(action);

        if (trendChartType == TrendChartType.TOOLS_AGGREGATION) {
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

    private QualityGateStatus evaluateQualityGate(final Report filtered, final DeltaReport deltaReport) {
        QualityGateStatus qualityGateStatus;
        if (qualityGate.isEnabled()) {
            filtered.logInfo("Evaluating quality gates");
            qualityGateStatus = qualityGate.evaluate(deltaReport.getStatistics(), filtered::logInfo);
            if (qualityGateStatus.isSuccessful()) {
                filtered.logInfo("-> All quality gates have been passed");
            }
            else {
                filtered.logInfo("-> Some quality gates have been missed: overall result is %s", qualityGateStatus);
            }
            if (!qualityGateStatus.isSuccessful()) {
                stageResultHandler.setResult(qualityGateStatus.getResult(),
                        "Some quality gates have been missed: overall result is " + qualityGateStatus.getResult());
            }
        }
        else {
            filtered.logInfo("No quality gates have been set - skipping");
            qualityGateStatus = QualityGateStatus.INACTIVE;
        }
        return qualityGateStatus;
    }

    private History createAnalysisHistory(final ResultSelector selector, final Report filtered) {
        Run<?, ?> baseline = run;

        if (referenceJobName != null) {
            Optional<Job<?, ?>> referenceJob = new JenkinsFacade().getJob(referenceJobName);
            if (referenceJob.isPresent()) {
                Job<?, ?> job = referenceJob.get();
                baseline = obtainReferenceBuild(job);
                if (baseline == null) {
                    filtered.logError("Reference job '%s' does not contain %s", job.getName(), getReferenceName());
                    return new NullAnalysisHistory();
                }
            }
        }
        return new AnalysisHistory(baseline, selector, determineQualityGateEvaluationMode(filtered),
                jobResultEvaluationMode);
    }

    private Run<?, ?> obtainReferenceBuild(final Job<?, ?> job) {
        if (StringUtils.isBlank(referenceBuildId)) {
            return job.getLastBuild();
        }
        else {
            return job.getBuild(referenceBuildId);
        }
    }

    private String getReferenceName() {
        if (StringUtils.isBlank(referenceBuildId)) {
            return "any valid build";
        }
        return String.format("build '%s'", referenceBuildId);
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

package io.jenkins.plugins.analysis.core.steps;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.Optional;

import edu.hm.hafner.analysis.Report;
import io.jenkins.plugins.analysis.core.JenkinsFacade;
import io.jenkins.plugins.analysis.core.history.AnalysisHistory;
import io.jenkins.plugins.analysis.core.history.AnalysisHistory.JobResultEvaluationMode;
import io.jenkins.plugins.analysis.core.history.AnalysisHistory.QualityGateEvaluationMode;
import io.jenkins.plugins.analysis.core.history.ResultSelector;
import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.ByIdResultSelector;
import io.jenkins.plugins.analysis.core.model.DeltaReport;
import io.jenkins.plugins.analysis.core.quality.HealthDescriptor;
import io.jenkins.plugins.analysis.core.quality.QualityGate;
import io.jenkins.plugins.analysis.core.quality.QualityGateStatus;
import io.jenkins.plugins.analysis.core.scm.Blames;
import io.jenkins.plugins.analysis.core.util.LogHandler;
import io.jenkins.plugins.analysis.core.views.ResultAction;
import static io.jenkins.plugins.analysis.core.history.AnalysisHistory.JobResultEvaluationMode.*;
import static io.jenkins.plugins.analysis.core.history.AnalysisHistory.QualityGateEvaluationMode.*;

import hudson.model.Job;
import hudson.model.Run;

/**
 * Publishes issues: Stores the created issues in an {@link AnalysisResult}. The result is attached to the
 * {@link Run} by registering a {@link ResultAction}.
 *
 * @author Ullrich Hafner
 */
class IssuesPublisher {
    private final AnnotatedReport report;
    private final Run<?, ?> run;
    private final HealthDescriptor healthDescriptor;
    private final String name;
    private final Charset sourceCodeEncoding;
    private final QualityGate qualityGate;
    private final String referenceJobName;
    private final QualityGateEvaluationMode qualityGateEvaluationMode;
    private final JobResultEvaluationMode jobResultEvaluationMode;
    private final LogHandler logger;

    @SuppressWarnings("ParameterNumber")
    IssuesPublisher(final Run<?, ?> run, final AnnotatedReport report,
                    final HealthDescriptor healthDescriptor, final QualityGate qualityGate,
            final String name, final String referenceJobName, final boolean ignoreQualityGate,
            final boolean ignoreFailedBuilds, final Charset sourceCodeEncoding, final LogHandler logger) {
        this.report = report;
        this.run = run;
        this.healthDescriptor = healthDescriptor;
        this.name = name;
        this.sourceCodeEncoding = sourceCodeEncoding;
        this.qualityGate = qualityGate;
        this.referenceJobName = referenceJobName;
        qualityGateEvaluationMode = ignoreQualityGate ? IGNORE_QUALITY_GATE : SUCCESSFUL_QUALITY_GATE;
        jobResultEvaluationMode = ignoreFailedBuilds ? NO_JOB_FAILURE : IGNORE_JOB_RESULT;
        this.logger = logger;
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
        logger.log("Attaching ResultAction with ID '%s' to run '%s'.", getId(), run);

        ResultSelector selector = ensureThatIdIsUnique();
        AnalysisResult result = createAnalysisResult(report.getReport(), selector, report.getBlames(), report.getSizeOfOrigin());
        logger.log("Created analysis result for %d issues (found %d new issues, fixed %d issues)",
                result.getTotalSize(), result.getNewSize(), result.getFixedSize());

        ResultAction action = new ResultAction(run, result, healthDescriptor, getId(), name, sourceCodeEncoding);
        run.addAction(action);

        run.addOrReplaceAction(new AggregationAction());

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

    @SuppressWarnings("PMD.PrematureDeclaration")
    private AnalysisResult createAnalysisResult(final Report filtered, final ResultSelector selector,
            final Blames blames, final Map<String, Integer> sizeOfOrigin) {
        DeltaReport deltaReport = new DeltaReport(filtered, createAnalysisHistory(selector), run.getNumber());
        QualityGateStatus qualityGateStatus = evaluateQualityGate(filtered, deltaReport);
        reportHealth(filtered);
        logger.log(filtered);
        return new AnalysisHistory(run, selector).getResult()
                .map(previous -> new AnalysisResult(run, getId(), deltaReport, blames, qualityGateStatus, sizeOfOrigin, previous))
                .orElseGet(() -> new AnalysisResult(run, getId(), deltaReport, blames, qualityGateStatus, sizeOfOrigin));
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
            qualityGateStatus = qualityGate.evaluate(deltaReport, filtered::logInfo);
            if (qualityGateStatus.isSuccessful()) {
                filtered.logInfo("-> All quality gates have been passed");
            }
            else {
                filtered.logInfo("-> Some quality gates have been missed: overall result is %s", qualityGateStatus);
            }
            qualityGateStatus.setResult(run);
        }
        else {
            filtered.logInfo("No quality gates have been set - skipping");
            qualityGateStatus = QualityGateStatus.INACTIVE;
        }
        return qualityGateStatus;
    }

    private AnalysisHistory createAnalysisHistory(final ResultSelector selector) {
        Run<?, ?> baseline = run;
        if (referenceJobName != null) {
            Optional<Job<?, ?>> referenceJob = new JenkinsFacade().getJob(referenceJobName);
            if (referenceJob.isPresent()) {
                baseline = referenceJob.get().getLastBuild();
            }
        }
        return new AnalysisHistory(baseline, selector, qualityGateEvaluationMode, jobResultEvaluationMode);
    }

}

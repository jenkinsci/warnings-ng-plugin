package io.jenkins.plugins.analysis.core.steps;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Report.IssueFilterBuilder;
import io.jenkins.plugins.analysis.core.JenkinsFacade;
import io.jenkins.plugins.analysis.core.filter.RegexpFilter;
import io.jenkins.plugins.analysis.core.history.AnalysisHistory;
import io.jenkins.plugins.analysis.core.history.AnalysisHistory.JobResultEvaluationMode;
import static io.jenkins.plugins.analysis.core.history.AnalysisHistory.JobResultEvaluationMode.*;
import io.jenkins.plugins.analysis.core.history.AnalysisHistory.QualityGateEvaluationMode;
import static io.jenkins.plugins.analysis.core.history.AnalysisHistory.QualityGateEvaluationMode.*;
import io.jenkins.plugins.analysis.core.history.ResultSelector;
import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.ByIdResultSelector;
import io.jenkins.plugins.analysis.core.model.DeltaReport;
import io.jenkins.plugins.analysis.core.quality.HealthDescriptor;
import io.jenkins.plugins.analysis.core.quality.QualityGate;
import io.jenkins.plugins.analysis.core.quality.QualityGateStatus;
import io.jenkins.plugins.analysis.core.scm.Blames;
import io.jenkins.plugins.analysis.core.views.ResultAction;

import hudson.model.Job;
import hudson.model.Run;

/**
 * Publishes issues: Stores the created issues in an {@link AnalysisResult}. The result is attached to the
 * {@link Run} by registering a {@link ResultAction}.
 *
 * @author Ullrich Hafner
 */
class IssuesPublisher {
    private final Report report;
    private final Blames blames;
    private final List<RegexpFilter> filters;
    private final Run<?, ?> run;
    private final HealthDescriptor healthDescriptor;
    private final String name;
    private final Charset sourceCodeEncoding;
    private final QualityGate qualityGate;
    private final String referenceJobName;
    private final QualityGateEvaluationMode qualityGateEvaluationMode;
    private final JobResultEvaluationMode jobResultEvaluationMode;
    private final LogHandler logger;
    private final String id;

    @SuppressWarnings("ParameterNumber")
    IssuesPublisher(final Run<?, ?> run, final Report report, final Blames blames,
            final List<RegexpFilter> filters,
            final HealthDescriptor healthDescriptor, final QualityGate qualityGate,
            final String name, final String referenceJobName, final boolean ignoreQualityGate,
            final boolean ignoreFailedBuilds, final Charset sourceCodeEncoding,
            final LogHandler logger) {
        this.report = report;
        id = report.getId();
        this.blames = blames;
        this.filters = new ArrayList<>(filters);
        this.run = run;
        this.healthDescriptor = healthDescriptor;
        this.name = name;
        this.sourceCodeEncoding = sourceCodeEncoding;
        this.qualityGate = qualityGate;
        this.referenceJobName = referenceJobName;
        this.qualityGateEvaluationMode = ignoreQualityGate ? IGNORE_QUALITY_GATE : SUCCESSFUL_QUALITY_GATE;
        this.jobResultEvaluationMode = ignoreFailedBuilds ? NO_JOB_FAILURE : IGNORE_JOB_RESULT;
        this.logger = logger;
    }

    /**
     * Creates a new {@link AnalysisResult} and attaches the result in an {@link ResultAction} that is registered with
     * the current run.
     *
     * @return the created result action
     */
    public ResultAction attachAction() {
        logger.log("Attaching ResultAction with ID '%s' to run '%s'.", id, run);

        ResultSelector selector = ensureThatIdIsUnique();
        Report filtered = filter();
        AnalysisResult result = createAnalysisResult(filtered, selector, blames);
        logger.log("Created analysis result for %d issues (found %d new issues, fixed %d issues)",
                result.getTotalSize(), result.getNewSize(), result.getFixedSize());

        ResultAction action = new ResultAction(run, result, healthDescriptor, id, name, sourceCodeEncoding);
        run.addAction(action);

        return action;
    }

    private ResultSelector ensureThatIdIsUnique() {
        ResultSelector selector = new ByIdResultSelector(id);
        Optional<ResultAction> other = selector.get(run);
        if (other.isPresent()) {
            throw new IllegalStateException(
                    String.format("ID %s is already used by another action: %s%n", id, other.get()));
        }
        return selector;
    }

    private Report filter() {
        int actualFilterSize = 0;
        IssueFilterBuilder builder = new IssueFilterBuilder();
        for (RegexpFilter filter : filters) {
            if (StringUtils.isNotBlank(filter.getPattern())) {
                filter.apply(builder);
                actualFilterSize++;
            }
        }
        Report filtered = report.filter(builder.build());
        if (actualFilterSize > 0) {
            filtered.logInfo("Applying %d filters on the set of %d issues (%d issues have been removed, %d issues will be published)",
                    filters.size(), report.size(), report.size() - filtered.size(), filtered.size());
        }
        else {
            filtered.logInfo("No filter has been set, publishing all %d issues", filtered.size());
        }
        logger.log(filtered);

        return filtered;
    }

    @SuppressWarnings("PMD.PrematureDeclaration")
    private AnalysisResult createAnalysisResult(final Report filtered, final ResultSelector selector,
            final Blames blames) {
        DeltaReport deltaReport = new DeltaReport(filtered, createAnalysisHistory(selector), run.getNumber());
        QualityGateStatus qualityGateStatus = evaluateQualityGate(filtered, deltaReport);
        reportHealth(filtered);
        logger.log(filtered);
        return new AnalysisHistory(run, selector).getResult()
                .map(previous -> new AnalysisResult(run, deltaReport, blames, qualityGateStatus, previous))
                .orElseGet(() -> new AnalysisResult(run, deltaReport, blames, qualityGateStatus));
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

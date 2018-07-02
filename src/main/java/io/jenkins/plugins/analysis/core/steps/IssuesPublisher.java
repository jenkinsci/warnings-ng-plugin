package io.jenkins.plugins.analysis.core.steps;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Report.IssueFilterBuilder;
import io.jenkins.plugins.analysis.core.JenkinsFacade;
import io.jenkins.plugins.analysis.core.history.AnalysisHistory;
import static io.jenkins.plugins.analysis.core.history.AnalysisHistory.JobResultEvaluationMode.*;
import static io.jenkins.plugins.analysis.core.history.AnalysisHistory.QualityGateEvaluationMode.*;
import io.jenkins.plugins.analysis.core.history.ResultSelector;
import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.ByIdResultSelector;
import io.jenkins.plugins.analysis.core.model.RegexpFilter;
import io.jenkins.plugins.analysis.core.quality.HealthDescriptor;
import io.jenkins.plugins.analysis.core.quality.QualityGate;
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
    private final List<RegexpFilter> filters;
    private final Run<?, ?> run;
    private final HealthDescriptor healthDescriptor;
    private final String name;
    private final Charset sourceCodeEncoding;
    private final QualityGate qualityGate;
    private final String referenceJobName;
    private final boolean ignoreAnalysisResult;
    private final boolean overallResultMustBeSuccess;
    private final LogHandler logger;
    private final String id;

    @SuppressWarnings("ParameterNumber")
    IssuesPublisher(final Run<?, ?> run, final Report report, final List<RegexpFilter> filters,
            final HealthDescriptor healthDescriptor, final QualityGate qualityGate,
            final String name, final String referenceJobName, final boolean ignoreAnalysisResult,
            final boolean overallResultMustBeSuccess, final Charset sourceCodeEncoding,
            final LogHandler logger) {
        this.report = report;
        id = report.getId();
        this.filters = new ArrayList<>(filters);
        this.run = run;
        this.healthDescriptor = healthDescriptor;
        this.name = name;
        this.sourceCodeEncoding = sourceCodeEncoding;
        this.qualityGate = qualityGate;
        this.referenceJobName = referenceJobName;
        this.ignoreAnalysisResult = ignoreAnalysisResult;
        this.overallResultMustBeSuccess = overallResultMustBeSuccess;
        this.logger = logger;
    }

    /**
     * Creates a new {@link AnalysisResult} and attaches the result in an {@link ResultAction} that is registered with
     * the current run.
     *
     * @return the created result action
     */
    public ResultAction attachAction() {
        ResultSelector selector = ensureThatIdIsUnique();

        Report filtered = filter();

        logger.log("Attaching ResultAction with ID '%s' to run '%s'.", id, run);
        AnalysisResult result = createResult(selector, filtered);
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

    private AnalysisResult createResult(final ResultSelector selector, final Report filtered) {
        AnalysisResult result = createAnalysisResult(filtered, selector);

        logger.log("Created analysis result for %d issues (found %d new issues, fixed %d issues)",
                result.getTotalSize(), result.getNewSize(), result.getFixedSize());

        return result;
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
    private AnalysisResult createAnalysisResult(final Report filtered, final ResultSelector selector) {
        filtered.setReference(String.valueOf(run.getNumber()));

        AnalysisHistory history = createAnalysisHistory(selector);
        return new AnalysisHistory(run, selector).getPreviousResult()
                .map(previous -> new AnalysisResult(run, history, filtered, qualityGate, previous))
                .orElseGet(() -> new AnalysisResult(run, history, filtered, qualityGate));
    }

    private AnalysisHistory createAnalysisHistory(final ResultSelector selector) {
        Run<?, ?> baseline = run;
        if (referenceJobName != null) {
            Optional<Job<?, ?>> referenceJob = new JenkinsFacade().getJob(referenceJobName);
            if (referenceJob.isPresent()) {
                baseline = referenceJob.get().getLastBuild();
            }
        }
        return new AnalysisHistory(baseline, selector,
                ignoreAnalysisResult ? IGNORE_QUALITY_GATE : SUCCESSFUL_QUALITY_GATE,
                overallResultMustBeSuccess ? JOB_MUST_BE_SUCCESSFUL : IGNORE_JOB_RESULT);
    }
}

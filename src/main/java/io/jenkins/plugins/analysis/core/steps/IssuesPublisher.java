package io.jenkins.plugins.analysis.core.steps;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Report.IssueFilterBuilder;
import io.jenkins.plugins.analysis.core.JenkinsFacade;
import io.jenkins.plugins.analysis.core.history.AnalysisHistory;
import static io.jenkins.plugins.analysis.core.history.AnalysisHistory.JobResultEvaluationMode.*;
import static io.jenkins.plugins.analysis.core.history.AnalysisHistory.QualityGateEvaluationMode.*;
import io.jenkins.plugins.analysis.core.history.ReferenceProvider;
import io.jenkins.plugins.analysis.core.history.ResultSelector;
import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.ByIdResultSelector;
import io.jenkins.plugins.analysis.core.model.RegexpFilter;
import io.jenkins.plugins.analysis.core.quality.HealthDescriptor;
import io.jenkins.plugins.analysis.core.quality.QualityGate;
import io.jenkins.plugins.analysis.core.util.AffectedFilesResolver;
import io.jenkins.plugins.analysis.core.views.ResultAction;

import hudson.FilePath;
import hudson.model.Job;
import hudson.model.Run;
import hudson.remoting.VirtualChannel;

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
    IssuesPublisher(Run<?, ?> run, Report report, List<RegexpFilter> filters,
            HealthDescriptor healthDescriptor, QualityGate qualityGate,
            String name, String referenceJobName, boolean ignoreAnalysisResult,
            boolean overallResultMustBeSuccess, Charset sourceCodeEncoding,
            LogHandler logger) {
        this.report = report;
        id = report.getOrigin();
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
     * the current run. This method will not copy the affected files to Jenkins' build folder.
     *
     * @return the created result action
     */
    public ResultAction attachAction() {
        report.logError("Can't copy affected files since channel to agent is not available");

        return run();
    }

    /**
     * Creates a new {@link AnalysisResult} and attaches the result in an {@link ResultAction} that is registered with
     * the current run. After the result has been created, all affected files to Jenkins' build folder.
     *
     * @param channel
     *         channel to the agent that stores the affected source files
     * @param buildFolder
     *         destination folder where all affected files will be copied to
     *
     * @return the created result action
     * @throws IOException
     *         if the files could not be written
     * @throws InterruptedException
     *         if the user cancels the processing
     */
    public ResultAction attachAction(VirtualChannel channel, FilePath buildFolder)
            throws IOException, InterruptedException {
        ResultAction resultAction = run();

        copyAffectedFiles(resultAction.getResult().getIssues(), channel, buildFolder);

        return resultAction;
    }

    private ResultAction run() {
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

    private void copyAffectedFiles(Report filtered,
            VirtualChannel channel, FilePath buildFolder)
            throws IOException, InterruptedException {
        new AffectedFilesResolver().copyFilesWithAnnotationsToBuildFolder(filtered, channel, buildFolder);

        logger.log(filtered);
    }

    private AnalysisResult createResult(ResultSelector selector, Report filtered) {
        AnalysisResult result = createAnalysisResult(filtered, selector);

        logger.log("Created analysis result for %d issues (found %d new issues, fixed %d issues)",
                result.getTotalSize(), result.getNewSize(), result.getFixedSize());

        return result;
    }

    private Report filter() {
        IssueFilterBuilder builder = new IssueFilterBuilder();
        for (RegexpFilter filter : filters) {
            filter.apply(builder);
        }
        Report filtered = report.filter(builder.build());
        filtered.logInfo("Applying %d filters on the set of %d issues (%d issues have been removed)",
                filters.size(), report.size(), report.size() - filtered.size());

        logger.log(filtered);

        return filtered;
    }

    @SuppressWarnings("PMD.PrematureDeclaration")
    private AnalysisResult createAnalysisResult(Report filtered, ResultSelector selector) {
        filtered.setReference(String.valueOf(run.getNumber()));

        ReferenceProvider referenceProvider = createReferenceProvider(selector);
        return new AnalysisHistory(run, selector).getPreviousResult()
                .map(previous -> new AnalysisResult(run, referenceProvider, filtered, qualityGate, previous))
                .orElseGet(() -> new AnalysisResult(run, referenceProvider, filtered, qualityGate));
    }

    private ReferenceProvider createReferenceProvider(ResultSelector selector) {
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

package io.jenkins.plugins.analysis.core.steps;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import edu.hm.hafner.analysis.Issues;
import edu.hm.hafner.analysis.Issues.IssueFilterBuilder;
import io.jenkins.plugins.analysis.core.JenkinsFacade;
import io.jenkins.plugins.analysis.core.history.BuildHistory;
import io.jenkins.plugins.analysis.core.history.OtherJobReferenceFinder;
import io.jenkins.plugins.analysis.core.history.ReferenceFinder;
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
    private final Issues<?> issues;
    private final List<RegexpFilter> filters;
    private final Run<?, ?> run;
    private final FilePath workspace;
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
    IssuesPublisher(final Run<?, ?> run, final Issues<?> issues, final List<RegexpFilter> filters,
            final HealthDescriptor healthDescriptor, final QualityGate qualityGate, final FilePath workspace,
            final String name, final String referenceJobName, final boolean ignoreAnalysisResult,
            final boolean overallResultMustBeSuccess, final Charset sourceCodeEncoding,
            final LogHandler logger) {
        this.issues = issues;
        this.id = issues.getId();
        this.filters = new ArrayList<>(filters);
        this.run = run;
        this.workspace = workspace;
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
        issues.logError("Can't copy affected files since channel to agent is not available");

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
    public ResultAction attachAction(final VirtualChannel channel, final FilePath buildFolder)
            throws IOException, InterruptedException {
        ResultAction resultAction = run();

        copyAffectedFiles(resultAction.getResult().getIssues(), channel, buildFolder);

        return resultAction;
    }

    private ResultAction run() {
        ResultSelector selector = ensureThatIdIsUnique();

        Issues<?> filtered = filter();

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

    private void copyAffectedFiles(final Issues<?> filtered,
            final VirtualChannel channel, final FilePath buildFolder)
            throws IOException, InterruptedException {
        Set<String> files = filtered.getFiles();
        String copyingLogMessage = new AffectedFilesResolver()
                .copyFilesWithAnnotationsToBuildFolder(channel, buildFolder, files);
        filtered.logInfo("Copying %d affected files from '%s' to build folder (%s)",
                files.size(), workspace, copyingLogMessage);

        logger.log(filtered);
    }

    private AnalysisResult createResult(final ResultSelector selector, final Issues<?> filtered) {
        AnalysisResult result = createAnalysisResult(filtered, selector);

        logger.log("Created analysis result for %d issues (found %d new issues, fixed %d issues)",
                result.getTotalSize(), result.getNewSize(), result.getFixedSize());

        return result;
    }

    private Issues<?> filter() {
        IssueFilterBuilder builder = new IssueFilterBuilder();
        for (RegexpFilter filter : filters) {
            filter.apply(builder);
        }
        Issues<?> filtered = issues.filter(builder.build());
        filtered.logInfo("Applying %d filters on the set of %d issues (%d issues have been removed)",
                filters.size(), issues.size(), issues.size() - filtered.size());

        logger.log(filtered);

        return filtered;
    }

    private AnalysisResult createAnalysisResult(final Issues<?> filtered, final ResultSelector selector) {
        ReferenceProvider referenceProvider = createReferenceProvider(selector);
        return new BuildHistory(run, selector).getPreviousResult()
                .map(previous -> new AnalysisResult(run, referenceProvider, filtered, qualityGate, previous))
                .orElseGet(() -> new AnalysisResult(run, referenceProvider, filtered, qualityGate));
    }

    private ReferenceProvider createReferenceProvider(final ResultSelector selector) {
        if (referenceJobName != null) {
            Optional<Job<?, ?>> referenceJob = new JenkinsFacade().getJob(referenceJobName);
            if (referenceJob.isPresent()) {
                // FIXME: what to do if last build is not available?
                return new OtherJobReferenceFinder(referenceJob.get().getLastBuild(), selector,
                        ignoreAnalysisResult, overallResultMustBeSuccess);
            }
        }
        return ReferenceFinder.create(run, selector, ignoreAnalysisResult, overallResultMustBeSuccess);
    }
}

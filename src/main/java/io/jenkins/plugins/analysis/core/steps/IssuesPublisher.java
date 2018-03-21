package io.jenkins.plugins.analysis.core.steps;

import java.io.IOException;
import java.nio.charset.Charset;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.eclipse.collections.api.list.ImmutableList;

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
import io.jenkins.plugins.analysis.core.util.Logger;
import io.jenkins.plugins.analysis.core.views.ResultAction;

import hudson.FilePath;
import hudson.model.Job;
import hudson.model.Run;
import hudson.plugins.analysis.util.EncodingValidator;
import hudson.remoting.VirtualChannel;

/**
 * Publishes issues: Stores the created issues in an {@link AnalysisResult}. The result is attached to the
 * {@link Run} by registering a {@link hudson.plugins.analysis.core.ResultAction}.
 *
 * @author Ullrich Hafner
 */
public class IssuesPublisher {
    private int infoPosition = 0;
    private int errorPosition = 0;

    private final Issues<?> issues;
    private final ArrayList<RegexpFilter> filters;
    private final Run<?, ?> run;
    private final FilePath workspace;
    private final HealthDescriptor healthDescriptor;
    private final String name;
    private final String sourceCodeEncoding;
    private final QualityGate qualityGate;
    private final String referenceJobName;
    private final boolean ignoreAnalysisResult;
    private final boolean overallResultMustBeSuccess;
    private final Logger logger;
    private final Logger errorLogger;

    public IssuesPublisher(final Issues<?> issues, final List<RegexpFilter> filters,
            final Run<?, ?> run, final FilePath workspace,
            final HealthDescriptor healthDescriptor, final String name, final String sourceCodeEncoding,
            final QualityGate qualityGate,
            final String referenceJobName, final boolean ignoreAnalysisResult,
            final boolean overallResultMustBeSuccess, Logger logger,
            Logger errorLogger) {
        this.issues = issues;
        this.filters = new ArrayList(filters);
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
        this.errorLogger = errorLogger;
    }

    public ResultAction attachAction() {
        issues.logError("Can't copy affected files since channel to agent is not available");

        return run();
    }

    public ResultAction attachAction(final VirtualChannel channel, final FilePath buildFolder)
            throws IOException, InterruptedException {
        ResultAction resultAction = run();

        copyAffectedFiles(resultAction.getResult().getIssues(), channel, buildFolder);

        return resultAction;
    }

    public ResultAction run() {

        ResultSelector selector = new ByIdResultSelector(issues.getId());
        Optional<ResultAction> other = selector.get(run);
        if (other.isPresent()) {
            throw new IllegalStateException(String.format("ID %s is already used by another action: %s%n",
                    issues.getId(), other.get()));
        }

        Logger logger = this.logger;

        Issues<?> filtered = filter();

        logger.log("Attaching ResultAction with ID '%s' to run '%s'.", getId(), run);
        AnalysisResult result = createResult(run, selector, filtered);
        ResultAction action = new ResultAction(run, result, healthDescriptor, getId(), name,
                EncodingValidator.defaultCharset(sourceCodeEncoding));
        run.addAction(action);

        return action;
    }

    private String getId() {
        return issues.getId();
    }

    private void copyAffectedFiles(final Issues<?> filtered, final VirtualChannel channel,
            final FilePath buildFolder)
            throws IOException, InterruptedException {
        Logger logger = this.logger;

        Instant start = Instant.now();

        Set<String> files = filtered.getFiles();
        String copyingLogMessage = new AffectedFilesResolver()
                .copyFilesWithAnnotationsToBuildFolder(channel, buildFolder, files);
        filtered.logInfo("Copying %d affected files from '%s' to build folder (%s)",
                files.size(), workspace, copyingLogMessage);
        logger.log("Copying affected files took %s", computeElapsedTime(start));

        log(filtered);
    }

    private AnalysisResult createResult(final Run<?, ?> run, final ResultSelector selector,
            final Issues<?> filtered) {
        Logger logger = this.logger;

        Instant start = Instant.now();
        AnalysisResult result = createAnalysisResult(filtered, run, selector);
        logger.log("Created analysis result for %d issues (found %d new issues, fixed %d issues)",
                result.getTotalSize(), result.getNewSize(), result.getFixedSize());
        logger.log("Creating analysis result took %s", computeElapsedTime(start));

        return result;
    }

    private Issues<?> filter() {
        Logger logger = this.logger;

        Instant start = Instant.now();
        IssueFilterBuilder builder = new IssueFilterBuilder();
        for (RegexpFilter filter : filters) {
            filter.apply(builder);
        }
        Issues<?> filtered = issues.filter(builder.build());
        filtered.logInfo("Applying %d filters on the set of %d issues (%d issues have been removed)",
                filters.size(), issues.size(), issues.size() - filtered.size());
        logger.log("Filtering issues took %s", computeElapsedTime(start));

        log(filtered);

        return filtered;
    }

    private AnalysisResult createAnalysisResult(final Issues<?> filtered,
            final Run<?, ?> run, final ResultSelector selector) {
        ReferenceProvider referenceProvider = createReferenceProvider(run, selector);
        return new BuildHistory(run, selector).getPreviousResult()
                .map(previous -> new AnalysisResult(run, referenceProvider, filtered, qualityGate, previous))
                .orElseGet(() -> new AnalysisResult(run, referenceProvider, filtered, qualityGate));
    }

    private ReferenceProvider createReferenceProvider(final Run<?, ?> run, final ResultSelector selector) {
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
    private void log(final Issues<?> issues) {
        logErrorMessages(issues);
        logInfoMessages(issues);
    }

    private void logErrorMessages(final Issues<?> issues) {
        ImmutableList<String> errorMessages = issues.getErrorMessages();
        if (errorPosition < errorMessages.size()) {
            errorLogger.logEachLine(errorMessages.subList(errorPosition, errorMessages.size()).castToList());
            errorPosition = errorMessages.size();
        }
    }

    private void logInfoMessages(final Issues<?> issues) {
        ImmutableList<String> infoMessages = issues.getInfoMessages();
        if (infoPosition < infoMessages.size()) {
            logger.logEachLine(infoMessages.subList(infoPosition, infoMessages.size()).castToList());
            infoPosition = infoMessages.size();
        }
    }

    private Duration computeElapsedTime(final Instant start) {
        return Duration.between(start, Instant.now());
    }

    private Charset getSourceCodeCharset() {
        return EncodingValidator.defaultCharset(sourceCodeEncoding);
    }
}

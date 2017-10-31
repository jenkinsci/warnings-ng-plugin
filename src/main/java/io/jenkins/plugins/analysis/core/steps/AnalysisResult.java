package io.jenkins.plugins.analysis.core.steps; // NOPMD

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.thoughtworks.xstream.XStream;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueDifference;
import edu.hm.hafner.analysis.Issues;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.jenkins.plugins.analysis.core.history.ReferenceProvider;
import io.jenkins.plugins.analysis.core.steps.ResultEvaluator.Evaluation;

import hudson.XmlFile;
import hudson.model.Api;
import hudson.model.Result;
import hudson.model.Run;
import hudson.plugins.analysis.core.GlobalSettings;
import hudson.plugins.analysis.core.HealthDescriptor;
import hudson.plugins.analysis.util.model.AnnotationStream;
import hudson.plugins.analysis.util.model.Priority;

/**
 * A base class for build results that is capable of storing a reference to the current build. Provides support for
 * persisting the results of the build and loading and saving of annotations (all, new, and fixed) and delta
 * computation.
 *
 * @author Ulli Hafner
 */
//CHECKSTYLE:COUPLING-OFF
@ExportedBean
@SuppressWarnings({"PMD.TooManyFields", "PMD.ExcessiveClassLength"})
public class AnalysisResult implements Serializable, StaticAnalysisRun2 {
    private static final long serialVersionUID = 1110545450292087475L;
    private static final Logger LOGGER = Logger.getLogger(AnalysisResult.class.getName());

    private final String id;

    private transient ReentrantLock lock = new ReentrantLock();
    private final String name;
    private transient Run<?, ?> run;

    /** The project containing the annotations. */
    @SuppressFBWarnings("Se")
    private transient WeakReference<Issues> project;
    /** All new warnings in the current build. */
    @SuppressFBWarnings("Se")
    private transient WeakReference<Issues> newWarningsReference;
    /** All fixed warnings in the current build. */
    @SuppressFBWarnings("Se")
    private transient WeakReference<Issues> fixedWarningsReference;

    /** All parsed modules. */
    private final ImmutableSet<String> modules;
    /** The total number of parsed modules (regardless if there are annotations). */
    private final int numberOfModules;

    /** The default encoding to be used when reading and parsing files. */
    private final String defaultEncoding;

    /** The number of warnings in this build. */
    private final int numberOfWarnings;
    /** The number of new warnings in this build. */
    private final int numberOfNewWarnings;
    /** The number of fixed warnings in this build. */
    private final int numberOfFixedWarnings;

    /** The number of low priority warnings in this build. */
    private final int lowWarnings;
    /** The number of normal priority warnings in this build. */
    private final int normalWarnings;
    /** The number of high priority warnings in this build. */
    private final int highWarnings;

    /** The number of low priority warnings in this build. */
    private final int lowNewWarnings;
    /** The number of normal priority warnings in this build. */
    private final int normalNewWarnings;
    /** The number of high priority warnings in this build. */
    private final int highNewWarnings;

    /** Determines since which build we have zero warnings. */
    private int zeroWarningsSinceBuild;
    /** Determines since which time we have zero warnings. */
    private long zeroWarningsSinceDate;
    /** Determines the zero warnings high score. */
    private long zeroWarningsHighScore;
    /** Determines if the old zero high score has been broken. */
    private boolean isZeroWarningsHighScore;
    /** Determines the number of msec still to go before a new high score is reached. */
    private long highScoreGap;

    /** Error messages. */
    @SuppressFBWarnings("Se")
    private final ImmutableList<String> errors;

    /**
     * The build result of the associated plug-in. This result is an additional state that denotes if this plug-in has
     * changed the overall build result.
     */
    private Result pluginResult = Result.SUCCESS;
    /** Describes the reason for the build result evaluation. */
    private String reasonForPluginResult; // FIXME: i18n?
    /** Determines since which build the result is successful. */
    private int successfulSinceBuild;
    /** Determines since which time the result is successful. */
    private long successfulSinceDate;
    /** Determines the successful build result high score. */
    private long successfulHighScore;
    /** Determines if the old successful build result high score has been broken. */
    private boolean isSuccessfulHighScore;
    /** Determines the number of msec still to go before a new high score is reached. */
    private long successfulHighScoreGap;
    /** Determines if this result has touched the successful state. */
    private boolean isSuccessfulStateTouched;

    /** Reference build number. If not defined then 0 or -1 could be used. */
    private final int referenceBuild;

    /**
     * Creates a new instance of {@link AnalysisResult}.
     *
     * @param run
     *         the current run as owner of this action
     * @param referenceProvider
     *         the run history
     */
    public AnalysisResult(final String id, final String name, final Run run, final ReferenceProvider referenceProvider,
            final Optional<AnalysisResult> previousResult, final ResultEvaluator resultEvaluator, final String defaultEncoding,
            final Issues... issues) {
        this(id, name, run, referenceProvider, previousResult, resultEvaluator, defaultEncoding, Issues.merge(issues), true);
    }

    /**
     * Creates a new instance of {@link AnalysisResult}.
     *
     * @param build
     *         the current build as owner of this action
     * @param referenceProvider
     *         build referenceProvider
     */
    // FIXME: should we ignore the result in previousResult?
    protected AnalysisResult(final String id, final String name, final Run<?, ?> build, final ReferenceProvider referenceProvider,
            final Optional<AnalysisResult> previousResult, final ResultEvaluator resultEvaluator, final String defaultEncoding,
            final Issues result, final boolean canSerialize) {
        this.id = id;
        this.name = name;
        run = build;
        this.defaultEncoding = defaultEncoding;

        modules = ImmutableSet.copyOf(result.getProperties(issue -> issue.getModuleName()));
        numberOfModules = modules.size();

        // errors = ImmutableList.copyOf(result.getLogMessages()); FIXME: errors and log?
        errors = ImmutableList.of();

        numberOfWarnings = result.getSize();

        highWarnings = result.getHighPrioritySize();
        normalWarnings = result.getNormalPrioritySize();
        lowWarnings = result.getLowPrioritySize();

        referenceBuild = referenceProvider.getNumber();
        Issues referenceResult = referenceProvider.getIssues();

        project = new WeakReference<>(result);

        IssueDifference difference = new IssueDifference(result, referenceResult);
        Issues newWarnings = difference.getNewIssues();
//        FIXME: build is no property of issue? Generic property or subclass.
//        for (FileAnnotation newWarning : newWarnings) {
//            newWarning.setBuild(build.getNumber());
//        }

        numberOfNewWarnings = newWarnings.getSize();
        highNewWarnings = newWarnings.getHighPrioritySize();
        normalNewWarnings = newWarnings.getNormalPrioritySize();
        lowNewWarnings = newWarnings.getLowPrioritySize();
        newWarningsReference = new WeakReference<>(newWarnings);

        Issues fixedWarnings = difference.getFixedIssues();
        numberOfFixedWarnings = fixedWarnings.getSize();
        fixedWarningsReference = new WeakReference<>(fixedWarnings);

        computeZeroWarningsHighScore(build, result, previousResult);

        evaluateStatus(resultEvaluator, previousResult);

        if (canSerialize) {
            serializeAnnotations(result, fixedWarnings);
        }
    }

    /**
     * Computes the zero warnings high score based on the current build and the previous build (with results of the
     * associated plug-in).
     *
     * @param build
     *         the current build
     */
    private void computeZeroWarningsHighScore(final Run<?, ?> build, final Issues currentResult,
            final Optional<AnalysisResult> previousResult) {
        if (previousResult.isPresent()) {
            AnalysisResult previous = previousResult.get();
            if (currentResult.isEmpty()) {
                if (previous.getNumberOfWarnings() == 0) {
                    zeroWarningsSinceBuild = previous.getZeroWarningsSinceBuild();
                    zeroWarningsSinceDate = previous.getZeroWarningsSinceDate();
                }
                else {
                    zeroWarningsSinceBuild = build.getNumber();
                    zeroWarningsSinceDate = build.getTimestamp().getTimeInMillis();
                }
                zeroWarningsHighScore = Math.max(previous.getZeroWarningsHighScore(),
                        build.getTimestamp().getTimeInMillis() - zeroWarningsSinceDate);
                if (previous.getZeroWarningsHighScore() == 0) {
                    isZeroWarningsHighScore = true;
                }
                else {
                    isZeroWarningsHighScore = zeroWarningsHighScore != previous.getZeroWarningsHighScore();

                }
                if (!isZeroWarningsHighScore) {
                    highScoreGap = previous.getZeroWarningsHighScore()
                            - (build.getTimestamp().getTimeInMillis() - zeroWarningsSinceDate);
                }
            }
            else {
                zeroWarningsHighScore = previous.getZeroWarningsHighScore();
            }
        }
        else {
            if (currentResult.isEmpty()) {
                zeroWarningsSinceBuild = build.getNumber();
                zeroWarningsSinceDate = build.getTimestamp().getTimeInMillis();
                isZeroWarningsHighScore = true;
                zeroWarningsHighScore = 0;
            }
        }
    }

    /**
     * Updates the build status, i.e. sets this plug-in result status field to the corresponding {@link Result}.
     * Additionally, the {@link Result} of the build that owns this instance of {@link AnalysisResult} will be also
     * changed.
     */
    private void evaluateStatus(final ResultEvaluator resultEvaluator, final Optional<AnalysisResult> previousResult) {
        if (resultEvaluator.isEnabled()) {
            Evaluation result = resultEvaluator.evaluate(previousResult, getProject(), getNewWarnings());

            reasonForPluginResult = result.reason;
            isSuccessfulStateTouched = true;
            pluginResult = result.result;

            run.setResult(pluginResult);

            if (previousResult.isPresent()) {
                AnalysisResult previous = previousResult.get();
                // FIXME: same code to compute zero warnings
                if (isSuccessful()) {
                    if (previous.isSuccessful() && previous.isSuccessfulTouched()) {
                        successfulSinceBuild = previous.getSuccessfulSinceBuild();
                        successfulSinceDate = previous.getSuccessfulSinceDate();
                    }
                    else {
                        successfulSinceBuild = run.getNumber();
                        successfulSinceDate = run.getTimestamp().getTimeInMillis();
                    }
                    successfulHighScore = Math.max(previous.getSuccessfulHighScore(),
                            run.getTimestamp().getTimeInMillis() - successfulSinceDate);
                    if (previous.getSuccessfulHighScore() == 0) {
                        isSuccessfulHighScore = true;
                    }
                    else {
                        isSuccessfulHighScore = successfulHighScore != previous.getSuccessfulHighScore();

                    }
                    if (!isSuccessfulHighScore) {
                        successfulHighScoreGap = previous.getSuccessfulHighScore()
                                - (run.getTimestamp().getTimeInMillis() - successfulSinceDate);
                    }
                }
                else {
                    successfulHighScore = previous.getSuccessfulHighScore();
                }
            }
            else {
                if (isSuccessful()) {
                    resetSuccessfulState();
                }
            }
        }
        else {
            pluginResult = Result.SUCCESS;
            reasonForPluginResult = "No threshold set"; // FIXME: i18n
            isSuccessfulStateTouched = false;
        }
    }

    /**
     * Sets the run for this result after Jenkins read its data from disk.
     *
     * @param run
     *         the initialized run
     */
    public void setRun(final Run<?, ?> run) {
        this.run = run;
        lock = new ReentrantLock();
    }

    /**
     * Returns whether a module with an error is part of this result.
     *
     * @return <code>true</code> if at least one module has an error.
     */
    public boolean hasError() {
        return !errors.isEmpty();
    }

    /**
     * Returns the error messages associated with this result.
     *
     * @return the error messages
     */
    @Exported
    public ImmutableList<String> getErrors() {
        return errors;
    }

    /**
     * Returns the modules in this result.
     *
     * @return the modules
     */
    @Exported
    public ImmutableSet<String> getModules() {
        return modules;
    }

    /**
     * Returns the number of modules in this result.
     *
     * @return the number of modules
     */
    public int getNumberOfModules() {
        return numberOfModules;
    }

    /**
     * Returns the defined default encoding.
     *
     * @return the default encoding
     */
    public String getDefaultEncoding() {
        return defaultEncoding;
    }

    /**
     * Returns the serialization file for all warnings.
     *
     * @return the serialization file.
     */
    private XmlFile getDataFile() {
        return new XmlFile(getXStream(), new File(getRun().getRootDir(), getSerializationFileName()));
    }

    /**
     * Returns the serialization file for the fixed warnings.
     *
     * @return the serialization file.
     */
    private XmlFile getFixedDataFile() {
        return new XmlFile(getXStream(), new File(getRun().getRootDir(),
                getSerializationFileName().replace(".xml", "-fixed.xml")));
    }

    /**
     * Returns the {@link XStream} to use.
     *
     * @return the annotation stream to use
     */
    private XStream getXStream() {
        return new AnnotationStream();
    }

    private String getSerializationFileName() {
        return id + "-issues.xml";
    }

    /**
     * Returns whether author and commit information should be gathered.
     *
     * @return if {@code true} author and commit information are shown, otherwise this information is hidden
     */
    public boolean useAuthors() {
        return !GlobalSettings.instance().getNoAuthors();
    }

    /**
     * Returns the build as owner of this action.
     *
     * @return the owner
     */
    public Run<?, ?> getRun() {
        return run;
    }

    private void serializeAnnotations(final Issues allIssues, final Issues fixedIssues) {
        try {
            getDataFile().write(allIssues);
        }
        catch (IOException exception) {
            LOGGER.log(Level.SEVERE, "Failed to serialize all issues of the run.", exception);
        }
        try {
            getFixedDataFile().write(fixedIssues);
        }
        catch (IOException exception) {
            LOGGER.log(Level.SEVERE, "Failed to serialize the fixed issues of the build.", exception);
        }
    }

    // FIXME: issues rather than warnings

    /**
     * Returns the build since we have zero warnings.
     *
     * @return the build since we have zero warnings
     */
    @Override
    @Exported
    public int getZeroWarningsSinceBuild() {
        return zeroWarningsSinceBuild;
    }

    /**
     * Returns the time since we have zero warnings.
     *
     * @return the time since we have zero warnings
     */
    @Exported
    public long getZeroWarningsSinceDate() {
        return zeroWarningsSinceDate;
    }

    /**
     * Returns the maximum period with zero warnings in a build.
     *
     * @return the time since we have zero warnings
     */
    @Override
    @Exported
    public long getZeroWarningsHighScore() {
        return zeroWarningsHighScore;
    }

    /**
     * Returns if the current result reached the old zero warnings high score.
     *
     * @return <code>true</code>, if the current result reached the old zero warnings high score.
     */
    @Override
    @Exported
    public boolean isNewZeroWarningsHighScore() {
        return isZeroWarningsHighScore;
    }

    /**
     * Returns the number of msec still to go before a new high score is reached.
     *
     * @return the number of msec still to go before a new high score is reached.
     */
    @Override
    public long getHighScoreGap() {
        return highScoreGap;
    }

    /**
     * Returns the build since we are successful.
     *
     * @return the build since we are successful
     */
    @Exported
    public int getSuccessfulSinceBuild() {
        return successfulSinceBuild;
    }

    /**
     * Returns the time since we are successful.
     *
     * @return the time since we are successful
     */
    @Exported
    public long getSuccessfulSinceDate() {
        return successfulSinceDate;
    }

    /**
     * Returns the maximum period of successful builds.
     *
     * @return the maximum period of successful builds
     */
    @Override
    @Exported
    public long getSuccessfulHighScore() {
        return successfulHighScore;
    }

    /**
     * Returns if the current result reached the old successful high score.
     *
     * @return <code>true</code>, if the current result reached the old successful high score.
     */
    @Override
    @Exported
    public boolean isNewSuccessfulHighScore() {
        return isSuccessfulHighScore;
    }

    /**
     * Returns the number of msec still to go before a new highscore is reached.
     *
     * @return the number of msec still to go before a new highscore is reached.
     */
    @Override
    public long getSuccessfulHighScoreGap() {
        return successfulHighScoreGap;
    }

    /**
     * Gets the number of warnings.
     *
     * @return the number of warnings
     */
    @Exported
    public int getNumberOfWarnings() {
        return numberOfWarnings;
    }

    /**
     * Gets the number of warnings.
     *
     * @return the number of warnings
     */
    public int getNumberOfAnnotations() {
        return getNumberOfWarnings();
    }

    /**
     * Returns the total number of warnings of the specified priority for this object.
     *
     * @param priority
     *         the priority
     *
     * @return total number of annotations of the specified priority for this object
     */
    public int getNumberOfAnnotations(final Priority priority) {
        if (priority == Priority.HIGH) {
            return highWarnings;
        }
        else if (priority == Priority.NORMAL) {
            return normalWarnings;
        }
        else {
            return lowWarnings;
        }
    }

    /**
     * Gets the number of fixed warnings.
     *
     * @return the number of fixed warnings
     */
    @Exported
    public int getNumberOfFixedWarnings() {
        return numberOfFixedWarnings;
    }

    /**
     * Gets the number of new warnings.
     *
     * @return the number of new warnings
     */
    @Exported
    public int getNumberOfNewWarnings() {
        return numberOfNewWarnings;
    }

    /**
     * Returns the number of warnings with high priority.
     *
     * @return the number of warnings with high priority
     */
    @Exported
    public int getNumberOfHighPriorityWarnings() {
        return highWarnings;
    }

    /**
     * Returns the number of warnings with normal priority.
     *
     * @return the number of warnings with normal priority
     */
    @Exported
    public int getNumberOfNormalPriorityWarnings() {
        return normalWarnings;
    }

    /**
     * Returns the number of warnings with low priority.
     *
     * @return the number of warnings with low priority
     */
    @Exported
    public int getNumberOfLowPriorityWarnings() {
        return lowWarnings;
    }

    /**
     * Returns the associated project of this result.
     *
     * @return the associated project of this result.
     */
    public Issues getProject() {
        lock.lock();
        try {
            if (project == null) {
                return loadResult();
            }
            Issues result = project.get();
            if (result == null) {
                return loadResult();
            }
            return result;
        }
        finally {
            lock.unlock();
        }
    }

    /**
     * Loads the results and wraps them in a weak reference that might get removed by the garbage collector.
     *
     * @return the loaded result
     */
    private Issues loadResult() {
        Issues result = readIssues();

        project = new WeakReference<>(result);

        return result;
    }

    private Issues readIssues() {
        try {
            Issues result = (Issues) getDataFile().read();

            LOGGER.log(Level.FINE, "Loaded data file " + getDataFile() + " for run " + getRun());

            return result;
        }
        catch (IOException exception) {
            LOGGER.log(Level.WARNING, "Failed to load " + getDataFile(), exception);

            return new Issues();
        }
    }

    /**
     * Returns the new warnings of this build.
     *
     * @return the new warnings of this build
     */
    @Exported
    public Issues getNewWarnings() {
        if (newWarningsReference == null) {
            return loadNewWarnings();
        }
        Issues result = newWarningsReference.get();
        if (result == null) {
            return loadNewWarnings();
        }
        return result;
    }

    /**
     * Filters all warnings by the current build number and wraps them in a weak reference that might get removed by the
     * garbage collector.
     *
     * @return the new warnings
     */
    private Issues loadNewWarnings() {
        Issues newWarnings = new Issues();
        for (Issue issue : getProject().all()) {
            // FIXME: build is no property
            // if (warning.getBuild() == getRun().getNumber()) {
            //     newWarnings.add(warning);
            // }
        }
        newWarningsReference = new WeakReference<>(newWarnings);

        return newWarnings;
    }

    /**
     * Returns the fixed warnings of this build.
     *
     * @return the fixed warnings of this build.
     */
    public Issues getFixedWarnings() {
        lock.lock();
        try {
            if (fixedWarningsReference == null) {
                return loadFixedWarnings();
            }
            Issues result = fixedWarningsReference.get();
            if (result == null) {
                return loadFixedWarnings();
            }
            return result;
        }
        finally {
            lock.unlock();
        }
    }

    private Issues loadFixedWarnings() {
        Issues fixedWarnings;
        try {
            fixedWarnings = (Issues) getFixedDataFile().read();

            LOGGER.log(Level.FINE, "Loaded data file " + getFixedDataFile() + " for build " + getRun().getNumber());
        }
        catch (IOException exception) {
            LOGGER.log(Level.WARNING, "Failed to load " + getFixedDataFile(), exception);
            fixedWarnings = new Issues();
        }
        fixedWarningsReference = new WeakReference<>(fixedWarnings);

        return fixedWarnings;

    }

   /**
     * Returns all possible priorities.
     *
     * @return all priorities
     */
    public Priority[] getPriorities() {
        return Priority.values();
    }

    // TODO: group all stapler/UI related methods
    public ImmutableList<Issue> getAnnotations(final String priority) {
        return getContainer().findByProperty(issue -> issue.getPriority().name().equalsIgnoreCase(priority));
    }

    public int getNumberOfAnnotations(final String priority) {
        return getNumberOfAnnotations(Priority.fromString(priority));
    }

    /**
     * Gets the annotation container.
     *
     * @return the container
     */
    public Issues getContainer() {
        return getProject();
    }

    /**
     * Gets the remote API for this build result.
     *
     * @return the remote API
     */
    public Api getApi() {
        return new Api(this);
    }

    /**
     * Returns whether this build is successful with respect to the {@link HealthDescriptor} of this result.
     *
     * @return <code>true</code> if the build is successful, <code>false</code> if the build has been set to {@link
     *         Result#UNSTABLE} or {@link Result#FAILURE} by this result.
     */
    @Override
    public boolean isSuccessful() {
        return pluginResult == Result.SUCCESS;
    }

    /**
     * Returns the {@link Result} of the plug-in.
     *
     * @return the plugin result
     */
    @Override
    @Exported
    public Result getPluginResult() {
        return pluginResult;
    }

    /**
     * Returns whether the successful state has been touched.
     *
     * @return <code>true</code> if the successful state has been touched, <code>false</code> otherwise
     */
    @Override
    public boolean isSuccessfulTouched() {
        return isSuccessfulStateTouched;
    }

    /**
     * /** Resets the successful high score counters.
     */
    private void resetSuccessfulState() {
        successfulSinceBuild = run.getNumber();
        successfulSinceDate = run.getTimestamp().getTimeInMillis();
        isSuccessfulHighScore = true;
        successfulHighScore = 0;
    }

    /**
     * Returns the reason for the computed value of the build result.
     *
     * @return the reason
     */
    public String getReasonForPluginResult() {
        return reasonForPluginResult;
    }

    public String getSummary() {
        return getTool().getSummary(getNumberOfAnnotations(), getNumberOfModules());
    }

    /**
     * Returns the detail messages for the summary.jelly file.
     *
     * @return the summary message
     */
    public String getDetails() {
        return new Summary(id, name, this).toString();
    }

    /**
     * Returns the header for the build result page.
     *
     * @return the header for the build result page
     */
    public String getHeader() {
        return getTool().getLinkName();
    }

    @Override
    public String toString() {
        return getDisplayName() + " : " + getNumberOfAnnotations() + " annotations";
    }

    private StaticAnalysisLabelProvider getTool() {
        return StaticAnalysisTool.find(id, name);
    }

    public String getDisplayName() {
        return getTool().getLinkName();
    }

    @Override
    public String getReason() {
        return reasonForPluginResult;
    }

    @Override
    public int getReferenceBuild() {
        return referenceBuild;
    }

    @Override
    public int getFixedSize() {
        return numberOfFixedWarnings;
    }

    @Override
    public int getTotalSize() {
        return numberOfWarnings;
    }

    @Override
    public int getTotalHighPrioritySize() {
        return highWarnings;
    }

    @Override
    public int getTotalNormalPrioritySize() {
        return normalWarnings;
    }

    @Override
    public int getTotalLowPrioritySize() {
        return lowWarnings;
    }

    @Override
    public int getNewSize() {
        return numberOfNewWarnings;
    }

    @Override
    public int getNewHighPrioritySize() {
        return highNewWarnings;
    }

    @Override
    public int getNewNormalPrioritySize() {
        return normalNewWarnings;
    }

    @Override
    public int getNewLowPrioritySize() {
        return lowNewWarnings;
    }

}

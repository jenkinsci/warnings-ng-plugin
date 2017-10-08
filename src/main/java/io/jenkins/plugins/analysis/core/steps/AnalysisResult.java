package io.jenkins.plugins.analysis.core.steps; // NOPMD

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.thoughtworks.xstream.XStream;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.jenkins.plugins.analysis.core.history.ReferenceProvider;
import io.jenkins.plugins.analysis.core.steps.ResultEvaluator.Evaluation;

import hudson.XmlFile;
import hudson.model.Api;
import hudson.model.ModelObject;
import hudson.model.Result;
import hudson.model.Run;
import hudson.plugins.analysis.core.GlobalSettings;
import hudson.plugins.analysis.core.HealthDescriptor;
import hudson.plugins.analysis.core.IssueDifference;
import hudson.plugins.analysis.core.ParserResult;
import hudson.plugins.analysis.util.ToolTipProvider;
import hudson.plugins.analysis.util.model.AnnotationContainer;
import hudson.plugins.analysis.util.model.AnnotationProvider;
import hudson.plugins.analysis.util.model.AnnotationStream;
import hudson.plugins.analysis.util.model.AnnotationsLabelProvider;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.JavaProject;
import hudson.plugins.analysis.util.model.Priority;
import hudson.plugins.analysis.views.DetailFactory;

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
public class AnalysisResult implements ModelObject, Serializable, AnnotationProvider, StaticAnalysisRun2 {
    private static final long serialVersionUID = 1110545450292087475L;
    private static final Logger LOGGER = Logger.getLogger(AnalysisResult.class.getName());

    private final String id;

    private transient ReentrantLock lock = new ReentrantLock();
    private transient Run<?, ?> run;

    /** The project containing the annotations. */
    @SuppressFBWarnings("Se")
    private transient WeakReference<JavaProject> project;
    /** All new warnings in the current build. */
    @SuppressFBWarnings("Se")
    private transient WeakReference<Collection<FileAnnotation>> newWarningsReference;
    /** All fixed warnings in the current build. */
    @SuppressFBWarnings("Se")
    private transient WeakReference<Collection<FileAnnotation>> fixedWarningsReference;

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
    public AnalysisResult(final String id, final Run run, final ReferenceProvider referenceProvider,
            final Optional<AnalysisResult> previousResult, final ResultEvaluator resultEvaluator, final String defaultEncoding,
            final ParserResult... issues) {
        this(id, run, referenceProvider, previousResult, resultEvaluator, defaultEncoding, merge(issues), true);
    }

    private static ParserResult merge(final ParserResult... issues) {
        ParserResult merged = issues[0];
        for (int i = 1; i < issues.length; i++) {
            merged.addProject(issues[i]);
        }
        return merged;
    }

    /**
     * Creates a new instance of {@link AnalysisResult}.
     *
     * @param build
     *         the current build as owner of this action
     * @param referenceProvider
     *         build referenceProvider
     */
    protected AnalysisResult(final String id, final Run<?, ?> build, final ReferenceProvider referenceProvider,
            final Optional<AnalysisResult> previousResult, final ResultEvaluator resultEvaluator, final String defaultEncoding,
            final ParserResult result, final boolean canSerialize) {
        this.id = id;
        run = build;
        this.defaultEncoding = defaultEncoding;

        modules = ImmutableSet.copyOf(result.getModules());
        numberOfModules = modules.size();

        errors = ImmutableList.copyOf(result.getErrorMessages());

        numberOfWarnings = result.getNumberOfAnnotations();

        highWarnings = result.getNumberOfAnnotations(Priority.HIGH);
        normalWarnings = result.getNumberOfAnnotations(Priority.NORMAL);
        lowWarnings = result.getNumberOfAnnotations(Priority.LOW);

        referenceBuild = referenceProvider.getNumber();
        AnnotationContainer referenceResult = referenceProvider.getIssues();

        Set<FileAnnotation> allWarnings = result.getAnnotations();
        JavaProject container = new JavaProject();
        container.addAnnotations(allWarnings);
        project = new WeakReference<>(container);

        IssueDifference difference = new IssueDifference(allWarnings, referenceResult.getAnnotations());
        Set<FileAnnotation> newWarnings = difference.getNewIssues();
        for (FileAnnotation newWarning : newWarnings) {
            newWarning.setBuild(build.getNumber());
        }

        ParserResult newWarningsResult = new ParserResult(newWarnings);
        numberOfNewWarnings = newWarnings.size();
        highNewWarnings = newWarningsResult.getNumberOfAnnotations(Priority.HIGH);
        normalNewWarnings = newWarningsResult.getNumberOfAnnotations(Priority.NORMAL);
        lowNewWarnings = newWarningsResult.getNumberOfAnnotations(Priority.LOW);
        newWarningsReference = new WeakReference<>(newWarnings);

        Set<FileAnnotation> fixedWarnings = difference.getFixedIssues();
        numberOfFixedWarnings = fixedWarnings.size();
        fixedWarningsReference = new WeakReference<>(fixedWarnings);

        computeZeroWarningsHighScore(build, result, previousResult);

        evaluateStatus(resultEvaluator, previousResult);

        if (canSerialize) {
            serializeAnnotations(allWarnings, fixedWarnings);
        }
    }

    /**
     * Computes the zero warnings high score based on the current build and the previous build (with results of the
     * associated plug-in).
     *
     * @param build
     *         the current build
     */
    private void computeZeroWarningsHighScore(final Run<?, ?> build, final ParserResult currentResult,
            final Optional<AnalysisResult> previousResult) {
        if (previousResult.isPresent()) {
            AnalysisResult previous = previousResult.get();
            if (currentResult.hasNoAnnotations()) {
                if (previous.hasNoAnnotations()) {
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
            if (currentResult.hasNoAnnotations()) {
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
            Evaluation result = resultEvaluator.evaluate(previousResult, getAnnotations(), getNewWarnings());

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

    @Override
    public boolean hasAnnotations(final Priority priority) {
        return getContainer().hasAnnotations(priority);
    }

    @Override
    public boolean hasAnnotations(final String priority) {
        return getContainer().hasAnnotations(priority);
    }

    @Override
    public boolean hasAnnotations() {
        return numberOfWarnings != 0;
    }

    @Override
    public boolean hasNoAnnotations() {
        return numberOfWarnings == 0;
    }

    @Override
    public boolean hasNoAnnotations(final Priority priority) {
        return getContainer().hasAnnotations(priority);
    }

    @Override
    public boolean hasNoAnnotations(final String priority) {
        return getContainer().hasAnnotations(priority);
    }

    @Override
    @Exported(name = "issues")
    public Set<FileAnnotation> getAnnotations() {
        return getContainer().getAnnotations();
    }

    @Override
    public FileAnnotation getAnnotation(final long key) {
        return getContainer().getAnnotation(key);
    }

    @Override
    public FileAnnotation getAnnotation(final String key) {
        return getContainer().getAnnotation(key);
    }

    @Override
    public Set<FileAnnotation> getAnnotations(final Priority priority) {
        return getContainer().getAnnotations(priority);
    }

    private void serializeAnnotations(final Collection<FileAnnotation> annotations,
            final Collection<FileAnnotation> fixedWarnings) {
        try {
            getDataFile().write(annotations.toArray(new FileAnnotation[annotations.size()]));

            Set<FileAnnotation> allAnnotations = new HashSet<>();
            allAnnotations.addAll(annotations);
            getFixedDataFile().write(fixedWarnings.toArray(new FileAnnotation[fixedWarnings.size()]));
        }
        catch (IOException exception) {
            LOGGER.log(Level.SEVERE, "Failed to serialize the annotations of the build.", exception);
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
    @Override
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
    @Override
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
    public JavaProject getProject() {
        lock.lock();
        try {
            if (project == null) {
                return loadResult();
            }
            JavaProject result = project.get();
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
    private JavaProject loadResult() {
        JavaProject result;
        try {
            JavaProject newProject = new JavaProject();
            FileAnnotation[] annotations = (FileAnnotation[]) getDataFile().read();
            newProject.addAnnotations(annotations);
            newProject.setLabelProvider(new AnnotationsLabelProvider(newProject.getPackageCategoryTitle()));

            LOGGER.log(Level.FINE, "Loaded data file " + getDataFile() + " for build " + getRun().getNumber());
            result = newProject;
        }
        catch (IOException exception) {
            LOGGER.log(Level.WARNING, "Failed to load " + getDataFile(), exception);
            result = new JavaProject();
        }
        project = new WeakReference<>(result);

        return result;
    }

    /**
     * Returns the new warnings of this build.
     *
     * @return the new warnings of this build
     */
    @Exported
    public Collection<FileAnnotation> getNewWarnings() {
        if (newWarningsReference == null) {
            return loadNewWarnings();
        }
        Collection<FileAnnotation> result = newWarningsReference.get();
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
    private Collection<FileAnnotation> loadNewWarnings() {
        Set<FileAnnotation> newWarnings = new HashSet<>();
        for (FileAnnotation warning : getProject().getAnnotations()) {
            if (warning.getBuild() == getRun().getNumber()) {
                newWarnings.add(warning);
            }
        }
        newWarningsReference = new WeakReference<>(newWarnings);

        return newWarnings;
    }

    /**
     * Returns the fixed warnings of this build.
     *
     * @return the fixed warnings of this build.
     */
    public Collection<FileAnnotation> getFixedWarnings() {
        lock.lock();
        try {
            if (fixedWarningsReference == null) {
                return loadFixedWarnings();
            }
            Collection<FileAnnotation> result = fixedWarningsReference.get();
            if (result == null) {
                return loadFixedWarnings();
            }
            return result;
        }
        finally {
            lock.unlock();
        }
    }

    private Collection<FileAnnotation> loadFixedWarnings() {
        Set<FileAnnotation> fixedWarnings;
        try {
            FileAnnotation[] annotations = (FileAnnotation[]) getFixedDataFile().read();
            fixedWarnings = Sets.newHashSet(annotations);

            LOGGER.log(Level.FINE, "Loaded data file " + getFixedDataFile() + " for build " + getRun().getNumber());
        }
        catch (IOException exception) {
            LOGGER.log(Level.WARNING, "Failed to load " + getFixedDataFile(), exception);
            fixedWarnings = new HashSet<>();
        }
        fixedWarningsReference = new WeakReference<>(fixedWarnings);

        return fixedWarnings;

    }

    /**
     * Returns the dynamic result of the selection element.
     *
     * @param link
     *         the link to identify the sub page to show
     * @param request
     *         Stapler request
     * @param response
     *         Stapler response
     *
     * @return the dynamic result of the analysis (detail page).
     */
    public Object getDynamic(final String link, final StaplerRequest request, final StaplerResponse response) {
        try {
            return new DetailFactory().createTrendDetails(link, getRun(), getContainer(), getFixedWarnings(),
                    getNewWarnings(), getErrors(), getDefaultEncoding(), getDisplayName());
        }
        catch (NoSuchElementException exception) {
            try {
                response.sendRedirect2("../");
            }
            catch (IOException e) {
                // ignore
            }
            return this; // fallback on broken URLs
        }
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
    @Override
    public Set<FileAnnotation> getAnnotations(final String priority) {
        return getContainer().getAnnotations(priority);
    }

    @Override
    public int getNumberOfAnnotations(final String priority) {
        return getNumberOfAnnotations(Priority.fromString(priority));
    }

    /**
     * Gets the annotation container.
     *
     * @return the container
     */
    public AnnotationContainer getContainer() {
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
        return getIssueParser().getSummary(getNumberOfAnnotations(), getNumberOfModules());
    }

    /**
     * Returns the detail messages for the summary.jelly file.
     *
     * @return the summary message
     */
    public String getDetails() {
        return new Summary(id, this).toString();
    }

    /**
     * Returns the header for the build result page.
     *
     * @return the header for the build result page
     */
    public String getHeader() {
        return StringUtils.EMPTY;
    }

    @Override
    public String toString() {
        return getDisplayName() + " : " + getNumberOfAnnotations() + " annotations";
    }

    // FIXME: How is this implemented in freestyle build?
    public ToolTipProvider getToolTipProvider() {
        return new ToolTipProvider() {
            @Override
            public String getTooltip(final int numberOfItems) {
                return "FIXME";
            }
        };
    }

    private StaticAnalysisTool getIssueParser() {
        return StaticAnalysisTool.find(id);
    }

    @Override
    public String getDisplayName() {
        return getIssueParser().getLinkName();
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

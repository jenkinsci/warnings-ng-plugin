package hudson.plugins.analysis.core; // NOPMD

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import com.thoughtworks.xstream.XStream;

import hudson.XmlFile;

import hudson.model.ModelObject;
import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.model.Api;
import hudson.model.Hudson;

import hudson.plugins.analysis.Messages;
import hudson.plugins.analysis.util.model.AnnotationContainer;
import hudson.plugins.analysis.util.model.AnnotationProvider;
import hudson.plugins.analysis.util.model.AnnotationStream;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.JavaProject;
import hudson.plugins.analysis.util.model.MavenModule;
import hudson.plugins.analysis.util.model.Priority;
import hudson.plugins.analysis.views.DetailFactory;

/**
 * A base class for build results that is capable of storing a reference to the
 * current build. Provides support for persisting the results of the build and
 * loading and saving of annotations (all, new, and fixed) and delta
 * computation.
 *
 * @author Ulli Hafner
 */
//CHECKSTYLE:COUPLING-OFF
@ExportedBean
@SuppressWarnings({"PMD.TooManyFields", "PMD.ExcessiveClassLength"})
public abstract class BuildResult implements ModelObject, Serializable, AnnotationProvider {
    private static final long serialVersionUID = 1110545450292087475L;
    private static final Logger LOGGER = Logger.getLogger(BuildResult.class.getName());

    private static final String UNSTABLE = "yellow.gif";
    private static final String FAILED = "red.gif";
    private static final String SUCCESS = "blue.gif";

    private Object projectLock = new Object();

    /**
     * Returns the number of days for the specified number of milliseconds.
     *
     * @param ms
     *            milliseconds
     * @return the number of days
     */
    public static long getDays(final long ms) {
        return Math.max(1, ms / DateUtils.MILLIS_PER_DAY);
    }

    /** Current build as owner of this action. */
    private AbstractBuild<?, ?> owner;
    /** All parsed modules. */
    private Set<String> modules;
    /** The total number of parsed modules (regardless if there are annotations). */
    private int numberOfModules;
    /** The default encoding to be used when reading and parsing files. */
    private String defaultEncoding;

    /** The project containing the annotations. */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("Se")
    private transient WeakReference<JavaProject> project;
    /** All new warnings in the current build. */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("Se")
    private transient WeakReference<Collection<FileAnnotation>> newWarningsReference;
    /** All fixed warnings in the current build. */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("Se")
    private transient WeakReference<Collection<FileAnnotation>> fixedWarningsReference;
    /** The build history for the results of this plug-in. */
    private transient BuildHistory history;

    /** The number of warnings in this build. */
    private int numberOfWarnings;
    /** The number of new warnings in this build. */
    private int numberOfNewWarnings;
    /** The number of fixed warnings in this build. */
    private int numberOfFixedWarnings;

    /** Difference between this and the previous build. */
    private int delta;
    /** Difference between this and the previous build (Priority low). */
    private int lowDelta;
    /** Difference between this and the previous build (Priority normal). */
    private int normalDelta;
    /** Difference between this and the previous build (Priority high). */
    private int highDelta;

    /** The number of low priority warnings in this build. */
    private int lowWarnings;
    /** The number of normal priority warnings in this build. */
    private int normalWarnings;
    /** The number of high priority warnings in this build. */
    private int highWarnings;

    /** Determines since which build we have zero warnings. */
    private int zeroWarningsSinceBuild;
    /** Determines since which time we have zero warnings. */
    private long zeroWarningsSinceDate;
    /** Determines the zero warnings high score. */
    private long zeroWarningsHighScore;
    /** Determines if the old zero high score has been broken. */
    private boolean isZeroWarningsHighscore;
    /** Determines the number of msec still to go before a new high score is reached. */
    private long highScoreGap;
    /** Error messages. */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("Se")
    private List<String> errors;

    /**
     * The build result of the associated plug-in. This result is an additional
     * state that denotes if this plug-in has changed the overall build result.
     *
     * @since 1.4
     */
    private Result pluginResult = Result.SUCCESS;
    /**
     * Determines since which build the result is successful.
     *
     * @since 1.4
     */
    private int successfulSinceBuild;
    /**
     * Determines since which time the result is successful.
     *
     * @since 1.4
     */
    private long successfulSinceDate;
    /**
     * Determines the succesful build result high score.
     *
     * @since 1.4
     */
    private long successfulHighscore;
    /**
     * Determines if the old successful build result high score has been broken.
     *
     * @since 1.4
     */
    private boolean isSuccessfulHighscore;
    /**
     * Determines the number of msec still to go before a new high score is
     * reached.
     *
     * @since 1.4
     */
    private long successfulHighScoreGap;
    /**
     * Determines if this result has touched the successful state.
     *
     * @since 1.4
     */
    private boolean isSuccessfulStateTouched;

    /**
     * Creates a new instance of {@link BuildResult}.
     *
     * @param build
     *            the current build as owner of this action
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     * @param result
     *            the parsed result with all annotations
     * @param history
     *            the history of build results of the associated plug-in
     */
    public BuildResult(final AbstractBuild<?, ?> build, final String defaultEncoding, final ParserResult result,
            final BuildHistory history) {
        initialize(history, build, defaultEncoding, result);
    }

    /**
     * Creates a new instance of {@link BuildResult}.
     *
     * @param build
     *            the current build as owner of this action
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     * @param result
     *            the parsed result with all annotations
     */
    public BuildResult(final AbstractBuild<?, ?> build, final String defaultEncoding, final ParserResult result) {
        initialize(createHistory(build), build, defaultEncoding, result);
    }

    /**
     * Creates a new history based on the specified build.
     *
     * @param build
     *            the build to start with
     * @return the history
     */
    private BuildHistory createHistory(final AbstractBuild<?, ?> build) {
        return new BuildHistory(build, getResultActionType());
    }

    /**
     * Initializes this new instance of {@link BuildResult}.
     *
     * @param build
     *            the current build as owner of this action
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     * @param result
     *            the parsed result with all annotations
     * @param history
     *            the history of build results of the associated plug-in
     */
    private void initialize(final BuildHistory history, final AbstractBuild<?, ?> build, final String defaultEncoding, // NOCHECKSTYLE
            final ParserResult result) {
        this.history = history;
        owner = build;
        this.defaultEncoding = defaultEncoding;

        modules = new HashSet<String>(result.getModules());
        numberOfModules = modules.size();
        errors = new ArrayList<String>(result.getErrorMessages());
        numberOfWarnings = result.getNumberOfAnnotations();
        AnnotationContainer referenceResult = history.getReferenceAnnotations();

        delta = result.getNumberOfAnnotations() - referenceResult.getNumberOfAnnotations();
        lowDelta = computeDelta(result, referenceResult, Priority.LOW);
        normalDelta = computeDelta(result, referenceResult, Priority.NORMAL);
        highDelta = computeDelta(result, referenceResult, Priority.HIGH);

        Set<FileAnnotation> allWarnings = result.getAnnotations();

        Set<FileAnnotation> newWarnings = AnnotationDifferencer.getNewAnnotations(allWarnings, referenceResult.getAnnotations());
        numberOfNewWarnings = newWarnings.size();
        newWarningsReference = new WeakReference<Collection<FileAnnotation>>(newWarnings);

        Set<FileAnnotation> fixedWarnings = AnnotationDifferencer.getFixedAnnotations(allWarnings, referenceResult.getAnnotations());
        numberOfFixedWarnings = fixedWarnings.size();
        fixedWarningsReference = new WeakReference<Collection<FileAnnotation>>(fixedWarnings);

        highWarnings = result.getNumberOfAnnotations(Priority.HIGH);
        normalWarnings = result.getNumberOfAnnotations(Priority.NORMAL);
        lowWarnings = result.getNumberOfAnnotations(Priority.LOW);

        serializeAnnotations(result.getAnnotations());

        JavaProject container = new JavaProject();
        container.addAnnotations(result.getAnnotations());

        project = new WeakReference<JavaProject>(container);

        computeZeroWarningsHighScore(build, result);
    }

    private int computeDelta(final ParserResult result, final AnnotationContainer referenceResult, final Priority priority) {
        return result.getNumberOfAnnotations(priority) - referenceResult.getNumberOfAnnotations(priority);
    }

    /**
     * Computes the zero warnings high score based on the current build and the
     * previous build (with results of the associated plug-in).
     *
     * @param build
     *            the current build
     * @param currentResult
     *            the current result
     */
    private void computeZeroWarningsHighScore(final AbstractBuild<?, ?> build, final ParserResult currentResult) {
        if (history.hasPreviousResult()) {
            BuildResult previous = history.getPreviousResult();
            if (currentResult.hasNoAnnotations()) {
                if (previous.hasNoAnnotations()) {
                    zeroWarningsSinceBuild = previous.getZeroWarningsSinceBuild();
                    zeroWarningsSinceDate = previous.getZeroWarningsSinceDate();
                }
                else {
                    zeroWarningsSinceBuild = build.getNumber();
                    zeroWarningsSinceDate = build.getTimestamp().getTimeInMillis();
                }
                zeroWarningsHighScore = Math.max(previous.getZeroWarningsHighScore(), build.getTimestamp().getTimeInMillis() - zeroWarningsSinceDate);
                if (previous.getZeroWarningsHighScore() == 0) {
                    isZeroWarningsHighscore = true;
                }
                else {
                    isZeroWarningsHighscore = zeroWarningsHighScore != previous.getZeroWarningsHighScore();

                }
                if (!isZeroWarningsHighscore) {
                    highScoreGap = previous.getZeroWarningsHighScore() - (build.getTimestamp().getTimeInMillis() - zeroWarningsSinceDate);
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
                isZeroWarningsHighscore = true;
                zeroWarningsHighScore = 0;
            }
        }
    }

    /**
     * Returns whether a module with an error is part of this project.
     *
     * @return <code>true</code> if at least one module has an error.
     */
    public boolean hasError() {
        return !errors.isEmpty();
    }

    /**
     * Returns the error messages associated with this build.
     *
     * @return the error messages
     */
    public List<String> getErrors() {
        return errors;
    }

    /**
     * Initializes members that were not present in previous versions of the associated plug-in.
     *
     * @return the created object
     */
    @SuppressWarnings("PMD")
    protected Object readResolve() {
        if (pluginResult == null) {
            pluginResult = Result.SUCCESS;
            resetSuccessfulState();
        }
        if (projectLock == null) {
            projectLock = new Object();
        }
        if (history == null) {
            history = createHistory(owner);
        }
        if (modules == null) {
            modules = new HashSet<String>();
        }
        if (errors == null) {
            errors = new ArrayList<String>();
        }
        try {
            if (low != null) {
                lowWarnings = Integer.valueOf(low);
            }
            if (normal != null) {
                normalWarnings = Integer.valueOf(normal);
            }
            if (high != null) {
                highWarnings = Integer.valueOf(high);
            }
        }
        catch (NumberFormatException exception) {
            // ignore, and start with zero
        }
        return this;
    }

    /**
     * Returns the modules of this build result.
     *
     * @return the modules
     */
    public Collection<String> getModules() {
        return modules;
    }

    /**
     * Returns the number of modules in this project.
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
     * Returns the serialization file.
     *
     * @return the serialization file.
     */
    public final XmlFile getDataFile() {
        return new XmlFile(getXStream(), new File(getOwner().getRootDir(), getSerializationFileName()));
    }

    /**
     * Returns the {@link XStream} to use.
     *
     * @return the annotation stream to use
     */
    private XStream getXStream() {
        AnnotationStream xstream = new AnnotationStream();
        configure(xstream);

        return xstream;
    }

    /**
     * Configures the {@link XStream}. This default implementation is empty.
     *
     * @param xstream the stream to configure
     */
    protected void configure(final XStream xstream) {
        // empty default
    }

    /**
     * Returns the name of the file to store the serialized annotations.
     *
     * @return the name of the file to store the serialized annotations
     */
    protected abstract String getSerializationFileName();

    /**
     * Returns whether this result belongs to the last build.
     *
     * @return <code>true</code> if this result belongs to the last build
     */
    public boolean isCurrent() {
        return getOwner().getProject().getLastBuild().number == getOwner().number;
    }

    /**
     * Returns the build as owner of this action.
     *
     * @return the owner
     */
    public AbstractBuild<?, ?> getOwner() {
        return owner;
    }

    /** {@inheritDoc} */
    public boolean hasAnnotations(final Priority priority) {
        return getContainer().hasAnnotations(priority);
    }

    /** {@inheritDoc} */
    public boolean hasAnnotations(final String priority) {
        return getContainer().hasAnnotations(priority);
    }

    /** {@inheritDoc} */
    public boolean hasAnnotations() {
        return numberOfWarnings != 0;
    }

    /** {@inheritDoc} */
    public boolean hasNoAnnotations() {
        return numberOfWarnings == 0;
    }

    /** {@inheritDoc} */
    public boolean hasNoAnnotations(final Priority priority) {
        return getContainer().hasAnnotations(priority);
    }

    /** {@inheritDoc} */
    public boolean hasNoAnnotations(final String priority) {
        return getContainer().hasAnnotations(priority);
    }

    /** {@inheritDoc} */
    @Exported(name = "warnings")
    public Set<FileAnnotation> getAnnotations() {
        return getContainer().getAnnotations();
    }

    /** {@inheritDoc} */
    public FileAnnotation getAnnotation(final long key) {
        return getContainer().getAnnotation(key);
    }

    /** {@inheritDoc} */
    public FileAnnotation getAnnotation(final String key) {
        return getContainer().getAnnotation(key);
    }

    /**
     * Sets the number of high warnings to the specified value.
     *
     * @param highWarnings the value to set
     */
    protected void setHighWarnings(final int highWarnings) {
        this.highWarnings = highWarnings;
    }

    /**
     * Sets the number of normal warnings to the specified value.
     *
     * @param normalWarnings the value to set
     */
    protected void setNormalWarnings(final int normalWarnings) {
        this.normalWarnings = normalWarnings;
    }

    /**
     * Sets the number of low warnings to the specified value.
     *
     * @param lowWarnings the value to set
     */
    protected void setLowWarnings(final int lowWarnings) {
        this.lowWarnings = lowWarnings;
    }

    /**
     * Sets the number of warnings to the specified value.
     *
     * @param warnings the value to set
     */
    protected void setWarnings(final int warnings) {
        numberOfWarnings = warnings;
    }

    /** {@inheritDoc} */
    public Set<FileAnnotation> getAnnotations(final Priority priority) {
        return getContainer().getAnnotations(priority);
    }

    /**
     * Serializes the annotations of the specified project.
     *
     * @param annotations
     *            the annotations to store
     */
    private void serializeAnnotations(final Collection<FileAnnotation> annotations) {
        try {
            Collection<FileAnnotation> files = annotations;
            getDataFile().write(files.toArray(new FileAnnotation[files.size()]));
        }
        catch (IOException exception) {
            LOGGER.log(Level.SEVERE, "Failed to serialize the annotations of the build.", exception);
        }
    }

    /**
     * Returns the build since we have zero warnings.
     *
     * @return the build since we have zero warnings
     */
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
    @Exported
    public long getZeroWarningsHighScore() {
        return zeroWarningsHighScore;
    }

    /**
     * Returns if the current result reached the old zero warnings highscore.
     *
     * @return <code>true</code>, if the current result reached the old zero warnings highscore.
     */
    @Exported
    public boolean isNewZeroWarningsHighScore() {
        return isZeroWarningsHighscore;
    }

    /**
     * Returns the number of msec still to go before a new highscore is reached.
     *
     * @return the number of msec still to go before a new highscore is reached.
     */
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
    @Exported
    public long getSuccessfulHighScore() {
        return successfulHighscore;
    }

    /**
     * Returns if the current result reached the old successful highscore.
     *
     * @return <code>true</code>, if the current result reached the old successful highscore.
     */
    @Exported
    public boolean isNewSuccessfulHighScore() {
        return isSuccessfulHighscore;
    }

    /**
     * Returns the number of msec still to go before a new highscore is reached.
     *
     * @return the number of msec still to go before a new highscore is reached.
     */
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
     * Returns the total number of warnings of the specified priority for
     * this object.
     *
     * @param priority
     *            the priority
     * @return total number of annotations of the specified priority for this
     *         object
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
     * Returns the delta.
     *
     * @return the delta
     */
    public int getDelta() {
        return delta;
    }

    /**
     * Returns the high delta.
     *
     * @return the delta
     */
    public int getHighDelta() {
        return highDelta;
    }

    /**
     * Returns the normal delta.
     *
     * @return the delta
     */
    public int getNormalDelta() {
        return normalDelta;
    }

    /**
     * Returns the low delta.
     *
     * @return the delta
     */
    public int getLowDelta() {
        return lowDelta;
    }

    /**
     * Returns the delta between two builds.
     *
     * @return the delta
     */
    @Exported
    public int getWarningsDelta() {
        return delta;
    }

    /**
     * Returns the associated project of this result.
     *
     * @return the associated project of this result.
     */
    public JavaProject getProject() {
        synchronized (projectLock) {
            if (project == null) {
                return loadResult();
            }
            JavaProject result = project.get();
            if (result == null) {
                return loadResult();
            }
            return result;
        }
    }

    /**
     * Loads the results and wraps them in a weak reference that might get
     * removed by the garbage collector.
     *
     * @return the loaded result
     */
    private JavaProject loadResult() {
        JavaProject result;
        try {
            JavaProject newProject = new JavaProject();
            FileAnnotation[] annotations = (FileAnnotation[])getDataFile().read();
            newProject.addAnnotations(annotations);

            LOGGER.log(Level.INFO, "Loaded data file " + getDataFile() + " for build " + getOwner().getNumber());
            result = newProject;
        }
        catch (IOException exception) {
            LOGGER.log(Level.WARNING, "Failed to load " + getDataFile(), exception);
            result = new JavaProject();
        }
        project = new WeakReference<JavaProject>(result);

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
     * Loads the results of the current and reference build, computes the new
     * warnings and wraps them in a weak reference that might get removed by the
     * garbage collector.
     *
     * @return the new warnings
     */
    private Collection<FileAnnotation> loadNewWarnings() {
        Collection<FileAnnotation> difference = history.getNewWarnings(getProject().getAnnotations());
        newWarningsReference = new WeakReference<Collection<FileAnnotation>>(difference);

        return difference;
    }

    /**
     * Returns the fixed warnings of this build.
     *
     * @return the fixed warnings of this build.
     */
    public Collection<FileAnnotation> getFixedWarnings() {
        if (fixedWarningsReference == null) {
            return loadFixedWarnings();
        }
        Collection<FileAnnotation> result = fixedWarningsReference.get();
        if (result == null) {
            return loadFixedWarnings();
        }
        return result;
    }

    /**
     * Loads the results of the current and previous build, computes the fixed
     * warnings and wraps them in a weak reference that might get removed by the
     * garbage collector.
     *
     * @return the fixed warnings
     */
    private Collection<FileAnnotation> loadFixedWarnings() {
        Collection<FileAnnotation> difference = history.getFixedWarnings(getProject().getAnnotations());
        fixedWarningsReference = new WeakReference<Collection<FileAnnotation>>(difference);

        return difference;
    }

    /**
     * Returns the actual type of the associated result action.
     *
     * @return the actual type of the associated result action
     */
    protected abstract Class<? extends ResultAction<? extends BuildResult>> getResultActionType();

    /**
     * Returns the dynamic result of the selection element.
     *
     * @param link
     *            the link to identify the sub page to show
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     * @return the dynamic result of the analysis (detail page).
     */
    public Object getDynamic(final String link, final StaplerRequest request, final StaplerResponse response) {
        return DetailFactory.create(getResultActionType()).createTrendDetails(link, getOwner(), getContainer(), getFixedWarnings(),
                getNewWarnings(), getErrors(), getDefaultEncoding(), getDisplayName());
    }

    /**
     * Returns all possible priorities.
     *
     * @return all priorities
     */
    public Priority[] getPriorities() {
        return Priority.values();
    }

    /** {@inheritDoc} */
    public Set<FileAnnotation> getAnnotations(final String priority) {
        return getContainer().getAnnotations(priority);
    }

    /** {@inheritDoc} */
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
     * Returns whether this build is successful with respect to the
     * {@link HealthDescriptor} of this result.
     *
     * @return <code>true</code> if the build is successful, <code>false</code>
     *         if the build has been set to {@link Result#UNSTABLE} or
     *         {@link Result#FAILURE} by this result.
     */
    public boolean isSuccessful() {
        return pluginResult == Result.SUCCESS;
    }

    /**
     * Sets the Hudson build result for the associated plug-in build result.
     *
     * @param result the Hudson build result
     */
    public void setResult(final Result result) {
        isSuccessfulStateTouched = true;
        pluginResult = result;
        owner.setResult(result);

        if (history.hasPreviousResult()) {
            BuildResult previous = history.getPreviousResult();
            if (isSuccessful()) {
                if (previous.isSuccessful() && previous.isSuccessfulTouched()) {
                    successfulSinceBuild = previous.getSuccessfulSinceBuild();
                    successfulSinceDate = previous.getSuccessfulSinceDate();
                }
                else {
                    successfulSinceBuild = owner.getNumber();
                    successfulSinceDate = owner.getTimestamp().getTimeInMillis();
                }
                successfulHighscore = Math.max(previous.getSuccessfulHighScore(),
                        owner.getTimestamp().getTimeInMillis() - successfulSinceDate);
                if (previous.getSuccessfulHighScore() == 0) {
                    isSuccessfulHighscore = true;
                }
                else {
                    isSuccessfulHighscore = successfulHighscore != previous.getSuccessfulHighScore();

                }
                if (!isSuccessfulHighscore) {
                    successfulHighScoreGap = previous.getSuccessfulHighScore()
                    - (owner.getTimestamp().getTimeInMillis() - successfulSinceDate);
                }
            }
            else {
                successfulHighscore = previous.getSuccessfulHighScore();
            }
        }
        else {
            if (isSuccessful()) {
                resetSuccessfulState();
            }
        }
    }

    /**
     * Returns whether the successful state has been touched.
     *
     * @return <code>true</code> if the successful state has been touched,
     *         <code>false</code> otherwise
     */
    public boolean isSuccessfulTouched() {
        return isSuccessfulStateTouched;
    }

    /**
     * Returns whether there is a previous result available.
     *
     * @return <code>true</code> if there is a previous result available
     */
    public boolean hasPreviousResult() {
        return history.hasPreviousResult();
    }

    /**
     * Returns the previous build result.
     *
     * @return the previous build result
     */
    public BuildResult getPreviousResult() {
        return history.getPreviousResult();
    }

    /**
     * Resets the successful high score counters.
     */
    private void resetSuccessfulState() {
        successfulSinceBuild = owner.getNumber();
        successfulSinceDate = owner.getTimestamp().getTimeInMillis();
        isSuccessfulHighscore = true;
        successfulHighscore = 0;
    }

    /**
     * Returns the detail messages for the summary.jelly file.
     *
     * @return the summary message
     */
    public String getDetails() {
        String message = createDeltaMessage();
        if (getNumberOfAnnotations() == 0 && getDelta() == 0) {
            message += createNoWarningsMessage();
            message += createHighScoreMessage();
        }
        else if (isSuccessfulTouched()) {
            message += createListItem(Messages.ResultAction_Status() + getResultIcon());
            if (isSuccessful()) {
                message += createSuccessfulHighScoreMessage();
            }
        }
        return message;
    }

    /**
     * Returns the icon for the build result.
     *
     * @return the icon for the build result
     */
    public String getResultIcon() {
        String rootUrl = Hudson.getInstance().getRootUrlFromRequest();

        String message = "<img src=\"" + rootUrl + "/images/16x16/%s\" alt=\"%s\" title=\"%s\"/>";
        if (pluginResult == Result.FAILURE) {
            return String.format(message, FAILED,
                    hudson.model.Messages.BallColor_Failed(), hudson.model.Messages.BallColor_Failed());
        }
        else if (pluginResult == Result.UNSTABLE) {
            return String.format(message, UNSTABLE,
                    hudson.model.Messages.BallColor_Unstable(), hudson.model.Messages.BallColor_Unstable());
        }
        else {
            return String.format(message, SUCCESS,
                    hudson.model.Messages.BallColor_Success(), hudson.model.Messages.BallColor_Success());
        }
    }

    /**
     * Creates a message that shows the number of warnings since a given build.
     *
     * @return a message
     */
    protected String createNoWarningsMessage() {
        return createListItem(Messages.ResultAction_NoWarningsSince(getZeroWarningsSinceBuild()));
    }

    /**
     * Creates a HTML list item.
     *
     * @param text
     *            the item text
     * @return the HTML item
     */
    protected String createListItem(final String text) {
        return "<li>" + text + "</li>";
    }

    /**
     * Returns the build summary HTML message.
     *
     * @return the build summary HTML message
     */
    protected String createDeltaMessage() {
        return StringUtils.EMPTY;
    }

    /**
     * Creates a highscore message.
     *
     * @return a highscore message
     */
    protected String createHighScoreMessage() {
        String message;
        if (isNewZeroWarningsHighScore()) {
            long days = getDays(getZeroWarningsHighScore());
            if (days == 1) {
                message = Messages.ResultAction_OneHighScore();
            }
            else {
                message = Messages.ResultAction_MultipleHighScore(days);
            }
        }
        else {
            long days = getDays(getHighScoreGap());
            if (days == 1) {
                message = Messages.ResultAction_OneNoHighScore();
            }
            else {
                message = Messages.ResultAction_MultipleNoHighScore(days);
            }
        }
        return createListItem(message);
    }

    /**
     * Creates a successful highscore message.
     *
     * @return a successful highscore message
     */
    protected String createSuccessfulHighScoreMessage() {
        String message;
        if (isNewSuccessfulHighScore()) {
            long days = getDays(getSuccessfulHighScore());
            if (days == 1) {
                message = Messages.ResultAction_SuccessfulOneHighScore();
            }
            else {
                message = Messages.ResultAction_SuccessfulMultipleHighScore(days);
            }
        }
        else {
            long days = getDays(getSuccessfulHighScoreGap());
            if (days == 1) {
                message = Messages.ResultAction_SuccessfulOneNoHighScore();
            }
            else {
                message = Messages.ResultAction_SuccessfulMultipleNoHighScore(days);
            }
        }
        return createListItem(message);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return getDisplayName() + ": " + getNumberOfAnnotations() + " annotations";
    }

    // Backward compatibility. Do not remove.
    // CHECKSTYLE:OFF
    @Deprecated
    @java.lang.SuppressWarnings("unused")
    private transient Map<String, MavenModule> emptyModules; // NOPMD
    @Deprecated
    @java.lang.SuppressWarnings("PMD")
    protected transient String low;
    @Deprecated
    @java.lang.SuppressWarnings("PMD")
    protected transient String normal;
    @Deprecated
    @java.lang.SuppressWarnings("PMD")
    protected transient String high;
}

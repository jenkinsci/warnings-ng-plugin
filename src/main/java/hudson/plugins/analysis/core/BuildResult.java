package hudson.plugins.analysis.core;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.time.DateUtils;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import com.thoughtworks.xstream.XStream;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import hudson.XmlFile;

import hudson.model.AbstractBuild;
import hudson.model.Api;
import hudson.model.ModelObject;
import hudson.model.Result;

import hudson.plugins.analysis.util.model.AnnotationContainer;
import hudson.plugins.analysis.util.model.AnnotationProvider;
import hudson.plugins.analysis.util.model.AnnotationStream;
import hudson.plugins.analysis.util.model.DefaultAnnotationContainer;
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
@SuppressWarnings("PMD.TooManyFields")
@ExportedBean
public abstract class BuildResult implements ModelObject, Serializable, AnnotationProvider {
    /** Unique ID of this class. */
    private static final long serialVersionUID = 1110545450292087475L;
    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(BuildResult.class.getName());

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
    private final AbstractBuild<?, ?> owner;
    /** All parsed modules. */
    private Set<String> modules;
    /** The total number of parsed modules (regardless if there are annotations). */
    private final int numberOfModules;
    /** The default encoding to be used when reading and parsing files. */
    private final String defaultEncoding;

    /** The project containing the annotations. */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("Se")
    private transient WeakReference<JavaProject> project;
    /** All new warnings in the current build. */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("Se")
    private transient WeakReference<Collection<FileAnnotation>> newWarningsReference;
    /** All fixed warnings in the current build. */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("Se")
    private transient WeakReference<Collection<FileAnnotation>> fixedWarningsReference;

    /** The number of warnings in this build. */
    private int numberOfWarnings;
    /** The number of new warnings in this build. */
    private final int numberOfNewWarnings;
    /** The number of fixed warnings in this build. */
    private final int numberOfFixedWarnings;

    /** Difference between this and the previous build. */
    private final int delta;

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
    /** private since which time we have zero warnings. */
    private long zeroWarningsHighScore;
    /** Determines if the old zero high score has been broken. */
    private boolean isZeroWarningsHighscore;
    /** Determines the number of msec still to go before a new high score is reached. */
    private long highScoreGap;
    /** Error messages. */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("Se")
    private List<String> errors;

    /**
     * The build result of the Hudson build.
     *
     * @since 1.4
     */
    private Result pluginResult;

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
        owner = build;
        modules = new HashSet<String>(result.getModules());
        numberOfModules = modules.size();
        errors = new ArrayList<String>(result.getErrorMessages());
        this.defaultEncoding = defaultEncoding;

        numberOfWarnings = result.getNumberOfAnnotations();
        AnnotationContainer referenceResult = getReferenceResult();

        delta = result.getNumberOfAnnotations() - referenceResult.getNumberOfAnnotations();

        Collection<FileAnnotation> allWarnings = result.getAnnotations();

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

    /**
     * Computes the zero warnings high score based on the current build and the
     * previous build (with results of the same plug-in).
     *
     * @param build
     *            the current build
     * @param currentResult
     *            the current result
     */
    private void computeZeroWarningsHighScore(final AbstractBuild<?, ?> build, final ParserResult currentResult) {
        if (hasPreviousResult()) {
            BuildResult previous = getPreviousResult().getResult();
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
     * Initializes members that were not present in previous versions of this plug-in.
     *
     * @return the created object
     */
    protected Object readResolve() {
        if (pluginResult == null) {
            pluginResult = Result.SUCCESS;
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
    public final boolean isCurrent() {
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
    public final boolean hasAnnotations() {
        return getContainer().hasAnnotations();
    }

    /** {@inheritDoc} */
    public boolean hasNoAnnotations() {
        return getContainer().hasNoAnnotations();
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
    public Collection<FileAnnotation> getAnnotations() {
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
    public Collection<FileAnnotation> getAnnotations(final Priority priority) {
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
     * Returns the detail messages for the summary.jelly file.
     *
     * @return the summary message
     */
    public abstract String getDetails();

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
    public synchronized JavaProject getProject() {
        if (project == null) {
            return loadResult();
        }
        JavaProject result = project.get();
        if (result == null) {
            return loadResult();
        }
        return result;
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
     * garbage collector. The reference build is the last build that has not
     * changed the build result by the associated plug-in action (see
     * {@link #getResultActionType()}.
     *
     * @return the new warnings
     */
    private Collection<FileAnnotation> loadNewWarnings() {
        Collection<FileAnnotation> difference = getProject().getAnnotations();
        if (hasReferenceResult()) {
            difference = AnnotationDifferencer.getNewAnnotations(difference, getReferenceResult().getAnnotations());
        }
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
        Collection<FileAnnotation> difference;
        if (hasReferenceResult()) {
            difference = AnnotationDifferencer.getFixedAnnotations(getProject().getAnnotations(), getReferenceResult().getAnnotations());
        }
        else {
            difference = Collections.emptyList();
        }
        fixedWarningsReference = new WeakReference<Collection<FileAnnotation>>(difference);

        return difference;
    }

    /**
     * Returns whether a reference build result exists.
     *
     * @return <code>true</code> if a reference build result exists.
     */
    public boolean hasReferenceResult() {
        return getReferenceAction() != null;
    }

    /**
     * Returns the results of the reference build.
     *
     * @return the result of the reference build
     */
    public AnnotationContainer getReferenceResult() {
        ResultAction<? extends BuildResult> action = getReferenceAction();
        if (action != null) {
            return action.getResult().getContainer();
        }
        return new DefaultAnnotationContainer();
    }

    /**
     * Returns the action of the reference build.
     *
     * @return the action of the reference build, or <code>null</code> if no
     *         such build exists
     */
    public ResultAction<? extends BuildResult> getReferenceAction() {
        ResultAction<? extends BuildResult> currentAction = getOwner().getAction(getResultActionType());
        if (currentAction.hasReferenceAction()) {
            return currentAction.getReferenceAction();
        }
        else {
            return getPreviousResult(); // fallback, use previous build
        }
    }

    /**
     * Returns whether a previous build result exists.
     *
     * @return <code>true</code> if a previous build result exists.
     */
    public boolean hasPreviousResult() {
        ResultAction<?> action = getOwner().getAction(getResultActionType());

        return action != null && action.hasPreviousAction();
    }

    /**
     * Returns the action of the previous build.
     *
     * @return the action of the previous build, or <code>null</code> if no
     *         such build exists
     */
    public ResultAction<? extends BuildResult> getPreviousResult() {
        ResultAction<? extends BuildResult> currentAction = getOwner().getAction(getResultActionType());

        return currentAction.getPreviousAction();
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
        return DetailFactory.create().createTrendDetails(link, getOwner(), getContainer(), getFixedWarnings(),
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
    public Collection<FileAnnotation> getAnnotations(final String priority) {
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
     * Sets the Hudson build result for this plug-in build result.
     *
     * @param result the Hudson build result
     */
    public void setResult(final Result result) {
        pluginResult = result;
    }

    // Backward compatibility. Do not remove.
    // CHECKSTYLE:OFF
    @Deprecated
    @java.lang.SuppressWarnings("unused")
    private transient Map<String, MavenModule> emptyModules;
    @Deprecated
    protected
    transient String low;
    @Deprecated
    protected
    transient String normal;
    @Deprecated
    protected
    transient String high;
}

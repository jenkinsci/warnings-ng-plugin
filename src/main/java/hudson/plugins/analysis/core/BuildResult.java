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
    private transient WeakReference<Collection<FileAnnotation>> newWarnings;
    /** All fixed warnings in the current build. */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("Se")
    private transient WeakReference<Collection<FileAnnotation>> fixedWarnings;

    /** The number of warnings in this build. */
    private int numberOfWarnings;
    /** The number of new warnings in this build. */
    private int numberOfNewWarnings;
    /** The number of fixed warnings in this build. */
    private int numberOfFixedWarnings;

    /** Difference between this and the previous build. */
    private int delta;

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
    /** Determines if the old zero highscore has been broken. */
    private boolean isZeroWarningsHighscore;
    /** Determines the number of msec still to go before a new highscore is reached. */
    private long highScoreGap;
    /** Error messages. */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("Se")
    private List<String> errors;

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
        initialize(build, defaultEncoding, result, new JavaProject());

        if (result.hasNoAnnotations()) {
            zeroWarningsSinceBuild = build.getNumber();
            zeroWarningsSinceDate = build.getTimestamp().getTimeInMillis();
            isZeroWarningsHighscore = true;
        }
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
     * @param previous
     *            the result of the previous build
     */
    public BuildResult(final AbstractBuild<?, ?> build, final String defaultEncoding, final ParserResult result, final BuildResult previous) {
        AnnotationContainer previousProject = previous.getProject();

        initialize(build, defaultEncoding, result, previousProject);

        if (result.hasNoAnnotations()) {
            if (previousProject.hasNoAnnotations()) {
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


    /**
     * Initializes this build result.
     *
     * @param build
     *            the current build as owner of this action
     * @param encoding
     *            the default encoding to be used when reading and parsing files
     * @param result
     *            the parsed result with all annotations
     * @param previousProject
     *            the project of the previous build
     */
    private void initialize(final AbstractBuild<?, ?> build, final String encoding, final ParserResult result, final AnnotationContainer previousProject) {
        owner = build;
        modules = new HashSet<String>(result.getModules());
        numberOfModules = modules.size();
        errors = new ArrayList<String>(result.getErrorMessages());
        defaultEncoding = encoding;

        numberOfWarnings = result.getNumberOfAnnotations();
        delta = result.getNumberOfAnnotations() - previousProject.getNumberOfAnnotations();

        Collection<FileAnnotation> allWarnings = result.getAnnotations();

        Set<FileAnnotation> warnings = AnnotationDifferencer.getNewAnnotations(allWarnings, previousProject.getAnnotations());
        numberOfNewWarnings = warnings.size();
        newWarnings = new WeakReference<Collection<FileAnnotation>>(warnings);

        warnings = AnnotationDifferencer.getFixedAnnotations(allWarnings, previousProject.getAnnotations());
        numberOfFixedWarnings = warnings.size();
        fixedWarnings = new WeakReference<Collection<FileAnnotation>>(warnings);

        highWarnings = result.getNumberOfAnnotations(Priority.HIGH);
        normalWarnings = result.getNumberOfAnnotations(Priority.NORMAL);
        lowWarnings = result.getNumberOfAnnotations(Priority.LOW);

        serializeAnnotations(result.getAnnotations());

        JavaProject container = new JavaProject();
        container.addAnnotations(result.getAnnotations());

        project = new WeakReference<JavaProject>(container);
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
    public final AbstractBuild<?, ?> getOwner() {
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
     * @return the new warnings of this build.
     */
    @Exported
    public Collection<FileAnnotation> getNewWarnings() {
        if (newWarnings == null) {
            return loadNewWarnings();
        }
        Collection<FileAnnotation> result = newWarnings.get();
        if (result == null) {
            return loadNewWarnings();
        }
        return result;
    }

    /**
     * Loads the results of the current and previous build, computes the new
     * warnings and wraps them in a weak reference that might get removed by the
     * garbage collector.
     *
     * @return the new warnings
     */
    private Collection<FileAnnotation> loadNewWarnings() {
        Collection<FileAnnotation> difference = getProject().getAnnotations();
        if (hasPreviousResult()) {
            difference = AnnotationDifferencer.getNewAnnotations(difference, getPreviousResult().getAnnotations());
        }
        newWarnings = new WeakReference<Collection<FileAnnotation>>(difference);

        return difference;
    }

    /**
     * Returns the fixed warnings of this build.
     *
     * @return the fixed warnings of this build.
     */
    public Collection<FileAnnotation> getFixedWarnings() {
        if (fixedWarnings == null) {
            return loadFixedWarnings();
        }
        Collection<FileAnnotation> result = fixedWarnings.get();
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
        if (hasPreviousResult()) {
            difference = AnnotationDifferencer.getFixedAnnotations(getProject().getAnnotations(), getPreviousResult().getAnnotations());
        }
        else {
            difference = Collections.emptyList();
        }
        fixedWarnings = new WeakReference<Collection<FileAnnotation>>(difference);

        return difference;
    }

    /**
     * Returns whether a previous build result exists.
     *
     * @return <code>true</code> if a previous build result exists.
     */
    public boolean hasPreviousResult() {
        ResultAction<?> action = getOwner().getAction(getResultActionType());

        return action != null && action.hasPreviousResultAction();
    }

    /**
     * Returns the results of the previous build.
     *
     * @return the result of the previous build, or <code>null</code> if no
     *         such build exists
     */
    public JavaProject getPreviousResult() {
        ResultAction<? extends BuildResult> action = getOwner().getAction(getResultActionType());
        if (action != null && action.hasPreviousResultAction()) {
            return action.getPreviousResultAction().getResult().getProject();
        }
        else {
            return null;
        }
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

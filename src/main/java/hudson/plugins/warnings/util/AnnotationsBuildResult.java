package hudson.plugins.warnings.util;

import hudson.model.AbstractBuild;
import hudson.plugins.warnings.util.model.AnnotationContainer;
import hudson.plugins.warnings.util.model.FileAnnotation;
import hudson.plugins.warnings.util.model.JavaProject;
import hudson.plugins.warnings.util.model.MavenModule;
import hudson.plugins.warnings.util.model.Priority;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.time.DateUtils;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * Common base class for build results that persist annotations. Provides
 * loading and saving of annotations (all, new, and fixed) and delta computation.
 *
 * @author Ulli Hafner
 */
@SuppressWarnings("PMD.TooManyFields")
public abstract class AnnotationsBuildResult extends BuildResult {
    /** Unique ID of this class. */
    private static final long serialVersionUID = -5183039263351537465L;
    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(AnnotationsBuildResult.class.getName());
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
    private int low;
    /** The number of normal priority warnings in this build. */
    private int normal;
    /** The number of high priority warnings in this build. */
    private int high;

    /** Determines since which build we have zero warnings. */
    private int zeroWarningsSinceBuild;
    /** Determines since which time we have zero warnings. */
    private long zeroWarningsSinceDate;
    /** Determines since which time we have zero warnings. */
    private long zeroWarningsHighScore;

    /** Error messages. */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("Se")
    private List<String> errors;

    /** The modules with no warnings. */
    @SuppressWarnings("unused")
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("Se")
    private Map<String, MavenModule> emptyModules; // backward compatibility;
    /** Determines if the old zero highscore has been broken. */
    private boolean isZeroWarningsHighscore;
    /** Determines the number of msec still to go before a new highscore is reached. */
    private long highScoreGap;

    /**
     * Creates a new instance of {@link AnnotationsBuildResult}.
     *
     * @param build
     *            the current build as owner of this action
     * @param result
     *            the parsed result with all annotations
     */
    public AnnotationsBuildResult(final AbstractBuild<?, ?> build, final ParserResult result) {
        super(build, result.getModules());

        initialize(result, new JavaProject());

        if (result.hasNoAnnotations()) {
            zeroWarningsSinceBuild = build.getNumber();
            zeroWarningsSinceDate = build.getTimestamp().getTimeInMillis();
            isZeroWarningsHighscore = true;
        }
    }

    /**
     * Creates a new instance of {@link AnnotationsBuildResult}.
     *
     * @param build
     *            the current build as owner of this action
     * @param result
     *            the parsed result with all annotations
     * @param previous
     *            the result of the previous build
     */
    public AnnotationsBuildResult(final AbstractBuild<?, ?> build, final ParserResult result, final AnnotationsBuildResult previous) {
        super(build, result.getModules());

        AnnotationContainer previousProject = previous.getProject();

        initialize(result, previousProject);

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
     * Returns the number of days for the specified number of milliseconds.
     *
     * @param ms milliseconds
     * @return the number of days
     */
    public static long getDays(final long ms) {
        return Math.max(1, ms / DateUtils.MILLIS_PER_DAY);
    }

    /**
     * Initializes this result.
     *
     * @param result
     *            the parsed result with all annotations
     * @param previousProject
     *            the project of the previous build
     */
    private void initialize(final ParserResult result, final AnnotationContainer previousProject) {
        numberOfWarnings = result.getNumberOfAnnotations();

        delta = result.getNumberOfAnnotations() - previousProject .getNumberOfAnnotations();

        Collection<FileAnnotation> allWarnings = result.getAnnotations();

        Set<FileAnnotation> warnings = AnnotationDifferencer.getNewWarnings(allWarnings, previousProject.getAnnotations());
        numberOfNewWarnings = warnings.size();
        newWarnings = new WeakReference<Collection<FileAnnotation>>(warnings);

        warnings = AnnotationDifferencer.getFixedWarnings(allWarnings, previousProject.getAnnotations());
        numberOfFixedWarnings = warnings.size();
        fixedWarnings = new WeakReference<Collection<FileAnnotation>>(warnings);

        high = result.getNumberOfAnnotations(Priority.HIGH);
        normal = result.getNumberOfAnnotations(Priority.NORMAL);
        low = result.getNumberOfAnnotations(Priority.LOW);

        errors = new ArrayList<String>(result.getErrorMessages());

        serializeAnnotations(result.getAnnotations());

        JavaProject container = new JavaProject();
        container.addAnnotations(result.getAnnotations());

        project = new WeakReference<JavaProject>(container);
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
     * Returns whether a module with an error is part of this project.
     *
     * @return <code>true</code> if at least one module has an error.
     */
    public boolean hasError() {
        return !errors.isEmpty();
    }

    /**
     * Returns the build since we have zero warnings.
     *
     * @return the build since we have zero warnings
     */
    public int getZeroWarningsSinceBuild() {
        return zeroWarningsSinceBuild;
    }

    /**
     * Returns the time since we have zero warnings.
     *
     * @return the time since we have zero warnings
     */
    public long getZeroWarningsSinceDate() {
        return zeroWarningsSinceDate;
    }

    /**
     * Returns the maximum period with zero warnings in a build.
     *
     * @return the time since we have zero warnings
     */
    public long getZeroWarningsHighScore() {
        return zeroWarningsHighScore;
    }

    /**
     * Returns if the current result reached the old zero warnings highscore.
     *
     * @return <code>true</code>, if the current result reached the old zero warnings highscore.
     */
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
    public int getNumberOfAnnotations() {
        return numberOfWarnings;
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
            return high;
        }
        else if (priority == Priority.NORMAL) {
            return normal;
        }
        else {
            return low;
        }
    }

    /**
     * Gets the number of fixed warnings.
     *
     * @return the number of fixed warnings
     */
    public int getNumberOfFixedWarnings() {
        return numberOfFixedWarnings;
    }

    /**
     * Gets the number of new warnings.
     *
     * @return the number of new warnings
     */
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
            difference = AnnotationDifferencer.getNewWarnings(difference, getPreviousResult().getAnnotations());
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
            difference = AnnotationDifferencer.getFixedWarnings(getProject().getAnnotations(), getPreviousResult().getAnnotations());
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
    protected abstract boolean hasPreviousResult();

    /**
     * Returns the results of the previous build.
     *
     * @return the result of the previous build, or an empty project if no such
     *         build exists
     */
    protected abstract JavaProject getPreviousResult();

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
        return new DetailBuilder().createTrendDetails(link, getOwner(), getContainer(), getFixedWarnings(), getNewWarnings(), errors, getDisplayName());
    }

    /**
     * Generates a PNG image for high/normal/low distribution of the specified object.
     *
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     * @throws IOException
     *             in case of an error
     */
    public final void doStatistics(final StaplerRequest request, final StaplerResponse response) throws IOException {
        new ChartRenderer().doStatistics(request, response, getContainer());
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
    @Override
    public AnnotationContainer getContainer() {
        return getProject();
    }
}

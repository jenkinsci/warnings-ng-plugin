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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * Common base class for build results that persist annotations. Provides
 * loading and saving of annotations (all, new, and fixed) and delta computation.
 *
 * @author Ulli Hafner
 */
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
    private transient WeakReference<Set<FileAnnotation>> newWarnings;
    /** All fixed warnings in the current build. */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("Se")
    private transient WeakReference<Set<FileAnnotation>> fixedWarnings;

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
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("Se")
    private Map<String, MavenModule> emptyModules;
    /** The total number of modules with or without warnings. */
    private int numberOfModules;

    /**
     * Creates a new instance of {@link AnnotationsBuildResult}.
     *
     * @param build
     *            the current build as owner of this action
     * @param project
     *            the parsed result with all annotations
     */
    public AnnotationsBuildResult(final AbstractBuild<?, ?> build, final JavaProject project) {
        super(build);

        initialize(project, new JavaProject());
    }

    /**
     * Creates a new instance of {@link AnnotationsBuildResult}.
     *
     * @param build
     *            the current build as owner of this action
     * @param project
     *            the parsed result with all annotations
     * @param previous
     *            the result of the previous build
     */
    public AnnotationsBuildResult(final AbstractBuild<?, ?> build, final JavaProject project, final AnnotationsBuildResult previous) {
        super(build);

        AnnotationContainer previousProject = previous.getProject();

        initialize(project, previousProject);

        if (project.hasNoAnnotations()) {
            if (previousProject.hasNoAnnotations()) {
                zeroWarningsSinceBuild = previous.getZeroWarningsSinceBuild();
                zeroWarningsSinceDate = previous.getZeroWarningsSinceDate();
            }
            else {
                zeroWarningsSinceBuild = build.getNumber();
                zeroWarningsSinceDate = build.getTimestamp().getTimeInMillis();
            }
            zeroWarningsHighScore = Math.max(previous.getZeroWarningsHighScore(), build.getTimestamp().getTimeInMillis() - zeroWarningsSinceDate);
        }
    }

    /**
     * Initializes this result.
     *
     * @param currentProject
     *            the parsed result with all annotations
     * @param previousProject
     *            the project of the previous build
     */
    private void initialize(final JavaProject currentProject, final AnnotationContainer previousProject) {
        numberOfWarnings = currentProject.getNumberOfAnnotations();

        project = new WeakReference<JavaProject>(currentProject);
        delta = currentProject.getNumberOfAnnotations() - previousProject .getNumberOfAnnotations();

        Collection<FileAnnotation> allWarnings = currentProject.getAnnotations();

        Set<FileAnnotation> warnings = AnnotationDifferencer.getNewWarnings(allWarnings, previousProject.getAnnotations());
        numberOfNewWarnings = warnings.size();
        newWarnings = new WeakReference<Set<FileAnnotation>>(warnings);

        warnings = AnnotationDifferencer.getFixedWarnings(allWarnings, previousProject.getAnnotations());
        numberOfFixedWarnings = warnings.size();
        fixedWarnings = new WeakReference<Set<FileAnnotation>>(warnings);

        high = currentProject.getNumberOfAnnotations(Priority.HIGH);
        normal = currentProject.getNumberOfAnnotations(Priority.NORMAL);
        low = currentProject.getNumberOfAnnotations(Priority.LOW);

        emptyModules = new HashMap<String, MavenModule>();
        for (MavenModule module : currentProject.getModules()) {
            if (module.getNumberOfAnnotations() == 0) {
                emptyModules.put(module.getName(), module);
            }
        }
        numberOfModules = currentProject.getModules().size();

        errors = composeErrorMessage(currentProject);

        serializeAnnotations(currentProject.getAnnotations());
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
     * Composes the error message for the specified project. The message
     * consists of the project error and the errors of the individual modules.
     *
     * @param javaProject
     *            the project
     * @return the list of error messages
     */
    private List<String> composeErrorMessage(final JavaProject javaProject) {
        List<String> messages = new ArrayList<String>();
        if (javaProject.hasError()) {
            messages.addAll(javaProject.getErrors());
        }
        return messages;
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
     * Returns the number of modules in this project.
     *
     * @return the number of modules
     */
    public int getNumberOfModules() {
        return numberOfModules;
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
    public JavaProject getProject() {
        if (project == null) {
            loadResult();
        }
        JavaProject result = project.get();
        if (result == null) {
            loadResult();
        }
        return project.get();
    }

    /**
     * Returns the new warnings of this build.
     *
     * @return the new warnings of this build.
     */
    public Set<FileAnnotation> getNewWarnings() {
        if (newWarnings == null) {
            loadPreviousResult();
        }
        Set<FileAnnotation> result = newWarnings.get();
        if (result == null) {
            loadPreviousResult();
        }
        return newWarnings.get();
    }

    /**
     * Returns the fixed warnings of this build.
     *
     * @return the fixed warnings of this build.
     */
    public Set<FileAnnotation> getFixedWarnings() {
        if (fixedWarnings == null) {
            loadPreviousResult();
        }
        Set<FileAnnotation> result = fixedWarnings.get();
        if (result == null) {
            loadPreviousResult();
        }
        return fixedWarnings.get();
    }

    /**
     * Loads the results and wraps them in a weak reference that might
     * get removed by the garbage collector.
     */
    private void loadResult() {
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
    }

    /**
     * Loads the results of the current and previous build and wraps
     * them in a weak reference that might get removed by the garbage collector.
     */
    @java.lang.SuppressWarnings("unchecked")
    private void loadPreviousResult() {
        loadResult();

        if (hasPreviousResult()) {
            newWarnings = new WeakReference<Set<FileAnnotation>>(
                    AnnotationDifferencer.getNewWarnings(getProject().getAnnotations(),
                            getPreviousResult().getAnnotations()));
        }
        else {
            newWarnings = new WeakReference<Set<FileAnnotation>>(new HashSet<FileAnnotation>(getProject().getAnnotations()));
        }
        if (hasPreviousResult()) {
            fixedWarnings = new WeakReference<Set<FileAnnotation>>(
                    AnnotationDifferencer.getFixedWarnings(getProject().getAnnotations(),
                            getPreviousResult().getAnnotations()));
        }
        else {
            fixedWarnings = new WeakReference<Set<FileAnnotation>>(Collections.EMPTY_SET);
        }
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

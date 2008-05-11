package hudson.plugins.warnings;

import hudson.XmlFile;
import hudson.model.AbstractBuild;
import hudson.model.ModelObject;
import hudson.plugins.warnings.parser.Warning;
import hudson.plugins.warnings.util.AnnotationDifferencer;
import hudson.plugins.warnings.util.ChartRenderer;
import hudson.plugins.warnings.util.ErrorDetail;
import hudson.plugins.warnings.util.FixedWarningsDetail;
import hudson.plugins.warnings.util.ModuleDetail;
import hudson.plugins.warnings.util.NewWarningsDetail;
import hudson.plugins.warnings.util.PackageDetail;
import hudson.plugins.warnings.util.PriorityDetailFactory;
import hudson.plugins.warnings.util.SourceDetail;
import hudson.plugins.warnings.util.model.AnnotationProvider;
import hudson.plugins.warnings.util.model.AnnotationStream;
import hudson.plugins.warnings.util.model.FileAnnotation;
import hudson.plugins.warnings.util.model.JavaPackage;
import hudson.plugins.warnings.util.model.JavaProject;
import hudson.plugins.warnings.util.model.MavenModule;
import hudson.plugins.warnings.util.model.Priority;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
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

import com.thoughtworks.xstream.XStream;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

/**
 * Represents the results of the warning analysis. One instance of this class is persisted for
 * each build via an XML file.
 *
 * @author Ulli Hafner
 */
public class WarningsResult implements ModelObject, Serializable, AnnotationProvider {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = 2768250056765266658L;
    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(WarningsResult.class.getName());
    /** Serialization provider. */
    private static final XStream XSTREAM = new AnnotationStream();
    static {
        XSTREAM.alias("warning", Warning.class);
    }

    /** The parsed warnings result. */
    @SuppressWarnings("Se")
    private transient WeakReference<JavaProject> project;
    /** All new warnings in the current build.*/
    @SuppressWarnings("Se")
    private transient WeakReference<Set<FileAnnotation>> newWarnings;
    /** All fixed warnings in the current build.*/
    @SuppressWarnings("Se")
    private transient WeakReference<Set<FileAnnotation>> fixedWarnings;

    /** The number of warnings in this build. */
    private final int numberOfWarnings;
    /** The number of new warnings in this build. */
    private final int numberOfNewWarnings;
    /** The number of fixed warnings in this build. */
    private final int numberOfFixedWarnings;
    /** Difference between this and the previous build. */
    private final int delta;
    /** The number of low priority warnings in this build. */
    private final int low;
    /** The number of normal priority warnings in this build. */
    private final int normal;
    /** The number of high priority warnings in this build. */
    private final int high;

    /** Determines since which build we have zero warnings. */
    private int zeroWarningsSinceBuild;
    /** Determines since which time we have zero warnings. */
    private long zeroWarningsSinceDate;
    /** Determines since which time we have zero warnings. */
    private long zeroWarningsHighScore;

    /** Error messages. */
    @SuppressWarnings("Se")
    private final List<String> errors;

    /** Current build as owner of this action. */
    @SuppressWarnings("Se")
    private final AbstractBuild<?, ?> owner;

    /** The modules with no warnings. */
    @SuppressWarnings("Se")
    private final Map<String, MavenModule> emptyModules;
    /** The total number of modules with or without warnings. */
    private final int numberOfModules;

    /**
     * Creates a new instance of <code>WarningsResult</code>.
     *
     * @param build
     *            the current build as owner of this action
     * @param project
     *            the parsed warnings result
     */
    public WarningsResult(final AbstractBuild<?, ?> build, final JavaProject project) {
        this(build, project, new JavaProject(), 0);
    }

    /**
     * Creates a new instance of <code>WarningsResult</code>.
     *
     * @param build
     *            the current build as owner of this action
     * @param project
     *            the parsed warnings result
     * @param previousProject
     *            the parsed warnings result of the previous build
     * @param highScore
     *            the maximum period with zero warnings in a build
     */
    public WarningsResult(final AbstractBuild<?, ?> build, final JavaProject project, final JavaProject previousProject, final long highScore) {
        owner = build;

        numberOfWarnings = project.getNumberOfAnnotations();

        this.project = new WeakReference<JavaProject>(project);
        delta = project.getNumberOfAnnotations() - previousProject.getNumberOfAnnotations();

        Collection<FileAnnotation> allWarnings = project.getAnnotations();

        Set<FileAnnotation> warnings = AnnotationDifferencer.getNewWarnings(allWarnings, previousProject.getAnnotations());
        numberOfNewWarnings = warnings.size();
        newWarnings = new WeakReference<Set<FileAnnotation>>(warnings);

        warnings = AnnotationDifferencer.getFixedWarnings(allWarnings, previousProject.getAnnotations());
        numberOfFixedWarnings = warnings.size();
        fixedWarnings = new WeakReference<Set<FileAnnotation>>(warnings);

        high = project.getNumberOfAnnotations(Priority.HIGH);
        normal = project.getNumberOfAnnotations(Priority.NORMAL);
        low = project.getNumberOfAnnotations(Priority.LOW);

        emptyModules = new HashMap<String, MavenModule>();
        for (MavenModule module : project.getModules()) {
            if (module.getNumberOfAnnotations() == 0) {
                emptyModules.put(module.getName(), module);
            }
        }
        numberOfModules = project.getModules().size();

        errors = composeErrorMessage(project);

        if (numberOfWarnings == 0) {
            if (previousProject.getNumberOfAnnotations() != 0) {
                zeroWarningsSinceBuild = build.getNumber();
                zeroWarningsSinceDate = build.getTimestamp().getTimeInMillis();
            }
            zeroWarningsHighScore = Math.max(highScore, build.getTimestamp().getTimeInMillis() - zeroWarningsSinceDate);
        }
        try {
            Collection<FileAnnotation> files = project.getAnnotations();
            getDataFile().write(files.toArray(new FileAnnotation[files.size()]));
        }
        catch (IOException exception) {
            LOGGER.log(Level.WARNING, "Failed to serialize the warnings result.", exception);
        }
    }

    /**
     * Composes the error message for the specified project. The message
     * consists of the project error and the errors of the individual modules.
     *
     * @param javaProject the project
     * @return the list of error messages
     */
    private List<String> composeErrorMessage(final JavaProject javaProject) {
        List<String> messages = new ArrayList<String>();
        if (javaProject.hasError()) {
            if (javaProject.getError() != null) {
                messages.add(javaProject.getError());
            }
            for (MavenModule module : javaProject.getModules()) {
                if (module.hasError()) {
                    messages.add(module.getError());
                }
            }
        }
        return messages;
    }

    /**
     * Returns a summary message for the summary.jelly file.
     *
     * @return the summary message
     */
    public String getSummary() {
        return ResultSummary.createSummary(this);
    }

    /**
     * Returns the detail messages for the summary.jelly file.
     *
     * @return the summary message
     */
    public String getDetails() {
        String message = ResultSummary.createDeltaMessage(this);
        if (numberOfWarnings == 0 && delta == 0) {
            return message + "<li>" + Messages.Warnings_ResultAction_NoWarningsSince(zeroWarningsSinceBuild) + "</li>";
        }
        return message;
    }

    /**
     * Returns whether this result belongs to the last build.
     *
     * @return <code>true</code> if this result belongs to the last build
     */
    public final boolean isCurrent() {
        return owner.getProject().getLastBuild().number == owner.number;
    }

    /**
     * Returns the build as owner of this action.
     *
     * @return the owner
     */
    public final AbstractBuild<?, ?> getOwner() {
        return owner;
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
     * Returns the serialization file.
     *
     * @return the serialization file.
     */
    private XmlFile getDataFile() {
        return new XmlFile(XSTREAM, new File(getOwner().getRootDir(), "compiler-warnings.xml"));
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

    /** {@inheritDoc} */
    public String getDisplayName() {
        return Messages.Warnings_ProjectAction_Name();
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
     * Loads the warnings results and wraps them in a weak reference that might
     * get removed by the garbage collector.
     */
    private void loadResult() {
        JavaProject result;
        try {
            JavaProject newProject = new JavaProject();
            FileAnnotation[] annotations = (FileAnnotation[])getDataFile().read();
            newProject.addAnnotations(annotations);

            LOGGER.log(Level.INFO, "Loaded warnings data file " + getDataFile() + " for build " + getOwner().getNumber());
            result = newProject;
        }
        catch (IOException exception) {
            LOGGER.log(Level.WARNING, "Failed to load " + getDataFile(), exception);
            result = new JavaProject();
        }
        project = new WeakReference<JavaProject>(result);
    }

    /**
     * Loads the warnings results and the result of the previous build and wraps
     * them in a weak reference that might get removed by the garbage collector.
     */
    @java.lang.SuppressWarnings("unchecked")
    private void loadPreviousResult() {
        loadResult();

        if (hasPreviousResult()) {
            newWarnings = new WeakReference<Set<FileAnnotation>>(
                    AnnotationDifferencer.getNewWarnings(getProject().getAnnotations(), getPreviousResult().getAnnotations()));
        }
        else {
            newWarnings = new WeakReference<Set<FileAnnotation>>(new HashSet<FileAnnotation>(getProject().getAnnotations()));
        }
        if (hasPreviousResult()) {
            fixedWarnings = new WeakReference<Set<FileAnnotation>>(
                    AnnotationDifferencer.getFixedWarnings(getProject().getAnnotations(), getPreviousResult().getAnnotations()));
        }
        else {
            fixedWarnings = new WeakReference<Set<FileAnnotation>>(Collections.EMPTY_SET);
        }
    }

    /**
     * Returns the dynamic result of the warnings analysis (a detail page for a
     * module, package or warnings file or a detail object for new or fixed
     * warnings).
     *
     * @param link
     *            the link to identify the sub page to show
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     * @return the dynamic result of the warnings analysis (detail page for a
     *         package).
     */
    public Object getDynamic(final String link, final StaplerRequest request, final StaplerResponse response) {
        PriorityDetailFactory factory = new PriorityDetailFactory();
        if (factory.isPriority(link)) {
            return factory.create(link, owner, getProject(), Messages.Warnings_ProjectAction_Name());
        }
        else if ("fixed".equals(link)) {
            return new FixedWarningsDetail(getOwner(), getFixedWarnings(), Messages.Warnings_FixedWarnings_Detail_header());
        }
        else if ("new".equals(link)) {
            return new NewWarningsDetail(getOwner(), getNewWarnings(), Messages.Warnings_NewWarnings_Detail_header());
        }
        else if ("error".equals(link)) {
            return new ErrorDetail(getOwner(), "Compiler Warnings", errors);
        }
        else {
            if (isSingleModuleProject()) {
                if (isSinglePackageProject()) {
                    return new SourceDetail(getOwner(), getProject().getAnnotation(link));
                }
                else {
                    return new PackageDetail(getOwner(), getModules().iterator().next().getPackage(link), Messages.Warnings_ProjectAction_Name());
                }
            }
            else {
                return new ModuleDetail(getOwner(), getModule(link), Messages.Warnings_ProjectAction_Name());
            }
        }
    }

    /**
     * Returns the module with the specified name.
     *
     * @param name
     *            the module to get
     * @return the module
     */
    private MavenModule getModule(final String name) {
        MavenModule module;
        if (emptyModules.containsKey(name)) {
            module = emptyModules.get(name);
        }
        else {
            module = getProject().getModule(name);
        }
        return module;
    }

    /**
     * Returns the packages of this project.
     *
     * @return the packages of this project
     */
    public Collection<JavaPackage> getPackages() {
        return getProject().getPackages();
    }

    /**
     * Returns the modules of this project.
     *
     * @return the modules of this project
     */
    public Collection<MavenModule> getModules() {
        List<MavenModule> modules = new ArrayList<MavenModule>();
        modules.addAll(emptyModules.values());
        for (MavenModule module : getProject().getModules()) {
            if (!emptyModules.containsKey(module.getName())) {
                modules.add(module);
            }
        }
        return modules;
    }

    /**
     * Returns whether this project contains just one maven module. In this case
     * we show package statistics instead of module statistics.
     *
     * @return <code>true</code> if this project contains just one maven
     *         module
     */
    public boolean isSingleModuleProject() {
        return getNumberOfModules() == 1;
    }

    /**
     * Returns whether we only have a single package. In this case the module
     * and package statistics are suppressed and only the tasks are shown.
     *
     * @return <code>true</code> for single module projects
     */
    public boolean isSinglePackageProject() {
        return isSingleModuleProject() && getProject().getPackages().size() == 1;
    }

    /**
     * Returns the number of warnings of the specified package in the previous build.
     *
     * @param packageName
     *            the package to return the warnings for
     * @return number of warnings of the specified package.
     */
    public int getPreviousNumberOfWarnings(final String packageName) {
        JavaProject previousResult = getPreviousResult();
        if (previousResult != null) {
            return previousResult.getPackage(packageName).getNumberOfAnnotations();
        }
        return 0;
    }

    /**
     * Returns the results of the previous build.
     *
     * @return the result of the previous build, or <code>null</code> if no
     *         such build exists
     */
    public JavaProject getPreviousResult() {
        WarningsResultAction action = getOwner().getAction(WarningsResultAction.class);
        if (action.hasPreviousResultAction()) {
            return action.getPreviousResultAction().getResult().getProject();
        }
        else {
            return null;
        }
    }

    /**
     * Returns whether a previous build result exists.
     *
     * @return <code>true</code> if a previous build result exists.
     */
    public boolean hasPreviousResult() {
        return getOwner().getAction(WarningsResultAction.class).hasPreviousResultAction();
    }

    /**
     * Generates a PNG image for high/normal/low distribution of a maven module.
     *
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     * @throws IOException
     *             in case of an error
     */
    public final void doModuleStatistics(final StaplerRequest request, final StaplerResponse response) throws IOException {
        ChartRenderer.renderPriorititesChart(request, response, getModule(request.getParameter("module")), getProject().getAnnotationBound());
    }

    /**
     * Generates a PNG image for high/normal/low distribution of a Java package.
     *
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     * @throws IOException
     *             in case of an error
     */
    public final void doPackageStatistics(final StaplerRequest request, final StaplerResponse response) throws IOException {
        MavenModule module = getModules().iterator().next();
        ChartRenderer.renderPriorititesChart(request, response, module.getPackage(request.getParameter("package")), module.getAnnotationBound());
    }

    /**
     * Returns a tooltip showing the distribution of priorities for the selected
     * package.
     *
     * @param name
     *            the package to show the distribution for
     * @return a tooltip showing the distribution of priorities
     */
    public String getToolTip(final String name) {
        if (isSingleModuleProject()) {
            return getModules().iterator().next().getPackage(name).getToolTip();
        }
        else {
            return getModule(name).getToolTip();
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

    /** {@inheritDoc} */
    public FileAnnotation getAnnotation(final long key) {
        return getProject().getAnnotation(key);
    }

    /** {@inheritDoc} */
    public FileAnnotation getAnnotation(final String key) {
        return getProject().getAnnotation(key);
    }

    /** {@inheritDoc} */
    public Collection<FileAnnotation> getAnnotations(final Priority priority) {
        return getProject().getAnnotations(priority);
    }

    /** {@inheritDoc} */
    public Collection<FileAnnotation> getAnnotations(final String priority) {
        return getProject().getAnnotations(priority);
    }

    /** {@inheritDoc} */
    public boolean hasAnnotations(final Priority priority) {
        return getProject().hasAnnotations(priority);
    }

    /** {@inheritDoc} */
    public boolean hasAnnotations(final String priority) {
        return getProject().hasAnnotations(priority);
    }

    /** {@inheritDoc} */
    public final boolean hasAnnotations() {
        return getProject().hasAnnotations();
    }

    /** {@inheritDoc} */
    public Collection<FileAnnotation> getAnnotations() {
        return getProject().getAnnotations();
    }

    /** {@inheritDoc} */
    public int getNumberOfAnnotations(final String priority) {
        return getNumberOfAnnotations(Priority.fromString(priority));
    }
}

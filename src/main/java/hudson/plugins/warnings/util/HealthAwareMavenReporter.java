package hudson.plugins.warnings.util;

import hudson.FilePath;
import hudson.maven.MavenBuild;
import hudson.maven.MavenBuildProxy;
import hudson.maven.MavenReporter;
import hudson.maven.MojoInfo;
import hudson.maven.MavenBuildProxy.BuildCallable;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.plugins.warnings.util.model.AbstractAnnotation;
import hudson.plugins.warnings.util.model.AnnotationContainer;
import hudson.plugins.warnings.util.model.DefaultAnnotationContainer;
import hudson.plugins.warnings.util.model.FileAnnotation;
import hudson.plugins.warnings.util.model.Priority;
import hudson.plugins.warnings.util.model.WorkspaceFile;
import hudson.remoting.Channel;
import hudson.tasks.BuildStep;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.project.MavenProject;

/**
 * A base class for maven reporters with the following two characteristics:
 * <ul>
 * <li>It provides a unstable threshold, that could be enabled and set in the
 * configuration screen. If the number of annotations in a build exceeds this
 * value then the build is considered as {@link Result#UNSTABLE UNSTABLE}.
 * </li>
 * <li>It provides thresholds for the build health, that could be adjusted in
 * the configuration screen. These values are used by the
 * {@link HealthReportBuilder} to compute the health and the health trend graph.</li>
 * </ul>
 *
 * @author Ulli Hafner
 */
// CHECKSTYLE:COUPLING-OFF
public abstract class HealthAwareMavenReporter extends MavenReporter implements HealthDescriptor {
    /** Default threshold priority limit. */
    private static final Priority DEFAULT_PRIORITY_THRESHOLD_LIMIT = Priority.LOW;
    /** Unique identifier of this class. */
    private static final long serialVersionUID = 3003791883748835331L;
    /** Annotation threshold to be reached if a build should be considered as unstable. */
    private final String threshold;
    /** Determines whether to use the provided threshold to mark a build as unstable. */
    private boolean thresholdEnabled;
    /** Integer threshold to be reached if a build should be considered as unstable. */
    private int minimumAnnotations;
    /** Report health as 100% when the number of warnings is less than this value. */
    private final String healthy;
    /** Report health as 0% when the number of warnings is greater than this value. */
    private final String unHealthy;
    /** Report health as 100% when the number of warnings is less than this value. */
    private int healthyAnnotations;
    /** Report health as 0% when the number of warnings is greater than this value. */
    private int unHealthyAnnotations;
    /** Determines whether to use the provided healthy thresholds. */
    private boolean healthyReportEnabled;
    /** Determines the height of the trend graph. */
    private final String height;
    /** The name of the plug-in. */
    private final String pluginName;
    /** Determines which warning priorities should be considered when evaluating the build stability and health. */
    private Priority minimumPriority;
    /** The default encoding to be used when reading and parsing files. */
    private String defaultEncoding;

    /**
     * Creates a new instance of <code>HealthReportingMavenReporter</code>.
     *
     * @param threshold
     *            Bug threshold to be reached if a build should be considered as
     *            unstable.
     * @param healthy
     *            Report health as 100% when the number of warnings is less than
     *            this value
     * @param unHealthy
     *            Report health as 0% when the number of warnings is greater
     *            than this value
     * @param height
     *            the height of the trend graph
     * @param minimumPriority
     *            determines which warning priorities should be considered when
     *            evaluating the build stability and health
     * @param pluginName
     *            the name of the plug-in
     */
    public HealthAwareMavenReporter(final String threshold, final String healthy, final String unHealthy,
            final String height, final Priority minimumPriority, final String pluginName) {
        super();
        this.threshold = threshold;
        this.healthy = healthy;
        this.unHealthy = unHealthy;
        this.height = height;
        this.minimumPriority = minimumPriority;
        this.pluginName = "[" + pluginName + "] ";

        validateThreshold(threshold);
        validateHealthiness(healthy, unHealthy);
    }

    /**
     * Validates the healthiness parameters and sets the according fields.
     *
     * @param healthyParameter
     *            the healthy value to validate
     * @param unHealthyParameter
     *            the unhealthy value to validate
     */
    private void validateHealthiness(final String healthyParameter, final String unHealthyParameter) {
        if (!StringUtils.isEmpty(healthyParameter) && !StringUtils.isEmpty(unHealthyParameter)) {
            try {
                healthyAnnotations = Integer.valueOf(healthyParameter);
                unHealthyAnnotations = Integer.valueOf(unHealthyParameter);
                if (healthyAnnotations >= 0 && unHealthyAnnotations > healthyAnnotations) {
                    healthyReportEnabled = true;
                }
            }
            catch (NumberFormatException exception) {
                // nothing to do, we use the default value
            }
        }
    }

    /**
     * Validates the threshold parameter and sets the according fields.
     *
     * @param thresholdParameter
     *            the threshold to validate
     */
    private void validateThreshold(final String thresholdParameter) {
        if (!StringUtils.isEmpty(thresholdParameter)) {
            try {
                minimumAnnotations = Integer.valueOf(thresholdParameter);
                if (minimumAnnotations >= 0) {
                    thresholdEnabled = true;
                }
            }
            catch (NumberFormatException exception) {
                // nothing to do, we use the default value
            }
        }
    }

    /**
     * Initializes new fields that are not serialized yet.
     *
     * @return the object
     */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("Se")
    private Object readResolve() {
        if (minimumPriority == null) {
            if (thresholdLimit == null) {
                minimumPriority = DEFAULT_PRIORITY_THRESHOLD_LIMIT;
            }
            else {
                minimumPriority = Priority.fromString(thresholdLimit);
            }
        }
        return this;
    }

    /** {@inheritDoc} */
    @SuppressWarnings({"serial", "PMD.AvoidFinalLocalVariable"})
    @Override
    public final boolean postExecute(final MavenBuildProxy build, final MavenProject pom, final MojoInfo mojo,
            final BuildListener listener, final Throwable error) throws InterruptedException, IOException {
        if (!acceptGoal(mojo.getGoal())) {
            return true;
        }
        PrintStream logger = listener.getLogger();
        if (hasResultAction(build)) {
            log(logger, "Scipping maven reporter: there is already a result available.");
            return true;
        }

        try {
            defaultEncoding = pom.getProperties().getProperty("project.build.sourceEncoding");

            final ParserResult result = perform(build, pom, mojo, logger);

            if (defaultEncoding == null) {
                log(logger, Messages.Reporter_Error_NoEncoding(Charset.defaultCharset().displayName()));
                result.addErrorMessage(pom.getName(), Messages.Reporter_Error_NoEncoding(Charset.defaultCharset().displayName()));
            }

            build.execute(new BuildCallable<Void, IOException>() {
                public Void call(final MavenBuild mavenBuild) throws IOException, InterruptedException {
                    persistResult(result, mavenBuild);

                    return null;
                }
            });

            if (build.getRootDir().isRemote()) {
                copyFilesToMaster(logger, build.getProjectRootDir(), build.getRootDir(), result.getAnnotations());
            }
            evaluateBuildResult(build, logger, result);
        }
        catch (AbortException exception) {
            logger.println(exception.getMessage());
            build.setResult(Result.FAILURE);
            return false;
        }

        return true;
    }


    /**
     * Copies all files with annotations from the slave to the master.
     *
     * @param logger
     *            logger to log any problems
     * @param slaveRoot
     *            directory to copy the files from
     * @param masterRoot
     *            directory to store the copied files in
     * @param annotations
     *            annotations determining the actual files to copy
     * @throws IOException
     *             if the files could not be written
     * @throws FileNotFoundException
     *             if the files could not be written
     * @throws InterruptedException
     *             if the user cancels the processing
     */
    private void copyFilesToMaster(final PrintStream logger, final FilePath slaveRoot, final FilePath masterRoot, final Collection<FileAnnotation> annotations) throws IOException,
            FileNotFoundException, InterruptedException {
        FilePath directory = new FilePath(masterRoot, AbstractAnnotation.WORKSPACE_FILES);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        AnnotationContainer container = new DefaultAnnotationContainer(annotations);
        for (WorkspaceFile file : container.getFiles()) {
            FilePath masterFile = new FilePath(directory, file.getTempName());
            if (!masterFile.exists()) {
                try {
                    new FilePath((Channel)null, file.getName()).copyTo(masterFile);
                }
                catch (IOException exception) {
                    String message = "Can't copy file from slave to master: slave="
                            + file.getName() + ", master=" + masterFile.getName();
                    log(logger, message);
                    exception.printStackTrace(logger);
                }
            }
        }
    }

    /**
     * Determines whether this plug-in will accept the specified goal. The
     * {@link #postExecute(MavenBuildProxy, MavenProject, MojoInfo,
     * BuildListener, Throwable)} will only by invoked if the plug-in returns
     * <code>true</code>.
     *
     * @param goal the maven goal
     * @return <code>true</code> if the plug-in accepts this goal
     */
    protected abstract boolean acceptGoal(final String goal);

    /**
     * Performs the publishing of the results of this plug-in.
     *
     * @param build the build proxy (on the slave)
     * @param pom the pom of the module
     * @param mojo the executed mojo
     * @param logger the logger to report the progress to
     *
     * @return the java project containing the found annotations
     *
     * @throws InterruptedException
     *             If the build is interrupted by the user (in an attempt to
     *             abort the build.) Normally the {@link BuildStep}
     *             implementations may simply forward the exception it got from
     *             its lower-level functions.
     * @throws IOException
     *             If the implementation wants to abort the processing when an
     *             {@link IOException} happens, it can simply propagate the
     *             exception to the caller. This will cause the build to fail,
     *             with the default error message. Implementations are
     *             encouraged to catch {@link IOException} on its own to provide
     *             a better error message, if it can do so, so that users have
     *             better understanding on why it failed.
     */
    protected abstract ParserResult perform(MavenBuildProxy build, MavenProject pom, MojoInfo mojo, PrintStream logger) throws InterruptedException, IOException;

    /**
     * Persists the result in the build (on the master).
     *
     * @param project the created project
     * @param build the build (on the master)
     */
    protected abstract void persistResult(ParserResult project, MavenBuild build);

    /**
     * Logs the specified message.
     *
     * @param logger the logger
     * @param message the message
     */
    protected void log(final PrintStream logger, final String message) {
        logger.println(StringUtils.defaultString(pluginName) + message);
    }

    /**
     * Returns the default encoding derived from the maven pom file.
     *
     * @return the default encoding
     */
    protected String getDefaultEncoding() {
        return defaultEncoding;
    }

    /**
     * Evaluates the build result. The build is marked as unstable if the
     * threshold has been exceeded.
     *
     * @param build
     *            the build to create the action for
     * @param logger
     *            the logger
     * @param result
     *            the project with the annotations
     */
    private void evaluateBuildResult(final MavenBuildProxy build, final PrintStream logger, final ParserResult result) {
        int annotationCount = 0;
        for (Priority priority : Priority.collectPrioritiesFrom(getMinimumPriority())) {
            int numberOfAnnotations = result.getNumberOfAnnotations(priority);
            log(logger, "A total of " + numberOfAnnotations + " annotations have been found for priority " + priority);
            annotationCount += numberOfAnnotations;
        }
        if (annotationCount > 0 && isThresholdEnabled() && annotationCount >= getMinimumAnnotations()) {
            build.setResult(Result.UNSTABLE);
        }
    }

    /**
     * Returns whether we already have a result for this build.
     *
     * @param build
     *            the current build.
     * @return <code>true</code> if we already have a task result action.
     * @throws IOException
     *             in case of an IO error
     * @throws InterruptedException
     *             if the call has been interrupted
     */
    @SuppressWarnings("serial")
    private Boolean hasResultAction(final MavenBuildProxy build) throws IOException, InterruptedException {
        return build.execute(new BuildCallable<Boolean, IOException>() {
            public Boolean call(final MavenBuild mavenBuild) throws IOException, InterruptedException {
                return mavenBuild.getAction(getResultActionClass()) != null;
            }
        });
    }

    /**
     * Returns the type of the result action.
     *
     * @return the type of the result action
     */
    protected abstract Class<? extends Action> getResultActionClass();

    /**
     * Returns the path to the target folder.
     *
     * @param pom the maven pom
     * @return the path to the target folder
     */
    protected FilePath getTargetPath(final MavenProject pom) {
        return new FilePath(new FilePath(pom.getBasedir()), "target");
    }

    /** {@inheritDoc} */
    public boolean isThresholdEnabled() {
        return thresholdEnabled;
    }

    /**
     * Returns the annotation threshold to be reached if a build should be considered as unstable.
     *
     * @return the annotation threshold to be reached if a build should be considered as unstable.
     */
    public String getThreshold() {
        return threshold;
    }

    /** {@inheritDoc} */
    public int getMinimumAnnotations() {
        return minimumAnnotations;
    }

    /** {@inheritDoc} */
    public boolean isHealthyReportEnabled() {
        return healthyReportEnabled;
    }

    /**
     * Returns the healthy threshold, i.e. when health is reported as 100%.
     *
     * @return the 100% healthiness
     */
    public String getHealthy() {
        return healthy;
    }

    /** {@inheritDoc} */
    public int getHealthyAnnotations() {
        return healthyAnnotations;
    }

    /**
     * Returns the unhealthy threshold, i.e. when health is reported as 0%.
     *
     * @return the 0% unhealthiness
     */
    public String getUnHealthy() {
        return unHealthy;
    }

    /** {@inheritDoc} */
    public int getUnHealthyAnnotations() {
        return unHealthyAnnotations;
    }

    /**
     * Returns the height of the trend graph.
     *
     * @return the height of the trend graph
     */
    public String getHeight() {
        return height;
    }

    /**
     * Returns the height of the trend graph.
     *
     * @return the height of the trend graph
     */
    public int getTrendHeight() {
        return TrendReportHeightValidator.defaultHeight(height);
    }

    /** {@inheritDoc} */
    public Priority getMinimumPriority() {
        return minimumPriority;
    }

    /** Not used anymore */
    @Deprecated
    private transient String thresholdLimit;
}


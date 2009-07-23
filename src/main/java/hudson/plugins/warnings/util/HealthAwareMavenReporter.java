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
    private static final String DEFAULT_PRIORITY_THRESHOLD_LIMIT = "low";
    /** Unique identifier of this class. */
    private static final long serialVersionUID = 3003791883748835331L;
    /** Annotation threshold to be reached if a build should be considered as unstable. */
    private final String threshold;
    /** Annotation threshold to be reached if a build should be considered as failure. */
    private final String failureThreshold;
    /** Threshold for new annotations to be reached if a build should be considered as failure. */
    private final String newFailureThreshold;
    /** Annotation threshold for new warnings to be reached if a build should be considered as unstable. */
    private final String newThreshold;
    /** Report health as 100% when the number of warnings is less than this value. */
    private final String healthy;
    /** Report health as 0% when the number of warnings is greater than this value. */
    private final String unHealthy;
    /** The name of the plug-in. */
    private final String pluginName;
    /** Determines which warning priorities should be considered when evaluating the build stability and health. */
    private String thresholdLimit;
    /** The default encoding to be used when reading and parsing files. */
    private String defaultEncoding;

    /**
     * Creates a new instance of <code>HealthReportingMavenReporter</code>.
     *
     * @param threshold
     *            Annotations threshold to be reached if a build should be
     *            considered as unstable.
     * @param newThreshold
     *            New annotations threshold to be reached if a build should be
     *            considered as unstable.
     * @param failureThreshold
     *            Annotation threshold to be reached if a build should be considered as
     *            failure.
     * @param newFailureThreshold
     *            New annotations threshold to be reached if a build should be
     *            considered as failure.
     * @param healthy
     *            Report health as 100% when the number of warnings is less than
     *            this value
     * @param unHealthy
     *            Report health as 0% when the number of warnings is greater
     *            than this value
     * @param thresholdLimit
     *            determines which warning priorities should be considered when
     *            evaluating the build stability and health
     * @param pluginName
     *            the name of the plug-in
     */
    // CHECKSTYLE:OFF
    public HealthAwareMavenReporter(final String threshold, final String newThreshold,
            final String failureThreshold, final String newFailureThreshold,
            final String healthy, final String unHealthy,
            final String thresholdLimit, final String pluginName) {
        super();
        this.threshold = threshold;
        this.newThreshold = newThreshold;
        this.failureThreshold = failureThreshold;
        this.newFailureThreshold = newFailureThreshold;
        this.healthy = healthy;
        this.unHealthy = unHealthy;
        this.thresholdLimit = thresholdLimit;
        this.pluginName = "[" + pluginName + "] ";
    }
    // CHECKSTYLE:ON

    /**
     * Initializes new fields that are not serialized yet.
     *
     * @return the object
     */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("Se")
    private Object readResolve() {
        if (thresholdLimit == null) {
            thresholdLimit = DEFAULT_PRIORITY_THRESHOLD_LIMIT;
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

        if (canContinue(getCurrentResult(build))) {
            PluginLogger logger = new PluginLogger(listener.getLogger(), pluginName);
            if (hasResultAction(build)) {
                logger.log("Skipping maven reporter: there is already a result available.");
                return true;
            }

            try {
                defaultEncoding = pom.getProperties().getProperty("project.build.sourceEncoding");

                final ParserResult result = perform(build, pom, mojo, logger);

                if (defaultEncoding == null) {
                    logger.log(Messages.Reporter_Error_NoEncoding(Charset.defaultCharset().displayName()));
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
            }
            catch (AbortException exception) {
                logger.log(exception);
                build.setResult(Result.FAILURE);
                return false;
            }
        }

        return true;
    }

    /**
     * Returns the current result of the build.
     *
     * @param build
     *            the build proxy
     * @return the current result of the build
     * @throws IOException
     * @throws InterruptedException
     */
    private Result getCurrentResult(final MavenBuildProxy build) throws IOException, InterruptedException {
        return build.execute(new BuildResultCallable());
    }

    /**
     * Returns whether the reporter can continue processing. This default
     * implementation returns <code>true</code> if the build is not aborted or
     * failed.
     *
     * @param result
     *            build result
     * @return <code>true</code> if the build can continue
     */
    protected boolean canContinue(final Result result) {
        return result != Result.ABORTED && result != Result.FAILURE;
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
    private void copyFilesToMaster(final PluginLogger logger, final FilePath slaveRoot, final FilePath masterRoot, final Collection<FileAnnotation> annotations) throws IOException,
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
                    String message = "Can't copy source file: source=" + file.getName() + ", destination=" + masterFile.getName();
                    logger.log(message);
                    logger.printStackTrace(exception);
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
     * @param build
     *            the build proxy (on the slave)
     * @param pom
     *            the pom of the module
     * @param mojo
     *            the executed mojo
     * @param logger
     *            the logger to report the progress to
     * @return the java project containing the found annotations
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
    protected abstract ParserResult perform(MavenBuildProxy build, MavenProject pom, MojoInfo mojo,
            PluginLogger logger) throws InterruptedException, IOException;

    /**
     * Persists the result in the build (on the master).
     *
     * @param project
     *            the created project
     * @param build
     *            the build (on the master)
     * @return the created result
     */
    protected abstract BuildResult persistResult(ParserResult project, MavenBuild build);

    /**
     * Returns the default encoding derived from the maven pom file.
     *
     * @return the default encoding
     */
    protected String getDefaultEncoding() {
        return defaultEncoding;
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

    /**
     * Returns the threshold of all annotations to be reached if a build should
     * be considered as unstable.
     *
     * @return the threshold of all annotations to be reached if a build should
     *         be considered as unstable.
     */
    public String getThreshold() {
        return threshold;
    }

    /**
     * Returns the threshold for new annotations to be reached if a build should
     * be considered as unstable.
     *
     * @return the threshold for new annotations to be reached if a build should
     *         be considered as unstable.
     */
    public String getNewThreshold() {
        return newThreshold;
    }

    /**
     * Returns the annotation threshold to be reached if a build should be
     * considered as failure.
     *
     * @return the annotation threshold to be reached if a build should be
     *         considered as failure.
     */
    public String getFailureThreshold() {
        return failureThreshold;
    }

    /**
     * Returns the threshold of new annotations to be reached if a build should
     * be considered as failure.
     *
     * @return the threshold of new annotations to be reached if a build should
     *         be considered as failure.
     */
    public String getNewFailureThreshold() {
        return newFailureThreshold;
    }

    /**
     * Returns the healthy threshold, i.e. when health is reported as 100%.
     *
     * @return the 100% healthiness
     */
    public String getHealthy() {
        return healthy;
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
    public Priority getMinimumPriority() {
        return Priority.valueOf(StringUtils.upperCase(getThresholdLimit()));
    }

    /**
     * Returns the threshold limit.
     *
     * @return the threshold limit
     */
    public String getThresholdLimit() {
        return thresholdLimit;
    }

    /**
     * Gets the build result from the master.
     */
    private static final class BuildResultCallable implements BuildCallable<Result, IOException> {
        /** Unique ID. */
        private static final long serialVersionUID = -270795641776014760L;

        /** {@inheritDoc} */
        public Result call(final MavenBuild mavenBuild) throws IOException, InterruptedException {
            return mavenBuild.getResult();
        }
    }

    /** Backward compatibility. */
    @SuppressWarnings("unused")
    @Deprecated
    private transient boolean thresholdEnabled;
    /** Backward compatibility. */
    @SuppressWarnings("unused")
    @Deprecated
    private transient int minimumAnnotations;
    /** Backward compatibility. */
    @SuppressWarnings("unused")
    @Deprecated
    private transient int healthyAnnotations;
    /** Backward compatibility. */
    @SuppressWarnings("unused")
    @Deprecated
    private transient int unHealthyAnnotations;
    /** Backward compatibility. */
    @SuppressWarnings("unused")
    @Deprecated
    private transient boolean healthyReportEnabled;
    /** Backward compatibility. */
    @SuppressWarnings("unused")
    @Deprecated
    private transient String height;
}


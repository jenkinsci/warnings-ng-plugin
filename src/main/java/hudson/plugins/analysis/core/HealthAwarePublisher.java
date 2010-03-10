package hudson.plugins.analysis.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import hudson.FilePath;
import hudson.Launcher;

import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Project;
import hudson.model.Result;

import hudson.plugins.analysis.util.PluginLogger;
import hudson.plugins.analysis.util.model.AbstractAnnotation;
import hudson.plugins.analysis.util.model.AnnotationContainer;
import hudson.plugins.analysis.util.model.DefaultAnnotationContainer;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;
import hudson.plugins.analysis.util.model.WorkspaceFile;

import hudson.remoting.VirtualChannel;
import hudson.tasks.Ant;
import hudson.tasks.BuildStep;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Builder;
import hudson.tasks.Maven;
import hudson.tasks.Recorder;

/**
 * A base class for publishers with the following two characteristics:
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
public abstract class HealthAwarePublisher extends Recorder implements HealthDescriptor {
    /** Unique ID of this class. */
    private static final long serialVersionUID = -7945220365563528457L;
    /** Default threshold priority limit. */
    private static final String DEFAULT_PRIORITY_THRESHOLD_LIMIT = "low";
    /** Annotation threshold to be reached if a build should be considered as unstable. */
    private final String threshold;
    /** Threshold for new annotations to be reached if a build should be considered as unstable. */
    private final String newThreshold;
    /** Annotation threshold to be reached if a build should be considered as failure. */
    private final String failureThreshold;
    /** Threshold for new annotations to be reached if a build should be considered as failure. */
    private final String newFailureThreshold;
    /** Report health as 100% when the number of warnings is less than this value. */
    private final String healthy;
    /** Report health as 0% when the number of warnings is greater than this value. */
    private final String unHealthy;
    /** The name of the plug-in. */
    private final String pluginName;
    /** Determines which warning priorities should be considered when evaluating the build stability and health. */
    private String thresholdLimit;
    /** The default encoding to be used when reading and parsing files. */
    private final String defaultEncoding;
    /**
     * Determines whether the absolute annotations delta or the actual
     * annotations set difference should be used to evaluate the build stability.
     *
     * @since 1.4
     */
    private final boolean useDeltaValues;

    /**
     * Creates a new instance of <code>HealthAwarePublisher</code>.
     *
     * @param threshold
     *            Annotations threshold to be reached if a build should be
     *            considered as unstable.
     * @param newThreshold
     *            New annotations threshold to be reached if a build should be
     *            considered as unstable.
     * @param failureThreshold
     *            Annotation threshold to be reached if a build should be
     *            considered as failure.
     * @param newFailureThreshold
     *            New annotations threshold to be reached if a build should be
     *            considered as failure.
     * @param healthy
     *            Report health as 100% when the number of open tasks is less
     *            than this value
     * @param unHealthy
     *            Report health as 0% when the number of open tasks is greater
     *            than this value
     * @param thresholdLimit
     *            determines which warning priorities should be considered when
     *            evaluating the build stability and health
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     * @param useDeltaValues
     *            determines whether the absolute annotations delta or the
     *            actual annotations set difference should be used to evaluate
     *            the build stability
     * @param pluginName
     *            the name of the plug-in
     */
    // CHECKSTYLE:OFF
    public HealthAwarePublisher(final String threshold, final String newThreshold,
            final String failureThreshold, final String newFailureThreshold, final String healthy,
            final String unHealthy, final String thresholdLimit,
            final String defaultEncoding, final boolean useDeltaValues, final String pluginName) {
        super();
        this.threshold = threshold;
        this.newThreshold = newThreshold;
        this.failureThreshold = failureThreshold;
        this.newFailureThreshold = newFailureThreshold;
        this.healthy = healthy;
        this.unHealthy = unHealthy;
        this.thresholdLimit = thresholdLimit;
        this.defaultEncoding = defaultEncoding;
        this.useDeltaValues = useDeltaValues;
        this.pluginName = "[" + pluginName + "] ";
    }
    // CHECKSTYLE:ON

    /**
     * Initializes new fields that are not serialized yet.
     *
     * @return the object
     */
    protected Object readResolve() {
        if (thresholdLimit == null) {
            thresholdLimit = DEFAULT_PRIORITY_THRESHOLD_LIMIT;
        }
        return this;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("deprecation") // Eclipse bug #298563
    @Override
    public final boolean perform(final AbstractBuild<?, ?> build, final Launcher launcher, final BuildListener listener) throws InterruptedException, IOException {
        if (canContinue(build.getResult())) {
            PluginLogger logger = new PluginLogger(listener.getLogger(), pluginName);
            BuildResult annotationsResult = perform(build, logger);

            if (new NullHealthDescriptor(this).isThresholdEnabled()) {
                BuildResultEvaluator resultEvaluator = new BuildResultEvaluator();
                Result buildResult;
                if (useDeltaValues) {
                    buildResult = resultEvaluator.evaluateBuildResult(logger, this,
                            annotationsResult.getAnnotations(), annotationsResult.getDelta());
                }
                else {
                    buildResult = resultEvaluator.evaluateBuildResult(logger, this,
                            annotationsResult.getAnnotations(), annotationsResult.getNewWarnings());
                }
                annotationsResult.setResult(buildResult);
            }

            copyFilesWithAnnotationsToBuildFolder(build.getRootDir(), launcher.getChannel(), annotationsResult.getAnnotations());
        }
        return true;
    }

    /**
     * Copies all files with annotations from the workspace to the build folder.
     *
     * @param rootDir
     *            directory to store the copied files in
     * @param channel
     *            channel to get the files from
     * @param annotations
     *            annotations determining the actual files to copy
     * @throws IOException
     *             if the files could not be written
     * @throws FileNotFoundException
     *             if the files could not be written
     * @throws InterruptedException
     *             if the user cancels the processing
     */
    private void copyFilesWithAnnotationsToBuildFolder(final File rootDir,
            final VirtualChannel channel, final Collection<FileAnnotation> annotations) throws IOException,
            FileNotFoundException, InterruptedException {
        File directory = new File(rootDir, AbstractAnnotation.WORKSPACE_FILES);
        if (!directory.exists()) {
            if (!directory.mkdir()) {
                throw new IOException("Can't create directory for workspace files that contain annotations: " + directory.getAbsolutePath());
            }
        }
        AnnotationContainer container = new DefaultAnnotationContainer(annotations);
        for (WorkspaceFile file : container.getFiles()) {
            File masterFile = new File(directory, file.getTempName());
            if (!masterFile.exists()) {
                try {
                    FileOutputStream outputStream = new FileOutputStream(masterFile);

                    new FilePath(channel, file.getName()).copyTo(outputStream);
                }
                catch (IOException exception) {
                    logExceptionToFile(exception, masterFile, file.getName());
                }
            }
        }
    }

    /**
     * Logs the specified exception in the specified file.
     *
     * @param exception
     *            the exception
     * @param masterFile
     *            the file on the master
     * @param slaveFileName
     *            the file name of the slave
     */
    private void logExceptionToFile(final IOException exception, final File masterFile, final String slaveFileName) {
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(masterFile);
            String message = "Can't copy file from workspace to build folder: workspace=" + slaveFileName
                    + ", build folder=" + masterFile.getAbsolutePath();
            exception.printStackTrace(new PrintStream(outputStream));
            IOUtils.write(message, outputStream);
        }
        catch (IOException error) {
            // ignore
        }
        finally {
            IOUtils.closeQuietly(outputStream);
        }
    }


    /**
     * Returns whether the publisher can continue processing. This default
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
     * Performs the publishing of the results of this plug-in.
     *
     * @param build
     *            the build
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
    protected abstract BuildResult perform(AbstractBuild<?, ?> build, PluginLogger logger) throws InterruptedException, IOException;

    /**
     * Returns the useDeltaValues.
     *
     * @return the useDeltaValues
     */
    public boolean getUseDeltaValues() {
        return useDeltaValues;
    }

    /**
     * Returns the annotation threshold to be reached if a build should be
     * considered as unstable.
     *
     * @return the annotation threshold to be reached if a build should be
     *         considered as unstable.
     */
    public String getThreshold() {
        return threshold;
    }

    /**
     * Returns the threshold of new annotations to be reached if a build should
     * be considered as unstable.
     *
     * @return the threshold of new annotations to be reached if a build should
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

    /**
     * Returns the defined default encoding.
     *
     * @return the default encoding
     */
    public String getDefaultEncoding() {
        return defaultEncoding;
    }

    /**
     * Returns whether the current build uses maven.
     *
     * @param build
     *            the current build
     * @return <code>true</code> if the current build uses maven,
     *         <code>false</code> otherwise
     */
    @SuppressWarnings("rawtypes")
    protected boolean isMavenBuild(final AbstractBuild<?, ?> build) {
        if (build.getProject() instanceof Project) {
            Project<?, ?> project = (Project<?, ?>)build.getProject();
            for (Builder builder : project.getBuilders()) {
                if (builder instanceof Maven) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns whether the current build uses ant.
     *
     * @param build
     *            the current build
     * @return <code>true</code> if the current build uses ant,
     *         <code>false</code> otherwise
     */
    @SuppressWarnings("rawtypes")
    protected boolean isAntBuild(final AbstractBuild<?, ?> build) {
        if (build.getProject() instanceof Project) {
            Project<?, ?> project = (Project<?, ?>)build.getProject();
            for (Builder builder : project.getBuilders()) {
                if (builder instanceof Ant) {
                    return true;
                }
            }
        }
        return false;
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

    /** {@inheritDoc} */
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.STEP;
    }

    /** Backward compatibility. @deprecated */
    @SuppressWarnings("unused")
    @Deprecated
    private transient boolean thresholdEnabled;
    /** Backward compatibility. @deprecated */
    @SuppressWarnings("unused")
    @Deprecated
    private transient int minimumAnnotations;
    /** Backward compatibility. @deprecated */
    @SuppressWarnings("unused")
    @Deprecated
    private transient int healthyAnnotations;
    /** Backward compatibility. @deprecated */
    @SuppressWarnings("unused")
    @Deprecated
    private transient int unHealthyAnnotations;
    /** Backward compatibility. @deprecated */
    @SuppressWarnings("unused")
    @Deprecated
    private transient boolean healthyReportEnabled;
    /** Backward compatibility. @deprecated */
    @SuppressWarnings("unused")
    @Deprecated
    private transient String height;
}

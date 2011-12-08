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
import hudson.matrix.MatrixAggregatable;

import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.model.Project;

import hudson.plugins.analysis.util.PluginLogger;
import hudson.plugins.analysis.util.model.AbstractAnnotation;
import hudson.plugins.analysis.util.model.AnnotationContainer;
import hudson.plugins.analysis.util.model.DefaultAnnotationContainer;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;
import hudson.plugins.analysis.util.model.WorkspaceFile;

import hudson.remoting.VirtualChannel;
import hudson.tasks.BuildStep;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Builder;
import hudson.tasks.Recorder;
import hudson.tasks.Ant;
import hudson.tasks.Maven;

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
@SuppressWarnings("PMD.TooManyFields")
public abstract class HealthAwarePublisher extends Recorder implements HealthDescriptor, MatrixAggregatable {
    private static final long serialVersionUID = -7945220365563528457L;
    private static final String SLASH = "/";

    /** Default threshold priority limit. */
    private static final String DEFAULT_PRIORITY_THRESHOLD_LIMIT = "low";

    /** Report health as 100% when the number of warnings is less than this value. */
    private final String healthy;
    /** Report health as 0% when the number of warnings is greater than this value. */
    private final String unHealthy;
    /** Determines which warning priorities should be considered when evaluating the build health. */
    private String thresholdLimit;

    /** The name of the plug-in. */
    private final String pluginName;
    /** The default encoding to be used when reading and parsing files. */
    private final String defaultEncoding;
    /** Determines whether the plug-in should run for failed builds, too. @since 1.6 */
    private final boolean canRunOnFailed;

    /**
     * Determines whether the absolute annotations delta or the actual
     * annotations set difference should be used to evaluate the build stability.
     *
     * @since 1.4
     */
    private final boolean useDeltaValues;
    /**
     * Thresholds for build status unstable and failed, resp. and priorities
     * all, high, normal, and low, resp.
     *
     * @since 1.14
     */
    private Thresholds thresholds = new Thresholds();
    /**
     * Determines whether module names should be derived from Maven POM or Ant build files.
     *
     * @since 1.19
     */
    private final boolean shouldDetectModules;
    /**
     * Determines whether new warnings should be computed (with respect to baseline).
     *
     * @since 1.34
     */
    private final boolean dontComputeNew;

    /**
     * Creates a new instance of {@link HealthAwarePublisher}.
     *
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
     * @param unstableTotalAll
     *            annotation threshold
     * @param unstableTotalHigh
     *            annotation threshold
     * @param unstableTotalNormal
     *            annotation threshold
     * @param unstableTotalLow
     *            annotation threshold
     * @param unstableNewAll
     *            annotation threshold
     * @param unstableNewHigh
     *            annotation threshold
     * @param unstableNewNormal
     *            annotation threshold
     * @param unstableNewLow
     *            annotation threshold
     * @param failedTotalAll
     *            annotation threshold
     * @param failedTotalHigh
     *            annotation threshold
     * @param failedTotalNormal
     *            annotation threshold
     * @param failedTotalLow
     *            annotation threshold
     * @param failedNewAll
     *            annotation threshold
     * @param failedNewHigh
     *            annotation threshold
     * @param failedNewNormal
     *            annotation threshold
     * @param failedNewLow
     *            annotation threshold
     * @param canRunOnFailed
     *            determines whether the plug-in can run for failed builds, too
     * @param shouldDetectModules
     *            determines whether module names should be derived from Maven POM or Ant build files
     * @param canComputeNew
     *            determines whether new warnings should be computed (with respect to baseline)
     * @param pluginName
     *            the name of the plug-in
     */
    // CHECKSTYLE:OFF
    @SuppressWarnings("PMD")
    public HealthAwarePublisher(final String healthy, final String unHealthy, final String thresholdLimit,
            final String defaultEncoding, final boolean useDeltaValues,
            final String unstableTotalAll, final String unstableTotalHigh, final String unstableTotalNormal, final String unstableTotalLow,
            final String unstableNewAll, final String unstableNewHigh, final String unstableNewNormal, final String unstableNewLow,
            final String failedTotalAll, final String failedTotalHigh, final String failedTotalNormal, final String failedTotalLow,
            final String failedNewAll, final String failedNewHigh, final String failedNewNormal, final String failedNewLow,
            final boolean canRunOnFailed, final boolean shouldDetectModules, final boolean canComputeNew,
            final String pluginName) {
        super();
        this.healthy = healthy;
        this.unHealthy = unHealthy;
        this.thresholdLimit = thresholdLimit;
        this.defaultEncoding = defaultEncoding;

        this.useDeltaValues = useDeltaValues;
        dontComputeNew = !canComputeNew;

        thresholds.unstableTotalAll = unstableTotalAll;
        thresholds.unstableTotalHigh = unstableTotalHigh;
        thresholds.unstableTotalNormal = unstableTotalNormal;
        thresholds.unstableTotalLow = unstableTotalLow;
        thresholds.unstableNewAll = unstableNewAll;
        thresholds.unstableNewHigh = unstableNewHigh;
        thresholds.unstableNewNormal = unstableNewNormal;
        thresholds.unstableNewLow = unstableNewLow;
        thresholds.failedTotalAll = failedTotalAll;
        thresholds.failedTotalHigh = failedTotalHigh;
        thresholds.failedTotalNormal = failedTotalNormal;
        thresholds.failedTotalLow = failedTotalLow;
        thresholds.failedNewAll = failedNewAll;
        thresholds.failedNewHigh = failedNewHigh;
        thresholds.failedNewNormal = failedNewNormal;
        thresholds.failedNewLow = failedNewLow;

        this.canRunOnFailed = canRunOnFailed;
        this.shouldDetectModules = shouldDetectModules;
        this.pluginName = "[" + pluginName + "] ";
    }

    @Deprecated
    @SuppressWarnings("PMD.ExcessiveParameterList")
    public HealthAwarePublisher(final String healthy, final String unHealthy, final String thresholdLimit,
            final String defaultEncoding, final boolean useDeltaValues,
            final String unstableTotalAll, final String unstableTotalHigh, final String unstableTotalNormal, final String unstableTotalLow,
            final String unstableNewAll, final String unstableNewHigh, final String unstableNewNormal, final String unstableNewLow,
            final String failedTotalAll, final String failedTotalHigh, final String failedTotalNormal, final String failedTotalLow,
            final String failedNewAll, final String failedNewHigh, final String failedNewNormal, final String failedNewLow,
            final boolean canRunOnFailed, final boolean shouldDetectModules, final String pluginName) {
        this(healthy, unHealthy, thresholdLimit, defaultEncoding, useDeltaValues,
                unstableTotalAll, unstableTotalHigh, unstableTotalNormal, unstableTotalLow,
                unstableNewAll, unstableNewHigh, unstableNewNormal, unstableNewLow,
                failedTotalAll, failedTotalHigh, failedTotalNormal, failedTotalLow,
                failedNewAll, failedNewHigh, failedNewNormal, failedNewLow,
                canRunOnFailed, shouldDetectModules, true, pluginName);
    }

    @Deprecated
    @SuppressWarnings("PMD.ExcessiveParameterList")
    public HealthAwarePublisher(final String healthy, final String unHealthy, final String thresholdLimit,
            final String defaultEncoding, final boolean useDeltaValues,
            final String unstableTotalAll, final String unstableTotalHigh, final String unstableTotalNormal, final String unstableTotalLow,
            final String unstableNewAll, final String unstableNewHigh, final String unstableNewNormal, final String unstableNewLow,
            final String failedTotalAll, final String failedTotalHigh, final String failedTotalNormal, final String failedTotalLow,
            final String failedNewAll, final String failedNewHigh, final String failedNewNormal, final String failedNewLow,
            final boolean canRunOnFailed, final String pluginName) {
        this(healthy, unHealthy, thresholdLimit,
                defaultEncoding, useDeltaValues,
                unstableTotalAll, unstableTotalHigh, unstableTotalNormal, unstableTotalLow,
                unstableNewAll, unstableNewHigh, unstableNewNormal, unstableNewLow,
                failedTotalAll, failedTotalHigh, failedTotalNormal, failedTotalLow,
                failedNewAll, failedNewHigh, failedNewNormal, failedNewLow,
                canRunOnFailed, false, pluginName);
    }

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
     * @param canRunOnFailed
     *            determines whether the plug-in can run for failed builds, too
     * @param pluginName
     *            the name of the plug-in
     */
    // CHECKSTYLE:OFF
    @SuppressWarnings("PMD")
    @Deprecated
    public HealthAwarePublisher(final String threshold, final String newThreshold,
            final String failureThreshold, final String newFailureThreshold, final String healthy,
            final String unHealthy, final String thresholdLimit,
            final String defaultEncoding, final boolean useDeltaValues, final boolean canRunOnFailed,
            final String pluginName) {
        super();

        thresholds.unstableTotalAll = threshold;
        thresholds.unstableNewAll = newThreshold;
        thresholds.failedTotalAll = failureThreshold;
        thresholds.failedNewAll = newFailureThreshold;

        this.healthy = healthy;
        this.unHealthy = unHealthy;
        this.thresholdLimit = thresholdLimit;
        this.defaultEncoding = defaultEncoding;
        this.useDeltaValues = useDeltaValues;
        this.canRunOnFailed = canRunOnFailed;
        dontComputeNew = false;
        shouldDetectModules = false;
        this.pluginName = "[" + pluginName + "] ";
    }

    /**
     * Initializes new fields that are not serialized yet.
     *
     * @return the object
     */
    protected Object readResolve() {
        if (thresholdLimit == null) {
            thresholdLimit = DEFAULT_PRIORITY_THRESHOLD_LIMIT;
        }
        if (thresholds == null) {
            thresholds = new Thresholds();

            if (threshold != null) {
                thresholds.unstableTotalAll = threshold;
                threshold = null; // NOPMD
            }
            if (newThreshold != null) {
                thresholds.unstableNewAll = newThreshold;
                newThreshold = null; // NOPMD
            }
            if (failureThreshold != null) {
                thresholds.failedTotalAll = failureThreshold;
                failureThreshold = null; //NOPMD
            }
            if (newFailureThreshold != null) {
                thresholds.failedNewAll = newFailureThreshold;
                newFailureThreshold = null; // NOPMD
            }
        }
        return this;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("deprecation") // Eclipse bug #298563
    @Override
    public final boolean perform(final AbstractBuild<?, ?> build, final Launcher launcher, final BuildListener listener) throws InterruptedException, IOException {
        PluginLogger logger = new PluginLogger(listener.getLogger(), pluginName);
        if (canContinue(build.getResult())) {
            BuildResult result;
            try {
                result = perform(build, logger);
            }
            catch (InterruptedException exception) {
                logger.log(exception.getMessage());

                return false;
            }

            if (new NullHealthDescriptor(this).isThresholdEnabled()) {
                result.evaluateStatus(getThresholds(), useDeltaValues, canComputeNew(), logger);
            }

            copyFilesWithAnnotationsToBuildFolder(build.getRootDir(), launcher.getChannel(), result.getAnnotations());
        }
        else {
            logger.log("Skipping publisher since build result is " + build.getResult());
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
        if (!directory.exists() && !directory.mkdir()) {
            throw new IOException("Can't create directory for workspace files that contain annotations: " + directory.getAbsolutePath());
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
            IOUtils.write(String.format(
                    "Copying the source file '%s' from the workspace to the build folder '%s' on the Hudson master failed.\n",
                    slaveFileName, masterFile.getAbsolutePath()), outputStream);
            if (!slaveFileName.startsWith(SLASH) && !slaveFileName.contains(":")) {
                IOUtils.write("Seems that the path is relative, however an absolute path is required when copying the sources.\n", outputStream);
                String base;
                if (slaveFileName.contains(SLASH)) {
                    base = StringUtils.substringAfterLast(slaveFileName, SLASH);
                }
                else {
                    base = slaveFileName;
                }
                IOUtils.write(String.format("Is the file '%s' contained more than once in your workspace?\n",
                        base), outputStream);
            }
            IOUtils.write(String.format("Is the file '%s' a valid filename?\n", slaveFileName), outputStream);
            IOUtils.write(String.format("If you are building on a slave: please check if the file is accessible under '$HUDSON_HOME/[job-name]/%s'\n", slaveFileName), outputStream);
            IOUtils.write(String.format("If you are building on the master: please check if the file is accessible under '$HUDSON_HOME/[job-name]/workspace/%s'\n", slaveFileName), outputStream);
            exception.printStackTrace(new PrintStream(outputStream));
        }
        catch (IOException error) {
            // ignore
        }
        finally {
            IOUtils.closeQuietly(outputStream);
        }
    }

    /**
     * Returns whether new warnings should be computed (with respect to
     * baseline).
     *
     * @return <code>true</code> if new warnings should be computed (with
     *         respect to baseline), <code>false</code> otherwise
     */
    public boolean getCanComputeNew() {
        return canComputeNew();
    }

    /**
     * Returns whether new warnings should be computed (with respect to
     * baseline).
     *
     * @return <code>true</code> if new warnings should be computed (with
     *         respect to baseline), <code>false</code> otherwise
     */
    public boolean canComputeNew() {
        return !dontComputeNew;
    }

    /**
     * Returns whether this plug-in can run for failed builds, too.
     *
     * @return <code>true</code> if this plug-in can run for failed builds,
     *         <code>false</code> otherwise
     */
    public boolean getCanRunOnFailed() {
        return canRunOnFailed;
    }

    /**
     * Returns whether module names should be derived from Maven POM or Ant build files.
     *
     * @return the can run on failed
     */
    public boolean getShouldDetectModules() {
        return shouldDetectModules;
    }

    /**
     * Returns whether module names should be derived from Maven POM or Ant build files.
     *
     * @return the can run on failed
     */
    public boolean shouldDetectModules() {
        return shouldDetectModules;
    }

    /**
     * Returns whether this publisher can continue processing. This default
     * implementation returns <code>true</code> if the property
     * <code>canRunOnFailed</code> is set or if the build is not aborted or
     * failed.
     *
     * @param result
     *            build result
     * @return <code>true</code> if the build can continue
     */
    protected boolean canContinue(final Result result) {
        if (canRunOnFailed) {
            return result != Result.ABORTED;
        }
        else {
            return result != Result.ABORTED && result != Result.FAILURE;
        }
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


    /** {@inheritDoc} */
    public Thresholds getThresholds() {
        return thresholds;
    }

    /**
     * Returns whether absolute annotations delta or the actual annotations set
     * difference should be used to evaluate the build stability.
     *
     * @return <code>true</code> if the annotation count should be used,
     *         <code>false</code> if the actual (set) difference should be
     *         computed
     */
    public boolean getUseDeltaValues() {
        return useDeltaValues;
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
        return canComputeNew() ? BuildStepMonitor.STEP : BuildStepMonitor.NONE;
    }

    /** Annotation threshold to be reached if a build should be considered as unstable. */
    /** Backward compatibility. @deprecated */
    @Deprecated
    private transient String threshold;
    /** Threshold for new annotations to be reached if a build should be considered as unstable. */
    /** Backward compatibility. @deprecated */
    @Deprecated
    private transient String newThreshold;
    /** Annotation threshold to be reached if a build should be considered as failure. */
    /** Backward compatibility. @deprecated */
    @Deprecated
    private transient String failureThreshold;
    /** Threshold for new annotations to be reached if a build should be considered as failure. */
    /** Backward compatibility. @deprecated */
    @Deprecated
    private transient String newFailureThreshold;
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

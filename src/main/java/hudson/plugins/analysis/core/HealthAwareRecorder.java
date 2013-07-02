package hudson.plugins.analysis.core; // NOPMD

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;

import javax.annotation.CheckForNull;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import hudson.FilePath;
import hudson.Launcher;
import hudson.matrix.MatrixAggregatable;

import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.model.Project;

import hudson.plugins.analysis.util.EncodingValidator;
import hudson.plugins.analysis.util.LoggerFactory;
import hudson.plugins.analysis.util.PluginLogger;
import hudson.plugins.analysis.util.model.AbstractAnnotation;
import hudson.plugins.analysis.util.model.AnnotationContainer;
import hudson.plugins.analysis.util.model.DefaultAnnotationContainer;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;
import hudson.plugins.analysis.util.model.WorkspaceFile;

import hudson.remoting.VirtualChannel;
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
 * value then the build is considered as {@link Result#UNSTABLE UNSTABLE}.</li>
 * <li>It provides thresholds for the build health, that could be adjusted in
 * the configuration screen. These values are used by the
 * {@link HealthReportBuilder} to compute the health and the health trend graph.
 * </li>
 * </ul>
 *
 * @author Ulli Hafner
 */
// CHECKSTYLE:COUPLING-OFF
@SuppressWarnings("PMD.TooManyFields")
public abstract class HealthAwareRecorder extends Recorder implements HealthDescriptor, MatrixAggregatable {
    private static final long serialVersionUID = 8892994325541840827L;
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
    /**
     * Determines whether the plug-in should run for failed builds, too.
     *
     * @since 1.6
     */
    private final boolean canRunOnFailed;
    /**
     * Determines whether only stable builds should be used as reference builds
     * or not.
     *
     * @since 1.48
     */
    private final boolean useStableBuildAsReference;
    /**
     * Determines whether the absolute annotations delta or the actual
     * annotations set difference should be used to evaluate the build
     * stability.
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
     * Determines whether module names should be derived from Maven POM or Ant
     * build files.
     *
     * @since 1.19
     */
    private final boolean shouldDetectModules;
    /**
     * Determines whether new warnings should be computed (with respect to
     * baseline).
     *
     * @since 1.34
     */
    private final boolean dontComputeNew;
    /**
     * Determines whether relative paths in warnings should be resolved using a
     * time expensive operation that scans the whole workspace for matching
     * files.
     *
     * @since 1.43
     */
    private final boolean doNotResolveRelativePaths;

    /**
     * Creates a new instance of {@link HealthAwareRecorder}.
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
     * @param useStableBuildAsReference
     *            determines whether only stable builds should be used as
     *            reference builds or not
     * @param shouldDetectModules
     *            determines whether module names should be derived from Maven
     *            POM or Ant build files
     * @param canComputeNew
     *            determines whether new warnings should be computed (with
     *            respect to baseline)
     * @param canResolveRelativePaths
     *            determines whether relative paths in warnings should be
     *            resolved using a time expensive operation that scans the whole
     *            workspace for matching files.
     * @param pluginName
     *            the name of the plug-in
     */
    // CHECKSTYLE:OFF
    @SuppressWarnings("PMD")
    public HealthAwareRecorder(final String healthy, final String unHealthy,
            final String thresholdLimit, final String defaultEncoding,
            final boolean useDeltaValues, final String unstableTotalAll,
            final String unstableTotalHigh, final String unstableTotalNormal,
            final String unstableTotalLow, final String unstableNewAll,
            final String unstableNewHigh, final String unstableNewNormal,
            final String unstableNewLow, final String failedTotalAll, final String failedTotalHigh,
            final String failedTotalNormal, final String failedTotalLow, final String failedNewAll,
            final String failedNewHigh, final String failedNewNormal, final String failedNewLow,
            final boolean canRunOnFailed, final boolean useStableBuildAsReference,
            final boolean shouldDetectModules, final boolean canComputeNew,
            final boolean canResolveRelativePaths, final String pluginName) {
        super();
        this.healthy = healthy;
        this.unHealthy = unHealthy;
        this.thresholdLimit = StringUtils.defaultIfEmpty(thresholdLimit, DEFAULT_PRIORITY_THRESHOLD_LIMIT);
        this.defaultEncoding = defaultEncoding;
        this.useDeltaValues = useDeltaValues;

        doNotResolveRelativePaths = !canResolveRelativePaths;
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
        this.useStableBuildAsReference = useStableBuildAsReference;
        this.shouldDetectModules = shouldDetectModules;
        this.pluginName = "[" + pluginName + "] ";
    }

    // CHECKSTYLE:ON

    /**
     * Returns whether relative paths in warnings should be resolved using a
     * time expensive operation that scans the whole workspace for matching
     * files.
     *
     * @return <code>true</code> if relative paths can be resolved,
     *         <code>false</code> otherwise
     */
    public boolean getCanResolveRelativePaths() {
        return !doNotResolveRelativePaths;
    }

    /**
     * Returns whether relative paths in warnings should be resolved using a
     * time expensive operation that scans the whole workspace for matching
     * files.
     *
     * @return <code>true</code> if relative paths can be resolved,
     *         <code>false</code> otherwise
     */
    public boolean canResolveRelativePaths() {
        return getCanResolveRelativePaths();
    }

    /**
     * Returns whether there is a health threshold enabled.
     *
     * @return <code>true</code> if at least one threshold is enabled,
     *         <code>false</code> otherwise
     */
    protected boolean isThresholdEnabled() {
        return new NullHealthDescriptor(this).isThresholdEnabled();
    }

    /**
     * Determines whether only stable builds should be used as reference builds
     * or not.
     *
     * @return <code>true</code> if only stable builds should be used
     */
    public boolean getUseStableBuildAsReference() {
        return useStableBuildAsReference;
    }

    /**
     * Determines whether only stable builds should be used as reference builds
     * or not.
     *
     * @return <code>true</code> if only stable builds should be used
     */
    public boolean useOnlyStableBuildsAsReference() {
        return getUseStableBuildAsReference();
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
                failureThreshold = null; // NOPMD
            }
            if (newFailureThreshold != null) {
                thresholds.failedNewAll = newFailureThreshold;
                newFailureThreshold = null; // NOPMD
            }
        }
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public final boolean perform(final AbstractBuild<?, ?> build, final Launcher launcher,
            final BuildListener listener) throws InterruptedException, IOException {
        PluginLogger logger = new LoggerFactory().createLogger(listener.getLogger(), pluginName);
        if (canContinue(build.getResult())) {
            return perform(build, launcher, logger);
        }
        else {
            logger.log("Skipping publisher since build result is " + build.getResult());
            return true;
        }
    }

    /**
     * Callback method that is invoked after the build where this recorder can
     * collect the results.
     *
     * @param build
     *            current build
     * @param launcher
     *            the launcher for this build
     * @param logger
     *            the logger
     * @return <code>true</code> if the build can continue, <code>false</code>
     *         otherwise
     * @throws IOException
     *             in case of problems during file copying
     * @throws InterruptedException
     *             if the user canceled the build
     */
    protected abstract boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
            PluginLogger logger) throws InterruptedException, IOException;

    @Override
    public PluginDescriptor getDescriptor() {
        return (PluginDescriptor)super.getDescriptor();
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
    protected void copyFilesWithAnnotationsToBuildFolder(final File rootDir,
            final VirtualChannel channel, final Collection<FileAnnotation> annotations)
            throws IOException, FileNotFoundException, InterruptedException {
        File directory = new File(rootDir, AbstractAnnotation.WORKSPACE_FILES);
        if (!directory.exists() && !directory.mkdir()) {
            throw new IOException(
                    "Can't create directory for workspace files that contain annotations: "
                            + directory.getAbsolutePath());
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
    private void logExceptionToFile(final IOException exception, final File masterFile,
            final String slaveFileName) {
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(masterFile);
            print(outputStream,
                    "Copying the source file '%s' from the workspace to the build folder '%s' on the Jenkins master failed.%n",
                    slaveFileName, masterFile.getAbsolutePath());
            if (!slaveFileName.startsWith(SLASH) && !slaveFileName.contains(":")) {
                print(outputStream,
                        "Seems that the path is relative, however an absolute path is required when copying the sources.%n");
                String base;
                if (slaveFileName.contains(SLASH)) {
                    base = StringUtils.substringAfterLast(slaveFileName, SLASH);
                }
                else {
                    base = slaveFileName;
                }
                print(outputStream,
                        "Is the file '%s' contained more than once in your workspace?%n", base);
            }
            print(outputStream, "Is the file '%s' a valid filename?%n", slaveFileName);
            print(outputStream,
                    "If you are building on a slave: please check if the file is accessible under '$JENKINS_HOME/[job-name]/%s'%n",
                    slaveFileName);
            print(outputStream,
                    "If you are building on the master: please check if the file is accessible under '$JENKINS_HOME/[job-name]/workspace/%s'%n",
                    slaveFileName);
            exception.printStackTrace(new PrintStream(outputStream, false, getEncoding()));
        }
        catch (IOException error) {
            // ignore
        }
        finally {
            IOUtils.closeQuietly(outputStream);
        }
    }

    private void print(final FileOutputStream outputStream, final String message,
            final Object... arguments) throws IOException {
        IOUtils.write(String.format(message, arguments), outputStream, getEncoding());
    }

    private String getEncoding() {
        return EncodingValidator.getEncoding(getDefaultEncoding());
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
     * Returns whether module names should be derived from Maven POM or Ant
     * build files.
     *
     * @return the can run on failed
     */
    public boolean getShouldDetectModules() {
        return shouldDetectModules;
    }

    /**
     * Returns whether module names should be derived from Maven POM or Ant
     * build files.
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
    @CheckForNull
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

    // CHECKSTYLE:OFF
    /** Backward compatibility. @deprecated */
    @Deprecated
    private transient String threshold;
    /** Backward compatibility. @deprecated */
    @Deprecated
    private transient String newThreshold;
    /** Backward compatibility. @deprecated */
    @Deprecated
    private transient String failureThreshold;
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

    /** Backward compatibility. @deprecated */
    @SuppressWarnings({"PMD","javadoc"})
    @Deprecated
    public HealthAwareRecorder(final String threshold, final String newThreshold,
            final String failureThreshold, final String newFailureThreshold, final String healthy,
            final String unHealthy, final String thresholdLimit, final String defaultEncoding,
            final boolean useDeltaValues, final boolean canRunOnFailed, final String pluginName) {
        super();

        thresholds.unstableTotalAll = threshold;
        thresholds.unstableNewAll = newThreshold;
        thresholds.failedTotalAll = failureThreshold;
        thresholds.failedNewAll = newFailureThreshold;
        doNotResolveRelativePaths = false;

        this.healthy = healthy;
        this.unHealthy = unHealthy;
        this.thresholdLimit = thresholdLimit;
        this.defaultEncoding = defaultEncoding;
        this.useDeltaValues = useDeltaValues;
        this.canRunOnFailed = canRunOnFailed;
        useStableBuildAsReference = false;
        dontComputeNew = false;
        shouldDetectModules = false;
        this.pluginName = "[" + pluginName + "] ";
    }

    /** Backward compatibility. @deprecated */
    @SuppressWarnings({"PMD","javadoc"})
    @Deprecated
    public HealthAwareRecorder(final String healthy, final String unHealthy,
            final String thresholdLimit, final String defaultEncoding,
            final boolean useDeltaValues, final String unstableTotalAll,
            final String unstableTotalHigh, final String unstableTotalNormal,
            final String unstableTotalLow, final String unstableNewAll,
            final String unstableNewHigh, final String unstableNewNormal,
            final String unstableNewLow, final String failedTotalAll, final String failedTotalHigh,
            final String failedTotalNormal, final String failedTotalLow, final String failedNewAll,
            final String failedNewHigh, final String failedNewNormal, final String failedNewLow,
            final boolean canRunOnFailed,
            final boolean shouldDetectModules, final boolean canComputeNew,
            final boolean canResolveRelativePaths, final String pluginName) {
        this(healthy, unHealthy, thresholdLimit, defaultEncoding, useDeltaValues,
                unstableTotalAll, unstableTotalHigh, unstableTotalNormal, unstableTotalLow,
                unstableNewAll, unstableNewHigh, unstableNewNormal, unstableNewLow,
                failedTotalAll, failedTotalHigh, failedTotalNormal, failedTotalLow,
                failedNewAll, failedNewHigh, failedNewNormal, failedNewLow,
                canRunOnFailed, false, shouldDetectModules, canComputeNew, canResolveRelativePaths, pluginName);
    }
    // CHECKSTYLE:OFF
}

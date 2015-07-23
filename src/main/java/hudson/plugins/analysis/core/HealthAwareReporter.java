package hudson.plugins.analysis.core; // NOPMD

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.project.MavenProject;

import hudson.FilePath;
import hudson.Launcher;
import hudson.maven.MavenAggregatedReport;
import hudson.maven.MavenBuild;
import hudson.maven.MavenBuildProxy;
import hudson.maven.MavenBuildProxy.BuildCallable;
import hudson.maven.MavenModuleSetBuild;
import hudson.maven.MavenReporter;
import hudson.maven.MojoInfo;

import hudson.model.Run;
import hudson.model.BuildListener;
import hudson.model.Result;

import hudson.plugins.analysis.Messages;
import hudson.plugins.analysis.util.EncodingValidator;
import hudson.plugins.analysis.util.Files;
import hudson.plugins.analysis.util.LoggerFactory;
import hudson.plugins.analysis.util.PluginLogger;
import hudson.plugins.analysis.util.StringPluginLogger;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;

import hudson.remoting.VirtualChannel;
import hudson.tasks.BuildStep;

/**
 * A base class for Maven reporters with the following two characteristics:
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
 * @param <T> the actual type of the build result
 * @author Ulli Hafner
 * @since 1.20
 */
// CHECKSTYLE:COUPLING-OFF
@SuppressWarnings("PMD.TooManyFields")
public abstract class HealthAwareReporter<T extends BuildResult> extends MavenReporter implements HealthDescriptor {
    /** Default threshold priority limit. */
    private static final String DEFAULT_PRIORITY_THRESHOLD_LIMIT = "low";
    /** Unique identifier of this class. */
    private static final long serialVersionUID = -5369644266347796143L;
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
    /** Determines whether the plug-in should run for failed builds, too. @since 1.6 */
    private final boolean canRunOnFailed;

    /**
     * Determines whether the absolute annotations delta or the actual
     * annotations set difference should be used to evaluate the build stability.
     *
     * @since 1.20
     */
    private final boolean useDeltaValues;
    /**
     * Thresholds for build status unstable and failed, resp. and priorities
     * all, high, normal, and low, resp.
     *
     * @since 1.20
     */
    private Thresholds thresholds = new Thresholds();
    /**
     * Determines whether new warnings should be computed (with respect to baseline).
     *
     * @since 1.34
     */
    private final boolean dontComputeNew;
    /**
     * Determine if the previous build should always be used as the reference
     * build, no matter its overall result.
     *
     * @since 1.66
     */
    private final boolean usePreviousBuildAsReference;
    /**
     * Determines whether only stable builds should be used as reference builds or not.
     *
     * @since 1.48
     */
    private final boolean useStableBuildAsReference;

    /**
     * Creates a new instance of <code>HealthReportingMavenReporter</code>.
     *
     * @param healthy
     *            Report health as 100% when the number of warnings is less than
     *            this value
     * @param unHealthy
     *            Report health as 0% when the number of warnings is greater
     *            than this value
     * @param thresholdLimit
     *            determines which warning priorities should be considered when
     *            evaluating the build stability and health
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
     * @param usePreviousBuildAsReference
     *            determine if the previous build should always be used as the
     *            reference build, no matter its overall result.
     * @param useStableBuildAsReference
     *            determines whether only stable builds should be used as reference builds or not
     * @param canComputeNew
     *            determines whether new warnings should be computed (with respect to baseline)
     * @param pluginName
     *            the name of the plug-in
     */
    // CHECKSTYLE:OFF
    @SuppressWarnings("PMD.ExcessiveParameterList")
    public HealthAwareReporter(final String healthy, final String unHealthy, final String thresholdLimit, final boolean useDeltaValues,
            final String unstableTotalAll, final String unstableTotalHigh, final String unstableTotalNormal, final String unstableTotalLow,
            final String unstableNewAll, final String unstableNewHigh, final String unstableNewNormal, final String unstableNewLow,
            final String failedTotalAll, final String failedTotalHigh, final String failedTotalNormal, final String failedTotalLow,
            final String failedNewAll, final String failedNewHigh, final String failedNewNormal, final String failedNewLow,
            final boolean canRunOnFailed, final boolean usePreviousBuildAsReference, final boolean useStableBuildAsReference, final boolean canComputeNew,
            final String pluginName) {
        super();
        this.healthy = healthy;
        this.unHealthy = unHealthy;
        this.thresholdLimit = StringUtils.defaultIfEmpty(thresholdLimit, DEFAULT_PRIORITY_THRESHOLD_LIMIT);
        this.canRunOnFailed = canRunOnFailed;
        this.usePreviousBuildAsReference = usePreviousBuildAsReference;
        this.useStableBuildAsReference = useStableBuildAsReference;
        this.dontComputeNew = !canComputeNew;
        this.pluginName = "[" + pluginName + "] ";

        this.useDeltaValues = useDeltaValues;

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

        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.Jdk14Logger");
    }
    // CHECKSTYLE:ON


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
     * Determines whether to always use the previous build as the reference.
     *
     * @return <code>true</code> if the previous build should always be used.
     */
    public boolean getUsePreviousBuildAsReference() {
        return usePreviousBuildAsReference;
    }

    /**
     * Determines whether to always use the previous build as the reference.
     *
     * @return <code>true</code> if the previous build should always be used.
     */
    public boolean usePreviousBuildAsReference() {
        return getUsePreviousBuildAsReference();
    }

    /**
     * Determines whether only stable builds should be used as reference builds or not.
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

    @Override
    public Thresholds getThresholds() {
        return thresholds;
    }

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

    @Override
    public final boolean postExecute(final MavenBuildProxy build, final MavenProject pom, final MojoInfo mojo,
            final BuildListener listener, final Throwable error) throws InterruptedException, IOException {
        if (!acceptGoal(mojo.getGoal())) {
            return true;
        }

        Result currentResult = getCurrentResult(build);
        PluginLogger logger = new LoggerFactory(receiveSettingsFromMaster(build)).createLogger(listener.getLogger(), pluginName);

        if (!canContinue(currentResult)) {
            logger.log("Skipping reporter since build result is " + currentResult);
            return true;
        }

        if (hasResultAction(build)) {
            return true;
        }

        ParserResult result;
        try {
            result = perform(build, pom, mojo, logger);

            if (result.getModules().isEmpty() && result.getNumberOfAnnotations() == 0) {
                logger.log("No report found for mojo " + mojo.getGoal());
                return true;
            }
        }
        catch (InterruptedException exception) {
            logger.log(exception.getMessage());

            return false;
        }
        logger.logLines(result.getLogMessages());

        setEncoding(pom, result, logger);
        registerResultsOnMaster(build, result, logger);
        copyFilesWithAnnotationsToBuildFolder(logger, build.getRootDir(), result.getAnnotations());

        return true;
    }

    private void registerResultsOnMaster(final MavenBuildProxy build, final ParserResult result, final PluginLogger logger)
            throws IOException, InterruptedException {
        @SuppressWarnings("serial")
        String resultLog = build.execute(new BuildCallable<String, IOException>() {
            @Override
            public String call(final MavenBuild mavenBuild) throws IOException, InterruptedException {
                return registerResults(result, mavenBuild);
            }
        });
        logger.logLines(resultLog);
    }

    private void setEncoding(final MavenProject pom, final ParserResult result, final PluginLogger logger) {
        defaultEncoding = pom.getProperties().getProperty("project.build.sourceEncoding");
        if (defaultEncoding == null) {
            logger.log(Messages.Reporter_Error_NoEncoding(Charset.defaultCharset().displayName()));
            result.addErrorMessage(pom.getName(), Messages.Reporter_Error_NoEncoding(Charset.defaultCharset().displayName()));
        }
    }

    @SuppressWarnings("serial")
    private Settings receiveSettingsFromMaster(final MavenBuildProxy build) throws IOException, InterruptedException {
        return build.execute(new BuildCallable<Settings, IOException>() {
            @Override
            public Settings call(final MavenBuild mavenBuild) throws IOException, InterruptedException {
                return new SerializableSettings(GlobalSettings.instance());
            }});
    }

    private String registerResults(final ParserResult result, final MavenBuild mavenBuild) {
        T buildResult = createResult(mavenBuild, result);

        StringPluginLogger pluginLogger = new StringPluginLogger(pluginName);
        if (new NullHealthDescriptor(this).isThresholdEnabled()) {
            String baseUrl = getDescriptor().getPluginResultUrlName();
            buildResult.evaluateStatus(thresholds, useDeltaValues, canComputeNew(), pluginLogger, baseUrl);
        }
        mavenBuild.addAction(createMavenAggregatedReport(mavenBuild, buildResult));
        mavenBuild.registerAsProjectAction(HealthAwareReporter.this);
        Run<?, ?> referenceBuild = buildResult.getHistory().getReferenceBuild();
        if (referenceBuild != null) {
            pluginLogger.log("Computing warning deltas based on reference build " + referenceBuild.getDisplayName());
        }
        return pluginLogger.toString();
    }

    @Override
    public ReporterDescriptor getDescriptor() {
        return (ReporterDescriptor)super.getDescriptor();
    }

    /**
     * Since aggregation is done in background we still need to log all messages
     * of that step to the log.
     *
     * @param build
     *            the finished maven module build
     * @param launcher
     *            the launcher
     * @param listener
     *            the lister that holds the log
     * @return <code>true</code>
     */
    @Override
    public boolean end(final MavenBuild build, final Launcher launcher, final BuildListener listener) {
        MavenModuleSetBuild moduleSetBuild = build.getParentBuild();
        if (moduleSetBuild != null) {
            MavenResultAction<T> action = moduleSetBuild.getAction(getResultActionClass());
            if (action != null) {
                listener.getLogger().append(action.getLog());
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
     *             if the results could not be read
     * @throws InterruptedException
     *             if the user canceled the operation
     */
    private Result getCurrentResult(final MavenBuildProxy build) throws IOException, InterruptedException {
        return build.execute(new BuildResultCallable());
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
     * Returns whether this reporter can continue processing. This default
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
     * Copies all files with annotations from the workspace to the build folder.
     *
     * @param logger
     *            logger to log any problems
     * @param buildRoot
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
    private void copyFilesWithAnnotationsToBuildFolder(final PluginLogger logger, final FilePath buildRoot,
            final Collection<FileAnnotation> annotations) throws IOException,
            FileNotFoundException, InterruptedException {
        new Files().copyFilesWithAnnotationsToBuildFolder(null, buildRoot, annotations,
                EncodingValidator.getEncoding(getDefaultEncoding()));
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
     * Creates a new {@link BuildResult} instance.
     *
     * @param build
     *            the build (on the master)
     * @param project
     *            the created project
     * @return the created result
     */
    protected abstract T createResult(final MavenBuild build, ParserResult project);

    /**
     * Creates a new {@link BuildResult} instance.
     *
     * @param build
     *            the build (on the master)
     * @param result
     *            the build result
     * @return the created result
     */
    protected abstract MavenAggregatedReport createMavenAggregatedReport(final MavenBuild build, T result);

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
            @Override
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
    protected abstract Class<? extends MavenResultAction<T>> getResultActionClass();

    /**
     * Returns the path to the target folder.
     *
     * @param pom the maven pom
     * @return the path to the target folder
     */
    protected FilePath getTargetPath(final MavenProject pom) {
        return new FilePath((VirtualChannel)null, pom.getBuild().getDirectory());
    }

    /**
     * Returns the healthy threshold, i.e. when health is reported as 100%.
     *
     * @return the 100% healthiness
     */
    @Override
    public String getHealthy() {
        return healthy;
    }

    /**
     * Returns the unhealthy threshold, i.e. when health is reported as 0%.
     *
     * @return the 0% unhealthiness
     */
    @Override
    public String getUnHealthy() {
        return unHealthy;
    }

    @Override
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
     * Returns the name of the module.
     *
     * @param pom
     *            the pom
     * @return the name of the module
     */
    protected String getModuleName(final MavenProject pom) {
        return StringUtils.defaultIfEmpty(pom.getName(), pom.getArtifactId());
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
    /** Backward compatibility. @deprecated */
    @Deprecated
    private transient String threshold;
    /** Backward compatibility. @deprecated */
    @Deprecated
    private transient String failureThreshold;
    /** Backward compatibility. @deprecated */
    @Deprecated
    private transient String newFailureThreshold;
    /** Backward compatibility. @deprecated */
    @Deprecated
    private transient String newThreshold;
    // CHECKSTYLE:OFF
    /** Backwards compatibility.
     * @deprecated
     */
    @SuppressWarnings({"PMD.ExcessiveParamaterList", "javadoc"})
    @Deprecated
    public HealthAwareReporter(final String healthy, final String unHealthy, final String thresholdLimit, final boolean useDeltaValues,
            final String unstableTotalAll, final String unstableTotalHigh, final String unstableTotalNormal, final String unstableTotalLow,
            final String unstableNewAll, final String unstableNewHigh, final String unstableNewNormal, final String unstableNewLow,
            final String failedTotalAll, final String failedTotalHigh, final String failedTotalNormal, final String failedTotalLow,
            final String failedNewAll, final String failedNewHigh, final String failedNewNormal, final String failedNewLow,
            final boolean canRunOnFailed, final boolean useStableBuildAsReference, final boolean canComputeNew,
            final String pluginName) {
            this(healthy, unHealthy, thresholdLimit, useDeltaValues,
            unstableTotalAll, unstableTotalHigh, unstableTotalNormal,
            unstableTotalLow, unstableNewAll, unstableNewHigh,
            unstableNewNormal, unstableNewLow, failedTotalAll, failedTotalHigh,
            failedTotalNormal, failedTotalLow, failedNewAll, failedNewHigh,
            failedNewNormal, failedNewLow, canRunOnFailed, false,
            useStableBuildAsReference, canComputeNew, pluginName);
    }
    /** Backward compatibility. @deprecated */
    @SuppressWarnings({"PMD.ExcessiveParameterList","javadoc"})
    @Deprecated
    public HealthAwareReporter(final String healthy, final String unHealthy, final String thresholdLimit, final boolean useDeltaValues,
            final String unstableTotalAll, final String unstableTotalHigh, final String unstableTotalNormal, final String unstableTotalLow,
            final String unstableNewAll, final String unstableNewHigh, final String unstableNewNormal, final String unstableNewLow,
            final String failedTotalAll, final String failedTotalHigh, final String failedTotalNormal, final String failedTotalLow,
            final String failedNewAll, final String failedNewHigh, final String failedNewNormal, final String failedNewLow,
            final boolean canRunOnFailed, final boolean canComputeNew,
            final String pluginName) {
        this(healthy, unHealthy, thresholdLimit, useDeltaValues,
                unstableTotalAll, unstableTotalHigh, unstableTotalNormal, unstableTotalLow,
                unstableNewAll, unstableNewHigh, unstableNewNormal, unstableNewLow,
                failedTotalAll, failedTotalHigh, failedTotalNormal, failedTotalLow,
                failedNewAll, failedNewHigh, failedNewNormal, failedNewLow,
                canRunOnFailed, false, canComputeNew,
                pluginName);
    }
    /** Backward compatibility. @deprecated */
    @SuppressWarnings({"PMD.ExcessiveParameterList","javadoc"})
    @Deprecated
    public HealthAwareReporter(final String healthy, final String unHealthy, final String thresholdLimit, final boolean useDeltaValues,
            final String unstableTotalAll, final String unstableTotalHigh, final String unstableTotalNormal, final String unstableTotalLow,
            final String unstableNewAll, final String unstableNewHigh, final String unstableNewNormal, final String unstableNewLow,
            final String failedTotalAll, final String failedTotalHigh, final String failedTotalNormal, final String failedTotalLow,
            final String failedNewAll, final String failedNewHigh, final String failedNewNormal, final String failedNewLow,
            final boolean canRunOnFailed, final String pluginName) {
        this(healthy, unHealthy, thresholdLimit, useDeltaValues,
                unstableTotalAll, unstableTotalHigh, unstableTotalNormal, unstableTotalLow,
                unstableNewAll, unstableNewHigh, unstableNewNormal, unstableNewLow,
                failedTotalAll, failedTotalHigh, failedTotalNormal, failedTotalLow,
                failedNewAll, failedNewHigh, failedNewNormal, failedNewLow,
                canRunOnFailed, true, pluginName);
    }
    /**
     * @deprecated mistyped method name from v1.72 - see {@link #getUsePreviousBuildAsReference()}
     */
    @Deprecated
    public boolean getUsePreviousBuildAsStable() {
        return getUsePreviousBuildAsReference();
    }
    /**
     * @deprecated mistyped method name from v1.72 - see {@link #usePreviousBuildAsReference()}
     */
    @Deprecated
    public boolean usePreviousBuildAsStable() {
        return usePreviousBuildAsReference();
    }

    // CHECKSTYLE:ON

    /**
     * Gets the build result from the master.
     */
    private static final class BuildResultCallable implements BuildCallable<Result, IOException> {
        /** Unique ID. */
        private static final long serialVersionUID = -270795641776014760L;

            @Override
        public Result call(final MavenBuild mavenBuild) throws IOException, InterruptedException {
            return mavenBuild.getResult();
        }
    }
}


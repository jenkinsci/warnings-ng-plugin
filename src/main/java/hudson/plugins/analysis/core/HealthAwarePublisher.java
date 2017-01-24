package hudson.plugins.analysis.core;

import java.io.IOException;

import hudson.FilePath;
import hudson.Launcher;

import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.model.Run;

import hudson.plugins.analysis.util.*;

import hudson.tasks.BuildStep;

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
public abstract class HealthAwarePublisher extends HealthAwareRecorder {
    private static final long serialVersionUID = -4225952809165635796L;

    /**
     * Constructor using only required field/s.
     * Use setters to initialize the object if needed.
     *
     * @param pluginName the plugin name
     */
    public HealthAwarePublisher(final String pluginName) {
        super(pluginName);
    }

    /**
     * Callback method that is invoked after the build where this recorder can collect the results. This default
     * implementation provides a template method that updates the build status based on the results and copies all files
     * with warnings to the build folder on the master.
     *
     * @param run
     *            current build
     * @param workspace
     *            the workspace for this build
     * @param launcher
     *            the launcher for this build
     * @param logger
     *            the logger
     * @return <code>true</code> if the build can continue, <code>false</code> otherwise
     * @throws IOException
     *             in case of problems during file copying
     * @throws InterruptedException
     *             if the user canceled the build
     */
    @Override
    protected boolean perform(final Run<?, ?> run, final FilePath workspace, final Launcher launcher, final PluginLogger logger)
            throws IOException, InterruptedException {
        BuildResult result;
        try {
            result = perform(run, workspace, logger);
            AbstractBlamer blamer = BlameFactory.createBlamer(run, workspace, logger);
            blamer.blame(result.getAnnotations());

            Run<?, ?> referenceBuild = result.getHistory().getReferenceBuild();

            if (GlobalSettings.instance().getFailOnCorrupt() && result.hasError()) {
                return false;
            }

            if (referenceBuild != null) {
                logger.log("Computing warning deltas based on reference build " + referenceBuild.getDisplayName());
            }
        }
        catch (InterruptedException exception) {
            logger.log(exception.getMessage());

            return false;
        }

        if (isThresholdEnabled()) {
            updateBuildResult(result, logger);
        }

        copyFilesWithAnnotationsToBuildFolder(run.getRootDir(), launcher.getChannel(), result.getAnnotations());

        return true;
    }

    /**
     * Will be invoked after the build result has been evaluated.
     *
     * @param result
     *            the evaluated build result
     * @param logger
     *            the logger
     */
    protected void updateBuildResult(final BuildResult result, final PluginLogger logger) {
        String baseUrl = getDescriptor().getPluginResultUrlName();
        result.evaluateStatus(getThresholds(), getUseDeltaValues(), canComputeNew(), logger, baseUrl);
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
    @Deprecated
    protected BuildResult perform(AbstractBuild<?, ?> build, PluginLogger logger)
            throws InterruptedException, IOException {
        return perform((Run) build, null, logger);
    }

    @SuppressWarnings("deprecation")
    protected /*abstract*/ BuildResult perform(Run<?, ?> run, FilePath workspace, PluginLogger logger) throws InterruptedException, IOException{
        if (run instanceof AbstractBuild && Compatibility.isOverridden(HealthAwarePublisher.class, getClass(),
                "perform", AbstractBuild.class, PluginLogger.class)) {
            return perform((AbstractBuild) run, logger);
        }
        else {
            // Runtime error to force overriding this method
            throw new AbstractMethodError("you must override the new overload of perform(Run, FilePath, Launcher, PluginLogger)");
        }
    }

    // CHECKSTYLE:OFF
    @Deprecated
    @SuppressWarnings({"PMD.ExcessiveParameterList", "javadoc", "deprecation"})
    public HealthAwarePublisher(final String healthy, final String unHealthy,
            final String thresholdLimit, final String defaultEncoding,
            final boolean useDeltaValues, final String unstableTotalAll,
            final String unstableTotalHigh, final String unstableTotalNormal,
            final String unstableTotalLow, final String unstableNewAll,
            final String unstableNewHigh, final String unstableNewNormal,
            final String unstableNewLow, final String failedTotalAll, final String failedTotalHigh,
            final String failedTotalNormal, final String failedTotalLow, final String failedNewAll,
            final String failedNewHigh, final String failedNewNormal, final String failedNewLow,
            final boolean canRunOnFailed, final boolean shouldDetectModules, final String pluginName) {
        this(healthy, unHealthy, thresholdLimit, defaultEncoding, useDeltaValues, unstableTotalAll,
                unstableTotalHigh, unstableTotalNormal, unstableTotalLow, unstableNewAll,
                unstableNewHigh, unstableNewNormal, unstableNewLow, failedTotalAll,
                failedTotalHigh, failedTotalNormal, failedTotalLow, failedNewAll, failedNewHigh,
                failedNewNormal, failedNewLow, canRunOnFailed, shouldDetectModules, true,
                pluginName);
    }

    @Deprecated
    @SuppressWarnings({"PMD.ExcessiveParameterList", "javadoc", "deprecation"})
    public HealthAwarePublisher(final String healthy, final String unHealthy,
            final String thresholdLimit, final String defaultEncoding,
            final boolean useDeltaValues, final String unstableTotalAll,
            final String unstableTotalHigh, final String unstableTotalNormal,
            final String unstableTotalLow, final String unstableNewAll,
            final String unstableNewHigh, final String unstableNewNormal,
            final String unstableNewLow, final String failedTotalAll, final String failedTotalHigh,
            final String failedTotalNormal, final String failedTotalLow, final String failedNewAll,
            final String failedNewHigh, final String failedNewNormal, final String failedNewLow,
            final boolean canRunOnFailed, final String pluginName) {
        this(healthy, unHealthy, thresholdLimit, defaultEncoding, useDeltaValues, unstableTotalAll,
                unstableTotalHigh, unstableTotalNormal, unstableTotalLow, unstableNewAll,
                unstableNewHigh, unstableNewNormal, unstableNewLow, failedTotalAll,
                failedTotalHigh, failedTotalNormal, failedTotalLow, failedNewAll, failedNewHigh,
                failedNewNormal, failedNewLow, canRunOnFailed, false, pluginName);
    }

    @SuppressWarnings({"PMD", "javadoc", "deprecation"})
    @Deprecated
    public HealthAwarePublisher(final String threshold, final String newThreshold,
            final String failureThreshold, final String newFailureThreshold, final String healthy,
            final String unHealthy, final String thresholdLimit, final String defaultEncoding,
            final boolean useDeltaValues, final boolean canRunOnFailed, final String pluginName) {
        super(threshold, newThreshold, failureThreshold, newFailureThreshold, healthy, unHealthy,
                thresholdLimit, defaultEncoding, useDeltaValues, canRunOnFailed, pluginName);
    }

    /** Backwards compatibility.
     * @deprecated
     */
    @SuppressWarnings("deprecation")
    @Deprecated
    public HealthAwarePublisher(final String healthy, final String unHealthy,
            final String thresholdLimit, final String defaultEncoding,
            final boolean useDeltaValues, final String unstableTotalAll,
            final String unstableTotalHigh, final String unstableTotalNormal,
            final String unstableTotalLow, final String unstableNewAll,
            final String unstableNewHigh, final String unstableNewNormal,
            final String unstableNewLow, final String failedTotalAll,
            final String failedTotalHigh, final String failedTotalNormal,
            final String failedTotalLow, final String failedNewAll,
            final String failedNewHigh, final String failedNewNormal,
            final String failedNewLow, final boolean canRunOnFailed,
            final boolean useStableBuildAsReference,
            final boolean shouldDetectModules, final boolean canComputeNew,
            final boolean canResolveRelativePaths, final String pluginName) {
        this(healthy, unHealthy, thresholdLimit, defaultEncoding,
                useDeltaValues, unstableTotalAll, unstableTotalHigh,
                unstableTotalNormal, unstableTotalLow, unstableNewAll,
                unstableNewHigh, unstableNewNormal, unstableNewLow,
                failedTotalAll, failedTotalHigh, failedTotalNormal,
                failedTotalLow, failedNewAll, failedNewHigh, failedNewNormal,
                failedNewLow, canRunOnFailed, false, useStableBuildAsReference,
                shouldDetectModules, canComputeNew, canResolveRelativePaths,
                pluginName);
    }

    @SuppressWarnings({"PMD", "javadoc", "deprecation"})
    @Deprecated
    public HealthAwarePublisher(final String healthy, final String unHealthy,
            final String thresholdLimit, final String defaultEncoding,
            final boolean useDeltaValues, final String unstableTotalAll,
            final String unstableTotalHigh, final String unstableTotalNormal,
            final String unstableTotalLow, final String unstableNewAll,
            final String unstableNewHigh, final String unstableNewNormal,
            final String unstableNewLow, final String failedTotalAll, final String failedTotalHigh,
            final String failedTotalNormal, final String failedTotalLow, final String failedNewAll,
            final String failedNewHigh, final String failedNewNormal, final String failedNewLow,
            final boolean canRunOnFailed, final boolean shouldDetectModules,
            final boolean canComputeNew, final boolean canResolveRelativePaths,
            final String pluginName) {
        this(healthy, unHealthy, thresholdLimit, defaultEncoding, useDeltaValues,
                unstableTotalAll, unstableTotalHigh, unstableTotalNormal, unstableTotalLow,
                unstableNewAll, unstableNewHigh, unstableNewNormal, unstableNewLow, failedTotalAll,
                failedTotalHigh, failedTotalNormal, failedTotalLow, failedNewAll, failedNewHigh,
                failedNewNormal, failedNewLow, canRunOnFailed, false, shouldDetectModules, canComputeNew,
                canResolveRelativePaths, pluginName);
    }

    @SuppressWarnings({"PMD", "javadoc", "deprecation"})
    @Deprecated
    public HealthAwarePublisher(final String healthy, final String unHealthy,
            final String thresholdLimit, final String defaultEncoding,
            final boolean useDeltaValues, final String unstableTotalAll,
            final String unstableTotalHigh, final String unstableTotalNormal,
            final String unstableTotalLow, final String unstableNewAll,
            final String unstableNewHigh, final String unstableNewNormal,
            final String unstableNewLow, final String failedTotalAll, final String failedTotalHigh,
            final String failedTotalNormal, final String failedTotalLow, final String failedNewAll,
            final String failedNewHigh, final String failedNewNormal, final String failedNewLow,
            final boolean canRunOnFailed, final boolean shouldDetectModules,
            final boolean canComputeNew, final String pluginName) {
        this(healthy, unHealthy, thresholdLimit, defaultEncoding, useDeltaValues, unstableTotalAll,
                unstableTotalHigh, unstableTotalNormal, unstableTotalLow, unstableNewAll,
                unstableNewHigh, unstableNewNormal, unstableNewLow, failedTotalAll,
                failedTotalHigh, failedTotalNormal, failedTotalLow, failedNewAll, failedNewHigh,
                failedNewNormal, failedNewLow, canRunOnFailed, shouldDetectModules, canComputeNew,
                true, pluginName);
    }

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
     * @param usePreviousBuildAsReference
     *            determine if the previous build should always be used as the
     *            reference build, no matter its overall result.
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
     * @since 1.66
     * @deprecated use {@link #HealthAwarePublisher(String)} and setters instead
     */
    @SuppressWarnings({"PMD", "deprecation"})
    @Deprecated
    public HealthAwarePublisher(final String healthy, final String unHealthy,
            final String thresholdLimit, final String defaultEncoding,
            final boolean useDeltaValues, final String unstableTotalAll,
            final String unstableTotalHigh, final String unstableTotalNormal,
            final String unstableTotalLow, final String unstableNewAll,
            final String unstableNewHigh, final String unstableNewNormal,
            final String unstableNewLow, final String failedTotalAll, final String failedTotalHigh,
            final String failedTotalNormal, final String failedTotalLow, final String failedNewAll,
            final String failedNewHigh, final String failedNewNormal, final String failedNewLow,
            final boolean canRunOnFailed,
            final boolean usePreviousBuildAsReference,
            final boolean useStableBuildAsReference,
            final boolean shouldDetectModules, final boolean canComputeNew,
            final boolean canResolveRelativePaths, final String pluginName) {
        super(healthy, unHealthy, thresholdLimit, defaultEncoding,
                useDeltaValues, unstableTotalAll, unstableTotalHigh,
                unstableTotalNormal, unstableTotalLow, unstableNewAll,
                unstableNewHigh, unstableNewNormal, unstableNewLow,
                failedTotalAll, failedTotalHigh, failedTotalNormal,
                failedTotalLow, failedNewAll, failedNewHigh, failedNewNormal,
                failedNewLow, canRunOnFailed, usePreviousBuildAsReference,
                useStableBuildAsReference, shouldDetectModules, canComputeNew,
                canResolveRelativePaths, pluginName);
    }
}

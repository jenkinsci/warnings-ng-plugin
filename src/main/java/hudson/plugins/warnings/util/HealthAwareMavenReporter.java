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
import hudson.plugins.warnings.util.model.Priority;
import hudson.tasks.BuildStep;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
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
public abstract class HealthAwareMavenReporter extends MavenReporter {
    /** Default threshold priority limit. */
    private static final String DEFAULT_PRIORITY_THRESHOLD_LIMIT = "low";
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
    private String thresholdLimit;

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
     * @param thresholdLimit
     *            determines which warning priorities should be considered when
     *            evaluating the build stability and health
     * @param pluginName
     *            the name of the plug-in
     */
    public HealthAwareMavenReporter(final String threshold, final String healthy, final String unHealthy,
            final String height, final String thresholdLimit, final String pluginName) {
        super();
        this.threshold = threshold;
        this.healthy = healthy;
        this.unHealthy = unHealthy;
        this.height = height;
        this.thresholdLimit = thresholdLimit;
        this.pluginName = "[" + pluginName + "] ";

        if (!StringUtils.isEmpty(threshold)) {
            try {
                minimumAnnotations = Integer.valueOf(threshold);
                if (minimumAnnotations >= 0) {
                    thresholdEnabled = true;
                }
            }
            catch (NumberFormatException exception) {
                // nothing to do, we use the default value
            }
        }
        if (!StringUtils.isEmpty(healthy) && !StringUtils.isEmpty(unHealthy)) {
            try {
                healthyAnnotations = Integer.valueOf(healthy);
                unHealthyAnnotations = Integer.valueOf(unHealthy);
                if (healthyAnnotations >= 0 && unHealthyAnnotations > healthyAnnotations) {
                    healthyReportEnabled = true;
                }
            }
            catch (NumberFormatException exception) {
                // nothing to do, we use the default value
            }
        }
        if (StringUtils.isBlank(thresholdLimit)) {
            this.thresholdLimit = DEFAULT_PRIORITY_THRESHOLD_LIMIT;
        }
    }

    /**
     * Initializes new fields that are not serialized yet.
     *
     * @return the object
     */
    private Object readResolve() {
        if (thresholdLimit == null) {
            thresholdLimit = DEFAULT_PRIORITY_THRESHOLD_LIMIT;
        }
        return this;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("serial")
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
            final ParserResult result = perform(build, pom, mojo, logger);

            build.execute(new BuildCallable<Void, IOException>() {
                public Void call(final MavenBuild mavenBuild) throws IOException, InterruptedException {
                    persistResult(result, mavenBuild);

                    return null;
                }
            });

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
        for (Priority priority : getPriorities()) {
            int numberOfAnnotations = result.getNumberOfAnnotations(priority);
            log(logger, "A total of " + numberOfAnnotations + " annotations have been found for priority " + priority);
            annotationCount += numberOfAnnotations;
        }
        if (annotationCount > 0) {
            if (isThresholdEnabled() && annotationCount >= getMinimumAnnotations()) {
                build.setResult(Result.UNSTABLE);
            }
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

    /**
     * Creates a new instance of <code>HealthReportBuilder</code>.
     *
     * @param reportSingleCount
     *            message to be shown if there is exactly one item found
     * @param reportMultipleCount
     *            message to be shown if there are zero or more than one items
     *            found
     * @return the new health report builder
     */
    protected HealthReportBuilder createHealthBuilder(final String reportSingleCount, final String reportMultipleCount) {
        return new HealthReportBuilder(isThresholdEnabled(), getMinimumAnnotations(),
                isHealthyReportEnabled(), getHealthyAnnotations(), getUnHealthyAnnotations(),
                reportSingleCount, reportMultipleCount);
    }

    /**
     * Determines whether a threshold has been defined.
     *
     * @return <code>true</code> if a threshold has been defined
     */
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

    /**
     * Returns the threshold to be reached if a build should be considered as unstable.
     *
     * @return the threshold to be reached if a build should be considered as unstable
     */
    public int getMinimumAnnotations() {
        return minimumAnnotations;
    }

    /**
     * Returns the isHealthyReportEnabled.
     *
     * @return the isHealthyReportEnabled
     */
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

    /**
     * Returns the healthy threshold for annotations, i.e. when health is reported as 100%.
     *
     * @return the 100% healthiness
     */
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

    /**
     * Returns the unhealthy threshold of annotations, i.e. when health is reported as 0%.
     *
     * @return the 0% unhealthiness
     */
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
        return new TrendReportSize(height).getHeight();
    }

    /**
     * Returns the priorities that should should be considered when evaluating
     * the build stability and health.
     *
     * @return the priorities
     */
    protected Collection<Priority> getPriorities() {
        ArrayList<Priority> priorities = new ArrayList<Priority>();
        priorities.add(Priority.HIGH);
        if ("normal".equals(thresholdLimit)) {
            priorities.add(Priority.NORMAL);
        }
        if ("low".equals(thresholdLimit)) {
            priorities.add(Priority.NORMAL);
            priorities.add(Priority.LOW);
        }
        return priorities;
    }

    /**
     * Returns the thresholdLimit.
     *
     * @return the thresholdLimit
     */
    public String getThresholdLimit() {
        return thresholdLimit;
    }
}


package hudson.plugins.analysis.core;

import javax.annotation.Nonnull;
import java.io.IOException;

import hudson.Launcher;
import hudson.matrix.MatrixAggregator;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixRun;
import hudson.model.Action;
import hudson.model.BuildListener;

/**
 * Aggregates {@link ResultAction}s of {@link MatrixRun}s into
 * {@link MatrixBuild}.
 *
 * @author Ulli Hafner
 */
public abstract class AnnotationsAggregator extends MatrixAggregator {
    private final ParserResult totals = new ParserResult();
    private final HealthDescriptor healthDescriptor;
    private final String defaultEncoding;
    /**
     * Determines whether only stable builds should be used as reference builds
     * or not.
     *
     * @since 1.48
     */
    private final boolean useStableBuildAsReference;
    /**
     * Determines whether to always use the previous build as the reference build.
     *
     * @since 1.66
     */
    private final boolean usePreviousBuildAsReference;

    /**
     * Creates a new instance of {@link AnnotationsAggregator}.
     *
     * @param build
     *            the matrix build
     * @param launcher
     *            the launcher
     * @param listener
     *            the build listener
     * @param healthDescriptor
     *            health descriptor
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     * @param usePreviousBuildAsReference
     *            determines whether the previous build should be used as the
     *            reference build
     * @param useStableBuildAsReference
     *            determines whether only stable builds should be used as
     *            reference builds or not
     * @since 1.66
     */
    public AnnotationsAggregator(final MatrixBuild build, final Launcher launcher, final BuildListener listener,
            final HealthDescriptor healthDescriptor, final String defaultEncoding,
            final boolean usePreviousBuildAsReference,
            final boolean useStableBuildAsReference) {
        super(build, launcher, listener);

        this.healthDescriptor = healthDescriptor;
        this.defaultEncoding = defaultEncoding;
        this.usePreviousBuildAsReference = usePreviousBuildAsReference;
        this.useStableBuildAsReference = useStableBuildAsReference;
    }

    @Override
    public boolean endRun(final MatrixRun run) throws InterruptedException, IOException {
        if (totals.hasNoAnnotations() && hasResult(run)) {
            BuildResult result = getResult(run);
            totals.addAnnotations(result.getAnnotations());
            totals.addModules(result.getModules());
        }
        return true;
    }

    @Override
    public boolean endBuild() throws InterruptedException, IOException {
        build.addAction(createAction(healthDescriptor, defaultEncoding, totals));

        return true;
    }

    /**
     * Returns whether the specified run has a result to aggregate.
     *
     * @param run
     *            the run to obtain the annotations from
     * @return <code>true</code> if there is a result to aggregate,
     *         <code>false</code> otherwise
     * @since 1.19
     */
    protected boolean hasResult(final MatrixRun run) {
        return false;
    }

    /**
     * Determines whether only stable builds should be used as reference builds
     * or not.
     *
     * @return <code>true</code> if only stable builds should be used
     */
    public boolean useOnlyStableBuildsAsReference() {
        return useStableBuildAsReference;
    }

    /**
     * Determines whether to always use the previous build as the reference
     * build.
     *
     * @return <code>true</code> if the previous build should always be used as
     *      the reference build.
     * @since 1.66
     */
    public boolean usePreviousBuildAsReference() {
        return usePreviousBuildAsReference;
    }

    /**
     * Returns the annotations of the specified run.
     *
     * @param run
     *            the run to obtain the annotations from
     * @return the annotations of the specified run
     * @see #hasResult(MatrixRun) if there is no valid result available
     */
    @Nonnull
    protected abstract BuildResult getResult(MatrixRun run);

    /**
     * Creates the action that will render the aggregated results.
     *
     * @param healthDescriptor
     *            health descriptor
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     * @param aggregatedResult
     *            the aggregated annotations
     * @return the created action
     */
    @SuppressWarnings("hiding")
    protected abstract Action createAction(HealthDescriptor healthDescriptor, String defaultEncoding, ParserResult aggregatedResult);

        /**
     * Creates a new instance of {@link AnnotationsAggregator}.
     *
     * @param build
     *            the matrix build
     * @param launcher
     *            the launcher
     * @param listener
     *            the build listener
     * @param healthDescriptor
     *            health descriptor
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     * @param useStableBuildAsReference
     *            determines whether only stable builds should be used as
     *            reference builds or not
     * @deprecated use {@link #AnnotationsAggregator(MatrixBuild, Launcher,
     *             BuildListener, HealthDescriptor, String, boolean, boolean)}
     */
    @Deprecated
    public AnnotationsAggregator(final MatrixBuild build, final Launcher launcher, final BuildListener listener,
            final HealthDescriptor healthDescriptor, final String defaultEncoding, final boolean useStableBuildAsReference) {
        this(build, launcher, listener, healthDescriptor, defaultEncoding, false, useStableBuildAsReference);
    }
    /**
     * Creates a new instance of {@link AnnotationsAggregator}.
     *
     * @param build
     *            the matrix build
     * @param launcher
     *            the launcher
     * @param listener
     *            the build listener
     * @param healthDescriptor
     *            health descriptor
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     *            @deprecated use {@link #AnnotationsAggregator(MatrixBuild, Launcher, BuildListener, HealthDescriptor, String, boolean)}
     */
    @SuppressWarnings("deprecation")
    @Deprecated
    public AnnotationsAggregator(final MatrixBuild build, final Launcher launcher, final BuildListener listener,
            final HealthDescriptor healthDescriptor, final String defaultEncoding) {
        this(build, launcher, listener, healthDescriptor, defaultEncoding, false);
    }
}


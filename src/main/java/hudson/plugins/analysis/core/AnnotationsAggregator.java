package hudson.plugins.analysis.core;

import java.io.IOException;

import javax.annotation.Nonnull;

import hudson.Launcher;
import hudson.matrix.MatrixAggregator;
import hudson.matrix.MatrixRun;
import hudson.matrix.MatrixBuild;

import hudson.model.Action;
import hudson.model.BuildListener;

/**
 * Aggregates {@link AbstractResultAction}s of {@link MatrixRun}s into
 * {@link MatrixBuild}.
 *
 * @author Ulli Hafner
 */
public abstract class AnnotationsAggregator extends MatrixAggregator {
    private final ParserResult totals = new ParserResult();
    private final HealthDescriptor healthDescriptor;
    private final String defaultEncoding;

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
     */
    public AnnotationsAggregator(final MatrixBuild build, final Launcher launcher, final BuildListener listener,
            final HealthDescriptor healthDescriptor, final String defaultEncoding) {
        super(build, launcher, listener);

        this.healthDescriptor = healthDescriptor;
        this.defaultEncoding = defaultEncoding;
    }

    /** {@inheritDoc} */
    @Override
    public boolean endRun(final MatrixRun run) throws InterruptedException, IOException {
        if (totals.hasNoAnnotations() && hasResult(run)) {
            BuildResult result = getResult(run);
            totals.addAnnotations(result.getAnnotations());
            totals.addModules(result.getModules());
        }
        return true;
    }

    /** {@inheritDoc} */
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
    protected abstract Action createAction(HealthDescriptor healthDescriptor, String defaultEncoding, ParserResult aggregatedResult);
}


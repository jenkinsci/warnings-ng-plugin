package hudson.plugins.analysis.core;

import java.io.IOException;
import java.util.Collection;

import hudson.Launcher;
import hudson.matrix.MatrixAggregator;
import hudson.matrix.MatrixRun;
import hudson.matrix.MatrixBuild;

import hudson.model.Action;
import hudson.model.BuildListener;

import hudson.plugins.analysis.util.model.FileAnnotation;

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
        if (totals.hasNoAnnotations()) {
            totals.addAnnotations(getAnnotations(run));
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
     * Returns the annotations of the specified run.
     *
     * @param run
     *            the run to obtain the annotations from
     * @return the annotations of the specified run
     */
    protected abstract Collection<? extends FileAnnotation> getAnnotations(MatrixRun run);

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


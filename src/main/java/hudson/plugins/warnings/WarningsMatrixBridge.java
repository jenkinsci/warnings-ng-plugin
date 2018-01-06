package hudson.plugins.warnings;

import hudson.Extension;
import hudson.Launcher;
import hudson.matrix.MatrixAggregatable;
import hudson.matrix.MatrixAggregator;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixProject;
import hudson.model.BuildListener;

/**
 * Provides a {@link MatrixAggregatable} for a {@link MatrixProject}.
 *
 * @author Ullrich Hafner
 */
@Extension(optional = true)
public class WarningsMatrixBridge implements MatrixAggregatable {
    @Override
    public MatrixAggregator createAggregator(final MatrixBuild build, final Launcher launcher, final BuildListener listener) {
        WarningsPublisher publisher = build.getParent().getPublishersList().get(WarningsPublisher.class);
        if (publisher == null) {
            return null;
        }
        return new WarningsAnnotationsAggregator(build, launcher, listener, publisher, publisher.getDefaultEncoding(),
                publisher.usePreviousBuildAsReference(), publisher.useOnlyStableBuildsAsReference());
    }
}

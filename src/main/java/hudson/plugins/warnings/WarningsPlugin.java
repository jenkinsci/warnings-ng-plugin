package hudson.plugins.warnings;

import hudson.Plugin;
import hudson.tasks.BuildStep;

/**
 * Registers the warnings plug-in publisher and reporter.
 *
 * @author Ulli Hafner
 */
public class WarningsPlugin extends Plugin {
    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("PMD")
    public void start() throws Exception {
        BuildStep.PUBLISHERS.addRecorder(WarningPublisher.WARNINGS_DESCRIPTOR);
    }
}

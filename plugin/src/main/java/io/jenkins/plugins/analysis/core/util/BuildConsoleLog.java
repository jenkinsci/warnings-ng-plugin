package io.jenkins.plugins.analysis.core.util;

import edu.hm.hafner.analysis.ReaderFactory;
import hudson.model.Run;

/**
 * Represents the global console log of a Jenkins build.
 *
 * @author Ullrich Hafner
 */
public class BuildConsoleLog implements ConsoleLog {
    @Override
    public ReaderFactory createReaderFactory(final Run<?, ?> run) {
        return new ConsoleLogReaderFactory(run);
    }
}

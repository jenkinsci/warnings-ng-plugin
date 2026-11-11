package io.jenkins.plugins.analysis.core.util;

import edu.hm.hafner.analysis.ReaderFactory;
import hudson.model.Run;

/**
 * Abstraction for the console log of a Jenkins build. Provides a {@link ReaderFactory} to scan the console log
 * for issues. A console log can either represent the entire build log or an isolated log from a parallel pipeline branch.
 *
 * @author Ullrich Hafner
 */
@FunctionalInterface
public interface ConsoleLog {
    /** Global build log instance. */
    ConsoleLog BUILD = new BuildConsoleLog();

    /**
     * Creates a {@link ReaderFactory} to read the console log.
     *
     * @param run
     *         the build
     * @return the reader factory
     */
    ReaderFactory createReaderFactory(Run<?, ?> run);

    /**
     * Returns whether this console log represents an isolated parallel branch log.
     *
     * @return {@code true} if an isolated branch log, {@code false} if the global build log
     */
    default boolean isBranchLog() {
        return false;
    }
}

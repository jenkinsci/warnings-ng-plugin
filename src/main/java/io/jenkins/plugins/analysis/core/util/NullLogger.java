package io.jenkins.plugins.analysis.core.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * Null logger.
 *
 * @author Ulli Hafner
 */
final class NullLogger extends AnalysisLogger {
    /**
     * Creates a new instance of {@link NullLogger}.
     */
    public NullLogger() {
        super(new PrintStream(new ByteArrayOutputStream()), "null");
    }
}
package hudson.plugins.analysis.util;

/**
 * Null logger.
 *
 * @author Ulli Hafner
 */
public final class NullLogger extends PluginLogger {
    /**
     * Creates a new instance of {@link NullLogger}.
     */
    public NullLogger() {
        super("null");
    }

    /** {@inheritDoc} */
    @Override
    public void log(final String message) {
        // do not log
    }

    /** {@inheritDoc} */
    @Override
    public void log(final Throwable throwable) {
        // do not log
    }

    /** {@inheritDoc} */
    @Override
    public void printStackTrace(final Throwable throwable) {
        // do not log
    }

    /** {@inheritDoc} */
    @Override
    public void logLines(final String lines) {
        // do not log
    }
}
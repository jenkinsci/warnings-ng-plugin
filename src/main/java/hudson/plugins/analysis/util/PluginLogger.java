package hudson.plugins.analysis.util;

import java.io.PrintStream;

import org.apache.commons.lang.StringUtils;

/**
 * A simple logger that prefixes each message with the plug-in name.
 *
 * @author Ulli Hafner
 */
public class PluginLogger {
    /** The plug-in name. */
    private final String pluginName;
    /** The actual print stream to log to. */
    private final PrintStream logger;

    /**
     * Creates a new instance of {@link PluginLogger}.
     *
     * @param logger
     *            the actual print stream to log to
     * @param pluginName
     *            the plug-in name
     */
    public PluginLogger(final PrintStream logger, final String pluginName) {
        this.logger = logger;
        this.pluginName = pluginName;
    }

    /**
     * Logs the specified message.
     *
     * @param message the message
     */
    public void log(final String message) {
        logger.println(StringUtils.defaultString(pluginName) + message);
    }


    /**
     * Logs the specified throwable.
     *
     * @param throwable
     *            the throwable
     */
    public void log(final Throwable throwable) {
        logger.println(StringUtils.defaultString(pluginName) + throwable.getMessage());
    }

    /**
     * Logs the stack trace of the throwable.
     *
     * @param throwable the throwable
     */
    public void printStackTrace(final Throwable throwable) {
        throwable.printStackTrace(logger);
    }
}


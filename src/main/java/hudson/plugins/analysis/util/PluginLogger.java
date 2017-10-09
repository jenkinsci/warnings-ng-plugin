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
    private PrintStream logger;

    /**
     * Creates a new instance of {@link PluginLogger}.
     *
     * @param logger
     *         the actual print stream to log to
     * @param pluginName
     *         the plug-in name
     */
    public PluginLogger(final PrintStream logger, final String pluginName) {
        this(pluginName);

        this.logger = logger;
    }

    /**
     * Creates a new instance of {@link PluginLogger}. Note that the logger needs to be set afterwards to avoid throwing
     * a {@link NullPointerException}.
     *
     * @param pluginName
     *         the plug-in name
     */
    protected PluginLogger(final String pluginName) {
        if (pluginName.contains("[")) {
            this.pluginName = pluginName;
        }
        else {
            this.pluginName = String.format("[%s] ", pluginName);
        }
    }

    /**
     * Sets the logger to the specified value.
     *
     * @param logger
     *         the value to set
     */
    protected void setLogger(final PrintStream logger) {
        this.logger = logger;
    }

    /**
     * Logs the specified message.
     *
     * @param message
     *         the message
     */
    public void log(final String message) {
        logger.println(StringUtils.defaultString(pluginName) + message);
    }

    /**
     * Logs the specified message.
     *
     * @param format
     *         A <a href="../util/Formatter.html#syntax">format string</a>
     * @param args
     *         Arguments referenced by the format specifiers in the format string.  If there are more arguments than
     *         format specifiers, the extra arguments are ignored.  The number of arguments is variable and may be zero.
     */
    public void format(final String format, final Object... args) {
        logger.println(StringUtils.defaultString(pluginName) + String.format(format, args));
    }

    /**
     * Logs the specified throwable.
     *
     * @param throwable
     *         the throwable
     */
    public void log(final Throwable throwable) {
        logger.println(StringUtils.defaultString(pluginName) + throwable.getMessage());
    }

    /**
     * Logs the stack trace of the throwable.
     *
     * @param throwable
     *         the throwable
     */
    public void printStackTrace(final Throwable throwable) {
        throwable.printStackTrace(logger);
    }

    /**
     * Logs several lines that already contain a prefix.
     *
     * @param lines
     *         the lines to log
     */
    public void logLines(final String lines) {
        logger.print(lines);
    }
}


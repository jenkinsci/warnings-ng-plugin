package io.jenkins.plugins.analysis.core.util;

import org.eclipse.collections.api.list.ImmutableList;

import com.google.errorprone.annotations.FormatMethod;

import edu.hm.hafner.analysis.Report;

import hudson.model.TaskListener;

/**
 * Handles logging of issues log and error messages to a {@link TaskListener} instance.
 *
 * @author Ullrich Hafner
 */
@SuppressWarnings("PMD.LoggerIsNotStaticFinal")
public class LogHandler {
    private final ToolLogger errorLogger;
    private final ToolLogger logger;
    private int infoPosition = 0;
    private int errorPosition = 0;

    /**
     * Creates a new {@link LogHandler}.
     *
     * @param listener
     *         the task listener that will print all log messages
     * @param name
     *         the name of the logger
     */
    public LogHandler(final TaskListener listener, final String name) {
        this(listener, name, 0, 0);
    }

    /**
     * Creates a new {@link LogHandler}.
     *
     * @param listener
     *         the task listener that will print all log messages
     * @param name
     *         the name of the logger
     * @param report
     *         the report to log the messages from
     */
    public LogHandler(final TaskListener listener, final String name, final Report report) {
        this(listener, name, report.getInfoMessages().size(), report.getErrorMessages().size());
    }

    private LogHandler(final TaskListener listener, final String name, final int infoPosition,
            final int errorPosition) {
        logger = createLogger(listener, name);
        errorLogger = createErrorLogger(listener, name);
        this.infoPosition = infoPosition;
        this.errorPosition = errorPosition;
    }

    private ToolLogger createErrorLogger(final TaskListener listener, final String name) {
        return createLogger(listener, String.format("[%s] [-ERROR-]", name));
    }

    private ToolLogger createLogger(final TaskListener listener, final String name) {
        return new ToolLogger(listener.getLogger(), name);
    }

    /**
     * Log all info and error messages that are stored in the set of issues. Note that subsequent calls to this method
     * will only log messages that have not yet been logged.
     *
     * @param report
     *         the issues with the collected logging messages
     */
    public void log(final Report report) {
        logErrorMessages(report);
        logInfoMessages(report);
    }

    /**
     * Logs the specified message.
     *
     * @param format
     *         A <a href="../util/Formatter.html#syntax">format string</a>
     * @param args
     *         Arguments referenced by the format specifiers in the format string.  If there are more arguments than
     *         format specifiers, the extra arguments are ignored.  The number of arguments is variable and may be
     *         zero.
     */
    @FormatMethod
    public void log(final String format, final Object... args) {
        logger.log(format, args);
    }

    private void logErrorMessages(final Report report) {
        ImmutableList<String> errorMessages = report.getErrorMessages();
        if (errorPosition < errorMessages.size()) {
            errorLogger.logEachLine(errorMessages.subList(errorPosition, errorMessages.size()).castToList());
            errorPosition = errorMessages.size();
        }
    }

    private void logInfoMessages(final Report report) {
        ImmutableList<String> infoMessages = report.getInfoMessages();
        if (infoPosition < infoMessages.size()) {
            logger.logEachLine(infoMessages.subList(infoPosition, infoMessages.size()).castToList());
            infoPosition = infoMessages.size();
        }
    }
}

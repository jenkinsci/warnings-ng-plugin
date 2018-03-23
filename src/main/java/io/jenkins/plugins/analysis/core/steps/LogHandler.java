package io.jenkins.plugins.analysis.core.steps;

import org.eclipse.collections.api.list.ImmutableList;

import edu.hm.hafner.analysis.Issues;
import io.jenkins.plugins.analysis.core.util.Logger;
import io.jenkins.plugins.analysis.core.util.LoggerFactory;

import hudson.model.TaskListener;

/**
 * Handles logging of issues log and error messages to a {@link TaskListener} instance.
 *
 * @author Ullrich Hafner
 */
class LogHandler {
    private final Logger errorLogger;
    private final Logger logger;
    private int infoPosition = 0;
    private int errorPosition = 0;

    /**
     * Creates a new {@link LogHandler}.
     *
     * @param listener
     *         the task listener that will print all log messages
     * @param id
     *         the ID of the logger
     */
    LogHandler(final TaskListener listener, final String id) {
        logger = createLogger(listener, id);
        errorLogger = createErrorLogger(listener, id);
    }

    private Logger createErrorLogger(final TaskListener listener, final String name) {
        return createLogger(listener, String.format("[%s] [ERROR]", name));
    }

    private Logger createLogger(final TaskListener listener, final String name) {
        return new LoggerFactory().createLogger(listener.getLogger(), name);
    }

    /**
     * Log all info and error messages that are stored in the set of issues. Note that subsequent calls to this method
     * will only log messages that have not yet been logged.
     *
     * @param issues
     *         the issues with the collected logging messages
     */
    public void log(final Issues<?> issues) {
        logErrorMessages(issues);
        logInfoMessages(issues);
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
    public void log(final String format, final Object... args) {
        logger.log(format, args);
    }

    private void logErrorMessages(final Issues<?> issues) {
        ImmutableList<String> errorMessages = issues.getErrorMessages();
        if (errorPosition < errorMessages.size()) {
            errorLogger.logEachLine(errorMessages.subList(errorPosition, errorMessages.size()).castToList());
            errorPosition = errorMessages.size();
        }
    }

    private void logInfoMessages(final Issues<?> issues) {
        ImmutableList<String> infoMessages = issues.getInfoMessages();
        if (infoPosition < infoMessages.size()) {
            logger.logEachLine(infoMessages.subList(infoPosition, infoMessages.size()).castToList());
            infoPosition = infoMessages.size();
        }
    }
}

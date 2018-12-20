package io.jenkins.plugins.analysis.core.util;

/**
 * Handles references to issues in the console log.
 *
 * @author Ullrich Hafner
 */
public final class ConsoleLogHandler {
    /** ID that indicates that an issue references a line in the console log of a build. */
    public static final String JENKINS_CONSOLE_LOG_FILE_NAME_ID = "jenkins-console.log";

    /**
     * Returns whether the specified issue refers to a line in the console log.
     *
     * @param fileName
     *         the affected file
     *
     * @return {@code true} if the issue refers to a line in the console log, {@code false} if the issue refers to a
     *         source code file in the workspace
     */
    public static boolean isInConsoleLog(final String fileName) {
        return JENKINS_CONSOLE_LOG_FILE_NAME_ID.equals(fileName);
    }

    private ConsoleLogHandler() {
        // prevents instantiation
    }
}

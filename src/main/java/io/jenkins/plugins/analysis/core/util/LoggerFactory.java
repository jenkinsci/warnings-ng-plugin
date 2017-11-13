package io.jenkins.plugins.analysis.core.util;

import javax.annotation.CheckForNull;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;

import hudson.plugins.analysis.core.GlobalSettings;
import hudson.plugins.analysis.core.Settings;

/**
 * Provides a mechanism to create an {@link PrintStreamLogger} which depends on the quiet-mode. The quiet-mode can be set
 * in Jenkins' global settings.
 *
 * @author Sebastian Seidl
 */
public class LoggerFactory {
    private final Settings settings;

    /**
     * Creates a new instance of {@link LoggerFactory}.
     */
    public LoggerFactory() {
        this(GlobalSettings.instance());
    }

    /**
     * Creates a new instance of {@link LoggerFactory}.
     *
     * @param settings
     *         the settings to use
     */
    public LoggerFactory(final Settings settings) {
        this.settings = settings;
    }

    /**
     * Creates a new instance of {@link PrintStreamLogger}. If the quite-mode is enabled, then a {@link NullLogger} is
     * returned.
     *
     * @param printStream
     *         The actual print stream to log to. If {@code null} then a {@link NullLogger} is returned.
     * @param toolName
     *         the name of the static analysis tool
     *
     * @return the PluginLogger to use
     */
    public Logger createLogger(@CheckForNull final PrintStream printStream, final String toolName) {
        if (isQuiet() || printStream == null) {
            return new NullLogger();
        }
        else {
            return new PrintStreamLogger(printStream, toolName);
        }
    }

    private boolean isQuiet() {
        return settings.getQuietMode();
    }

    /**
     * A simple logger that prefixes each message with the name of the static analysis tool.
     *
     * @author Ulli Hafner
     */
    private static class PrintStreamLogger implements Logger {
        private final String toolName;
        private final PrintStream delegate;

        private PrintStreamLogger(final PrintStream logger, final String toolName) {
            if (toolName.contains("[")) {
                this.toolName = toolName + " ";
            }
            else {
                this.toolName = String.format("[%s] ", toolName);
            }
            delegate = logger;
        }

        @Override
        public void log(final String format, final Object... args) {
            print(String.format(format, args));
        }

        @Override
        public void logEachLine(final Collection<String> lines) {
            lines.forEach(this::print);
        }

        private void print(final String line) {
            delegate.println(StringUtils.defaultString(toolName) + line);
        }
    }
    
    /**
     * Null logger.
     */
    private static class NullLogger extends PrintStreamLogger {
        /**
         * Creates a new instance of {@link NullLogger}.
         */
        private NullLogger() {
            super(new PrintStream(new ByteArrayOutputStream()), "null");
        }
    }
}

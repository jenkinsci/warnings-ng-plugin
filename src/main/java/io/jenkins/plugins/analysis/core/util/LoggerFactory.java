package io.jenkins.plugins.analysis.core.util;

import javax.annotation.CheckForNull;
import java.io.PrintStream;

import hudson.plugins.analysis.core.GlobalSettings;
import hudson.plugins.analysis.core.Settings;

/**
 * Provides a mechanism to create an {@link AnalysisLogger} which depends on the quiet-mode. The quiet-mode can be set
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
     * Creates a new instance of {@link AnalysisLogger}. If the quite-mode is enabled, then a {@link NullLogger} is
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
            return new AnalysisLogger(printStream, toolName);
        }
    }

    private boolean isQuiet() {
        return settings.getQuietMode();
    }
}

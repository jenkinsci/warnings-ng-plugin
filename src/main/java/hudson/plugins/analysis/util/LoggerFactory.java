package hudson.plugins.analysis.util;

import java.io.PrintStream;

import hudson.plugins.analysis.core.Settings;
import hudson.plugins.analysis.core.GlobalSettings;

/**
 * Provides a Mechanism to create a PluginLogger which depends on the QuietMode. QuietMode can be set in GlobalSettings.
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
     *            the settings to use
     */
    public LoggerFactory(final Settings settings) {
        this.settings = settings;
    }

    /**
     * Creates a new instance of {@link PluginLogger}.
     *
     * @param logger
     *            the actual print stream to log to
     * @param pluginName
     *            the plug-in name
     * @return the PluginLogger to use
     */
    public PluginLogger createLogger(final PrintStream logger, final String pluginName) {
        if (isQuiet()) {
            return new NullLogger();
        }
        else {
            return new PluginLogger(logger, pluginName);
        }
    }

    private boolean isQuiet() {
        return settings.getQuietMode();
    }
}

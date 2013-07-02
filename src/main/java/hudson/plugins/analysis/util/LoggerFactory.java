package hudson.plugins.analysis.util;

import java.io.PrintStream;

import jenkins.model.Jenkins;

import hudson.plugins.analysis.core.GlobalSettings;

/**
 * Provides a Mechanism to create a PluginLogger
 * which depends on the QuietMode.
 * QuietMode can be set in GlobalSettings.
 *
 * @author Sebastian Seidl
 */
public class LoggerFactory {

    /**
     * Provides the setting of QuitMode.
     */
    private final GlobalSettings.DescriptorImpl settings;

    /**
     * Creates a new instance of {@link LoggerFactory}.
     */
    public LoggerFactory() {
       settings  = (GlobalSettings.DescriptorImpl)Jenkins.getInstance().getDescriptorOrDie(GlobalSettings.class);
    }

    /**
     * Creates a new instance of {@link LoggerFactory}.
     * Note: This Contructor is used for test purpose only.
     *
     * @param settings Mock of GlobalSettings.DescriptorImpl
     */
    public LoggerFactory(final GlobalSettings.DescriptorImpl settings) {
       this.settings  = settings;
    }

    /**
     * Creates a new instance of {@link PluginLogger}.
     *
     * @param logger
     *            the actual print stream to log to
     * @param pluginName
     *            the plug-in name
     * @return Pluginlogger the PluginLogger to use
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
        return settings.getQuiet();
    }
}


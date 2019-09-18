package io.jenkins.plugins.analysis.core.util;

import jenkins.model.GlobalConfiguration;

/**
 * Facade to Jenkins {@link GlobalConfiguration} instance to prevent IO access during tests.
 *
 * @author Ullrich Hafner
 */
public class GlobalConfigurationFacade {
    private final GlobalConfiguration configuration;

    /**
     * Creates a new facade to the specified {@link GlobalConfiguration}.
     *
     * @param configuration
     *         the global configuration instance
     */
    public GlobalConfigurationFacade(final GlobalConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * Loads the data from the disk into this object.
     */
    public void load() {
        configuration.load();
    }

    /**
     * Saves the configuration info to the disk.
     */
    public void save() {
        configuration.save();
    }
}

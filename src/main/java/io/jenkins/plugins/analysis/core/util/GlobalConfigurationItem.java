package io.jenkins.plugins.analysis.core.util;

import edu.hm.hafner.util.VisibleForTesting;

import jenkins.model.GlobalConfiguration;

/**
 * Testable base class for items of the {@link GlobalConfiguration} page.
 *
 * @author Ullrich Hafner
 */
public class GlobalConfigurationItem extends GlobalConfiguration {
    private final Runnable actualLoad;
    private final Runnable actualSave;

    /**
     * Creates a new {@link GlobalConfigurationItem}.
     */
    protected GlobalConfigurationItem() {
        super();

        actualLoad = super::load;
        actualSave = super::save;
    }

    /**
     * Creates a new {@link GlobalConfigurationItem}.
     *
     * @param facade
     *         the facade to use
     */
    @VisibleForTesting
    protected GlobalConfigurationItem(final GlobalConfigurationFacade facade) {
        super();

        actualLoad = facade::load;
        actualSave = facade::save;
    }

    @Override
    public final synchronized void load() {
        actualLoad.run();
    }

    @Override
    public final synchronized void save() {
        actualSave.run();
    }
}

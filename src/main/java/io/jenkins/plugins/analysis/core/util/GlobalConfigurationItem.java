package io.jenkins.plugins.analysis.core.util;

import edu.hm.hafner.util.VisibleForTesting;

import jenkins.model.GlobalConfiguration;

/**
 * Testable base class for items of the {@link GlobalConfiguration} page.
 *
 * @author Ullrich Hafner
 */
public class GlobalConfigurationItem extends GlobalConfiguration {
    private transient Runnable actualLoad;
    private transient Runnable actualSave;

    /**
     * Creates a new {@link GlobalConfigurationItem}.
     */
    protected GlobalConfigurationItem() {
        super();

        activatePersistence();
    }

    private void activatePersistence() {
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

    /**
     * Called after de-serialization to restore transient fields.
     *
     * @return this
     */
    protected Object readResolve() {
        activatePersistence();

        return this;
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

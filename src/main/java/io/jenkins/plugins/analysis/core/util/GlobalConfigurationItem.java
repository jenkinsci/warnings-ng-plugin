package io.jenkins.plugins.analysis.core.util;

import edu.hm.hafner.util.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.StaplerRequest;
import jenkins.model.GlobalConfiguration;

/**
 * Testable base class for items of the {@link GlobalConfiguration} page.
 *
 * @author Ullrich Hafner
 */
@SuppressFBWarnings("IS2_INCONSISTENT_SYNC")
public class GlobalConfigurationItem extends GlobalConfiguration {
    @SuppressWarnings("PMD.DoNotUseThreads")
    private transient Runnable actualSave;
    @SuppressWarnings("PMD.DoNotUseThreads")
    private transient Runnable actualLoad;

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
    public boolean configure(final StaplerRequest req, final JSONObject json) throws FormException {
        clearRepeatableProperties();

        return super.configure(req, json);
    }

    /**
     * Clears all model elements of a repeatable property. Due to a bug in Stapler data binding of repeatable properties
     * the model elements are only changed if they consist of one or more values. If all values have been removed and
     * the associated form is empty, then the setter is not invoked anymore.
     */
    protected void clearRepeatableProperties() {
        // empty default implementation
    }

    @Override
    @SuppressWarnings("PMD.AvoidSynchronizedAtMethodLevel")
    public final synchronized void load() {
        actualLoad.run();
    }

    @Override
    @SuppressWarnings("PMD.AvoidSynchronizedAtMethodLevel")
    public final synchronized void save() {
        actualSave.run();
    }
}

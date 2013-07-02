package hudson.plugins.analysis.core;

import jenkins.model.Jenkins;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.StaplerRequest;

import hudson.Extension;

import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Run;
import hudson.model.listeners.RunListener;

/**
 * Global settings common to all static analysis plug-ins. The actual extension point {@link RunListener} is not used
 * yet, this object is only used to provide a model for the view global.jelly.
 *
 * @author Ulli Hafner
 */
@Extension
public class GlobalSettings extends RunListener<Run<?, ?>> implements Describable<GlobalSettings> {
    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    public Descriptor<GlobalSettings> getDescriptor() {
        return Jenkins.getInstance().getDescriptorOrDie(getClass());
    }

    /**
     * Descriptor for {@link GlobalSettings}.
     *
     * @author Ulli Hafner
     */
    @Extension
    public static class DescriptorImpl extends Descriptor<GlobalSettings> {
        private Boolean isQuiet;

        @Override
        public String getDisplayName() {
            return StringUtils.EMPTY;
        }

        /**
         * Creates a new instance of {@link GlobalSettings.DescriptorImpl}.
         */
        public DescriptorImpl() {
            super();

            load();
        }

        @Override
        public boolean configure(final StaplerRequest req, final JSONObject json) throws FormException {
            req.bindJSON(this, json);
            save();

            return true;
        }

        /**
         * Returns the value of the example boolean property.
         *
         * @return the status of the example boolean property
         */
        public Boolean getQuiet() {
            return isQuiet;
        }

        /**
         * Sets the value of the example boolean property.
         *
         * @param value the value to set
         */
        public void setQuiet(final Boolean value) {
            isQuiet = value;
        }
    }
}

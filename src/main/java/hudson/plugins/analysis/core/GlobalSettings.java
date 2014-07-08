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
 * @since 1.50
 */
@Extension
public class GlobalSettings extends RunListener<Run<?, ?>> implements Describable<GlobalSettings> {
    @Override
    public DescriptorImpl getDescriptor() {
        return findDescriptor();
    }

    private static DescriptorImpl findDescriptor() {
        return (DescriptorImpl)Jenkins.getInstance().getDescriptorOrDie(GlobalSettings.class);
    }

    /**
     * Returns the global settings.
     *
     * @return the global settings
     */
    public static Settings instance() {
        return findDescriptor();
    }

    /**
     * Descriptor for {@link GlobalSettings}.
     *
     * @author Ulli Hafner
     */
    @Extension
    public static class DescriptorImpl extends Descriptor<GlobalSettings> implements Settings {
        private Boolean isQuiet;
        private Boolean failOnCorrupt;

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

            @Override
        public Boolean getQuietMode() {
            return getValidBoolean(isQuiet);
        }

        /**
         * Sets the value of the quiet boolean property.
         *
         * @param value
         *            the value to set
         */
        public void setQuietMode(final Boolean value) {
            isQuiet = value;
        }

            @Override
        public Boolean getFailOnCorrupt() {
            return getValidBoolean(failOnCorrupt);
        }

        /**
         * Sets the value of the failOnCorrupt boolean property.
         *
         * @param value
         *            the value to set
         */
        public void setFailOnCorrupt(final Boolean value) {
            failOnCorrupt = value;
        }

        private Boolean getValidBoolean(final Boolean value) {
            return value == null ? Boolean.FALSE : value;
        }
    }
}

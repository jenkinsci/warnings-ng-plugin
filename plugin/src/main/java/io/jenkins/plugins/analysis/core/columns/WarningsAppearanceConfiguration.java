package io.jenkins.plugins.analysis.core.columns;

import edu.hm.hafner.util.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.verb.POST;
import org.jenkinsci.Symbol;
import hudson.Extension;
import hudson.util.ListBoxModel;
import jenkins.appearance.AppearanceCategory;
import jenkins.model.GlobalConfigurationCategory;
import jenkins.model.Jenkins;

import io.jenkins.plugins.analysis.core.util.IssuesStatistics.StatisticProperties;
import io.jenkins.plugins.util.GlobalConfigurationFacade;
import io.jenkins.plugins.util.GlobalConfigurationItem;
import io.jenkins.plugins.util.JenkinsFacade;

/**
 * Global appearance configuration for the Warnings Plugin.
 */
@Extension
@Symbol("warnings")
public class WarningsAppearanceConfiguration extends GlobalConfigurationItem {
    /**
     * Returns the singleton instance of this {@link WarningsAppearanceConfiguration}.
     *
     * @return the singleton instance
     */
    public static WarningsAppearanceConfiguration getInstance() {
        return all().get(WarningsAppearanceConfiguration.class);
    }

    private boolean enableColumnByDefault = true;
    private StatisticProperties defaultType = StatisticProperties.TOTAL;
    private String defaultName = Messages.IssuesTotalColumn_Label();

    private final JenkinsFacade jenkins;

    /**
     * Creates the global configuration and loads the initial values from the corresponding
     * XML file.
     */
    @DataBoundConstructor
    public WarningsAppearanceConfiguration() {
        super();

        jenkins =  new JenkinsFacade();

        load();
    }

    @VisibleForTesting
    WarningsAppearanceConfiguration(final GlobalConfigurationFacade facade, final JenkinsFacade jenkins) {
        super(facade);

        this.jenkins = jenkins;

        load();
    }

    @NonNull
    @Override
    public GlobalConfigurationCategory getCategory() {
        return GlobalConfigurationCategory.get(AppearanceCategory.class);
    }

    /**
     * Returns whether the warnings column should be displayed by default.
     *
     * @return {@code true} if warnings column is shown by default, {@code false} otherwise
     */
    public boolean isEnableColumnByDefault() {
        return enableColumnByDefault;
    }

    /**
     * Enables or disables the warnings column by default.
     *
     * @param enableColumnByDefault
     *         {@code true} to enable the warnings column by default, {@code false} to disable it
     */
    @DataBoundSetter
    public void setEnableColumnByDefault(final boolean enableColumnByDefault) {
        this.enableColumnByDefault = enableColumnByDefault;

        save();
    }

    /**
     * Returns the default type to be used.
     *
     * @return the default type
     */
    public StatisticProperties getDefaultType() {
        return defaultType;
    }

    /**
     * Sets the default type to be used.
     *
     * @param defaultType
     *         the default type to use
     */
    @DataBoundSetter
    public void setDefaultType(final StatisticProperties defaultType) {
        this.defaultType = defaultType;

        save();
    }

    /**
     * Returns the default name for the warnings' column.
     *
     * @return the default name for the warnings' column
     */
    public String getDefaultName() {
        return defaultName;
    }

    /**
     * Sets the default name for the warnings' column.
     *
     * @param defaultName
     *         the default name for the warnings' column
     */
    @DataBoundSetter
    public void setDefaultName(final String defaultName) {
        this.defaultName = defaultName;

        save();
    }

    /**
     * Returns a model with all {@link StatisticProperties types} that can be used in the column.
     *
     * @return a model with all {@link StatisticProperties types}.
     */
    @POST
    @SuppressWarnings("unused") // used by Stapler view data binding
    public ListBoxModel doFillDefaultTypeItems() {
        var model = new ListBoxModel();
        if (jenkins.hasPermission(Jenkins.READ)) {
            for (StatisticProperties types : StatisticProperties.values()) {
                model.add(types.getDisplayName(), types.name());
            }
        }
        return model;
    }
}

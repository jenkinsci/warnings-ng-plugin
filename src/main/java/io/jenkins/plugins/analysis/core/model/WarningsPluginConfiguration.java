package io.jenkins.plugins.analysis.core.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import edu.hm.hafner.util.VisibleForTesting;

import org.kohsuke.stapler.DataBoundSetter;
import org.jenkinsci.Symbol;
import hudson.Extension;
import jenkins.model.GlobalConfiguration;

import io.jenkins.plugins.analysis.core.util.GlobalConfigurationFacade;
import io.jenkins.plugins.analysis.core.util.GlobalConfigurationItem;
import io.jenkins.plugins.analysis.warnings.groovy.ParserConfiguration;

/**
 * Global system configuration of the warnings plugin. These configuration options are used globally for all jobs and
 * require administrator permissions.
 *
 * @author Ullrich Hafner
 */
@Extension
@Symbol("warningsPlugin")
public class WarningsPluginConfiguration extends GlobalConfigurationItem {
    private List<SourceDirectory> sourceDirectories = Collections.emptyList();

    /**
     * Creates the global configuration for the warnings plugins.
     */
    public WarningsPluginConfiguration() {
        super();

        load();
    }

    @VisibleForTesting
    WarningsPluginConfiguration(final GlobalConfigurationFacade facade) {
        super(facade);

        load();
    }

    /**
     * Returns the singleton instance of this {@link ParserConfiguration}.
     *
     * @return the singleton instance
     */
    public static WarningsPluginConfiguration getInstance() {
        return GlobalConfiguration.all()
                .get(WarningsPluginConfiguration.class); // FIXME: indirectly calls Jenkins.getInstance
    }

    /**
     * Returns the list of additional source root folders.
     *
     * @return the source root folders
     */
    public List<SourceDirectory> getSourceDirectories() {
        return sourceDirectories;
    }

    /**
     * Sets the list of available source root folders to the specified elements. Previously set source root folders will
     * be removed.
     *
     * @param sourceDirectories
     *         the new source root folders
     */
    @DataBoundSetter
    public void setSourceDirectories(final Collection<SourceDirectory> sourceDirectories) {
        this.sourceDirectories = new ArrayList<>(sourceDirectories);

        save();
    }

    /**
     * Filters the specified collection of directories so that only permitted source directories will be returned.
     *
     * @param directories
     *         the new source root folders to filter
     *
     * @return the source root folders
     */
    public Collection<String> getPermittedSourceDirectories(final Collection<String> directories) {
        return sourceDirectories.stream()
                .map(SourceDirectory::getPath)
                .filter(directories::contains)
                .collect(Collectors.toList());
    }
}

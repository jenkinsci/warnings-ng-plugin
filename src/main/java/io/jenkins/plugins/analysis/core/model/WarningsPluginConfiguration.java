package io.jenkins.plugins.analysis.core.model;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;

import edu.hm.hafner.util.PathUtil;
import edu.hm.hafner.util.VisibleForTesting;

import org.kohsuke.stapler.DataBoundSetter;
import org.jenkinsci.Symbol;
import hudson.Extension;
import hudson.FilePath;
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
    private Set<String> normalizedSourceDirectories = Collections.emptySet();

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
                .get(WarningsPluginConfiguration.class);
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

        PathUtil pathUtil = new PathUtil();
        normalizedSourceDirectories = sourceDirectories.stream()
                .map(SourceDirectory::getPath)
                .map(pathUtil::getAbsolutePath)
                .collect(Collectors.toSet());

        save();
    }

    /**
     * Filters the specified collection of additional directories so that only permitted source directories will be
     * returned. Permitted source directories are absolute paths that have been registered using {@link
     * #setSourceDirectories(Collection)} or relative paths in the workspace.
     *
     * @param workspace
     *         the workspace containing the affected files
     * @param additionalPaths
     *         additional paths that may contain the affected files
     *
     * @return Permitted source directories including the workspace, all relative paths in the workspace, and all
     *         registered permitted absolute paths. The elements in the collection are converted normalized Unix paths -
     *         relative paths are resolved to absolute paths in the workspace.
     */
    public Collection<String> getPermittedSourceDirectories(final FilePath workspace,
            final Collection<String> additionalPaths) {
        List<String> permitted = new ArrayList<>();
        permitted.add(workspace.getRemote());

        PathUtil pathUtil = new PathUtil();
        for (String path : additionalPaths) {
            String normalized = pathUtil.getAbsolutePath(path);
            if (isAbsolute(normalized)) {
                if (normalizedSourceDirectories.contains(normalized)) { // skip not registered absolute paths
                    permitted.add(normalized);
                }
            }
            else {
                permitted.add(workspace.child(normalized).getRemote());
            }
        }
        return permitted;
    }

    /**
     * Returns whether the specified  path is absolute. Note that we cannot depend on {@link Path#isAbsolute()} since
     * Jenkins master may run on a different OS than the agent.
     *
     * @param path
     *         the path to check
     *
     * @return {@code true} if this path is absolute, {@code false} otherwise
     */
    // TODO: replace with PathUtil.isAbsolute
    private boolean isAbsolute(final String path) {
        return FilenameUtils.getPrefixLength(path) > 0;
    }
}

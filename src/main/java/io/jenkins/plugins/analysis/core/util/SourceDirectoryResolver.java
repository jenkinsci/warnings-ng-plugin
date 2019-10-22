package io.jenkins.plugins.analysis.core.util;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;

import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.util.PathUtil;

/**
 * Resolves source directories by expanding relative paths to absolute paths in the workspace.
 *
 * @author Ullrich Hafner
 */
public class SourceDirectoryResolver {
    /**
     * Wraps the specified {@code sourceDirectory} into a collection.
     *
     * @param sourceDirectory
     *         the directory
     *
     * @return an empty collection if the source directory is empty, otherwise a singleton collection with the specified
     *         source directory
     */
    public Collection<String> asCollection(final String sourceDirectory) {
        if (StringUtils.isEmpty(sourceDirectory)) {
            return Collections.emptyList();
        }
        return Collections.singleton(sourceDirectory);
    }

    /**
     * Converts the workspace and the specified {@code additional paths} into a collection of absolute paths. Each
     * absolute path will be returned as such, each relative path will be expanded to an absolute path in the
     * workspace.
     *
     * @param workspace
     *         the workspace containing the affected files
     * @param additionalPaths
     *         additional paths that may contain the affected files
     *
     * @return an empty collection if the source directory is empty, otherwise a singleton collection with the specified
     *         source directory
     */
    public Collection<String> toAbsolutePaths(final String workspace, final Collection<String> additionalPaths) {
        PathUtil pathUtil = new PathUtil();
        LinkedHashSet<String> allPaths = new LinkedHashSet<>();
        allPaths.add(workspace);
        additionalPaths.stream()
                .map(path -> {
                    if (pathUtil.isAbsolute(path)) {
                        return path;
                    }
                    else {
                        return workspace + "/" + path;
                    }
                }).forEachOrdered(allPaths::add);
        return allPaths;
    }
}

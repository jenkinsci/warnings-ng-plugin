package io.jenkins.plugins.analysis.core.util;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.selectors.TypeSelector;
import org.apache.tools.ant.types.selectors.TypeSelector.FileType;

import hudson.remoting.VirtualChannel;
import jenkins.MasterToSlaveFileCallable;

/**
 * Scans the workspace and finds all files matching a given ant pattern.
 *
 * @author Ullrich Hafner
 */
public class FileFinder extends MasterToSlaveFileCallable<String[]> {
    private static final long serialVersionUID = 2970029366847565970L;

    private final String includesPattern;
    private final String excludesPattern;
    private final boolean followSymlinks;

    /**
     * Creates a new instance of {@link FileFinder}.
     *
     * @param includesPattern
     *         the ant file includes pattern to scan for
     */
    public FileFinder(final String includesPattern) {
        this(includesPattern, StringUtils.EMPTY);
    }

    /**
     * Creates a new instance of {@link FileFinder}.
     *
     * @param includesPattern
     *         the ant file includes pattern to scan for
     * @param excludesPattern
     *         the ant file excludes pattern to scan for
     */
    public FileFinder(final String includesPattern, final String excludesPattern) {
        this(includesPattern, excludesPattern, true);
    }

    /**
     * Creates a new instance of {@link FileFinder}.
     *
     * @param includesPattern
     *         the ant file includes pattern to scan for
     * @param excludesPattern
     *         the ant file excludes pattern to scan for
     * @param followSymlinks
     *         if the scanner should traverse symbolic links
     */
    public FileFinder(final String includesPattern, final String excludesPattern, final boolean followSymlinks) {
        super();

        this.includesPattern = includesPattern;
        this.excludesPattern = excludesPattern;
        this.followSymlinks = followSymlinks;
    }

    /**
     * Returns an array with the file names of the specified file pattern that have been found in the workspace.
     *
     * @param workspace
     *         root directory of the workspace
     * @param channel
     *         not used
     *
     * @return the file names of all found files
     * @throws IOException
     *         if the workspace could not be read
     */
    @Override
    public String[] invoke(final File workspace, final VirtualChannel channel) throws IOException {
        return find(workspace);
    }

    /**
     * Returns an array with the file names of the specified file pattern that have been found in the workspace.
     *
     * @param workspace
     *         root directory of the workspace
     *
     * @return the file names of all found files
     */
    public String[] find(final File workspace) {
        try {
            FileSet fileSet = new FileSet();
            Project antProject = new Project();
            fileSet.setProject(antProject);
            fileSet.setDir(workspace);
            fileSet.setIncludes(includesPattern);
            TypeSelector selector = new TypeSelector();
            FileType fileType = new FileType();
            fileType.setValue(FileType.FILE);
            selector.setType(fileType);
            fileSet.addType(selector);
            if (StringUtils.isNotBlank(excludesPattern)) {
                fileSet.setExcludes(excludesPattern);
            }
            fileSet.setFollowSymlinks(followSymlinks);

            return fileSet.getDirectoryScanner(antProject).getIncludedFiles();
        }
        catch (BuildException ignored) {
            return new String[0]; // as fallback do not return any file
        }
    }
}
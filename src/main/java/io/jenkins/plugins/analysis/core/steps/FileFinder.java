package io.jenkins.plugins.analysis.core.steps;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;

import jenkins.MasterToSlaveFileCallable;

import hudson.remoting.VirtualChannel;

/**
 * Scans the workspace and finds all files matching a give pattern.
 *
 * @author Ullrich Hafner
 */
public class FileFinder extends MasterToSlaveFileCallable<String[]> {
    /** Generated ID. */
    private static final long serialVersionUID = 2970029366847565970L;
    /** File name pattern for java files. */
    private static final String JAVA_PATTERN = "**/*.java";
    /** The pattern to scan for. */
    private final String pattern;

    /**
     * Creates a new instance of {@link FileFinder}. This instance will scan for Java files, see {@link #JAVA_PATTERN}.
     */
    public FileFinder() {
        this(JAVA_PATTERN);
    }

    /**
     * Creates a new instance of {@link FileFinder}.
     *
     * @param pattern
     *         the ant file pattern to scan for
     */
    public FileFinder(final String pattern) {
        super();

        this.pattern = pattern;

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
     */
    @Override
    public String[] invoke(final File workspace, final VirtualChannel channel) {
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
            fileSet.setIncludes(pattern);

            return fileSet.getDirectoryScanner(antProject).getIncludedFiles();
        }
        catch (BuildException exception) {
            return new String[0];
        }
    }
}
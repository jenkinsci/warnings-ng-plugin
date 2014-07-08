package hudson.plugins.analysis.views;

import java.util.ArrayList;
import java.util.Collection;

import hudson.model.AbstractBuild;

import hudson.plugins.analysis.util.model.WorkspaceFile;

/**
 * Result object to visualize the package statistics of a module.
 *
 * @author Ulli Hafner
 */
public class FileDetail extends AbstractAnnotationsDetail {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = -5315146140343619856L;
    /** The package to show the details for. */
    private final WorkspaceFile file;

    /**
     * Creates a new instance of <code>ModuleDetail</code>.
     *
     * @param owner
     *            current build as owner of this action.
     * @param detailFactory
     *            factory to create detail objects with
     * @param file
     *            the file to show the details for
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     * @param header
     *            header to be shown on detail page
     */
    public FileDetail(final AbstractBuild<?, ?> owner, final DetailFactory detailFactory, final WorkspaceFile file, final String defaultEncoding, final String header) {
        super(owner, detailFactory, file.getAnnotations(), defaultEncoding, header, Hierarchy.FILE);
        this.file = file;
    }

    /**
     * Returns the header for the detail screen.
     *
     * @return the header
     */
    @Override
    public String getHeader() {
        return getName() + " - File " + file.getShortName();
    }

    @Override
    public String getDisplayName() {
        return file.getShortName();
    }

    @Override
    public Collection<WorkspaceFile> getFiles() {
        ArrayList<WorkspaceFile> files = new ArrayList<WorkspaceFile>();
        files.add(file);
        return files;
    }

    @Override
    public WorkspaceFile getFile(final int hashCode) {
        return file;
    }

    @Override
    public WorkspaceFile getFile(final String name) {
        return file;
    }
}


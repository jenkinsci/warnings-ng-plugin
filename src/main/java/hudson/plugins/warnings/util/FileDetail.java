package hudson.plugins.warnings.util;

import hudson.model.AbstractBuild;
import hudson.plugins.warnings.util.model.AnnotationContainer;
import hudson.plugins.warnings.util.model.WorkspaceFile;

import java.util.ArrayList;
import java.util.Collection;

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
     * @param file
     *            the file to show the details for
     * @param header
     *            header to be shown on detail page
     */
    public FileDetail(final AbstractBuild<?, ?> owner, final WorkspaceFile file, final String header) {
        super(owner, file.getAnnotations(), header, Hierarchy.FILE);
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

    /** {@inheritDoc} */
    public String getDisplayName() {
        return file.getShortName();
    }

    /** {@inheritDoc} */
    @Override
    protected Collection<? extends AnnotationContainer> getChildren() {
        return getFiles();
    }

    /** {@inheritDoc} */
    @Override
    public Collection<WorkspaceFile> getFiles() {
        ArrayList<WorkspaceFile> files = new ArrayList<WorkspaceFile>();
        files.add(file);
        return files;
    }

    /** {@inheritDoc} */
    @Override
    public WorkspaceFile getFile(final int hashCode) {
        return file;
    }

    /** {@inheritDoc} */
    @Override
    public WorkspaceFile getFile(final String name) {
        return file;
    }
}


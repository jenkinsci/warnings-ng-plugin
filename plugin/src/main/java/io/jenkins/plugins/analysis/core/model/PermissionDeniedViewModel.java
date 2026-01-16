package io.jenkins.plugins.analysis.core.model;

import hudson.model.ModelObject;
import hudson.model.Run;

/**
 * View model that shows a permission denied message when a user tries to view source code without the required
 * {@link hudson.model.Item#WORKSPACE} permission.
 *
 * @author Akash Manna
 */
public class PermissionDeniedViewModel implements ModelObject {
    private final Run<?, ?> owner;
    private final String fileName;

    /**
     * Creates a new instance of {@link PermissionDeniedViewModel}.
     *
     * @param owner
     *         the build as owner of this view
     * @param fileName
     *         the name of the file that could not be accessed
     */
    public PermissionDeniedViewModel(final Run<?, ?> owner, final String fileName) {
        this.owner = owner;
        this.fileName = fileName;
    }

    @Override
    public String getDisplayName() {
        return fileName;
    }

    /**
     * Returns the build as owner of this view.
     *
     * @return the build
     */
    public Run<?, ?> getOwner() {
        return owner;
    }

    /**
     * Returns the file name that could not be accessed.
     *
     * @return the file name
     */
    public String getFileName() {
        return fileName;
    }
}

package hudson.plugins.warnings.util.model;

import java.util.Collection;

import org.apache.commons.lang.StringUtils;



/**
 * A serializable Java Bean class representing a project that has been built by
 * Hudson.
 *
 * @author Ulli Hafner
 */
public class JavaProject extends AnnotationContainer {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = 8556968267678442661L;
    /** Path of the workspace. */
    private String workspacePath;
    /** Determines whether a module with an error is part of this project. */
    private boolean hasModuleError;
    /** The error message that denotes that why project creation has been failed. */
    private String error;

    /**
     * Creates a new instance of {@link JavaProject}.
     */
    public JavaProject() {
        super(Hierarchy.PROJECT);
    }
    /**
     * Rebuilds the priorities mapping.
     *
     * @return the created object
     */
    private Object readResolve() {
        setHierarchy(Hierarchy.PROJECT);
        rebuildMappings();
        return this;
    }

    /**
     * Adds the specified module with its annotations to this project.
     *
     * @param module the module to add
     */
    public void addModule(final MavenModule module) {
        addAnnotations(module.getAnnotations());
        if (module.hasError()) {
            hasModuleError = true;
            addError(module.getError());
        }
    }

    /**
     * Adds the specified modules with their annotations to this project.
     *
     * @param modules the modules to add
     */
    public void addModules(final Collection<MavenModule> modules) {
        for (MavenModule mavenModule : modules) {
            addModule(mavenModule);
        }
    }

    /**
     * Sets the root path of the workspace files.
     *
     * @param workspacePath path to workspace
     */
    public void setWorkspacePath(final String workspacePath) {
        this.workspacePath = workspacePath;
    }

    /**
     * Returns the root path of the workspace files.
     *
     * @return the workspace path
     */
    public String getWorkspacePath() {
        return workspacePath;
    }

    /**
     * Returns whether a module with an error is part of this project.
     *
     * @return <code>true</code> if at least one module has an error.
     */
    public boolean hasError() {
        return hasModuleError || error != null;
    }

    /**
     * Sets the error message that denotes that why project creation has been
     * failed.
     *
     * @param error
     *            the new error message
     */
    public void setError(final String error) {
        this.error = error;
    }

    /**
     * Appends the error message to the project error messages.
     *
     * @param additionalError
     *            the new error message to add
     */
    public void addError(final String additionalError) {
        if (StringUtils.isEmpty(error)) {
            error = additionalError;
        }
        else {
            error = error + "\n" + additionalError;
        }
    }

    /**
     * Returns the error message that denotes that why project creation has been
     * failed.
     *
     * @return the error message that denotes that why project creation has been
     *         failed.
     */
    public String getError() {
        return error;
    }

    /** {@inheritDoc} */
    @Override
    protected Collection<? extends AnnotationContainer> getChildren() {
        return getModules();
    }
}


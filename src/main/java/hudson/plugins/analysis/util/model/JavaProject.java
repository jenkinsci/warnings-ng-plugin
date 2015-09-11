package hudson.plugins.analysis.util.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
    /** The error message that denotes that why project creation has been failed. */
    private String error;
    /** The error messages of the modules. */
    @SuppressWarnings("Se")
    private final List<String> moduleErrors = new ArrayList<String>();

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
            moduleErrors.add(module.getError());
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
        return !moduleErrors.isEmpty() || error != null;
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
     * Returns the error messages recorded during creation of the project.
     *
     * @return the error messages recorded during creation of the project
     */
    public List<String> getErrors() {
        ArrayList<String> allErrors = new ArrayList<String>();
        if (error != null) {
            allErrors.add(error);
        }
        allErrors.addAll(moduleErrors);

        return allErrors;
    }
}


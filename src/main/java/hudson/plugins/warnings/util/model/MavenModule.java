package hudson.plugins.warnings.util.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

/**
 * A serializable Java Bean class representing a maven module.
 *
 * @author Ulli Hafner
 */
public class MavenModule extends AnnotationContainer {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = 5467122420572804130L;
    /** Name of this module. */
    private String name; // NOPMD: backward compatibility
    /** All Java packages in this maven module (mapped by their name). */
    @SuppressWarnings("Se")
    private final Map<String, JavaPackage> packageMapping = new HashMap<String, JavaPackage>();
    /** The error message that denotes that the creation of the module has been failed. */
    private String error;

    /**
     * Creates a new instance of <code>MavenModule</code>. File handling is
     * performed in this class since the files are already mapped in the modules
     * of this project.
     */
    public MavenModule() {
        super();
    }

    /**
     * Creates a new instance of <code>MavenModule</code>.
     *
     * @param moduleName
     *            name of the module
     */
    public MavenModule(final String moduleName) {
        super(false, moduleName);
    }

    /**
     * Rebuilds the priorities mapping.
     *
     * @return the created object
     */
    private Object readResolve() {
        rebuildMappings(false);
        if (name != null) {
            setName(name);
        }
        return this;
    }

    /**
     * Creates the mapping of packages.
     *
     * @param annotation
     *            the added annotation
     */
    @Override
    protected void annotationAdded(final FileAnnotation annotation) {
        String packageName = annotation.getPackageName();
        if (!packageMapping.containsKey(packageName)) {
            packageMapping.put(packageName, new JavaPackage(packageName));
        }
        packageMapping.get(packageName).addAnnotation(annotation);
    }

    /**
     * Gets the packages of this module that have annotations.
     *
     * @return the packages with annotations
     */
    public Collection<JavaPackage> getPackages() {
        return Collections.unmodifiableCollection(packageMapping.values());
    }

    /**
     * Gets the package with the given name.
     *
     * @param packageName
     *            the name of the package
     * @return the package with the given name
     */
    public JavaPackage getPackage(final String packageName) {
        JavaPackage javaPackage = packageMapping.get(packageName);
        if (javaPackage != null) {
            return javaPackage;
        }
        throw new NoSuchElementException("Package not found: " + packageName);
    }

    /** {@inheritDoc} */
    @Override
    public Collection<WorkspaceFile> getFiles() {
        List<WorkspaceFile> files = new ArrayList<WorkspaceFile>();
        for (JavaPackage javaPackage : packageMapping.values()) {
            files.addAll(javaPackage.getFiles());
        }
        return files;
    }

    /** {@inheritDoc} */
    @Override
    public WorkspaceFile getFile(final String fileName) {
        if (packageMapping.size() != 1) {
            throw new IllegalArgumentException("Number of packages != 1");
        }
        return packageMapping.values().iterator().next().getFile(fileName);
    }

    /**
     * Gets the maximum number of tasks in a package of this module.
     *
     * @return the maximum number of tasks
     */
    public int getAnnotationBound() {
        int tasks = 0;
        for (JavaPackage javaPackage : packageMapping.values()) {
            tasks = Math.max(tasks, javaPackage.getNumberOfAnnotations());
        }
        return tasks;
    }

    /**
     * Sets an error message that denotes that the creation of the module has
     * been failed.
     *
     * @param error
     *            the error message
     */
    public void setError(final String error) {
        this.error = error;
    }

    /**
     * Return whether this module has an error message stored.
     *
     * @return <code>true</code> if this module has an error message stored.
     */
    public boolean hasError() {
        return error != null;
    }

    /**
     * Returns the error message for this module.
     *
     * @return the error message for this module
     */
    public String getError() {
        return error;
    }
}


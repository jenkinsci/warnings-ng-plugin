package hudson.plugins.analysis.util.model;

import java.util.Map;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;


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
    @SuppressFBWarnings("Se")
    private Map<String, JavaPackage> packageMapping; // NOPMD: backward compatibility
    /** The error message that denotes that the creation of the module has been failed. */
    private String error;

    /**
     * Creates a new instance of <code>MavenModule</code>. File handling is
     * performed in this class since the files are already mapped in the modules
     * of this project.
     */
    public MavenModule() {
        super(Hierarchy.MODULE);
    }

    /**
     * Creates a new instance of <code>MavenModule</code>.
     *
     * @param moduleName
     *            name of the module
     */
    public MavenModule(final String moduleName) {
        super(moduleName, Hierarchy.MODULE);
    }

    /**
     * Rebuilds the priorities mapping.
     *
     * @return the created object
     */
    private Object readResolve() {
        setHierarchy(Hierarchy.MODULE);
        rebuildMappings();
        if (name != null) {
            setName(name);
        }
        return this;
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


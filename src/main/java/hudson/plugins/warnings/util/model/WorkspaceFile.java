package hudson.plugins.warnings.util.model;

import org.apache.commons.lang.StringUtils;

/**
 * A serializable Java Bean class representing a file in the Hudson workspace.
 *
 * @author Ulli Hafner
 */
public class WorkspaceFile extends AnnotationContainer {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = 601361940925156719L;
    /** The absolute filename of this file. */
    private String name; // NOPMD: backward compatibility

    /**
     * Creates a new instance of <code>WorkspaceFile</code>.
     *
     * @param fileName
     *            absolute path of this file
     */
    public WorkspaceFile(final String fileName) {
        super(fileName.replace('\\', '/'), Hierarchy.FILE);
    }

    /**
     * Returns a readable name of this workspace file without path prefix.
     *
     * @return a readable name of this workspace file.
     */
    public String getShortName() {
        if (getName().contains("/")) {
            return StringUtils.substringAfterLast(getName(), "/");
        }
        else {
            return getName();
        }
    }

    /**
     * Rebuilds the bidirectional links between the annotations and this
     * workspace file after deserialization.
     *
     * @return the created object
     */
    private Object readResolve() {
        setHierarchy(Hierarchy.FILE);
        rebuildMappings();
        if (name != null) {
            setName(name);
        }
        return this;
    }

    /**
     * Returns a file name for a temporary file that will hold the contents of the source.
     *
     * @return the temporary name
     */
    public String getTempName() {
        return Integer.toHexString(getName().hashCode()) + ".tmp";
    }

    /** {@inheritDoc} */
    @Override
    public int compareTo(final AnnotationContainer other) {
        if (other instanceof WorkspaceFile) {
            return getShortName().compareTo(((WorkspaceFile)other).getShortName());
        }
        return super.compareTo(other);
    }
}


package hudson.plugins.warnings.util.model;

import java.util.Collection;
import java.util.Collections;

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
        return StringUtils.substringAfterLast(getName(), "/");
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

    /** {@inheritDoc} */
    @Override
    protected Collection<? extends AnnotationContainer> getChildren() {
        return Collections.emptyList();
    }
}


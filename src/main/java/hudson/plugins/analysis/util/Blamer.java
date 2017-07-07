package hudson.plugins.analysis.util;

import java.io.Serializable;
import java.util.Set;

import hudson.plugins.analysis.util.model.FileAnnotation;

/**
 * Adds SCM information to a set of annotations (e.g., author, commit ID, etc.).
 *
 * @author Lukas Krose
 */
public interface Blamer extends Serializable {
    /**
     * Adds the authors to the given annotations.
     *
     * @param annotations The annotations that should be blamed.
     */
    void blame(Set<FileAnnotation> annotations);
}

package hudson.plugins.analysis.util;

import hudson.plugins.analysis.util.model.FileAnnotation;

import java.util.Set;

/**
 * An interface for assigning user from SCMs to FileAnnotations
 *
 * @author Lukas Krose
 */
public interface BlameInterface {

    /**
     *
     * Add the authors to the given annotations.
     *
     * @param annotations The annotations that should be blamed.
     */
    public void blame(Set<FileAnnotation> annotations);
}

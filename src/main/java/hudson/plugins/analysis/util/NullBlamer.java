package hudson.plugins.analysis.util;

import java.util.Set;

import hudson.plugins.analysis.util.model.FileAnnotation;

/**
 * A blamer that does nothing.
 *
 * @author Ullrich Hafner
 */
public class NullBlamer implements Blamer {
    @Override
    public void blame(final Set<FileAnnotation> annotations) {
        // nothing to do
    }
}

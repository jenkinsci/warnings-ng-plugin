package hudson.plugins.analysis.util.model;

import hudson.util.HeapSpaceStringConverter;
import hudson.util.XStream2;

/**
 * An XStream for annotations.
 *
 * @author Ulli Hafner
 */
public class AnnotationStream extends XStream2 {
    /**
     * Creates a new instance of <code>AnnotationStream</code>.
     */
    public AnnotationStream() {
        super();

        alias("annotation", FileAnnotation.class);
        alias("hudson.plugins.tasks.util.model.LineRange", LineRange.class);
        alias("range", LineRange.class);
        registerConverter(new HeapSpaceStringConverter(), 100);
        registerConverter(new Priority.PriorityConverter(), 100);
        addImmutableType(Priority.class);
    }
}


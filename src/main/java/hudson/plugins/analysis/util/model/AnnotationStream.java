package hudson.plugins.analysis.util.model;

import hudson.util.HeapSpaceStringConverter;
import hudson.util.XStream2;

/**
 * An XStream for annotations.
 *
 * @author Ulli Hafner
 */
public class AnnotationStream extends XStream2 {
    private static final int HIGH_PRIORITY = 100;

    /**
     * Creates a new instance of <code>AnnotationStream</code>.
     */
    public AnnotationStream() {
        super();

        alias("annotation", FileAnnotation.class);
        alias("hudson.plugins.tasks.util.model.LineRange", LineRange.class);
        alias("range", LineRange.class);
        registerConverter(new HeapSpaceStringConverter(), HIGH_PRIORITY);
        registerConverter(new Priority.PriorityConverter(), HIGH_PRIORITY);
        addImmutableType(Priority.class);
    }
}


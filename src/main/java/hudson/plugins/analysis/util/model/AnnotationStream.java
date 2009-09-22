package hudson.plugins.analysis.util.model;

import hudson.util.StringConverter2;
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
        alias("range", LineRange.class);
        registerConverter(new StringConverter2(), 100);
        registerConverter(new Priority.PriorityConverter(), 100);
        addImmutableType(Priority.class);
    }
}


package hudson.plugins.warnings.util;

import hudson.plugins.warnings.util.model.FileAnnotation;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

/**
 * A parser for annotations.
 *
 * @author Ulli Hafner
 */
public interface AnnotationParser extends Serializable {
    /**
     * Returns the annotations found in the specified file.
     *
     * @param file
     *            the file to parse
     * @param moduleName
     *            name of the maven module
     * @return the parsed result (stored in the module instance)
     * @throws InvocationTargetException
     *             if the file could not be parsed (wrap your exception in this exception)
     */
    Collection<FileAnnotation> parse(final File file, final String moduleName) throws InvocationTargetException;

    /**
     * Returns the name of this parser.
     *
     * @return the name of this parser
     */
    String getName();
}


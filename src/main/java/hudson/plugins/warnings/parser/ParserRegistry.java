package hudson.plugins.warnings.parser;

import hudson.plugins.warnings.util.model.FileAnnotation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Registry for the active parsers in this plug-in.
 *
 * @author Ulli Hafner
 */
public class ParserRegistry {
    /** The available parsers of this plug-in. */
    private final List<AnnotationParser> parsers = new ArrayList<AnnotationParser>();

    /**
     * Creates a new instance of <code>ParserRegistry</code>.
     */
    public ParserRegistry() {
        parsers.add(new HpiCompileParser());
        parsers.add(new JavacParser());
        parsers.add(new AntJavacParser());
        parsers.add(new MsBuildParser());
        parsers.add(new MavenParser());
        parsers.add(new GccParser());
        parsers.add(new InvalidsParser());
        parsers.add(new SunCParser());
    }

    /**
     * Iterates over the available parsers and parses the specified file with each parser.
     * Returns all found warnings.
     *
     * @param file the input stream
     * @return all found warnings
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public Collection<FileAnnotation> parse(final File file) throws IOException {
        List<FileAnnotation> annotations = new ArrayList<FileAnnotation>();
        for (AnnotationParser parser : parsers) {
            annotations.addAll(parser.parse(createInputStream(file)));
        }
        return annotations;
    }

    /**
     * Creates the input stream to parse from the specified file.
     *
     * @param file the file to parse
     * @return the input stream
     * @throws FileNotFoundException
     */
    protected InputStream createInputStream(final File file) throws FileNotFoundException {
        return new FileInputStream(file);
    }
}


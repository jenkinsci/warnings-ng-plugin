package hudson.plugins.warnings.parser;

import hudson.plugins.warnings.util.model.FileAnnotation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.DirectoryScanner;

/**
 * Registry for the active parsers in this plug-in.
 *
 * @author Ulli Hafner
 */
// CHECKSTYLE:COUPLING-OFF
public class ParserRegistry {
    /** The available parsers of this registry. */
    private static final List<WarningsParser> ALL_PARSERS;
    /** The unique set of parser names of this registry, sorted by name. */
    private static final List<String> ALL_PARSER_NAMES;
    static {
        ALL_PARSERS = getAllParsers();
        ALL_PARSER_NAMES = getAllParserNames();
    }

    /** The actual parsers to use when scanning a file. */
    private final List<WarningsParser> parsers;
    /** Filter for ant file-set pattern of files to exclude from report. */
    private ExcludeFilter excludeFilter;

    /**
     * Creates a new instance of <code>ParserRegistry</code>.
     *
     * @param parsers the parsers to use when scanning a file
     */
    public ParserRegistry(final List<WarningsParser> parsers) {
        this(parsers, StringUtils.EMPTY);
    }

    /**
     * Creates a new instance of <code>ParserRegistry</code>.
     *
     * @param parsers
     *            the parsers to use when scanning a file
     * @param excludePattern
     *            Ant file-set pattern of files to exclude from report,
     *            <code>null</code> or an empty string do not filter the output
     */
    public ParserRegistry(final List<WarningsParser> parsers, final String excludePattern) {
        this.parsers = new ArrayList<WarningsParser>(parsers);
        if (this.parsers.isEmpty()) {
            this.parsers.addAll(ALL_PARSERS);
        }

        if (!StringUtils.isEmpty(excludePattern)) {
            excludeFilter = new ExcludeFilter(excludePattern);
        }
    }

    /**
     * Returns all registers parsers. Note that removal of elements is not
     * supported.
     *
     * @return the registered parsers
     */
    protected Iterable<WarningsParser> getParsers() {
        return Collections.unmodifiableList(parsers);
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
        List<FileAnnotation> allAnnotations = new ArrayList<FileAnnotation>();
        for (WarningsParser parser : parsers) {
            allAnnotations.addAll(parser.parse(createInputStream(file)));
        }
        if (excludeFilter == null) {
            return allAnnotations;
        }
        else {
            return filterAnnotations(allAnnotations);
        }
    }

    /**
     * Filters the annotations based on the {@link #excludeFilter}.
     *
     * @param annotations
     *            the annotations to filter
     * @return the annotations that are not excluded in the filter
     */
    private Collection<FileAnnotation> filterAnnotations(final List<FileAnnotation> annotations) {
        List<FileAnnotation> filteredAnnotations = new ArrayList<FileAnnotation>();
        for (FileAnnotation annotation : annotations) {
            if (!excludeFilter.matches(annotation.getFileName())) {
                filteredAnnotations.add(annotation);
            }
        }
        return filteredAnnotations;
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

    /**
     * Filters file names based on Ant file-set patterns.
     */
    private static final class ExcludeFilter extends DirectoryScanner {
        /**
         * Creates a new instance of {@link ExcludeFilter}.
         *
         * @param excludePattern
         *            Ant file-set pattern of files to exclude from report
         */
        public ExcludeFilter(final String excludePattern) {
            super();

            setExcludes(excludePattern.split(",\\s*"));
            setIncludes(new String[] {"**/*"});
        }

        /**
         * Returns whether the name matches one of the exclusion patterns.
         *
         * @param name
         *            the file name to test
         * @return <code>true</code> if the name matches one of the exclusion patterns.
         */
        public boolean matches(final String name) {
            if (File.separatorChar == '\\') {
                return isExcluded(StringUtils.replaceChars(name, '/', '\\'));
            }
            else {
                return isExcluded(name);
            }
        }
    }

    /**
     * Returns all available parsers.
     *
     * @return all available parsers
     */
    private static List<WarningsParser> getAllParsers() {
        ArrayList<WarningsParser> parsers = new ArrayList<WarningsParser>();
        parsers.add(new JavacParser());
        parsers.add(new AntJavacParser());
        parsers.add(new JavaDocParser());
        parsers.add(new AntEclipseParser());
        parsers.add(new MsBuildParser());
        parsers.add(new GccParser());
        parsers.add(new InvalidsParser());
        parsers.add(new SunCParser());
        parsers.add(new GnatParser());
        parsers.add(new ErlcParser());

        return Collections.unmodifiableList(parsers);
    }

    /**
     * Returns all available parser names.
     *
     * @return all available parser names
     */
    private static List<String> getAllParserNames() {
        Set<String> parsers = new HashSet<String>();
        for (WarningsParser parser : ALL_PARSERS) {
            parsers.add(parser.getName());
        }

        ArrayList<String> sortedParsers = new ArrayList<String>(parsers);
        Collections.sort(sortedParsers);
        return Collections.unmodifiableList(sortedParsers);
    }

    /**
     * Returns all available parser names.
     *
     * @return all available parser names
     */
    public static List<String> getAvailableParsers() {
        return ALL_PARSER_NAMES;
    }

    /**
     * Returns a list of parsers that match the specified names. Note that the
     * mapping of names to parsers is one to many.
     *
     * @param parserNames
     *            the parser names
     * @return a list of parsers, might be modified by the receiver
     */
    public static List<WarningsParser> getParsers(final Set<String> parserNames) {
        List<WarningsParser> actualParsers = new ArrayList<WarningsParser>();
        for (String name : parserNames) {
            for (WarningsParser warningsParser : ALL_PARSERS) {
                if (warningsParser.getName().equals(name)) {
                    actualParsers.add(warningsParser);
                }
            }
        }
        return actualParsers;
    }
}


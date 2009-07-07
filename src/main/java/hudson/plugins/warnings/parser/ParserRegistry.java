package hudson.plugins.warnings.parser;

import hudson.plugins.warnings.util.EncodingValidator;
import hudson.plugins.warnings.util.model.FileAnnotation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
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
    /** Compound include/exclude filter for files that should get into report. */
    private final FileFilter fileFilter;
    /** The default charset to be used when reading and parsing files. */
    private final Charset defaultCharset;


    /**
     * Creates a new instance of <code>ParserRegistry</code>.
     *
     * @param parsers
     *            the parsers to use when scanning a file
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     */
    public ParserRegistry(final List<WarningsParser> parsers, final String defaultEncoding) {
        this(parsers, defaultEncoding, StringUtils.EMPTY, StringUtils.EMPTY);
    }

    /**
     * Creates a new instance of <code>ParserRegistry</code>.
     *
     * @param parsers
     *            the parsers to use when scanning a file
     * @param includePattern
     *            Ant file-set pattern of files to include in report,
     *            <code>null</code> or an empty string do not filter the output
     * @param excludePattern
     *            Ant file-set pattern of files to exclude from report,
     *            <code>null</code> or an empty string do not filter the output
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     */
    public ParserRegistry(final List<WarningsParser> parsers, final String defaultEncoding, final String includePattern, final String excludePattern) {
        defaultCharset = EncodingValidator.defaultCharset(defaultEncoding);
        this.parsers = new ArrayList<WarningsParser>(parsers);
        if (this.parsers.isEmpty()) {
            this.parsers.addAll(ALL_PARSERS);
        }

        if (!StringUtils.isEmpty(includePattern) || !StringUtils.isEmpty(excludePattern)) {
            fileFilter = new FileFilter(includePattern, excludePattern);
        }
        else {
            fileFilter = null;
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
            allAnnotations.addAll(parser.parse(createReader(file)));
        }
        return applyExcludeFilter(allAnnotations);
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
    public Collection<FileAnnotation> parse(final InputStream file) throws IOException {
        List<FileAnnotation> allAnnotations = new ArrayList<FileAnnotation>();
        for (WarningsParser parser : parsers) {
            allAnnotations.addAll(parser.parse(createReader(file)));
        }
        return applyExcludeFilter(allAnnotations);
    }


    /**
     * Applies the exclude filter to the found annotations.
     *
     * @param allAnnotations
     *            all annotations
     * @return the filtered annotations if there is a filter defined
     */
    private Collection<FileAnnotation> applyExcludeFilter(final List<FileAnnotation> allAnnotations) {
        if (fileFilter == null) {
            return allAnnotations;
        }
        else {
            return filterAnnotations(allAnnotations);
        }
    }

    /**
     * Filters the annotations based on the {@link #fileFilter}.
     *
     * @param annotations
     *            the annotations to filter
     * @return the annotations that are not excluded in the filter
     */
    private Collection<FileAnnotation> filterAnnotations(final List<FileAnnotation> annotations) {
        List<FileAnnotation> filteredAnnotations = new ArrayList<FileAnnotation>();
        for (FileAnnotation annotation : annotations) {
            if (fileFilter.matches(annotation.getFileName())) {
                filteredAnnotations.add(annotation);
            }
        }
        return filteredAnnotations;
    }

    /**
     * Creates a reader from the specified file. Uses the defined character set to
     * read the content of the input stream.
     *
     * @param file the file
     * @return the reader
     * @throws FileNotFoundException if the file does not exist
     */
    protected Reader createReader(final File file) throws FileNotFoundException {
        return createReader(new FileInputStream(file));
    }

    /**
     * Creates a reader from the specified input stream. Uses the defined character set to
     * read the content of the input stream.
     *
     * @param inputStream the input stream
     * @return the reader
     */
    protected Reader createReader(final InputStream inputStream) {
        return new InputStreamReader(inputStream, defaultCharset);
    }

    /**
     * Filters file names based on Ant file-set patterns.
     */
    private static final class FileFilter extends DirectoryScanner {
        /**
         * Creates a new instance of {@link FileFilter}.
         *
         * @param includePattern
         *            Ant file-set pattern of files to include in report
         * @param excludePattern
         *            Ant file-set pattern of files to exclude from report
         */
        public FileFilter(final String includePattern, final String excludePattern) {
            super();

            if (StringUtils.isEmpty(includePattern)) {
                setIncludes(new String[] {"**/*"});
            }
            else {
                setIncludes(includePattern.split(",\\s*"));
            }
            if (StringUtils.isEmpty(excludePattern)) {
                setExcludes(new String[] {});
            }
            else {
                setExcludes(excludePattern.split(",\\s*"));
            }
        }

        /**
         * Returns whether the name
         * matches the one of the inclusion patterns
         * and does not match one of the exclusion patterns.
         *
         * @param name
         *            the file name to test
         * @return <code>true</code> if the name
         * matches one of the inclusion patterns
         * and does not match any of the exclusion patterns.
         */
        public boolean matches(final String name) {
            final String canonicalName;
            if (File.separatorChar == '\\') {
                canonicalName = StringUtils.replaceChars(name, '/', '\\');
            }
            else {
                canonicalName = name;
            }
            return isIncluded(canonicalName) && !isExcluded(canonicalName);
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
        parsers.add(new IntelCParser());
        parsers.add(new IarParser());
        MsBuildParser pclintParser = new MsBuildParser();
        pclintParser.setName("PC-Lint");
        parsers.add(pclintParser);
        parsers.add(new BuckminsterParser());
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


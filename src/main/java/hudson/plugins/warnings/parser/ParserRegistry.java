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

import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.DirectoryScanner;


/**
 * Registry for the active parsers in this plug-in.
 *
 * @author Ulli Hafner
 */
// CHECKSTYLE:COUPLING-OFF
public class ParserRegistry {
    /** The available parsers of this plug-in. */
    private final List<WarningsParser> parsers = new ArrayList<WarningsParser>();
    /** Filter for ant file-set pattern of files to exclude from report. */
    private ExcludeFilter excludeFilter;

    /**
     * Creates a new instance of <code>ParserRegistry</code>.
     *
     * @param excludePattern
     *            Ant file-set pattern of files to exclude from report,
     *            <code>null</code> or an empty string do not filter the output
     */
    public ParserRegistry(final String excludePattern) {
        parsers.add(new HpiCompileParser());
        parsers.add(new JavacParser());
        parsers.add(new AntJavacParser());
        parsers.add(new JavaDocParser());
        parsers.add(new AntEclipseParser());
        parsers.add(new MsBuildParser());
        parsers.add(new MavenParser());
        parsers.add(new GccParser());
        parsers.add(new InvalidsParser());
        parsers.add(new SunCParser());
        parsers.add(new GnatParser());

        if (!StringUtils.isEmpty(excludePattern)) {
            excludeFilter = new ExcludeFilter(excludePattern);
        }
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
}


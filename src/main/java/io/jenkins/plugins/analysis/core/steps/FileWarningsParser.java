package io.jenkins.plugins.analysis.core.steps;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.apache.commons.io.input.BOMInputStream;

import edu.hm.hafner.analysis.AbstractParser;
import edu.hm.hafner.analysis.Issues;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import hudson.plugins.analysis.util.EncodingValidator;

/**
 * Uses a collection of {@link AbstractParser parsers} to scan a set of files for issues.
 *
 * @author Ulli Hafner
 */
public class FileWarningsParser implements IssueParser {
    private static final long serialVersionUID = -262047528431480332L;

    /** The parsers to scan the files with. */
    private final List<AbstractParser> parsers;
    /** The default encoding to be used when reading and parsing files. */
    private final String defaultEncoding;

    /**
     * Creates a new instance of {@link FileWarningsParser}.
     *
     * @param parsers
     *            the parsers to scan the files with
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     */
    public FileWarningsParser(final List<AbstractParser> parsers, final String defaultEncoding) {
        this.parsers = parsers;
        this.defaultEncoding = defaultEncoding;
    }

    @Override
    public Issues parse(final File file, final String moduleName) throws InvocationTargetException {
        try {
            Issues issues = parse(file);
            issues.setModuleName(moduleName); // TODO: In parser!
            return issues;
        }
        catch (IOException exception) {
            throw new InvocationTargetException(exception, "Can't scan file for warnings: " + file.getAbsolutePath());
        }
    }

    /**
     * Iterates over the available parsers and parses the specified file with
     * each parser. Returns all found warnings.
     *
     * @param file
     *            the input stream
     * @return all found warnings
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public Issues parse(final File file) throws IOException {
        Issues issues = new Issues();
        for (AbstractParser parser : parsers) {
            try (Reader input = createReader(file)) {
                issues.addAll(parser.parse(input));
            }
        }
        return issues;
    }

    /**
     * Creates a reader from the specified file. Uses the defined character set to
     * read the content of the input stream.
     *
     * @param file the file
     * @return the reader
     * @throws FileNotFoundException if the file does not exist
     */
    @SuppressFBWarnings("OBL")
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
        return new InputStreamReader(new BOMInputStream(inputStream),
                EncodingValidator.defaultCharset(defaultEncoding));
    }
}


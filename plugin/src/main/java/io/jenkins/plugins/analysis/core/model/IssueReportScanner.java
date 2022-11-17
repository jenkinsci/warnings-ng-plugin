package io.jenkins.plugins.analysis.core.model;

import java.nio.charset.Charset;
import java.nio.file.Path;

import edu.hm.hafner.analysis.FileReaderFactory;
import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.ParsingCanceledException;
import edu.hm.hafner.analysis.ParsingException;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.util.FilteredLog;

import io.jenkins.plugins.util.FilesVisitor;

/**
 * Scans the workspace for issues reports that match a specified Ant file pattern and parse these files with the
 * specified parser. Creates a new {@link Report} for each parsed file. For files that cannot be read, an empty
 * report will be returned.
 *
 * @author Ullrich Hafner
 */
public class IssueReportScanner extends FilesVisitor<Report> {
    private static final long serialVersionUID = 1743707071107346225L;

    private final IssueParser parser;

    /**
     * Creates a new instance of {@link IssueReportScanner}.
     *
     * @param filePattern
     *         ant file-set pattern to scan for files to parse
     * @param encoding
     *         encoding of the files to parse
     * @param followSymbolicLinks
     *         if the scanner should traverse symbolic links
     * @param parser
     *         the parser to use
     */
    public IssueReportScanner(final String filePattern, final String encoding,
            final boolean followSymbolicLinks, final IssueParser parser) {
        super(filePattern, encoding, followSymbolicLinks);

        this.parser = parser;
    }

    @Override
    protected Report processFile(final Path file, final Charset charset, final FilteredLog log) {
        try {
            Report fileReport = parser.parseFile(new FileReaderFactory(file, charset));

            log.logInfo("Successfully parsed file %s", file);
            log.logInfo("-> found %s (skipped %s)",
                    plural(fileReport.getSize(), "issue"),
                    plural(fileReport.getDuplicatesSize(), "duplicate"));

            return fileReport;
        }
        catch (ParsingException exception) {
            log.logException(exception, "Parsing of file '%s' failed due to an exception:", file);
        }
        catch (ParsingCanceledException ignored) {
            log.logInfo("Parsing of file %s has been canceled", file);
        }
        return new Report();
    }
}

package io.jenkins.plugins.analysis.core.model;

import edu.hm.hafner.analysis.FileReaderFactory;
import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.ParsingCanceledException;
import edu.hm.hafner.analysis.ParsingException;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.util.FilteredLog;

import java.io.Serial;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Optional;

import io.jenkins.plugins.util.AgentFileVisitor;

/**
 * Scans the workspace for issues reports that match a specified Ant file pattern and parse these files with the
 * specified parser. Creates a new {@link Report} for each parsed file. For files that cannot be read, an empty
 * report will be returned.
 *
 * @author Ullrich Hafner
 */
public class IssueReportScanner extends AgentFileVisitor<Report> {
    @Serial
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
     *         determines whether the visitor should traverse symbolic
     * @param parser
     *         the parser to use
     * @param errorOnEmptyFiles
     *         determines whether the visitor should log errors if a file is empty
     */
    public IssueReportScanner(final String filePattern, final String encoding,
            final boolean followSymbolicLinks, final IssueParser parser, final boolean errorOnEmptyFiles) {
        super(filePattern, encoding, followSymbolicLinks, errorOnEmptyFiles);

        this.parser = parser;
    }

    @Override
    protected Optional<Report> processFile(final Path file, final Charset charset, final FilteredLog log) {
        try {
            Report fileReport = parser.parse(new FileReaderFactory(file, charset));

            log.logInfo("Successfully parsed file %s", file);
            log.logInfo("-> found %s (skipped %s)",
                    plural(fileReport.getSize(), "issue"),
                    plural(fileReport.getDuplicatesSize(), "duplicate"));

            return Optional.of(fileReport);
        }
        catch (ParsingException exception) {
            log.logException(exception, "Parsing of file '%s' failed due to an exception:", file);
        }
        catch (ParsingCanceledException ignored) {
            log.logInfo("Parsing of file %s has been canceled", file);
        }
        return Optional.empty();
    }
}

package io.jenkins.plugins.analysis.core.util;

import java.io.File;
import java.util.function.Function;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.Issues;
import edu.hm.hafner.analysis.ParsingCanceledException;
import edu.hm.hafner.analysis.ParsingException;
import jenkins.MasterToSlaveFileCallable;

import hudson.plugins.analysis.util.EncodingValidator;
import hudson.plugins.analysis.util.FileFinder;
import hudson.remoting.VirtualChannel;

/**
 * Parses all files that match a specified pattern for {@link Issues issues}.
 *
 * @author Ulli Hafner
 */
// FIXME: do not create issues here
public class FilesParser extends MasterToSlaveFileCallable<Issues<?>> {
    private final String filePattern;
    private final IssueParser<?> parser;
    private final String id;
    private final String encoding;

    /**
     * Creates a new instance of {@link FilesParser}.
     *
     * @param filePattern
     *         ant file-set pattern to scan for files to parse
     * @param parser
     *         the parser to scan the found files for issues
     * @param id
     *         the ID of the parser
     * @param encoding
     *         the encoding to use when reading files
     */
    public FilesParser(final String filePattern, final IssueParser<?> parser, final String id,
            final String encoding) {
        this.filePattern = filePattern;
        this.parser = parser;
        this.id = id;
        this.encoding = encoding;
    }

    @Override
    public Issues<Issue> invoke(final File workspace, final VirtualChannel channel) {
        Issues<Issue> issues = new Issues<>();
        issues.logInfo("Searching for all files in '%s' that match the pattern '%s'",
                workspace.getAbsolutePath(), filePattern);

        String[] fileNames = new FileFinder(filePattern).find(workspace);
        if (fileNames.length == 0) {
            issues.logError("No files found for pattern '%s'. Configuration error?", filePattern);
        }
        else {
            issues.logInfo("-> found %s", plural(fileNames.length, "file"));
            parseFiles(workspace, fileNames, issues);
        }

        return issues;
    }

    /**
     * Parses the specified collection of files and appends the results to the provided container.
     *
     * @param workspace
     *         the workspace root
     * @param fileNames
     *         the names of the file to parse
     * @param issues
     *         the issues of the parsing
     */
    private void parseFiles(final File workspace, final String[] fileNames, final Issues<Issue> issues) {
        for (String fileName : fileNames) {
            File file = new File(fileName);

            if (!file.isAbsolute()) {
                file = new File(workspace, fileName);
            }

            if (!file.canRead()) {
                issues.logError("Skipping file '%s' because Jenkins has no permission to read the file.", fileName);
            }
            else if (file.length() <= 0) {
                issues.logError("Skipping file '%s' because it's empty.", fileName);
            }
            else {
                // FIXME: setting of attributes should be a lambda on issue builder so that builder can be created after each warning
                IssueBuilder builder = new IssueBuilder();
                builder.setOrigin(id);
                parseFile(file, issues, builder);
            }
        }
    }

    private String plural(final int count, final String itemName) {
        StringBuilder builder = new StringBuilder(itemName);
        if (count != 1) {
            builder.append('s');
        }
        builder.insert(0, ' ');
        builder.insert(0, count);
        return builder.toString();
    }

    /**
     * Parses the specified file and stores all found annotations. If the file could not be parsed then an error message
     * is appended to the issues.
     *
     * @param file
     *         the file to parse
     */
    private void parseFile(final File file, final Issues<Issue> issues, final IssueBuilder builder) {
        try {
            Issues<?> result = parser.parse(file, EncodingValidator.defaultCharset(encoding),
                    builder, Function.identity());
            // FIXME: why two issue instances?
            issues.addAll(result);
            issues.logInfo("Successfully parsed file %s: found %s (skipped %s)", file,
                    plural(issues.getSize(), "issue"),
                    plural(issues.getDuplicatesSize(), "duplicate"));
        }
        catch (ParsingException exception) {
            issues.logError(Messages.FilesParser_Error_Exception(file) + "\n\n"
                    + ExceptionUtils.getStackTrace((Throwable) ObjectUtils.defaultIfNull(exception.getCause(), exception)));
        }
        catch (ParsingCanceledException ignored) {
            issues.logInfo("Parsing of file %s has been canceled", file);
        }
    }
}
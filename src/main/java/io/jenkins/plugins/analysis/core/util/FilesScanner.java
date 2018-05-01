package io.jenkins.plugins.analysis.core.util;

import java.io.File;
import java.io.Serializable;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.Issues;
import edu.hm.hafner.analysis.ParsingCanceledException;
import edu.hm.hafner.analysis.ParsingException;
import jenkins.MasterToSlaveFileCallable;

import hudson.plugins.analysis.util.EncodingValidator;
import hudson.remoting.VirtualChannel;

/**
 * Scans files that match a specified Ant files pattern for issues and aggregates the found issues into a single {@link
 * Issues issues} instance. This callable will be invoked on a slave agent so all fields and the returned issues need to
 * be {@link Serializable}.
 *
 * @author Ulli Hafner
 */
public class FilesScanner extends MasterToSlaveFileCallable<Issues<?>> {
    private final String filePattern;
    private final IssueParser<?> parser;
    private final String encoding;

    /**
     * Creates a new instance of {@link FilesScanner}.
     *
     * @param filePattern
     *         ant file-set pattern to scan for files to parse
     * @param parser
     *         the parser to scan the found files for issues
     * @param encoding
     *         encoding of the files to parse
     */
    public FilesScanner(final String filePattern, final IssueParser<?> parser, final String encoding) {
        this.filePattern = filePattern;
        this.parser = parser;
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
            scanFiles(workspace, fileNames, issues);
        }

        return issues;
    }

    private void scanFiles(final File workspace, final String[] fileNames, final Issues<Issue> issues) {
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
                aggregateIssuesOfFile(file, issues);
            }
        }
    }

    private void aggregateIssuesOfFile(final File file, final Issues<Issue> issues) {
        try {
            Issues<?> result = parser.parse(file, EncodingValidator.defaultCharset(encoding));
            issues.addAll(result);
            issues.logInfo("Successfully parsed file %s: found %s (skipped %s)", file,
                    plural(issues.getSize(), "issue"),
                    plural(issues.getDuplicatesSize(), "duplicate"));
        }
        catch (ParsingException exception) {
            issues.logError("Parsing of file '%s' failed due to an exception: \n\n%s", file, getStackTrace(exception));
        }
        catch (ParsingCanceledException ignored) {
            issues.logInfo("Parsing of file %s has been canceled", file);
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

    private String getStackTrace(final ParsingException exception) {
        return ExceptionUtils.getStackTrace(ObjectUtils.defaultIfNull(exception.getCause(), exception));
    }
}
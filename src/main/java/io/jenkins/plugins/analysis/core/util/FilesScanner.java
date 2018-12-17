package io.jenkins.plugins.analysis.core.util;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import edu.hm.hafner.analysis.FileReaderFactory;
import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.ParsingCanceledException;
import edu.hm.hafner.analysis.ParsingException;
import edu.hm.hafner.analysis.Report;
import io.jenkins.plugins.analysis.core.model.ReportScanningTool;
import io.jenkins.plugins.analysis.core.steps.JobConfigurationModel;
import jenkins.MasterToSlaveFileCallable;

import hudson.remoting.VirtualChannel;

/**
 * Scans files that match a specified Ant files pattern for issues and aggregates the found issues into a single {@link
 * Report issues} instance. This callable will be invoked on a slave agent so all fields and the returned issues need to
 * be {@link Serializable}.
 *
 * @author Ullrich Hafner
 */
public class FilesScanner extends MasterToSlaveFileCallable<Report> {
    private static final long serialVersionUID = -4242755766101768715L;

    private final String filePattern;
    private final IssueParser parser;
    private final String encoding;

    /**
     * Creates a new instance of {@link FilesScanner}.
     *
     * @param filePattern
     *         ant file-set pattern to scan for files to parse
     * @param tool
     *         the static code analysis tool that reports the issues
     * @param encoding
     *         encoding of the files to parse
     */
    public FilesScanner(final String filePattern, final ReportScanningTool tool, final String encoding) {
        super();

        this.filePattern = filePattern;
        this.parser = tool.createParser();
        this.encoding = encoding;
    }

    @Override
    public Report invoke(final File workspace, final VirtualChannel channel) {
        Report report = new Report();
        report.logInfo("Searching for all files in '%s' that match the pattern '%s'",
                workspace.getAbsolutePath(), filePattern);

        String[] fileNames = new FileFinder(filePattern).find(workspace);
        if (fileNames.length == 0) {
            report.logError("No files found for pattern '%s'. Configuration error?", filePattern);
        }
        else {
            report.logInfo("-> found %s", plural(fileNames.length, "file"));
            scanFiles(workspace, fileNames, report);
        }

        return report;
    }

    private void scanFiles(final File workspace, final String[] fileNames, final Report report) {
        for (String fileName : fileNames) {
            Path file = workspace.toPath().resolve(fileName);

            if (!Files.isReadable(file)) {
                report.logError("Skipping file '%s' because Jenkins has no permission to read the file", fileName);
            }
            else if (isEmpty(file)) {
                report.logError("Skipping file '%s' because it's empty", fileName);
            }
            else {
                aggregateIssuesOfFile(file, report);
            }
        }
    }

    private boolean isEmpty(final Path file) {
        try {
            return Files.size(file) <= 0;
        }
        catch (IOException e) {
            return true;
        }
    }

    private void aggregateIssuesOfFile(final Path file, final Report report) {
        try {
            Report result = parser.parse(new FileReaderFactory(file, new JobConfigurationModel().getCharset(encoding)));
            report.addAll(result);
            report.logInfo("Successfully parsed file %s", file);
            report.logInfo("-> found %s (skipped %s)", 
                    plural(report.getSize(), "issue"),
                    plural(report.getDuplicatesSize(), "duplicate"));
        }
        catch (ParsingException exception) {
            report.logException(exception, "Parsing of file '%s' failed due to an exception:", file);
        }
        catch (ParsingCanceledException ignored) {
            report.logInfo("Parsing of file %s has been canceled", file);
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
}
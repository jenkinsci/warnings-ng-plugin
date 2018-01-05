package io.jenkins.plugins.analysis.core.util;

import java.io.File;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Issues;
import edu.hm.hafner.analysis.ParsingCanceledException;
import edu.hm.hafner.analysis.ParsingException;
import io.jenkins.plugins.analysis.core.model.IssueParser;
import jenkins.MasterToSlaveFileCallable;

import hudson.plugins.analysis.Messages;
import hudson.plugins.analysis.util.EncodingValidator;
import hudson.plugins.analysis.util.FileFinder;
import hudson.plugins.analysis.util.ModuleDetector;
import hudson.plugins.analysis.util.NullModuleDetector;
import hudson.remoting.VirtualChannel;

/**
 * Parses all files that match a specified pattern for {@link Issues issues}.
 *
 * @author Ulli Hafner
 */
public class FilesParser extends MasterToSlaveFileCallable<Issues<Issue>> {
    private final String filePattern;
    private final IssueParser parser;

    /** Determines whether module names should be derived from Maven pom.xml or Ant build.xml files. */
    private final boolean shouldDetectModules;
    private final String defaultEncoding;

    /**
     * Creates a new instance of {@link FilesParser}.
     *
     * @param filePattern
     *         ant file-set pattern to scan for files to parse
     * @param parser
     *         the parser to scan the found files for issues
     * @param shouldDetectModules
     *         determines whether modules should be detected from pom.xml or build.xml files
     * @param defaultEncoding
     *         the default encoding used to read files (warnings, source code, etc.).
     */
    public FilesParser(final String filePattern,
            final IssueParser parser, final boolean shouldDetectModules, final String defaultEncoding) {
        this.filePattern = filePattern;
        this.parser = parser;
        this.shouldDetectModules = shouldDetectModules;
        this.defaultEncoding = defaultEncoding;
    }

    @Override
    public Issues<Issue> invoke(final File workspace, final VirtualChannel channel) {
        Issues<Issue> issues = new Issues<>();
        issues.log("Searching for all files in '%s' that match the pattern '%s'.",
                workspace.getAbsolutePath(), filePattern);

        String[] fileNames = new FileFinder(filePattern).find(workspace);
        if (fileNames.length == 0) {
            issues.log("No files found. Configuration error?");
        }
        else {
            issues.log("Parsing %s in %s", plural(fileNames.length, "file"), workspace.getAbsolutePath());
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
        ModuleDetector detector = createModuleDetector(workspace);

        for (String fileName : fileNames) {
            File file = new File(fileName);

            if (!file.isAbsolute()) {
                file = new File(workspace, fileName);
            }

            String module = detector.guessModuleName(file.getAbsolutePath());

            if (!file.canRead()) {
                issues.log(Messages.FilesParser_Error_NoPermission(module, file));
            }
            else if (file.length() <= 0) {
                issues.log(Messages.FilesParser_Error_EmptyFile(module, file));
            }
            else {
                IssueBuilder builder = new IssueBuilder();
                builder.setModuleName(module);
                builder.setOrigin(parser.getId());
                parseFile(file, issues, builder);
            }
        }
    }

    private ModuleDetector createModuleDetector(final File workspace) {
        if (shouldDetectModules) {
            return new ModuleDetector(workspace);
        }
        else {
            return new NullModuleDetector();
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
            Issues<Issue> result = parser.parse(file, EncodingValidator.defaultCharset(defaultEncoding), builder);

            issues.addAll(result);
            issues.log("Successfully parsed file %s: found %s (skipped %s)", file,
                    plural(issues.getSize(), "issue"),
                    plural(issues.getDuplicatesSize(), "duplicate"));
        }
        catch (ParsingException exception) {
            issues.log(Messages.FilesParser_Error_Exception(file) + "\n\n"
                    + ExceptionUtils.getStackTrace((Throwable) ObjectUtils.defaultIfNull(exception.getCause(), exception)));
        }
        catch (ParsingCanceledException ignored) {
            issues.log("Parsing of file %s has been canceled", file);
        }
    }
}
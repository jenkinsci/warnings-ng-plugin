package io.jenkins.plugins.analysis.core.steps;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.time.Duration;
import java.time.Instant;

import org.eclipse.collections.api.list.ImmutableList;

import edu.hm.hafner.analysis.FingerprintGenerator;
import edu.hm.hafner.analysis.FullTextFingerprint;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Issues;
import edu.hm.hafner.analysis.ModuleDetector;
import edu.hm.hafner.analysis.ModuleDetector.FileSystem;
import edu.hm.hafner.analysis.PackageNameResolver;
import edu.hm.hafner.util.Ensure;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;
import io.jenkins.plugins.analysis.core.util.AbsolutePathGenerator;
import io.jenkins.plugins.analysis.core.util.FilesScanner;
import io.jenkins.plugins.analysis.core.util.Logger;
import io.jenkins.plugins.analysis.core.util.ModuleResolver;

import hudson.FilePath;
import hudson.console.ConsoleNote;
import hudson.plugins.analysis.util.EncodingValidator;
import hudson.plugins.analysis.util.FileFinder;

/**
 * Scans report files or the console log for issues.
 *
 * @author Ullrich Hafner
 */
public class IssuesScanner {
    private int infoPosition = 0;
    private int errorPosition = 0;

    private FilePath workspace;
    private final String logFileEncoding;
    private final String sourceCodeEncoding;
    private final StaticAnalysisTool tool;
    private final Logger logger;
    private final Logger errorLogger;

    public IssuesScanner(final StaticAnalysisTool tool, final FilePath workspace,
            final String logFileEncoding, final String sourceCodeEncoding, Logger logger, Logger errorLogger) {
        this.workspace = workspace;
        this.logFileEncoding = logFileEncoding;
        this.sourceCodeEncoding = sourceCodeEncoding;
        this.tool = tool;
        this.logger = logger;
        this.errorLogger = errorLogger;
    }

    /**
     * Scans for issues in a set of files specified by a pattern. The pattern will be applied on the files of the given
     * workspace.
     *
     * @param pattern
     *         the pattern of files
     * @throws InterruptedException
     *         if the step is interrupted
     * @throws IOException
     *         if something goes wrong
     */
    // TODO: Pattern should be a glob
    public Issues<?> scanInWorkspace(final String pattern)
            throws InterruptedException, IOException {
        Instant start = Instant.now();

        Issues<?> issues = workspace.act(new FilesScanner(pattern, tool.createParser(), logFileEncoding));

        log(issues);

        logger.log("Parsing took %s", computeElapsedTime(start));

        return postProcess(issues);
    }

    /**
     * Scans for issues in the console log.
     *
     * @param consoleLog
     *         file containing the console log
     */
    public Issues<?> scanInConsoleLog(final File consoleLog) {
        Ensure.that(tool.canScanConsoleLog()).isTrue(
                "Static analysis tool %s cannot scan console log output, please define a file pattern",
                tool.getName());

        waitForConsoleToFlush();

        Instant start = Instant.now();
        logger.log("Parsing console log (workspace: '%s')", workspace);

        Issues<?> issues = tool.createParser().parse(consoleLog, getLogFileCharset(), ConsoleNote::removeNotes);

        log(issues);

        logger.log("Parsing took %s", computeElapsedTime(start));

        return postProcess(issues);
    }

    private void waitForConsoleToFlush() {
        logger.log("Sleeping for 5 seconds due to JENKINS-32191...");
        try {
            Thread.sleep(5000);
        }
        catch (InterruptedException ignored) {
            // ignore
        }
    }

    private Issues<?> postProcess(final Issues<?> issues)
            throws IllegalStateException {
        issues.setId(tool.getId());
        issues.forEach(issue -> issue.setOrigin(tool.getId()));

        resolveAbsolutePaths(issues);
        resolveModuleNames(issues);
        resolvePackageNames(issues);
        createFingerprints(issues);

        return issues;
    }

    private void log(final Issues<?> issues) {
        logErrorMessages(issues);
        logInfoMessages(issues);
    }

    private void logErrorMessages(final Issues<?> issues) {
        ImmutableList<String> errorMessages = issues.getErrorMessages();
        if (errorPosition < errorMessages.size()) {
            errorLogger.logEachLine(errorMessages.subList(errorPosition, errorMessages.size()).castToList());
            errorPosition = errorMessages.size();
        }
    }

    private void logInfoMessages(final Issues<?> issues) {
        ImmutableList<String> infoMessages = issues.getInfoMessages();
        if (infoPosition < infoMessages.size()) {
            logger.logEachLine(infoMessages.subList(infoPosition, infoMessages.size()).castToList());
            infoPosition = infoMessages.size();
        }
    }

    private Duration computeElapsedTime(final Instant start) {
        return Duration.between(start, Instant.now());
    }

    private Charset getLogFileCharset() {
        return EncodingValidator.defaultCharset(logFileEncoding);
    }

    private Charset getSourceCodeCharset() {
        return EncodingValidator.defaultCharset(sourceCodeEncoding);
    }

    private void resolveModuleNames(final Issues<?> issues) {
        Instant start = Instant.now();
        logger.log("Resolving module names from module definitions (build.xml, pom.xml, or Manifest.mf files)");

        ModuleResolver resolver = new ModuleResolver();
        File workspaceAsFile = new File(workspace.getRemote());
        resolver.run(issues, workspaceAsFile, new ModuleDetector(workspaceAsFile, new DefaultFileSystem()));
        log(issues);

        logger.log("Resolving module names took %s", computeElapsedTime(start));
    }

    private void resolvePackageNames(final Issues<?> issues) {
        Instant start = Instant.now();
        logger.log("Using encoding '%s' to resolve package names (or namespaces)", getSourceCodeCharset());

        PackageNameResolver resolver = new PackageNameResolver();
        resolver.run(issues, new IssueBuilder(), getSourceCodeCharset());
        log(issues);

        logger.log("Resolving package names took %s", computeElapsedTime(start));
    }

    private void resolveAbsolutePaths(final Issues<?> issues) {
        Instant start = Instant.now();
        logger.log("Resolving absolute file names for all issues");

        AbsolutePathGenerator generator = new AbsolutePathGenerator();
        generator.run(issues, workspace);
        log(issues);

        logger.log("Resolving absolute file names took %s", computeElapsedTime(start));
    }

    private void createFingerprints(final Issues<?> issues) {
        Instant start = Instant.now();
        logger.log("Using encoding '%s' to read source files", getSourceCodeCharset());

        FingerprintGenerator generator = new FingerprintGenerator();
        generator.run(new FullTextFingerprint(), issues, getSourceCodeCharset());
        log(issues);

        logger.log("Extracting fingerprints took %s", computeElapsedTime(start));
    }

    /**
     * Provides file system operations using real IO.
     */
    private static final class DefaultFileSystem implements FileSystem {
        @Override
        public InputStream create(final String fileName) throws FileNotFoundException {
            return new FileInputStream(new File(fileName));
        }

        @Override
        public String[] find(final File root, final String pattern) {
            return new FileFinder(pattern).find(root);
        }
    }
}

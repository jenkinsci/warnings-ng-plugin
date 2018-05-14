package io.jenkins.plugins.analysis.core.steps;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.analysis.FingerprintGenerator;
import edu.hm.hafner.analysis.FullTextFingerprint;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.ModuleDetector;
import edu.hm.hafner.analysis.ModuleDetector.FileSystem;
import edu.hm.hafner.analysis.PackageNameResolver;
import edu.hm.hafner.util.Ensure;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;
import io.jenkins.plugins.analysis.core.util.AbsolutePathGenerator;
import io.jenkins.plugins.analysis.core.util.FilesScanner;
import io.jenkins.plugins.analysis.core.util.ModuleResolver;

import hudson.FilePath;
import hudson.console.ConsoleNote;
import hudson.plugins.analysis.util.FileFinder;

/**
 * Scans report files or the console log for issues.
 *
 * @author Ullrich Hafner
 */
class IssuesScanner {
    private final FilePath workspace;
    private final Charset logFileEncoding;
    private final Charset sourceCodeEncoding;
    private final StaticAnalysisTool tool;

    private final LogHandler logger;

    IssuesScanner(final StaticAnalysisTool tool, final FilePath workspace,
            final Charset logFileEncoding, final Charset sourceCodeEncoding, final LogHandler logger) {
        this.workspace = workspace;
        this.logFileEncoding = logFileEncoding;
        this.sourceCodeEncoding = sourceCodeEncoding;
        this.tool = tool;
        this.logger = logger;
    }

    public Report scan(final String pattern, final File consoleLog) throws IOException, InterruptedException {
        if (StringUtils.isBlank(pattern)) {
            String defaultPattern = tool.getDescriptor().getPattern();
            if (defaultPattern.isEmpty()) {
                return scanInConsoleLog(consoleLog);
            }
            else {
                logger.log("Using default pattern '%s' since user defined pattern is not set", defaultPattern);
                return scanInWorkspace(defaultPattern);
            }
        }
        else {
            return scanInWorkspace(pattern);
        }
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
    public Report scanInWorkspace(final String pattern) throws InterruptedException, IOException {
        Report report = workspace.act(new FilesScanner(pattern, tool.createParser(), logFileEncoding.name()));

        logger.log(report);

        return postProcess(report);
    }

    /**
     * Scans for issues in the console log.
     *
     * @param consoleLog
     *         file containing the console log
     */
    public Report scanInConsoleLog(final File consoleLog) {
        Ensure.that(tool.canScanConsoleLog()).isTrue(
                "Static analysis tool %s cannot scan console log output, please define a file pattern",
                tool.getName());

        waitForConsoleToFlush();

        logger.log("Parsing console log (workspace: '%s')", workspace);

        Report report = tool.createParser().parse(consoleLog, logFileEncoding, ConsoleNote::removeNotes);

        logger.log(report);

        return postProcess(report);
    }

    private void waitForConsoleToFlush() {
        try {
            logger.log("Sleeping for 5 seconds due to JENKINS-32191...");
            Thread.sleep(5000);
        }
        catch (InterruptedException ignored) {
            // ignore
        }
    }

    private Report postProcess(final Report report) {
        report.setOrigin(tool.getId());
        report.forEach(issue -> issue.setOrigin(tool.getId()));

        resolveAbsolutePaths(report);
        resolveModuleNames(report);
        resolvePackageNames(report);
        createFingerprints(report);

        return report;
    }

    private void resolveAbsolutePaths(final Report report) {
        logger.log("Resolving absolute file names for all issues");

        AbsolutePathGenerator generator = new AbsolutePathGenerator();
        generator.run(report, workspace);

        logger.log(report);
    }

    private void resolveModuleNames(final Report report) {
        logger.log("Resolving module names from module definitions (build.xml, pom.xml, or Manifest.mf files)");

        ModuleResolver resolver = new ModuleResolver();
        File workspaceAsFile = new File(workspace.getRemote());
        resolver.run(report, new ModuleDetector(workspaceAsFile, new DefaultFileSystem()));

        logger.log(report);
    }

    private void resolvePackageNames(final Report report) {
        logger.log("Using encoding '%s' to resolve package names (or namespaces)", sourceCodeEncoding);

        PackageNameResolver resolver = new PackageNameResolver();
        resolver.run(report, new IssueBuilder(), sourceCodeEncoding);

        logger.log(report);
    }

    private void createFingerprints(final Report report) {
        logger.log("Using encoding '%s' to read source files", sourceCodeEncoding);

        FingerprintGenerator generator = new FingerprintGenerator();
        generator.run(new FullTextFingerprint(), report, sourceCodeEncoding);

        logger.log(report);
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

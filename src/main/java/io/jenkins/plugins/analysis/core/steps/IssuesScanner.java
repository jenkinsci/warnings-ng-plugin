package io.jenkins.plugins.analysis.core.steps;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

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
    private FilePath workspace;
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
    public Issues<?> scanInWorkspace(final String pattern) throws InterruptedException, IOException {
        Issues<?> issues = workspace.act(new FilesScanner(pattern, tool.createParser(), logFileEncoding.name()));

        logger.log(issues);

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

        logger.log("Parsing console log (workspace: '%s')", workspace);

        Issues<?> issues = tool.createParser().parse(consoleLog, logFileEncoding, ConsoleNote::removeNotes);

        logger.log(issues);

        return postProcess(issues);
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

    private Issues<?> postProcess(final Issues<?> issues) {
        issues.setId(tool.getId());
        issues.forEach(issue -> issue.setOrigin(tool.getId()));

        resolveAbsolutePaths(issues);
        resolveModuleNames(issues);
        resolvePackageNames(issues);
        createFingerprints(issues);

        return issues;
    }

    private void resolveAbsolutePaths(final Issues<?> issues) {
        logger.log("Resolving absolute file names for all issues");

        AbsolutePathGenerator generator = new AbsolutePathGenerator();
        generator.run(issues, workspace);

        logger.log(issues);
    }

    private void resolveModuleNames(final Issues<?> issues) {
        logger.log("Resolving module names from module definitions (build.xml, pom.xml, or Manifest.mf files)");

        ModuleResolver resolver = new ModuleResolver();
        File workspaceAsFile = new File(workspace.getRemote());
        resolver.run(issues, workspaceAsFile, new ModuleDetector(workspaceAsFile, new DefaultFileSystem()));

        logger.log(issues);
    }

    private void resolvePackageNames(final Issues<?> issues) {
        logger.log("Using encoding '%s' to resolve package names (or namespaces)", sourceCodeEncoding);

        PackageNameResolver resolver = new PackageNameResolver();
        resolver.run(issues, new IssueBuilder(), sourceCodeEncoding);

        logger.log(issues);
    }

    private void createFingerprints(final Issues<?> issues) {
        logger.log("Using encoding '%s' to read source files", sourceCodeEncoding);

        FingerprintGenerator generator = new FingerprintGenerator();
        generator.run(new FullTextFingerprint(), issues, sourceCodeEncoding);

        logger.log(issues);
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

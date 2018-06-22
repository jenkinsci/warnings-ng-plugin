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
import edu.hm.hafner.analysis.ModuleDetector;
import edu.hm.hafner.analysis.ModuleDetector.FileSystem;
import edu.hm.hafner.analysis.PackageNameResolver;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.util.Ensure;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;
import io.jenkins.plugins.analysis.core.util.AbsolutePathGenerator;
import io.jenkins.plugins.analysis.core.util.AffectedFilesResolver;
import io.jenkins.plugins.analysis.core.util.FilesScanner;
import io.jenkins.plugins.analysis.core.util.ModuleResolver;
import jenkins.MasterToSlaveFileCallable;

import hudson.FilePath;
import hudson.console.ConsoleNote;
import hudson.model.Computer;
import hudson.plugins.analysis.util.FileFinder;
import hudson.remoting.VirtualChannel;

/**
 * Scans report files or the console log for issues.
 *
 * @author Ullrich Hafner
 */
class IssuesScanner {
    private final FilePath workspace;
    private final FilePath jenkinsRootDir;
    private final Charset logFileEncoding;
    private final Charset sourceCodeEncoding;
    private final StaticAnalysisTool tool;

    private final LogHandler logger;

    IssuesScanner(final StaticAnalysisTool tool, final FilePath workspace,
            final Charset logFileEncoding, final Charset sourceCodeEncoding, final FilePath jenkinsRootDir,
            final LogHandler logger) {
        this.workspace = workspace; 
        this.logFileEncoding = logFileEncoding;
        this.sourceCodeEncoding = sourceCodeEncoding;
        this.tool = tool;
        this.jenkinsRootDir = jenkinsRootDir;
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
     *
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
     *
     * @throws InterruptedException
     *         if the step is interrupted
     * @throws IOException
     *         if something goes wrong
     */
    public Report scanInConsoleLog(final File consoleLog) throws InterruptedException, IOException {
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
        catch (final InterruptedException ignored) {
            // ignore
        }
    }

    private Report postProcess(final Report report) throws IOException, InterruptedException {
        report.setOrigin(tool.getId());
        report.forEach(issue -> issue.setOrigin(tool.getId()));

        Report postProcessed;
        if (report.isEmpty()) {
            postProcessed = report; // nothing to post process
            if (report.hasErrors()) {
                report.logInfo("Skipping post processing due to errors");
            }
        }
        else {
            report.logInfo("Post processing issues on '%s' with encoding '%s'", getAgentName(), sourceCodeEncoding);

            postProcessed = workspace.act(new ReportPostProcessor(report, sourceCodeEncoding.name(), jenkinsRootDir));
        }
        logger.log(postProcessed);
        return postProcessed;
    }

    private String getAgentName() {
        return StringUtils.defaultIfBlank(getComputerName(), "Master");
    }

    private String getComputerName() {
        Computer computer = workspace.toComputer();
        if (computer != null) {
            return computer.getName();
        }
        return StringUtils.EMPTY;
    }

    /**
     * Post processes the report on the build agent. Assigns absolute paths, package names, and module names and
     * computes fingerprints for each issue.
     */
    private static class ReportPostProcessor extends MasterToSlaveFileCallable<Report> {
        private static final long serialVersionUID = -9138045560271783096L;

        private final Report report;
        private final String sourceCodeEncoding;
        private final FilePath jenkinsRootDir;

        ReportPostProcessor(final Report report, final String sourceCodeEncoding, final FilePath jenkinsRootDir) {
            super();

            this.report = report;
            this.sourceCodeEncoding = sourceCodeEncoding;
            this.jenkinsRootDir = jenkinsRootDir;
        }

        @Override
        public Report invoke(final File workspace, final VirtualChannel channel) throws IOException, InterruptedException {
            resolveAbsolutePaths(workspace);
            copyAffectedFiles(workspace);
            resolveModuleNames(workspace);
            resolvePackageNames();
            createFingerprints();

            return report;
        }

        private void resolveAbsolutePaths(final File workspace) {
            report.logInfo("Resolving absolute file names for all issues");

            AbsolutePathGenerator generator = new AbsolutePathGenerator();
            generator.run(report, workspace);
        }

        private void copyAffectedFiles(final File workspace)
                throws IOException, InterruptedException {
            report.logInfo("Copying affected files to Jenkins' build folder %s", jenkinsRootDir);
            
            new AffectedFilesResolver().copyFilesWithAnnotationsToBuildFolder(report, jenkinsRootDir, workspace);
        }


        private void resolveModuleNames(final File workspace) {
            report.logInfo("Resolving module names from module definitions (build.xml, pom.xml, or Manifest.mf files)");

            ModuleResolver resolver = new ModuleResolver();
            resolver.run(report, new ModuleDetector(workspace, new DefaultFileSystem()));
        }

        private void resolvePackageNames() {
            report.logInfo("Resolving package names (or namespaces) by parsing the affected files");

            PackageNameResolver resolver = new PackageNameResolver();
            resolver.run(report, new IssueBuilder(), getCharset());
        }

        private Charset getCharset() {
            return Charset.forName(sourceCodeEncoding);
        }

        private void createFingerprints() {
            report.logInfo("Creating fingerprints for all affected code blocks to track issues over different builds");

            FingerprintGenerator generator = new FingerprintGenerator();
            generator.run(new FullTextFingerprint(), report, getCharset());
        }
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

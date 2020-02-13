package io.jenkins.plugins.analysis.core.steps;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.google.errorprone.annotations.MustBeClosed;

import edu.hm.hafner.analysis.FileNameResolver;
import edu.hm.hafner.analysis.FingerprintGenerator;
import edu.hm.hafner.analysis.FullTextFingerprint;
import edu.hm.hafner.analysis.ModuleDetector;
import edu.hm.hafner.analysis.ModuleDetector.FileSystem;
import edu.hm.hafner.analysis.ModuleResolver;
import edu.hm.hafner.analysis.PackageNameResolver;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Report.IssueFilterBuilder;
import edu.hm.hafner.util.FilteredLog;

import hudson.FilePath;
import hudson.model.Computer;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import jenkins.MasterToSlaveFileCallable;

import io.jenkins.plugins.analysis.core.filter.RegexpFilter;
import io.jenkins.plugins.analysis.core.model.ReportLocations;
import io.jenkins.plugins.analysis.core.model.Tool;
import io.jenkins.plugins.analysis.core.model.WarningsPluginConfiguration;
import io.jenkins.plugins.analysis.core.util.AffectedFilesResolver;
import io.jenkins.plugins.analysis.core.util.ConsoleLogHandler;
import io.jenkins.plugins.analysis.core.util.FileFinder;
import io.jenkins.plugins.analysis.core.util.LogHandler;
import io.jenkins.plugins.forensics.blame.Blamer;
import io.jenkins.plugins.forensics.blame.Blamer.NullBlamer;
import io.jenkins.plugins.forensics.blame.BlamerFactory;
import io.jenkins.plugins.forensics.blame.Blames;
import io.jenkins.plugins.forensics.blame.FileLocations;
import io.jenkins.plugins.forensics.miner.MinerFactory;
import io.jenkins.plugins.forensics.miner.RepositoryMiner;
import io.jenkins.plugins.forensics.miner.RepositoryMiner.NullMiner;
import io.jenkins.plugins.forensics.miner.RepositoryStatistics;

import static io.jenkins.plugins.analysis.core.util.AffectedFilesResolver.*;
import static java.util.Collections.*;

/**
 * Scans report files or the console log for issues.
 *
 * @author Ullrich Hafner
 */
@SuppressWarnings("PMD.ExcessiveImports")
class IssuesScanner {
    private final FilePath workspace;
    private final String sourceDirectory;
    private final Run<?, ?> run;
    private final FilePath jenkinsRootDir;
    private final Charset sourceCodeEncoding;
    private final Tool tool;
    private final List<RegexpFilter> filters;
    private final TaskListener listener;
    private final BlameMode blameMode;
    private final ForensicsMode forensicsMode;

    enum BlameMode {
        ENABLED, DISABLED
    }

    enum ForensicsMode {
        ENABLED, DISABLED
    }

    @SuppressWarnings("checkstyle:ParameterNumber")
    IssuesScanner(final Tool tool, final List<RegexpFilter> filters, final Charset sourceCodeEncoding,
            final FilePath workspace, final String sourceDirectory, final Run<?, ?> run,
            final FilePath jenkinsRootDir, final TaskListener listener,
            final BlameMode blameMode, final ForensicsMode forensicsMode) {
        this.filters = new ArrayList<>(filters);
        this.sourceCodeEncoding = sourceCodeEncoding;
        this.tool = tool;
        this.workspace = workspace;
        this.sourceDirectory = sourceDirectory;
        this.run = run;
        this.jenkinsRootDir = jenkinsRootDir;
        this.listener = listener;
        this.blameMode = blameMode;
        this.forensicsMode = forensicsMode;
    }

    public AnnotatedReport scan() throws IOException, InterruptedException {
        LogHandler logger = new LogHandler(listener, tool.getActualName());
        Report report = tool.scan(run, workspace, sourceCodeEncoding, logger);
        report.setNameOfOrigin(tool.getActualId(), tool.getName());

        AnnotatedReport annotatedReport = postProcessReport(report);
        logger.log(annotatedReport.getReport());

        return annotatedReport;
    }

    private AnnotatedReport postProcessReport(final Report report) throws IOException, InterruptedException {
        if (tool.getDescriptor().isPostProcessingEnabled()) {
            return postProcess(report);
        }
        else {
            return createAnnotatedReport(filter(report, filters, tool.getActualId()));
        }
    }

    private AnnotatedReport postProcess(final Report report)
            throws IOException, InterruptedException {
        AnnotatedReport result;
        if (report.isEmpty()) {
            result = createAnnotatedReport(report); // nothing to post process
        }
        else {
            report.logInfo("Post processing issues on '%s' with source code encoding '%s'",
                    getAgentName(), sourceCodeEncoding);
            result = workspace.act(new ReportPostProcessor(tool.getActualId(), report, sourceCodeEncoding.name(),
                    createBlamer(report), createMiner(report), filters,
                    getPermittedSourceDirectory(report).getRemote()));
            copyAffectedFiles(result.getReport(), createAffectedFilesFolder(result.getReport()));
        }
        return result;
    }

    private AnnotatedReport createAnnotatedReport(final Report report) {
        return new AnnotatedReport(tool.getActualId(), report);
    }

    private FilePath getPermittedSourceDirectory(final Report report) {
        FilePath permittedSourceDirectory = WarningsPluginConfiguration.getInstance()
                .getPermittedSourceDirectory(workspace, sourceDirectory);
        if (StringUtils.isNotBlank(sourceDirectory) && permittedSourceDirectory.equals(workspace)) {
            report.logError("Additional source directory '%s' must be registered in Jenkins system configuration",
                    sourceDirectory);
        }
        return permittedSourceDirectory;
    }

    private Blamer createBlamer(final Report report) {
        if (blameMode == BlameMode.DISABLED) {
            report.logInfo("Skipping SCM blames as requested");
            return new NullBlamer();
        }
        else {
            FilteredLog log = new FilteredLog("Errors while determining a supported blamer for "
                    + run.getFullDisplayName());
            report.logInfo("Creating SCM blamer to obtain author and commit information for affected files");
            Blamer blamer = BlamerFactory.findBlamer(run,
                    singleton(getPermittedSourceDirectory(report)), listener, log);
            log.logSummary();
            log.getInfoMessages().forEach(report::logInfo);
            log.getErrorMessages().forEach(report::logError);

            return blamer;
        }
    }

    private RepositoryMiner createMiner(final Report report) {
        if (forensicsMode == ForensicsMode.ENABLED) {
            FilteredLog log = new FilteredLog("Errors while determining a supported SCM miner for "
                    + run.getFullDisplayName());
            report.logInfo("Creating SCM miner to obtain statistics for affected repository files");
            RepositoryMiner miner = MinerFactory.findMiner(run,
                    singleton(getPermittedSourceDirectory(report)), listener, log);
            log.logSummary();
            log.getInfoMessages().forEach(report::logInfo);
            log.getErrorMessages().forEach(report::logError);

            return miner;
        }
        else {
            report.logInfo("Skipping SCM repository mining as requested");

            return new NullMiner();
        }
    }

    private void copyAffectedFiles(final Report report, final FilePath buildFolder)
            throws InterruptedException {
        report.logInfo("Copying affected files to Jenkins' build folder '%s'", buildFolder);

        new AffectedFilesResolver().copyAffectedFilesToBuildFolder(
                report, buildFolder, getPermittedSourceDirectory(report));
    }

    private FilePath createAffectedFilesFolder(final Report report) throws InterruptedException {
        FilePath buildDirectory = jenkinsRootDir.child(AFFECTED_FILES_FOLDER_NAME);
        try {
            buildDirectory.mkdirs();
        }
        catch (IOException exception) {
            report.logException(exception,
                    "Can't create directory '%s' for affected workspace files.", buildDirectory);
        }
        return buildDirectory;
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

    private static Report filter(final Report report, final List<RegexpFilter> filters, final String id) {
        int actualFilterSize = 0;
        IssueFilterBuilder builder = new IssueFilterBuilder();
        for (RegexpFilter filter : filters) {
            if (StringUtils.isNotBlank(filter.getPattern())) {
                filter.apply(builder);
                actualFilterSize++;
            }
        }
        Report filtered = report.filter(builder.build());
        if (actualFilterSize > 0) {
            filtered.logInfo(
                    "Applying %d filters on the set of %d issues (%d issues have been removed, %d issues will be published)",
                    filters.size(), report.size(), report.size() - filtered.size(), filtered.size());
        }
        else {
            filtered.logInfo("No filter has been set, publishing all %d issues", filtered.size());
        }

        filtered.stream().forEach(issue -> issue.setOrigin(id));
        return filtered;
    }

    /**
     * Post processes the report on the build agent. Assigns absolute paths, package names, and module names and
     * computes fingerprints for each issue. Finally, for each file the SCM blames are computed.
     */
    @SuppressWarnings("checkstyle:ClassDataAbstractionCoupling")
    private static class ReportPostProcessor extends MasterToSlaveFileCallable<AnnotatedReport> {
        private static final long serialVersionUID = -9138045560271783096L;

        private final String id;
        private final Report originalReport;
        private final String sourceCodeEncoding;
        private final Blamer blamer;
        private final RepositoryMiner miner;
        private final String sourceDirectory;
        private final List<RegexpFilter> filters;

        ReportPostProcessor(final String id, final Report report, final String sourceCodeEncoding,
                final Blamer blamer, final RepositoryMiner miner, final List<RegexpFilter> filters,
                final String sourceDirectory) {
            super();

            this.id = id;
            originalReport = report;
            this.sourceCodeEncoding = sourceCodeEncoding;
            this.blamer = blamer;
            this.filters = filters;
            this.miner = miner;
            this.sourceDirectory = sourceDirectory;
        }

        @Override
        public AnnotatedReport invoke(final File workspace, final VirtualChannel channel) throws InterruptedException {
            resolvePaths(originalReport);
            resolveModuleNames(originalReport, workspace);
            resolvePackageNames(originalReport);

            Report filtered = filter(originalReport, filters, id);

            createFingerprints(filtered);

            FileLocations fileLocations = new ReportLocations().toFileLocations(filtered);
            return new AnnotatedReport(id, filtered,
                    blame(filtered, fileLocations),
                    mineRepository(filtered, fileLocations));
        }

        private Blames blame(final Report filtered, final FileLocations fileLocations) {
            if (fileLocations.isEmpty()) {
                return new Blames();
            }
            FilteredLog log = new FilteredLog("Errors while extracting author and commit information from Git:");
            Blames blames = blamer.blame(fileLocations, log);
            log.logSummary();
            log.getInfoMessages().forEach(filtered::logInfo);
            log.getErrorMessages().forEach(filtered::logError);
            return blames;
        }

        private RepositoryStatistics mineRepository(final Report filtered, final FileLocations fileLocations)
                throws InterruptedException {
            if (fileLocations.isEmpty()) {
                return new RepositoryStatistics();
            }

            FilteredLog log = new FilteredLog("Errors while mining source control repository:");
            RepositoryStatistics statistics = miner.mine(fileLocations.getFiles(), log);
            log.logSummary();
            log.getInfoMessages().forEach(filtered::logInfo);
            log.getErrorMessages().forEach(filtered::logError);
            return statistics;
        }

        private void resolvePaths(final Report report) {
            report.logInfo("Resolving file names for all issues in source directory '%s'", sourceDirectory);

            FileNameResolver generator = new FileNameResolver();
            generator.run(report, sourceDirectory, ConsoleLogHandler::isInConsoleLog);
        }

        private void resolveModuleNames(final Report report, final File workspace) {
            report.logInfo("Resolving module names from module definitions (build.xml, pom.xml, or Manifest.mf files)");

            ModuleResolver resolver = new ModuleResolver();
            resolver.run(report, new ModuleDetector(workspace.toPath(), new DefaultFileSystem()));
        }

        private void resolvePackageNames(final Report report) {
            report.logInfo("Resolving package names (or namespaces) by parsing the affected files");

            PackageNameResolver resolver = new PackageNameResolver();
            resolver.run(report, getCharset());
        }

        private Charset getCharset() {
            return Charset.forName(sourceCodeEncoding);
        }

        private void createFingerprints(final Report report) {
            report.logInfo("Creating fingerprints for all affected code blocks to track issues over different builds");

            FingerprintGenerator generator = new FingerprintGenerator();
            generator.run(new FullTextFingerprint(), report, getCharset());
        }
    }

    /**
     * Provides file system operations using real IO.
     */
    private static final class DefaultFileSystem implements FileSystem {
        @MustBeClosed
        @Override
        public InputStream open(final String fileName) throws IOException {
            return Files.newInputStream(Paths.get(fileName));
        }

        @Override
        public String[] find(final Path root, final String pattern) {
            return new FileFinder(pattern).find(root.toFile());
        }
    }
}

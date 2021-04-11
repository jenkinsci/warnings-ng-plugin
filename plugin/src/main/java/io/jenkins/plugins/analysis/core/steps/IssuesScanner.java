package io.jenkins.plugins.analysis.core.steps;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
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
import io.jenkins.plugins.forensics.miner.MinerService;
import io.jenkins.plugins.forensics.miner.RepositoryStatistics;

import static io.jenkins.plugins.analysis.core.util.AffectedFilesResolver.*;

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
    private final String scm;
    private final BlameMode blameMode;

    enum BlameMode {
        ENABLED, DISABLED
    }

    @SuppressWarnings("checkstyle:ParameterNumber")
    IssuesScanner(final Tool tool, final List<RegexpFilter> filters, final Charset sourceCodeEncoding,
            final FilePath workspace, final String sourceDirectory, final Run<?, ?> run,
            final FilePath jenkinsRootDir, final TaskListener listener,
            final String scm, final BlameMode blameMode) {
        this.filters = new ArrayList<>(filters);
        this.sourceCodeEncoding = sourceCodeEncoding;
        this.tool = tool;
        this.workspace = workspace;
        this.sourceDirectory = sourceDirectory;
        this.run = run;
        this.jenkinsRootDir = jenkinsRootDir;
        this.listener = listener;
        this.scm = scm;
        this.blameMode = blameMode;
    }

    public AnnotatedReport scan() throws IOException, InterruptedException {
        LogHandler logger = new LogHandler(listener, tool.getActualName());
        Report report = tool.scan(run, workspace, sourceCodeEncoding, logger);

        AnnotatedReport annotatedReport = postProcessReport(report);

        RepositoryStatistics statistics = getRepositoryStatistics(annotatedReport.getReport());
        annotatedReport.addRepositoryStatistics(statistics);

        logger.log(annotatedReport.getReport());

        return annotatedReport;
    }

    private RepositoryStatistics getRepositoryStatistics(final Report report) {
        MinerService minerService = new MinerService();
        FilteredLog log = new FilteredLog("Errors while obtaining repository statistics");
        RepositoryStatistics statistics = minerService.queryStatisticsFor(scm, run, report.getFiles(), log);
        log.getErrorMessages().forEach(report::logError);
        log.getInfoMessages().forEach(report::logInfo);
        return statistics;
    }

    private AnnotatedReport postProcessReport(final Report report) throws IOException, InterruptedException {
        if (tool.getDescriptor().isPostProcessingEnabled() && report.isNotEmpty()) {
            report.logInfo("Post processing issues on '%s' with source code encoding '%s'",
                    getAgentName(), sourceCodeEncoding);
            AnnotatedReport result = workspace.act(createPostProcessor(report));
            copyAffectedFiles(result.getReport(), createAffectedFilesFolder(result.getReport()));
            return result;
        }
        else {
            report.logInfo("Skipping post processing");
            return new AnnotatedReport(tool.getActualId(), filter(report, filters));
        }
    }

    private ReportPostProcessor createPostProcessor(final Report report) {
        return new ReportPostProcessor(tool.getActualId(), report, sourceCodeEncoding.name(),
                createBlamer(report), filters, getPermittedSourceDirectory(report).getRemote());
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
            if (!StringUtils.isBlank(scm)) {
                report.logInfo("-> Filtering SCMs by key '%s'", scm);
            }
            Blamer blamer = BlamerFactory.findBlamer(scm, run, workspace, listener, log);
            log.logSummary();
            log.getInfoMessages().forEach(report::logInfo);
            log.getErrorMessages().forEach(report::logError);

            return blamer;
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

    private static Report filter(final Report report, final List<RegexpFilter> filters) {
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
        private final String sourceDirectory;
        private final List<RegexpFilter> filters;

        ReportPostProcessor(final String id, final Report report, final String sourceCodeEncoding,
                final Blamer blamer, final List<RegexpFilter> filters, final String sourceDirectory) {
            super();

            this.id = id;
            originalReport = report;
            this.sourceCodeEncoding = sourceCodeEncoding;
            this.blamer = blamer;
            this.filters = filters;
            this.sourceDirectory = sourceDirectory;
        }

        @Override
        public AnnotatedReport invoke(final File workspace, final VirtualChannel channel) {
            resolvePaths(originalReport);
            resolveModuleNames(originalReport, workspace);
            resolvePackageNames(originalReport);

            Report filtered = filter(originalReport, filters);

            createFingerprints(filtered);

            FileLocations fileLocations = new ReportLocations().toFileLocations(filtered);

            return new AnnotatedReport(id, filtered, blame(filtered, fileLocations));
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

        private void resolvePaths(final Report report) {
            report.logInfo("Resolving file names for all issues in source directory '%s'", sourceDirectory);

            try {
                FileNameResolver generator = new FileNameResolver();
                generator.run(report, sourceDirectory, ConsoleLogHandler::isInConsoleLog);
            }
            catch (InvalidPathException exception) {
                report.logException(exception, "Resolving of file names aborted");
            }
        }

        private void resolveModuleNames(final Report report, final File workspace) {
            report.logInfo("Resolving module names from module definitions (build.xml, pom.xml, or Manifest.mf files)");

            try {
                ModuleResolver resolver = new ModuleResolver();
                resolver.run(report, new ModuleDetector(workspace.toPath(), new DefaultFileSystem()));
            }
            catch (InvalidPathException exception) {
                report.logException(exception, "Resolving of modul names aborted");
            }
        }

        private void resolvePackageNames(final Report report) {
            report.logInfo("Resolving package names (or namespaces) by parsing the affected files");

            try {
                PackageNameResolver resolver = new PackageNameResolver();
                resolver.run(report, getCharset());
            }
            catch (InvalidPathException exception) {
                report.logException(exception, "Resolving of package names aborted");
            }
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

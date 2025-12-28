package io.jenkins.plugins.analysis.core.steps;

import org.apache.commons.lang3.StringUtils;

import com.google.errorprone.annotations.MustBeClosed;

import edu.hm.hafner.analysis.FileNameResolver;
import edu.hm.hafner.analysis.FingerprintGenerator;
import edu.hm.hafner.analysis.FullTextFingerprint;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.ModuleDetectorRunner;
import edu.hm.hafner.analysis.ModuleDetectorRunner.FileSystemFacade;
import edu.hm.hafner.analysis.ModuleResolver;
import edu.hm.hafner.analysis.PackageNameResolver;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Report.IssueFilterBuilder;
import edu.hm.hafner.util.FilteredLog;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import hudson.FilePath;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import jenkins.MasterToSlaveFileCallable;

import io.jenkins.plugins.analysis.core.filter.RegexpFilter;
import io.jenkins.plugins.analysis.core.model.ReportLocations;
import io.jenkins.plugins.analysis.core.model.ReportScanningTool;
import io.jenkins.plugins.analysis.core.model.Tool;
import io.jenkins.plugins.analysis.core.util.AffectedFilesResolver;
import io.jenkins.plugins.analysis.core.util.ConsoleLogHandler;
import io.jenkins.plugins.analysis.core.util.FileFinder;
import io.jenkins.plugins.forensics.blame.Blamer;
import io.jenkins.plugins.forensics.blame.Blamer.NullBlamer;
import io.jenkins.plugins.forensics.blame.BlamerFactory;
import io.jenkins.plugins.forensics.blame.Blames;
import io.jenkins.plugins.forensics.blame.FileLocations;
import io.jenkins.plugins.forensics.miner.MinerService;
import io.jenkins.plugins.forensics.miner.RepositoryStatistics;
import io.jenkins.plugins.prism.PermittedSourceCodeDirectory;
import io.jenkins.plugins.prism.PrismConfiguration;
import io.jenkins.plugins.prism.SourceCodeRetention;
import io.jenkins.plugins.prism.SourceDirectoryFilter;
import io.jenkins.plugins.util.LogHandler;

import static io.jenkins.plugins.analysis.core.util.AffectedFilesResolver.*;

/**
 * Scans report files or the console log for issues.
 *
 * @author Ullrich Hafner
 */
@SuppressWarnings("PMD.CouplingBetweenObjects")
class IssuesScanner {
    private final FilePath workspace;
    private final Set<String> sourceDirectories;
    private final SourceCodeRetention sourceCodeRetention;
    private final Run<?, ?> run;
    private final FilePath jenkinsRootDir;
    private final Charset sourceCodeEncoding;
    private final Tool tool;
    private final List<RegexpFilter> filters;
    private final TaskListener listener;
    private final String scm;
    private final BlameMode blameMode;
    private final PostProcessingMode postProcessingMode;
    private final boolean quiet;
    private final String sourcePathPrefix;
    private final String targetPathPrefix;

    enum BlameMode {
        ENABLED, DISABLED
    }

    enum PostProcessingMode {
        ENABLED, DISABLED
    }

    @SuppressWarnings("checkstyle:ParameterNumber")
    IssuesScanner(final Tool tool, final List<RegexpFilter> filters, final Charset sourceCodeEncoding,
            final FilePath workspace, final Set<String> sourceDirectories,
            final SourceCodeRetention sourceCodeRetention, final Run<?, ?> run,
            final FilePath jenkinsRootDir, final TaskListener listener,
            final String scm, final BlameMode blameMode, final PostProcessingMode postProcessingMode,
            final boolean quiet, final String sourcePathPrefix, final String targetPathPrefix) {
        this.filters = new ArrayList<>(filters);
        this.sourceCodeEncoding = sourceCodeEncoding;
        this.tool = tool;
        this.workspace = workspace;
        this.sourceDirectories = sourceDirectories;
        this.sourceCodeRetention = sourceCodeRetention;
        this.run = run;
        this.jenkinsRootDir = jenkinsRootDir;
        this.listener = listener;
        this.scm = scm;
        this.blameMode = blameMode;
        this.postProcessingMode = postProcessingMode;
        this.quiet = quiet;
        this.sourcePathPrefix = sourcePathPrefix;
        this.targetPathPrefix = targetPathPrefix;
    }

    public AnnotatedReport scan() throws IOException, InterruptedException {
        var logger = new LogHandler(listener, tool.getActualName());
        logger.setQuiet(quiet);
        var report = tool.scan(run, workspace, sourceCodeEncoding, logger);

        var annotatedReport = postProcessReport(report);

        var statistics = getRepositoryStatistics(annotatedReport.getReport());
        annotatedReport.addRepositoryStatistics(statistics);

        logger.logInfoMessages(annotatedReport.getReport().getInfoMessages());
        logger.logErrorMessages(annotatedReport.getReport().getErrorMessages());

        return annotatedReport;
    }

    private RepositoryStatistics getRepositoryStatistics(final Report report) {
        var minerService = new MinerService();
        var log = new FilteredLog("Errors while obtaining repository statistics");
        var statistics = minerService.queryStatisticsFor(scm, run, report.getFiles(), log);
        report.mergeLogMessages(log);
        return statistics;
    }

    private AnnotatedReport postProcessReport(final Report report) throws IOException, InterruptedException {
        if (tool.getDescriptor().isPostProcessingEnabled()
                && report.isNotEmpty()) {
            report.logInfo("Post processing issues on '%s' with source code encoding '%s'",
                    getAgentName(), sourceCodeEncoding);
            var result = workspace.act(createPostProcessor(report));
            copyAffectedFiles(result.getReport(), createAffectedFilesFolder(result.getReport()));
            return result;
        }
        else {
            report.logInfo("Skipping post processing");
            return new AnnotatedReport(tool.getActualId(), filter(report, filters));
        }
    }

    private ReportPostProcessor createPostProcessor(final Report report) {
        int linesLookAhead = -1;
        if (tool instanceof ReportScanningTool scanningTool) {
            linesLookAhead = scanningTool.getLinesLookAhead();
        }
        return new ReportPostProcessor(tool.getActualId(), report, sourceCodeEncoding.name(),
                createBlamer(report), filters, getPermittedSourceDirectories(), sourceDirectories,
                postProcessingMode, linesLookAhead, sourcePathPrefix, targetPathPrefix);
    }

    private Set<String> getPermittedSourceDirectories() {
        return PrismConfiguration.getInstance()
                .getSourceDirectories()
                .stream()
                .map(PermittedSourceCodeDirectory::getPath)
                .collect(Collectors.toSet());
    }

    private Blamer createBlamer(final Report report) {
        if (blameMode == BlameMode.DISABLED) {
            report.logInfo("Skipping SCM blames as requested");
            return new NullBlamer();
        }
        else {
            var log = new FilteredLog("Errors while determining a supported blamer for "
                    + run.getFullDisplayName());
            report.logInfo("Creating SCM blamer to obtain author and commit information for affected files");
            if (!StringUtils.isBlank(scm)) {
                report.logInfo("-> Filtering SCMs by key '%s'", scm);
            }
            Blamer blamer = BlamerFactory.findBlamer(scm, run, workspace, listener, log);
            report.mergeLogMessages(log);

            return blamer;
        }
    }

    private void copyAffectedFiles(final Report report, final FilePath buildFolder)
            throws InterruptedException {
        var log = new FilteredLog("Errors while processing affected files");
        if (sourceCodeRetention == SourceCodeRetention.NEVER) {
            report.logInfo("Skipping copying of affected files");
        }
        else {
            report.logInfo("Copying affected files to Jenkins' build folder '%s'", buildFolder);

            Set<String> permittedSourceDirectories = getPermittedSourceDirectories();
            permittedSourceDirectories.add(workspace.getRemote());
            new AffectedFilesResolver().copyAffectedFilesToBuildFolder(
                    report, workspace, permittedSourceDirectories, buildFolder);
        }
        sourceCodeRetention.cleanup(run, AFFECTED_FILES_FOLDER_NAME, log);

        report.mergeLogMessages(log);
    }

    private FilePath createAffectedFilesFolder(final Report report) throws InterruptedException {
        var buildDirectory = jenkinsRootDir.child(AFFECTED_FILES_FOLDER_NAME);
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
        var computer = workspace.toComputer();
        if (computer != null) {
            return computer.getName();
        }
        return StringUtils.EMPTY;
    }

    private static Report filter(final Report report, final List<RegexpFilter> filters) {
        int actualFilterSize = 0;
        var builder = new IssueFilterBuilder();
        for (RegexpFilter filter : filters) {
            if (StringUtils.isNotBlank(filter.getPattern())) {
                filter.apply(builder);
                actualFilterSize++;
            }
        }
        var filtered = report.filter(builder.build());
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
        private static final String SKIPPING_POST_PROCESSING = "Skipping detection of missing package and module names";

        private final String id;
        private final Report originalReport;
        private final String sourceCodeEncoding;
        private final Blamer blamer;
        private final Set<String> permittedSourceDirectories;
        private final Set<String> requestedSourceDirectories;
        private final PostProcessingMode postProcessingMode;
        private final List<RegexpFilter> filters;
        private final int linesLookAhead;
        private final String sourcePathPrefix;
        private final String targetPathPrefix;

        @SuppressWarnings("checkstyle:ParameterNumber")
        ReportPostProcessor(final String id, final Report report, final String sourceCodeEncoding,
                            final Blamer blamer, final List<RegexpFilter> filters, final Set<String> permittedSourceDirectories,
                            final Set<String> requestedSourceDirectories, final PostProcessingMode postProcessingMode, final int linesLookAhead,
                            final String sourcePathPrefix, final String targetPathPrefix) {
            super();

            this.id = id;
            originalReport = report;
            this.sourceCodeEncoding = sourceCodeEncoding;
            this.blamer = blamer;
            this.filters = filters;
            this.permittedSourceDirectories = permittedSourceDirectories;
            this.requestedSourceDirectories = requestedSourceDirectories;
            this.postProcessingMode = postProcessingMode;
            this.linesLookAhead = linesLookAhead;
            this.sourcePathPrefix = sourcePathPrefix;
            this.targetPathPrefix = targetPathPrefix;
        }

        @Override
        public AnnotatedReport invoke(final File workspace, final VirtualChannel channel) {
            Report processedReport = resolvePaths(workspace, originalReport);
            if (postProcessingMode == PostProcessingMode.ENABLED) {
                resolveModuleNames(processedReport, workspace);
                resolvePackageNames(processedReport);
            }
            else {
                processedReport.logInfo(SKIPPING_POST_PROCESSING);
            }

            Report filtered = filter(processedReport, filters); // the filters may depend on the resolved paths

            createFingerprints(filtered);

            var fileLocations = new ReportLocations().toFileLocations(filtered);

            return new AnnotatedReport(id, filtered, blame(filtered, fileLocations));
        }

        private Blames blame(final Report filtered, final FileLocations fileLocations) {
            if (fileLocations.isEmpty()) {
                return new Blames();
            }
            var log = new FilteredLog("Errors while extracting author and commit information from Git:");
            var blames = blamer.blame(fileLocations, log);
            filtered.mergeLogMessages(log);
            return blames;
        }

        private Report resolvePaths(final File workspace, final Report report) {
            try {
                var nameResolver = new FileNameResolver();
                report.logInfo("Resolving file names for all issues in workspace '%s'", workspace);
                nameResolver.run(report, workspace.getAbsolutePath(), ConsoleLogHandler::isInConsoleLog,
                        sourcePathPrefix, targetPathPrefix);
                var errors = new FilteredLog("Source-Directories");
                Set<String> filteredSourceDirectories = filterSourceDirectories(workspace, errors);
                errors.getErrorMessages().forEach(report::logError);
                for (String sourceDirectory : filteredSourceDirectories) {
                    report.logInfo("Resolving file names for all issues in source directory '%s'", sourceDirectory);
                    nameResolver.run(report, sourceDirectory, ConsoleLogHandler::isInConsoleLog,
                            sourcePathPrefix, targetPathPrefix);
                }
                
                if (!filteredSourceDirectories.isEmpty()) {
                    return makePathsRelativeToSourceDirectories(report, filteredSourceDirectories);
                }
                return report;
            }
            catch (InvalidPathException exception) {
                report.logException(exception, "Resolving of file names aborted");
                return report;
            }
        }

        /**
         * Makes file paths in the report relative to the configured source directories.
         * This addresses JENKINS-68856 by fixing paths in the model, not in the REST API layer.
         *
         * @param originalReport the report containing issues with file paths  
         * @param sourceDirectories the configured source directories (absolute paths)
         * @return a new report with relative file paths
         */
        private Report makePathsRelativeToSourceDirectories(final Report originalReport, final Set<String> sourceDirectories) {
            originalReport.logInfo("Making file paths relative to source directories in the model");
            
            try (var builder = new IssueBuilder()) {
                var reportWithRelativePaths = new Report();
                
                for (var issue : originalReport) {
                    String fileName = issue.getFileName();
                    String normalizedFileName = fileName.replace("\\", "/");
                    String relativePath = null; 
                    
                    for (String sourceDir : sourceDirectories) {
                        String normalizedSourceDir = sourceDir.replace("\\", "/");
                        
                        if (!normalizedSourceDir.isEmpty() && !normalizedSourceDir.endsWith("/")) {
                            normalizedSourceDir += "/";
                        }
                        
                        if (normalizedFileName.startsWith(normalizedSourceDir)) {
                            relativePath = normalizedFileName.substring(normalizedSourceDir.length());
                            originalReport.logInfo("Transformed '%s' to relative path '%s' using source directory '%s'", 
                                    fileName, relativePath, sourceDir);
                            break;
                        }
                    }
                    
                    if (relativePath != null) {
                        reportWithRelativePaths.add(builder.copy(issue).setFileName(relativePath).build());
                    } else {
                        reportWithRelativePaths.add(issue);
                    }
                }
                
                reportWithRelativePaths.getInfoMessages().addAll(originalReport.getInfoMessages());
                reportWithRelativePaths.getErrorMessages().addAll(originalReport.getErrorMessages());
                
                return reportWithRelativePaths;
            }
        }

        private Set<String> filterSourceDirectories(final File workspace, final FilteredLog errors) {
            var filter = new SourceDirectoryFilter();
            return filter.getPermittedSourceDirectories(
                    workspace.getAbsolutePath(), permittedSourceDirectories, requestedSourceDirectories, errors);
        }

        private void resolveModuleNames(final Report report, final File workspace) {
            report.logInfo("Resolving module names from module definitions (build.xml, pom.xml, or Manifest.mf files)");

            try {
                var runner = new ModuleDetectorRunner(workspace.toPath(), new DefaultFileSystem());
                var resolver = new ModuleResolver(runner);
                resolver.run(report);
            }
            catch (InvalidPathException exception) {
                report.logException(exception, "Resolving of modul names aborted");
            }
        }

        private void resolvePackageNames(final Report report) {
            report.logInfo("Resolving package names (or namespaces) by parsing the affected files");

            try {
                var resolver = new PackageNameResolver();
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

            var generator = new FingerprintGenerator();
            if (linesLookAhead < 0) {
                // no-arg constructor defaults context lines to 3
                generator.run(new FullTextFingerprint(), report, getCharset());
            }
            else {
                generator.run(new FullTextFingerprint(linesLookAhead), report, getCharset());
            }
        }
    }

    /**
     * Provides file system operations using real IO.
     */
    private static final class DefaultFileSystem implements FileSystemFacade {
        @MustBeClosed
        @Override
        public InputStream open(final String fileName) throws IOException {
            return Files.newInputStream(Path.of(fileName));
        }

        @Override
        public List<String> find(final Path root, final String pattern) {
            return Arrays.stream(new FileFinder(pattern).find(root.toFile())).toList();
        }
    }
}

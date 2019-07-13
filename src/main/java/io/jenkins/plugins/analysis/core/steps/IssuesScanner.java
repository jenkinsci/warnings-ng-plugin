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

import edu.hm.hafner.analysis.FingerprintGenerator;
import edu.hm.hafner.analysis.FullTextFingerprint;
import edu.hm.hafner.analysis.ModuleDetector;
import edu.hm.hafner.analysis.ModuleDetector.FileSystem;
import edu.hm.hafner.analysis.ModuleResolver;
import edu.hm.hafner.analysis.PackageNameResolver;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Report.IssueFilterBuilder;

import hudson.FilePath;
import hudson.model.Computer;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import jenkins.MasterToSlaveFileCallable;

import io.jenkins.plugins.analysis.core.filter.RegexpFilter;
import io.jenkins.plugins.analysis.core.model.ReportLocations;
import io.jenkins.plugins.analysis.core.model.Tool;
import io.jenkins.plugins.analysis.core.util.AbsolutePathGenerator;
import io.jenkins.plugins.analysis.core.util.AffectedFilesResolver;
import io.jenkins.plugins.analysis.core.util.FileFinder;
import io.jenkins.plugins.analysis.core.util.LogHandler;
import io.jenkins.plugins.forensics.blame.Blamer;
import io.jenkins.plugins.forensics.blame.Blamer.NullBlamer;
import io.jenkins.plugins.forensics.blame.BlamerFactory;
import io.jenkins.plugins.forensics.blame.Blames;
import io.jenkins.plugins.forensics.blame.FileLocations;
import io.jenkins.plugins.forensics.util.FilteredLog;

import static io.jenkins.plugins.analysis.core.util.AffectedFilesResolver.*;

/**
 * Scans report files or the console log for issues.
 *
 * @author Ullrich Hafner
 */
@SuppressWarnings("PMD.ExcessiveImports")
class IssuesScanner {
    private final FilePath workspace;
    private final Run<?, ?> run;
    private final FilePath jenkinsRootDir;
    private final Charset sourceCodeEncoding;
    private final Tool tool;
    private final List<RegexpFilter> filters;
    private final TaskListener listener;
    private final BlameMode blameMode;

    enum BlameMode {
        ENABLED, DISABLED
    }

    IssuesScanner(final Tool tool, final List<RegexpFilter> filters,
            final Charset sourceCodeEncoding, final FilePath workspace, final Run<?, ?> run,
            final FilePath jenkinsRootDir, final TaskListener listener, final BlameMode blameMode) {
        this.filters = new ArrayList<>(filters);
        this.sourceCodeEncoding = sourceCodeEncoding;
        this.tool = tool;
        this.workspace = workspace;
        this.run = run;
        this.jenkinsRootDir = jenkinsRootDir;
        this.listener = listener;
        this.blameMode = blameMode;
    }

    public AnnotatedReport scan() throws IOException, InterruptedException {
        LogHandler logger = new LogHandler(listener, tool.getActualName());
        Report report = tool.scan(run, workspace, sourceCodeEncoding, logger);

        if (tool.getDescriptor().isPostProcessingEnabled()) {
            return postProcess(report, logger);
        }
        else {
            return new AnnotatedReport(tool.getActualId(), filter(report, filters, tool.getActualId()));
        }
    }

    private AnnotatedReport postProcess(final Report report, final LogHandler logger)
            throws IOException, InterruptedException {
        AnnotatedReport result;
        if (report.isEmpty()) {
            result = new AnnotatedReport(tool.getActualId(), report); // nothing to post process
            if (report.hasErrors()) {
                report.logInfo("Skipping post processing due to errors");
            }
        }
        else {
            report.logInfo("Post processing issues on '%s' with source code encoding '%s'",
                    getAgentName(workspace), sourceCodeEncoding);

            Blamer blamer;
            if (blameMode == BlameMode.DISABLED) {
                blamer = new NullBlamer();
            }
            else {
                FilteredLog log = new FilteredLog("Errors while determining a supported blamer for "
                        + run.getFullDisplayName());
                blamer = BlamerFactory.findBlamerFor(run, workspace, listener, log);
                log.logSummary();
                log.getInfoMessages().forEach(report::logInfo);
                log.getErrorMessages().forEach(report::logError);

            }
            result = workspace.act(new ReportPostProcessor(
                    tool.getActualId(), report, sourceCodeEncoding.name(),
                    blamer, filters));

            copyAffectedFiles(result.getReport(), createAffectedFilesFolder(result.getReport()), workspace);
        }
        logger.log(result.getReport());
        return result;
    }

    private void copyAffectedFiles(final Report report, final FilePath affectedFilesFolder,
            final FilePath workspace) throws InterruptedException {
        report.logInfo("Copying affected files to Jenkins' build folder '%s'", affectedFilesFolder);

        new AffectedFilesResolver().copyAffectedFilesToBuildFolder(report, affectedFilesFolder, workspace);
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

    private String getAgentName(final FilePath workspace) {
        return StringUtils.defaultIfBlank(getComputerName(workspace), "Master");
    }

    private String getComputerName(final FilePath workspace) {
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
    private static class ReportPostProcessor extends MasterToSlaveFileCallable<AnnotatedReport> {
        private static final long serialVersionUID = -9138045560271783096L;

        private final String id;
        private final Report originalReport;
        private final String sourceCodeEncoding;
        private final Blamer blamer;
        private final List<RegexpFilter> filters;

        ReportPostProcessor(final String id, final Report report, final String sourceCodeEncoding,
                final Blamer blamer, final List<RegexpFilter> filters) {
            super();

            this.id = id;
            originalReport = report;
            this.sourceCodeEncoding = sourceCodeEncoding;
            this.blamer = blamer;
            this.filters = filters;
        }

        @Override
        public AnnotatedReport invoke(final File workspace, final VirtualChannel channel) {
            resolveAbsolutePaths(originalReport, workspace);
            resolveModuleNames(originalReport, workspace);
            resolvePackageNames(originalReport);

            Report filtered = filter(originalReport, filters, id);

            createFingerprints(filtered);

            FileLocations fileLocations = new ReportLocations().toFileLocations(filtered, workspace.getPath());
            fileLocations.logSummary();
            fileLocations.getInfoMessages().forEach(filtered::logInfo);
            fileLocations.getErrorMessages().forEach(filtered::logError);
            Blames blames = blamer.blame(fileLocations);
            blames.logSummary();
            blames.getInfoMessages().forEach(filtered::logInfo);
            blames.getErrorMessages().forEach(filtered::logError);
            filtered.logInfo("---------------------");
            fileLocations.getAbsolutePaths().forEach(filtered::logInfo);
            fileLocations.getRelativePaths().forEach(filtered::logInfo);
            filtered.logInfo("---------------------");
            filtered.logInfo("---------------------");
            blames.getFiles().forEach(filtered::logInfo);
            filtered.logInfo("---------------------");
            return new AnnotatedReport(id, filtered, blames);
        }

        private void resolveAbsolutePaths(final Report report, final File workspace) {
            report.logInfo("Resolving absolute file names for all issues in workspace '%s'", workspace.toString());

            AbsolutePathGenerator generator = new AbsolutePathGenerator();
            generator.run(report, workspace.toPath());
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

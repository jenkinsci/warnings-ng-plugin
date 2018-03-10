package io.jenkins.plugins.analysis.core.steps;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.Collection;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import com.google.common.collect.ImmutableSet;

import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool.StaticAnalysisToolDescriptor;
import io.jenkins.plugins.analysis.core.util.AbsolutePathGenerator;
import io.jenkins.plugins.analysis.core.util.EnvironmentResolver;
import io.jenkins.plugins.analysis.core.util.FilesScanner;
import io.jenkins.plugins.analysis.core.util.Logger;
import io.jenkins.plugins.analysis.core.util.ModuleResolver;

import jenkins.model.Jenkins;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.console.ConsoleNote;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.plugins.analysis.util.EncodingValidator;
import hudson.plugins.analysis.util.FileFinder;

import edu.hm.hafner.analysis.FingerprintGenerator;
import edu.hm.hafner.analysis.FullTextFingerprint;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Issues;
import edu.hm.hafner.analysis.ModuleDetector;
import edu.hm.hafner.analysis.ModuleDetector.FileSystem;
import edu.hm.hafner.analysis.PackageNameResolver;
import edu.hm.hafner.util.Ensure;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Scan files or the console log for issues.
 */
@SuppressWarnings("InstanceVariableMayNotBeInitialized")
public class ScanForIssuesStep extends Step {
    private String logFileEncoding;
    private String sourceCodeEncoding;
    private String pattern;
    private StaticAnalysisTool tool;

    @DataBoundConstructor
    public ScanForIssuesStep() {
        // empty constructor required for Stapler
    }

    @CheckForNull
    public String getPattern() {
        return pattern;
    }

    /**
     * Sets the Ant file-set pattern of files to work with. If the pattern is undefined then the console log is
     * scanned.
     *
     * @param pattern
     *         the pattern to use
     */
    @DataBoundSetter
    public void setPattern(final String pattern) {
        this.pattern = pattern;
    }

    @CheckForNull
    public StaticAnalysisTool getTool() {
        return tool;
    }

    /**
     * Sets the static analysis tool that will scan files and create issues.
     *
     * @param tool
     *         the static analysis tool
     */
    @DataBoundSetter
    public void setTool(final StaticAnalysisTool tool) {
        this.tool = tool;
    }

    @CheckForNull
    public String getLogFileEncoding() {
        return logFileEncoding;
    }

    /**
     * Sets the default encoding used to read the log files that contain the warnings.
     *
     * @param logFileEncoding
     *         the encoding, e.g. "ISO-8859-1"
     */
    @DataBoundSetter
    public void setLogFileEncoding(final String logFileEncoding) {
        this.logFileEncoding = logFileEncoding;
    }

    @CheckForNull
    public String getSourceCodeEncoding() {
        return sourceCodeEncoding;
    }

    /**
     * Sets the default encoding used to read the log files that contain the warnings.
     *
     * @param sourceCodeEncoding
     *         the encoding, e.g. "ISO-8859-1"
     */
    @DataBoundSetter
    public void setSourceCodeEncoding(final String sourceCodeEncoding) {
        this.sourceCodeEncoding = sourceCodeEncoding;
    }

    @Override
    public StepExecution start(final StepContext context) {
        return new Execution(context, this);
    }

    /**
     * Actually performs the execution of the associated step.
     */
    public static class Execution extends AnalysisExecution<Issues<?>> {
        private final String logFileEncoding;
        private final String sourceCodeEncoding;
        private final StaticAnalysisTool tool;
        private final String pattern;

        protected Execution(@NonNull final StepContext context, final ScanForIssuesStep step) {
            super(context);

            logFileEncoding = step.getLogFileEncoding();
            sourceCodeEncoding = step.getSourceCodeEncoding();
            tool = step.getTool();
            pattern = step.getPattern();
        }

        @Override
        protected String getId() {
            return tool.getId();
        }

        @Override
        protected Issues<?> run() throws IOException, InterruptedException, IllegalStateException {
            FilePath workspace = getWorkspace();

            Issues<?> issues = findIssues(workspace);
            resolveAbsolutePaths(issues, workspace);
            resolveModuleNames(issues);
            resolvePackageNames(issues);
            createFingerprints(issues);

            return issues;
        }

        private Charset getLogFileCharset() {
            return EncodingValidator.defaultCharset(logFileEncoding);
        }

        private Charset getSourceCodeCharset() {
            return EncodingValidator.defaultCharset(sourceCodeEncoding);
        }

        private Issues<?> findIssues(final FilePath workspace)
                throws IOException, InterruptedException {
            Logger logger = getLogger();
            
            Instant start = Instant.now();

            Issues<?> issues;
            if (StringUtils.isNotBlank(pattern)) {
                issues = scanFiles(workspace);
            }
            else {
                Ensure.that(tool.canScanConsoleLog()).isTrue(
                        "Static analysis tool %s cannot scan console log output, please define a file pattern",
                        tool.getName());
                logger.log("Sleeping for 5 seconds due to JENKINS-32191...");
                Thread.sleep(5000);
                getLogger().log("Parsing console log (workspace: '%s')", workspace);
                issues = scanConsoleLog();
            }
            issues.setId(tool.getId());
            issues.forEach(issue -> issue.setOrigin(tool.getId()));

            logger.log("Parsing took %s", computeElapsedTime(start));
            return issues;
        }

        private Issues<?> scanConsoleLog() throws IOException, InterruptedException {
            Issues<?> issues = tool.createParser().parse(getRun().getLogFile(),
                    getLogFileCharset(), line -> ConsoleNote.removeNotes(line));

            log(issues);

            return issues;
        }

        private Issues<?> scanFiles(final FilePath workspace) throws IOException, InterruptedException {
            Issues<?> issues = workspace.act(
                    new FilesScanner(expandEnvironmentVariables(), tool.createParser(), logFileEncoding));

            log(issues);

            return issues;
        }

        private void resolveModuleNames(final Issues<?> issues)
                throws IOException, InterruptedException {
            Logger logger = getLogger();

            Instant start = Instant.now();
            logger.log("Resolving module names from module definitions (build.xml, pom.xml, or Manifest.mf files)");
            ModuleResolver resolver = new ModuleResolver();
            File workspace = new File(getWorkspace().getRemote());
            resolver.run(issues, workspace, new ModuleDetector(workspace, new DefaultFileSystem()));
            log(issues);

            logger.log("Resolving module names took %s", computeElapsedTime(start));
        }

        private void resolvePackageNames(final Issues<?> issues) {
            Logger logger = getLogger();

            Instant start = Instant.now();
            logger.log("Using encoding '%s' to resolve package names (or namespaces)", getSourceCodeCharset());
            PackageNameResolver resolver = new PackageNameResolver();
            resolver.run(issues, new IssueBuilder(), getSourceCodeCharset());
            log(issues);

            logger.log("Resolving package names took %s", computeElapsedTime(start));
        }

        private void resolveAbsolutePaths(final Issues<?> issues, final FilePath workspace) {
            Logger logger = getLogger();

            Instant start = Instant.now();
            logger.log("Resolving absolute file names for all issues");
            AbsolutePathGenerator generator = new AbsolutePathGenerator();
            generator.run(issues, workspace);
            log(issues);

            logger.log("Resolving absolute file names took %s", computeElapsedTime(start));
        }

        private void createFingerprints(final Issues<?> issues) {
            Logger logger = getLogger();

            Instant start = Instant.now();
            logger.log("Using encoding '%s' to read source files", getSourceCodeCharset());
            FingerprintGenerator generator = new FingerprintGenerator();
            generator.run(new FullTextFingerprint(), issues, getSourceCodeCharset());
            log(issues);

            logger.log("Extracting fingerprints took %s", computeElapsedTime(start));
        }

        private String expandEnvironmentVariables() throws IOException, InterruptedException {
            return getEnvironment()
                    .map(envVars -> new EnvironmentResolver().expandEnvironmentVariables(envVars, pattern))
                    .orElse(pattern);
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

    /**
     * Descriptor for this step: defines the context and the UI elements.
     */
    @Extension
    public static class Descriptor extends StepDescriptor {
        @Override
        public Set<Class<?>> getRequiredContext() {
            return ImmutableSet.of(FilePath.class, EnvVars.class, TaskListener.class, Run.class);
        }

        @Override
        public String getFunctionName() {
            return "scanForIssues";
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.ScanForIssues_DisplayName();
        }

        public Collection<? extends StaticAnalysisToolDescriptor> getAvailableTools() {
            return Jenkins.getInstance().getDescriptorList(StaticAnalysisTool.class);
        }
    }
}

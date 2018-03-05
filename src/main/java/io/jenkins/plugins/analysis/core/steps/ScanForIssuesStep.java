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
import org.eclipse.collections.api.list.ImmutableList;
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
import io.jenkins.plugins.analysis.core.util.FilesScanner;
import io.jenkins.plugins.analysis.core.util.Logger;
import io.jenkins.plugins.analysis.core.util.ModuleResolver;

import jenkins.model.Jenkins;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Util;
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
        private int logPosition = 0;

        protected Execution(@NonNull final StepContext context, final ScanForIssuesStep step) {
            super(context);

            logFileEncoding = step.getLogFileEncoding();
            sourceCodeEncoding = step.getSourceCodeEncoding();
            tool = step.getTool();
            pattern = step.getPattern();
        }

        @Override
        protected Issues<?> run() throws IOException, InterruptedException, IllegalStateException {
            FilePath workspace = getWorkspace();

            Logger logger = createLogger(tool.getName());
            Issues<?> issues = findIssues(workspace, logger);
            resolveAbsolutePaths(issues, workspace, logger);
            resolveModuleNames(issues, logger);
            resolvePackageNames(issues, logger);
            createFingerprints(issues, logger);

            return issues;
        }

        private Charset getLogFileCharset() {
            return EncodingValidator.defaultCharset(logFileEncoding);
        }

        private Charset getSourceCodeCharset() {
            return EncodingValidator.defaultCharset(sourceCodeEncoding);
        }

        private Issues<?> findIssues(final FilePath workspace, final Logger logger)
                throws IOException, InterruptedException {
            Instant start = Instant.now();

            Issues<?> issues;
            if (StringUtils.isNotBlank(pattern)) {
                issues = scanFiles(workspace, logger);
            }
            else {
                Ensure.that(tool.canScanConsoleLog()).isTrue(
                        "Static analysis tool %s cannot scan console log output, please define a file pattern",
                        tool.getName());
                logger.log("Sleeping for 5 seconds due to JENKINS-32191...");
                Thread.sleep(5000);
                issues = scanConsoleLog(workspace, logger);
            }
            issues.setId(tool.getId());
            issues.forEach(issue -> issue.setOrigin(tool.getId()));

            logger.log("Parsing took %s", computeElapsedTime(start));
            return issues;
        }

        private void resolveModuleNames(final Issues<?> issues, final Logger logger)
                throws IOException, InterruptedException {
            Instant start = Instant.now();

            logger.log("Resolving module names from module definitions (build.xml, pom.xml, or Manifest.mf files)");
            ModuleResolver resolver = new ModuleResolver();
            File workspace = new File(getWorkspace().getRemote());
            resolver.run(issues, workspace, new ModuleDetector(workspace, new DefaultFileSystem()));
            logIssuesMessages(issues, logger);

            logger.log("Resolving module names took %s", computeElapsedTime(start));
        }

        private void resolvePackageNames(final Issues<?> issues, final Logger logger) {
            Instant start = Instant.now();

            logger.log("Using encoding '%s' to resolve package names (or namespaces)", getSourceCodeCharset());
            PackageNameResolver resolver = new PackageNameResolver();
            resolver.run(issues, new IssueBuilder(), getSourceCodeCharset());
            logIssuesMessages(issues, logger);

            logger.log("Resolving package names took %s", computeElapsedTime(start));
        }

        private void resolveAbsolutePaths(final Issues<?> issues, final FilePath workspace, final Logger logger) {
            Instant start = Instant.now();

            AbsolutePathGenerator generator = new AbsolutePathGenerator();
            generator.run(issues, workspace);
            logIssuesMessages(issues, logger);

            logger.log("Resolving absolute file names took %s", computeElapsedTime(start));
        }

        private void createFingerprints(final Issues<?> issues, final Logger logger) {
            Instant start = Instant.now();

            logger.log("Using encoding '%s' to read source files", getSourceCodeCharset());
            FingerprintGenerator generator = new FingerprintGenerator();
            generator.run(new FullTextFingerprint(), issues, new IssueBuilder(), getSourceCodeCharset());
            logIssuesMessages(issues, logger);

            logger.log("Extracting fingerprints took %s", computeElapsedTime(start));
        }

        private Issues<?> scanConsoleLog(final FilePath workspace, final Logger logger)
                throws IOException, InterruptedException {
            logger.log("Parsing console log (workspace: '%s')", workspace);

            Issues<?> issues = tool.createParser().parse(getRun().getLogFile(),
                    getLogFileCharset(), line -> ConsoleNote.removeNotes(line));

            logIssuesMessages(issues, logger);

            return issues;
        }

        private Issues<?> scanFiles(final FilePath workspace, final Logger logger)
                throws IOException, InterruptedException {
            Issues<?> issues = workspace.act(
                    new FilesScanner(expandEnvironmentVariables(pattern), tool.createParser(), logFileEncoding));

            logIssuesMessages(issues, logger);

            return issues;
        }

        private void logIssuesMessages(final Issues<?> issues, final Logger logger) {
            ImmutableList<String> infoMessages = issues.getInfoMessages();
            if (logPosition < infoMessages.size()) {
                logger.logEachLine(infoMessages.subList(logPosition, infoMessages.size()).castToList());
                logPosition = infoMessages.size();
            }
        }

        // FIXME: expansion should be extracted to new class
        /** Maximum number of times that the environment expansion is executed. */
        private static final int RESOLVE_VARIABLES_DEPTH = 10;

        /**
         * Resolve build parameters in the file pattern up to {@link #RESOLVE_VARIABLES_DEPTH} times.
         *
         * @param unexpanded
         *         the pattern to expand
         */
        private String expandEnvironmentVariables(final String unexpanded) {
            String expanded = unexpanded;
            try {
                EnvVars environment = getContext().get(EnvVars.class);
                if (environment != null && !environment.isEmpty()) {
                    for (int i = 0; i < RESOLVE_VARIABLES_DEPTH && StringUtils.isNotBlank(expanded); i++) {
                        String old = expanded;
                        expanded = Util.replaceMacro(expanded, environment);
                        if (old.equals(expanded)) {
                            return expanded;
                        }
                    }
                }
            }
            catch (IOException | InterruptedException ignored) {
                // ignore
            }
            return expanded;
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

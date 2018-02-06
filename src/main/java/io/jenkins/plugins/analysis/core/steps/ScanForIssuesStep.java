package io.jenkins.plugins.analysis.core.steps;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.collections.api.list.ImmutableList;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import com.google.common.collect.ImmutableSet;

import edu.hm.hafner.analysis.FingerprintGenerator;
import edu.hm.hafner.analysis.FullTextFingerprint;
import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.Issues;
import edu.hm.hafner.analysis.PackageNameResolver;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool.StaticAnalysisToolDescriptor;
import io.jenkins.plugins.analysis.core.util.AbsolutePathGenerator;
import io.jenkins.plugins.analysis.core.util.FilesParser;
import io.jenkins.plugins.analysis.core.util.Logger;
import io.jenkins.plugins.analysis.core.util.LoggerFactory;
import jenkins.model.Jenkins;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Util;
import hudson.console.ConsoleNote;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.plugins.analysis.util.EncodingValidator;

/**
 * Scan files or the console log for issues.
 */
@SuppressWarnings("InstanceVariableMayNotBeInitialized")
public class ScanForIssuesStep extends Step {
    private String defaultEncoding;
    private boolean shouldDetectModules;
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
     * Sets the static analysis tool that produced the issues.
     *
     * @param tool
     *         the static analysis tool
     */
    @DataBoundSetter
    public void setTool(final StaticAnalysisTool tool) {
        this.tool = tool;
    }

    // FIXME: Should modules be scanned afterwards? Why is this optional?
    public boolean getShouldDetectModules() {
        return shouldDetectModules;
    }

    /**
     * Enables or disables module scanning. If {@code shouldDetectModules} is set, then the module name is derived by
     * parsing Maven POM or Ant build files.
     *
     * @param shouldDetectModules
     *         if set to {@code true} then modules are scanned.
     */
    @DataBoundSetter
    public void setShouldDetectModules(final boolean shouldDetectModules) {
        this.shouldDetectModules = shouldDetectModules;
    }

    @CheckForNull
    public String getDefaultEncoding() {
        return defaultEncoding;
    }

    /**
     * Sets the default encoding used to read files (warnings, source code, etc.).
     *
     * @param defaultEncoding
     *         the encoding, e.g. "ISO-8859-1"
     */
    // FIXME: two encodings are required: log file AND source file
    @DataBoundSetter
    public void setDefaultEncoding(final String defaultEncoding) {
        this.defaultEncoding = defaultEncoding;
    }

    @Override
    public StepExecution start(final StepContext context) {
        return new Execution(context, this);
    }

    /**
     * Actually performs the execution of the associated step.
     */
    public static class Execution extends SynchronousNonBlockingStepExecution<Issues<?>> {
        private final String defaultEncoding;
        private final boolean shouldDetectModules;
        private final StaticAnalysisTool tool;
        private final String pattern;
        private int logPosition = 0;

        protected Execution(@Nonnull final StepContext context, final ScanForIssuesStep step) {
            super(context);

            defaultEncoding = step.getDefaultEncoding();
            shouldDetectModules = step.getShouldDetectModules();
            tool = step.getTool();
            pattern = step.getPattern();
        }

        private Logger createLogger() throws IOException, InterruptedException {
            TaskListener listener = getContext().get(TaskListener.class);
            if (listener == null) {
                return new LoggerFactory().createNullLogger();
            }
            return new LoggerFactory().createLogger(listener.getLogger(), tool.getName());
        }

        private Run<?, ?> getRun() throws IOException, InterruptedException {
            return getContext().get(Run.class);
        }

        @Override
        protected Issues<?> run() throws IOException, InterruptedException, IllegalStateException {
            FilePath workspace = getContext().get(FilePath.class);

            if (workspace == null) {
                throw new IllegalStateException("No workspace set for step " + this);
            }
            else {
                Logger logger = createLogger();

                Issues<?> issues = findIssues(workspace, logger);
                resolveAbsolutePaths(issues, workspace, logger);
                resolvePackageNames(issues, logger);
                createFingerprints(issues, logger);
                return issues;
            }
        }

        private Issues<?> findIssues(final FilePath workspace, final Logger logger)
                throws IOException, InterruptedException {
            Instant start = Instant.now();

            Issues<?> issues;
            if (StringUtils.isNotBlank(pattern)) {
                issues = scanFiles(workspace, logger);
            }
            else {
                logger.log("Sleeping for 5 seconds due to JENKINS-32191...");
                Thread.sleep(5000);
                issues = scanConsoleLog(workspace, logger);
            }
            issues.setId(tool.getId());

            logger.log("Parsing took %s", Duration.between(start, Instant.now()));
            return issues;
        }

        private void resolvePackageNames(final Issues<?> issues, final Logger logger) {
            Instant start = Instant.now();

            PackageNameResolver resolver = new PackageNameResolver();
            resolver.run(issues, new IssueBuilder(), getCharset());
            logIssuesMessages(issues, logger);

            logger.log("Resolving package names took %s", Duration.between(start, Instant.now()));
        }

        private void resolveAbsolutePaths(final Issues<?> issues, final FilePath workspace, final Logger logger) {
            Instant start = Instant.now();

            AbsolutePathGenerator generator = new AbsolutePathGenerator();
            generator.run(issues, new IssueBuilder(), workspace);
            logIssuesMessages(issues, logger);

            logger.log("Resolving absolute file names took %s", Duration.between(start, Instant.now()));
        }

        private void createFingerprints(final Issues<?> issues, final Logger logger) {
            Instant start = Instant.now();

            FingerprintGenerator generator = new FingerprintGenerator();
            generator.run(new FullTextFingerprint(), issues, new IssueBuilder(), getCharset());
            logIssuesMessages(issues, logger);

            logger.log("Extracting fingerprints took %s", Duration.between(start, Instant.now()));
        }

        private Charset getCharset() {
            return EncodingValidator.defaultCharset(defaultEncoding);
        }

        private Issues<?> scanConsoleLog(final FilePath workspace,
                final Logger logger) throws IOException, InterruptedException {
            logger.log("Parsing console log (workspace: '%s')", workspace);

            Issues<?> issues = tool.createParser().parse(getRun().getLogFile(),
                    getCharset(), new IssueBuilder().setOrigin(tool.getId()),
                    line -> ConsoleNote.removeNotes(line));
            logIssuesMessages(issues, logger);
            return issues;
        }

        private Issues<Issue> scanFiles(final FilePath workspace,
                final Logger logger) throws IOException, InterruptedException {
            IssueParser<?> parser = tool.createParser();
            // FIXME: ID is not needed, origin should be set afterwards!
            // Actually issue setters should be protected so that all updates need to go through issues
            FilesParser filesParser = new FilesParser(expandEnvironmentVariables(pattern), parser,
                    tool.getId(), shouldDetectModules, defaultEncoding);
            Issues<Issue> issues = workspace.act(filesParser);

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

        @Nonnull
        @Override
        public String getDisplayName() {
            return "Scan files or the console log for issues";
        }


        public Collection<? extends StaticAnalysisToolDescriptor> getAvailableTools() {
            return Jenkins.getInstance().getDescriptorList(StaticAnalysisTool.class);
        }
    }
}

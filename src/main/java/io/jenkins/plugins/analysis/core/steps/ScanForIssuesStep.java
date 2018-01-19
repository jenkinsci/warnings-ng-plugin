package io.jenkins.plugins.analysis.core.steps;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
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
import edu.hm.hafner.analysis.Issues;
import edu.hm.hafner.analysis.PackageNameResolver;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;
import io.jenkins.plugins.analysis.core.model.ToolRegistry;
import io.jenkins.plugins.analysis.core.util.AbsolutePathGenerator;
import io.jenkins.plugins.analysis.core.util.FilesParser;
import io.jenkins.plugins.analysis.core.util.Logger;
import io.jenkins.plugins.analysis.core.util.LoggerFactory;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Util;
import hudson.console.ConsoleNote;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.plugins.analysis.util.EncodingValidator;
import hudson.util.ListBoxModel;

/**
 * Scan files or the console log for issues.
 */
@SuppressWarnings("InstanceVariableMayNotBeInitialized")
public class ScanForIssuesStep extends Step {
    private String defaultEncoding;
    private boolean shouldDetectModules;
    private String tool;
    private String pattern;

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
    public String getTool() {
        return tool;
    }

    // FIXME: Should modules be scanned afterwards? Why is this optional?

    /**
     * Sets the ID of the static analysis tool that produced the issues.
     *
     * @param tool
     *         the static analysis tool
     */
    @DataBoundSetter
    public void setTool(final String tool) {
        this.tool = tool;
    }

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
    public static class Execution extends SynchronousNonBlockingStepExecution<Issues<Issue>> {
        private final String defaultEncoding;
        private final boolean shouldDetectModules;
        private final String tool;
        private final String pattern;

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
            return new LoggerFactory().createLogger(listener.getLogger(), findTool().getName());
        }

        private StaticAnalysisTool findTool() {
            // FIXME: should be static!
            ToolRegistry registry = new ToolRegistry();
            return registry.find(tool);
        }

        private Run<?, ?> getRun() throws IOException, InterruptedException {
            return getContext().get(Run.class);
        }

        @Override
        protected Issues<Issue> run() throws IOException, InterruptedException, IllegalStateException {
            FilePath workspace = getContext().get(FilePath.class);

            if (workspace == null) {
                throw new IllegalStateException("No workspace set for step " + this);
            }
            else {
                Logger logger = createLogger();

                Issues<Issue> issues = findIssues(workspace, logger);
                Issues<Issue> issuesWithPath = resolveAbsolutePaths(issues, workspace, logger);
                Issues<Issue> issuesWithPackage = resolvePackageNames(issuesWithPath, logger);

                return createFingerprints(issuesWithPackage, logger);
            }
        }

        private Issues<Issue> findIssues(final FilePath workspace, final Logger logger)
                throws IOException, InterruptedException {
            Instant start = Instant.now();

            Issues<Issue> issues;
            if (StringUtils.isNotBlank(pattern)) {
                issues = scanFiles(workspace, logger);
            }
            else {
                issues = scanConsoleLog(workspace, logger);
            }
            issues.setId(tool);

            logger.log("Parsing took %s", Duration.between(start, Instant.now()));
            return issues;
        }

        private Issues<Issue> resolvePackageNames(final Issues<Issue> issues, final Logger logger) {
            Instant start = Instant.now();

            PackageNameResolver resolver = new PackageNameResolver();
            Issues<Issue> resolved = resolver.run(issues, new IssueBuilder(), getCharset());
            logIssuesMessages(resolved, logger);

            logger.log("Resolving package names took %s", Duration.between(start, Instant.now()));

            return resolved;
        }

        private Issues<Issue> resolveAbsolutePaths(final Issues<Issue> issues, final FilePath workspace,
                final Logger logger) {
            Instant start = Instant.now();

            AbsolutePathGenerator generator = new AbsolutePathGenerator();
            Issues<Issue> resolved = generator.run(issues, new IssueBuilder(), workspace);
            logIssuesMessages(resolved, logger);

            logger.log("Resolving absolute file names took %s", Duration.between(start, Instant.now()));

            return resolved;
        }

        private Issues<Issue> createFingerprints(final Issues<Issue> issues, final Logger logger) {
            Instant start = Instant.now();

            FingerprintGenerator generator = new FingerprintGenerator();
            Issues<Issue> fingerPrinted = generator.run(new FullTextFingerprint(),
                    issues, new IssueBuilder(), getCharset());

            logger.log("Extracting fingerprints took %s", Duration.between(start, Instant.now()));

            return fingerPrinted;
        }

        private Charset getCharset() {
            return EncodingValidator.defaultCharset(defaultEncoding);
        }

        private Issues<Issue> scanConsoleLog(final FilePath workspace,
                final Logger logger) throws IOException, InterruptedException {
            logger.log("Parsing console log (workspace: '%s')", workspace);

            Issues<Issue> issues = findTool().createParser().parse(getRun().getLogFile(),
                    getCharset(), new IssueBuilder().setOrigin(tool), line -> ConsoleNote.removeNotes(line));
            logIssuesMessages(issues, logger);
            return issues;
        }

        private Issues<Issue> scanFiles(final FilePath workspace,
                final Logger logger) throws IOException, InterruptedException {
            FilesParser filesParser = new FilesParser(expandEnvironmentVariables(pattern), findTool().createParser(),
                    tool, shouldDetectModules, defaultEncoding);
            Issues<Issue> issues = workspace.act(filesParser);

            logIssuesMessages(issues, logger);

            return issues;
        }

        private void logIssuesMessages(final Issues<Issue> issues, final Logger logger) {
            logger.logEachLine(issues.getInfoMessages().castToList());
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

        /**
         * Returns all registered static analysis tools. These are packed into a {@link ListBoxModel} in order to show
         * them in the list box of the config.jelly view part.
         *
         * @return the model of the list box
         */
        public ListBoxModel doFillToolItems() {
            ListBoxModel items = new ListBoxModel();
            ToolRegistry registry = new ToolRegistry();
            for (StaticAnalysisTool tool : registry.getAll()) {
                items.add(tool.getName(), tool.getId());
            }
            return items;
        }
    }
}

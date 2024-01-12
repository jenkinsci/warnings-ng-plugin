package io.jenkins.plugins.analysis.core.steps;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.collections.impl.factory.Sets;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.Run;
import hudson.model.TaskListener;

import io.jenkins.plugins.analysis.core.filter.RegexpFilter;
import io.jenkins.plugins.analysis.core.model.Tool;
import io.jenkins.plugins.analysis.core.steps.IssuesScanner.BlameMode;
import io.jenkins.plugins.analysis.core.steps.IssuesScanner.PostProcessingMode;
import io.jenkins.plugins.prism.SourceCodeDirectory;
import io.jenkins.plugins.prism.SourceCodeRetention;

/**
 * Scan files or the console log for issues.
 */
@SuppressWarnings({"InstanceVariableMayNotBeInitialized", "PMD.ExcessivePublicCount", "PMD.ExcessiveImports", "PMD.DataClass"})
public class ScanForIssuesStep extends Step {
    private Tool tool;

    private String sourceCodeEncoding = StringUtils.EMPTY;
    private Set<SourceCodeDirectory> sourceDirectories = new HashSet<>(); // @since 9.11.0
    private SourceCodeRetention sourceCodeRetention = SourceCodeRetention.EVERY_BUILD;
    private boolean isBlameDisabled;
    private boolean skipPostProcessing; // @since 10.6.0: by default, post-processing will be enabled
    private boolean quiet;

    private List<RegexpFilter> filters = new ArrayList<>();
    private String scm = StringUtils.EMPTY;

    /**
     * Creates a new instance of {@link ScanForIssuesStep}.
     */
    @DataBoundConstructor
    public ScanForIssuesStep() {
        super();

        // empty constructor required for Stapler
    }

    @CheckForNull
    public Tool getTool() {
        return tool;
    }

    /**
     * Sets the static analysis tool that will scan files and create issues.
     *
     * @param tool
     *         the static analysis tool
     */
    @DataBoundSetter
    public void setTool(final Tool tool) {
        this.tool = tool;
    }

    public List<RegexpFilter> getFilters() {
        return filters;
    }

    @DataBoundSetter
    public void setFilters(final List<RegexpFilter> filters) {
        this.filters = new ArrayList<>(filters);
    }

    /**
     * Sets whether logging output should be enabled.
     *
     * @param quiet
     *         boolean flag to mute logging
     */
    @DataBoundSetter
    public void setQuiet(final boolean quiet) {
        this.quiet = quiet;
    }

    public boolean isQuiet() {
        return quiet;
    }

    /**
     * Sets the SCM that should be used to find the reference build for. The reference recorder will select the SCM
     * based on a substring comparison, there is no need to specify the full name.
     *
     * @param scm
     *         the ID of the SCM to use (a substring of the full ID)
     */
    @DataBoundSetter
    public void setScm(final String scm) {
        this.scm = scm;
    }

    public String getScm() {
        return scm;
    }

    /**
     * Returns whether SCM blaming should be disabled.
     *
     * @return {@code true} if SCM blaming should be disabled
     */
    @SuppressWarnings("PMD.BooleanGetMethodName")
    public boolean getBlameDisabled() {
        return isBlameDisabled;
    }

    @DataBoundSetter
    public void setBlameDisabled(final boolean blameDisabled) {
        isBlameDisabled = blameDisabled;
    }

    /**
     * Returns whether post-processing of the issues should be disabled.
     *
     * @return {@code true} if post-processing of the issues should be disabled.
     */
    public boolean isSkipPostProcessing() {
        return skipPostProcessing;
    }

    @DataBoundSetter
    public void setSkipPostProcessing(final boolean skipPostProcessing) {
        this.skipPostProcessing = skipPostProcessing;
    }

    @CheckForNull
    public String getSourceCodeEncoding() {
        return sourceCodeEncoding;
    }

    /**
     * Sets the encoding to use to read source files.
     *
     * @param sourceCodeEncoding
     *         the encoding, e.g. "ISO-8859-1"
     */
    @DataBoundSetter
    public void setSourceCodeEncoding(final String sourceCodeEncoding) {
        this.sourceCodeEncoding = sourceCodeEncoding;
    }

    /**
     * Sets the paths to the directories that contain the source code. If not relative and thus not part of the
     *  workspace, then these directories need to be added in Jenkins global configuration to prevent accessing of
     * forbidden resources.
     *
     * @param sourceDirectories
     *         directories containing the source code
     */
    @DataBoundSetter
    public void setSourceDirectories(final List<SourceCodeDirectory> sourceDirectories) {
        this.sourceDirectories = new HashSet<>(sourceDirectories);
    }

    public List<SourceCodeDirectory> getSourceDirectories() {
        return new ArrayList<>(sourceDirectories);
    }

    private Set<String> getAllSourceDirectories() {
        return getSourceDirectories().stream()
                .map(SourceCodeDirectory::getPath)
                .collect(Collectors.toSet());
    }

    /**
     * Defines the retention strategy for source code files.
     *
     * @param sourceCodeRetention
     *         the retention strategy for source code files
     */
    @DataBoundSetter
    public void setSourceCodeRetention(final SourceCodeRetention sourceCodeRetention) {
        this.sourceCodeRetention = sourceCodeRetention;
    }

    public SourceCodeRetention getSourceCodeRetention() {
        return sourceCodeRetention;
    }

    @Override
    public StepExecution start(final StepContext context) {
        return new Execution(context, this);
    }

    /**
     * Actually performs the execution of the associated step.
     */
    @SuppressFBWarnings(value = "THROWS", justification = "false positive")
    static class Execution extends AnalysisExecution<AnnotatedReport> {
        private static final long serialVersionUID = -4627988939459725361L;

        private final Tool tool;
        private final String sourceCodeEncoding;
        private final boolean isBlameDisabled;
        private final boolean skipPostProcessing;
        private final List<RegexpFilter> filters;
        private final Set<String> sourceDirectories;
        private final String scm;
        private final boolean quiet;
        private final SourceCodeRetention sourceCodeRetention;

        /**
         * Creates a new instance of the step execution object.
         *
         * @param context
         *         context for this step
         * @param step
         *         the actual step to execute
         */
        Execution(@NonNull final StepContext context, final ScanForIssuesStep step) {
            super(context);

            tool = step.getTool();
            sourceCodeEncoding = step.getSourceCodeEncoding();
            isBlameDisabled = step.getBlameDisabled();
            filters = step.getFilters();
            sourceDirectories = step.getAllSourceDirectories();
            sourceCodeRetention = step.getSourceCodeRetention();
            scm = step.getScm();
            skipPostProcessing = step.isSkipPostProcessing();
            quiet = step.isQuiet();
        }

        @Override
        protected AnnotatedReport run() throws IOException, InterruptedException, IllegalStateException {
            FilePath workspace = getWorkspace();
            TaskListener listener = getTaskListener();

            IssuesScanner issuesScanner = new IssuesScanner(tool, filters,
                    getCharset(sourceCodeEncoding), workspace, sourceDirectories,
                    sourceCodeRetention, getRun(), new FilePath(getRun().getRootDir()), listener,
                    scm, isBlameDisabled ? BlameMode.DISABLED : BlameMode.ENABLED,
                    skipPostProcessing ? PostProcessingMode.DISABLED : PostProcessingMode.ENABLED,
                    quiet);

            return issuesScanner.scan();
        }
    }

    /**
     * Descriptor for this step: defines the context and the UI elements.
     */
    @Extension
    public static class Descriptor extends AnalysisStepDescriptor {
        @Override
        public Set<Class<?>> getRequiredContext() {
            return Sets.immutable.of(FilePath.class, EnvVars.class, TaskListener.class, Run.class).castToSet();
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
    }
}

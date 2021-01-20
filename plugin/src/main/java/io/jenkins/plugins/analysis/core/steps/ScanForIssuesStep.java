package io.jenkins.plugins.analysis.core.steps;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.collections.impl.factory.Sets;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;

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

/**
 * Scan files or the console log for issues.
 */
@SuppressWarnings({"InstanceVariableMayNotBeInitialized", "PMD.ExcessivePublicCount", "PMD.ExcessiveImports"})
public class ScanForIssuesStep extends Step {
    private Tool tool;

    private String sourceCodeEncoding = StringUtils.EMPTY;
    private String sourceDirectory = StringUtils.EMPTY;
    private boolean isBlameDisabled;

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
     * Not used anymore.
     *
     * @return {@code true} if SCM forensics should be disabled
     * @deprecated Forensics will be automatically skipped if the Forensics recorder is not activated.
     */
    @SuppressWarnings("PMD.BooleanGetMethodName")
    @Deprecated
    public boolean getForensicsDisabled() {
        return false;
    }

    /**
     * Not used anymore.
     *
     * @param forensicsDisabled
     *         not used
     *
     * @deprecated Forensics will be automatically skipped if the Forensics recorder is not activated.
     */
    @DataBoundSetter
    @Deprecated
    public void setForensicsDisabled(final boolean forensicsDisabled) {
        // do nothing
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

    public String getSourceDirectory() {
        return sourceDirectory;
    }

    /**
     * Sets the path to the folder that contains the source code. If not relative and thus not part of the workspace
     * then this folder needs to be added in Jenkins global configuration.
     *
     * @param sourceDirectory
     *         a folder containing the source code
     */
    @DataBoundSetter
    public void setSourceDirectory(final String sourceDirectory) {
        this.sourceDirectory = sourceDirectory;
    }

    @Override
    public StepExecution start(final StepContext context) {
        return new Execution(context, this);
    }

    /**
     * Actually performs the execution of the associated step.
     */
    static class Execution extends AnalysisExecution<AnnotatedReport> {
        private static final long serialVersionUID = -4627988939459725361L;

        private final Tool tool;
        private final String sourceCodeEncoding;
        private final boolean isBlameDisabled;
        private final List<RegexpFilter> filters;
        private final String sourceDirectory;
        private final String scm;

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
            sourceDirectory = step.getSourceDirectory();
            scm = step.getScm();
        }

        @Override
        protected AnnotatedReport run() throws IOException, InterruptedException, IllegalStateException {
            FilePath workspace = getWorkspace();
            TaskListener listener = getTaskListener();

            IssuesScanner issuesScanner = new IssuesScanner(tool, filters,
                    getCharset(sourceCodeEncoding), workspace, sourceDirectory,
                    getRun(), new FilePath(getRun().getRootDir()), listener,
                    scm, isBlameDisabled ? BlameMode.DISABLED : BlameMode.ENABLED);

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

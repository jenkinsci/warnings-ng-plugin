package io.jenkins.plugins.analysis.core.steps;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.collections.impl.factory.Sets;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.ComboBoxModel;
import hudson.util.FormValidation;

import io.jenkins.plugins.analysis.core.filter.RegexpFilter;
import io.jenkins.plugins.analysis.core.model.Tool;
import io.jenkins.plugins.analysis.core.steps.IssuesScanner.BlameMode;
import io.jenkins.plugins.analysis.core.util.ModelValidation;

/**
 * Scan files or the console log for issues.
 */
@SuppressWarnings({"InstanceVariableMayNotBeInitialized", "PMD.ExcessivePublicCount", "PMD.ExcessiveImports"})
public class ScanForIssuesStep extends Step {
    private Tool tool;

    private String sourceCodeEncoding = StringUtils.EMPTY;
    private boolean isBlameDisabled;

    private List<RegexpFilter> filters = new ArrayList<>();

    /**
     * Creates a new instance of {@link ScanForIssuesStep}.
     */
    @DataBoundConstructor
    public ScanForIssuesStep() {
        super();

        // empty constructor required for Stapler
    }

    @Nullable
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

    @Nullable
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
        }

        @Override
        protected AnnotatedReport run() throws IOException, InterruptedException, IllegalStateException {
            FilePath workspace = getWorkspace();
            TaskListener listener = getTaskListener();

            IssuesScanner issuesScanner = new IssuesScanner(tool, filters,
                    getCharset(sourceCodeEncoding), workspace, getRun(), new FilePath(getRun().getRootDir()), listener,
                    isBlameDisabled ? BlameMode.DISABLED : BlameMode.ENABLED);

            return issuesScanner.scan();
        }
    }

    /**
     * Descriptor for this step: defines the context and the UI elements.
     */
    @Extension
    public static class Descriptor extends StepDescriptor {
        private final ModelValidation model = new ModelValidation();

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

        /**
         * Returns a model with all available charsets.
         *
         * @return a model with all available charsets
         */
        public ComboBoxModel doFillSourceCodeEncodingItems() {
            return model.getAllCharsets();
        }

        /**
         * Performs on-the-fly validation of the character encoding.
         *
         * @param reportEncoding
         *         the character encoding
         *
         * @return the validation result
         */
        public FormValidation doCheckReportEncoding(@QueryParameter final String reportEncoding) {
            return model.validateCharset(reportEncoding);
        }

        /**
         * Performs on-the-fly validation on the character encoding.
         *
         * @param sourceCodeEncoding
         *         the character encoding
         *
         * @return the validation result
         */
        public FormValidation doCheckSourceCodeEncoding(@QueryParameter final String sourceCodeEncoding) {
            return model.validateCharset(sourceCodeEncoding);
        }
    }
}

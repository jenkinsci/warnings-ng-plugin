package io.jenkins.plugins.analysis.core.steps;

import java.io.IOException;
import java.util.Set;

import org.eclipse.collections.impl.factory.Sets;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import edu.hm.hafner.analysis.Report;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;
import io.jenkins.plugins.analysis.core.scm.BlameFactory;
import io.jenkins.plugins.analysis.core.scm.Blames;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.ComboBoxModel;
import hudson.util.FormValidation;

/**
 * Scan files or the console log for issues.
 */
@SuppressWarnings("InstanceVariableMayNotBeInitialized")
public class ScanForIssuesStep extends Step {
    private String reportEncoding;
    private String sourceCodeEncoding;
    private String pattern;
    private StaticAnalysisTool tool;
    private boolean isBlameDisabled;

    /**
     * Creates a new instance of {@link ScanForIssuesStep}.
     */
    @DataBoundConstructor
    public ScanForIssuesStep() {
        super();

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
     * Returns whether SCM blaming should be disabled.
     *
     * @return {@code true} if SCM blaming should be disabled
     */
    public boolean getBlameDisabled() {
        return isBlameDisabled;
    }

    @DataBoundSetter
    public void setBlameDisabled(final boolean blameDisabled) {
        this.isBlameDisabled = blameDisabled;
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
    public String getReportEncoding() {
        return reportEncoding;
    }

    /**
     * Sets the encoding to use to read the log files that contain the warnings.
     *
     * @param reportEncoding
     *         the encoding, e.g. "ISO-8859-1"
     */
    @DataBoundSetter
    public void setReportEncoding(final String reportEncoding) {
        this.reportEncoding = reportEncoding;
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

    @Override
    public StepExecution start(final StepContext context) {
        return new Execution(context, this);
    }

    /**
     * Actually performs the execution of the associated step.
     */
    public static class Execution extends AnalysisExecution<AnnotatedReport> {
        private final String reportEncoding;
        private final String sourceCodeEncoding;
        private final StaticAnalysisTool tool;
        private final String pattern;
        private final boolean isBlameDisabled;

        /**
         * Creates a new instance of the step execution object.
         *
         * @param context
         *         context for this step
         * @param step
         *         the actual step to execute
         */
        protected Execution(@NonNull final StepContext context, final ScanForIssuesStep step) {
            super(context);

            reportEncoding = step.getReportEncoding();
            sourceCodeEncoding = step.getSourceCodeEncoding();
            tool = step.getTool();
            pattern = step.getPattern();
            isBlameDisabled = step.getBlameDisabled();
        }

        @Override
        protected AnnotatedReport run() throws IOException, InterruptedException, IllegalStateException {
            FilePath workspace = getWorkspace();
            TaskListener listener = getTaskListener();
            
            IssuesScanner issuesScanner = new IssuesScanner(tool, workspace, getCharset(reportEncoding),
                    getCharset(sourceCodeEncoding), new FilePath(getRun().getRootDir()), 
                    new LogHandler(listener, tool.getName()));
            
            Report report = issuesScanner.scan(pattern, getRun().getLogFile());
            return new AnnotatedReport(report, blame(report, workspace, listener));
        }

        private Blames blame(final Report report, final FilePath workspace, final TaskListener listener)
                throws IOException, InterruptedException {
            if (isBlameDisabled) {
                Blames blames = new Blames();
                blames.logInfo("Skipping blaming as requested in the job configuration");
                return blames;
            }
            return BlameFactory.createBlamer(getRun(), workspace, listener).blame(report);
        }
    }

    /**
     * Descriptor for this step: defines the context and the UI elements.
     */
    @Extension
    public static class Descriptor extends StepDescriptor {
        private final JobConfigurationModel model = new JobConfigurationModel();

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
        public ComboBoxModel doFillReportEncodingItems() {
            return model.getAllCharsets();
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

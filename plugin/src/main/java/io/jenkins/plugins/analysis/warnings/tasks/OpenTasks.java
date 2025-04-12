package io.jenkins.plugins.analysis.warnings.tasks;

import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.ParsingCanceledException;
import edu.hm.hafner.analysis.Report;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Serial;
import java.io.StringReader;
import java.nio.charset.Charset;

import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.verb.POST;
import org.jenkinsci.Symbol;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.AbstractProject;
import hudson.model.BuildableItem;
import hudson.model.Item;
import hudson.model.Run;
import hudson.util.FormValidation;

import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.SymbolIconLabelProvider;
import io.jenkins.plugins.analysis.core.model.Tool;
import io.jenkins.plugins.analysis.warnings.Messages;
import io.jenkins.plugins.analysis.warnings.tasks.TaskScanner.CaseMode;
import io.jenkins.plugins.analysis.warnings.tasks.TaskScanner.MatcherMode;
import io.jenkins.plugins.util.JenkinsFacade;
import io.jenkins.plugins.util.LogHandler;
import io.jenkins.plugins.util.ValidationUtilities;

/**
 * Provides a files scanner that detects open tasks in source code files.
 *
 * @author Ullrich Hafner
 */
@SuppressWarnings({"PMD.DataClass", "PMD.ExcessiveImports"})
public class OpenTasks extends Tool {
    @Serial
    private static final long serialVersionUID = 4692318309214830824L;

    private static final String ID = "open-tasks";

    private String highTags = StringUtils.EMPTY;
    private String normalTags = StringUtils.EMPTY;
    private String lowTags = StringUtils.EMPTY;
    private boolean ignoreCase;
    private boolean isRegularExpression;
    private String includePattern = StringUtils.EMPTY;
    private String excludePattern = StringUtils.EMPTY;

    /**
     * Returns the Ant file-set pattern of files to work with.
     *
     * @return Ant file-set pattern of files to work with
     */
    public String getIncludePattern() {
        return includePattern;
    }

    @DataBoundSetter
    public void setIncludePattern(final String includePattern) {
        this.includePattern = includePattern;
    }

    /**
     * Returns the Ant file-set pattern of files to exclude from work.
     *
     * @return Ant file-set pattern of files to exclude from work
     */
    public String getExcludePattern() {
        return excludePattern;
    }

    @DataBoundSetter
    public void setExcludePattern(final String excludePattern) {
        this.excludePattern = excludePattern;
    }

    /**
     * Returns the high priority tag identifiers.
     *
     * @return the high priority tag identifiers
     */
    public String getHighTags() {
        return highTags;
    }

    @DataBoundSetter
    public void setHighTags(final String highTags) {
        this.highTags = highTags;
    }

    /**
     * Returns the normal priority tag identifiers.
     *
     * @return the normal priority tag identifiers
     */
    public String getNormalTags() {
        return normalTags;
    }

    @DataBoundSetter
    public void setNormalTags(final String normalTags) {
        this.normalTags = normalTags;
    }

    /**
     * Returns the low priority tag identifiers.
     *
     * @return the low priority tag identifiers
     */
    public String getLowTags() {
        return lowTags;
    }

    @DataBoundSetter
    public void setLowTags(final String lowTags) {
        this.lowTags = lowTags;
    }

    /**
     * Returns whether the case of the characters should be ignored during the scanning.
     *
     * @return {@code true} if the case should be ignored during the scanning
     */
    @SuppressWarnings("PMD.BooleanGetMethodName")
    public boolean getIgnoreCase() {
        return ignoreCase;
    }

    @DataBoundSetter
    public void setIgnoreCase(final boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
    }

    /**
     * Returns whether the identifiers should be treated as regular expression.
     *
     * @return {@code true} if the identifiers should be treated as regular expression
     */
    @SuppressWarnings("PMD.BooleanGetMethodName")
    public boolean getIsRegularExpression() {
        return isRegularExpression;
    }

    @DataBoundSetter
    public void setIsRegularExpression(final boolean isRegularExpression) {
        this.isRegularExpression = isRegularExpression;
    }

    @Override
    public Report scan(final Run<?, ?> run, final FilePath workspace, final Charset sourceCodeEncoding,
            final LogHandler logger) {
        try {
            var openTasks = workspace.act(new AgentScanner(highTags, normalTags, lowTags,
                    ignoreCase ? CaseMode.IGNORE_CASE : CaseMode.CASE_SENSITIVE,
                    isRegularExpression ? MatcherMode.REGEXP_MATCH : MatcherMode.STRING_MATCH,
                    includePattern, excludePattern, sourceCodeEncoding.name()));
            openTasks.setOrigin(getActualId(), getActualName());

            return openTasks;
        }
        catch (IOException e) {
            var report = new Report();
            report.logException(e, "Exception while reading the source code files:");
            return report;
        }
        catch (InterruptedException e) {
            throw new ParsingCanceledException(e);
        }
    }

    /** Creates a new instance of {@link OpenTasks}. */
    @DataBoundConstructor
    public OpenTasks() {
        super();
        // empty constructor required for stapler
    }

    /** Label provider with customized messages. */
    private static class LabelProvider extends SymbolIconLabelProvider {
        LabelProvider() {
            super(ID, Messages.Warnings_OpenTasks_Name(), i -> StringUtils.EMPTY,
                    "symbol-solid/clipboard-check plugin-font-awesome-api");
        }

        @Override
        public String getLinkName() {
            return Messages.Warnings_OpenTasks_LinkName();
        }

        @Override
        public String getTrendName() {
            return Messages.Warnings_OpenTasks_TrendName();
        }
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("taskScanner")
    @Extension
    public static class Descriptor extends ToolDescriptor {
        private static final JenkinsFacade JENKINS = new JenkinsFacade();
        private static final ValidationUtilities VALIDATION_UTILITIES = new ValidationUtilities();

        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_OpenTasks_Name();
        }

        @Override
        public StaticAnalysisLabelProvider getLabelProvider() {
            return new LabelProvider();
        }

        /**
         * Performs on-the-fly validation on the ant pattern for input files.
         *
         * @param project
         *         the project that is configured
         * @param includePattern
         *         the file pattern
         *
         * @return the validation result
         */
        @POST
        public FormValidation doCheckIncludePattern(@AncestorInPath final AbstractProject<?, ?> project,
                @QueryParameter final String includePattern) {
            if (!JENKINS.hasPermission(Item.CONFIGURE, project)) {
                return FormValidation.ok();
            }

            return VALIDATION_UTILITIES.doCheckPattern(project, includePattern);
        }

        /**
         * Performs on-the-fly validation on the ant pattern for input files.
         *
         * @param project
         *         the project that is configured
         * @param excludePattern
         *         the file pattern
         *
         * @return the validation result
         */
        @POST
        public FormValidation doCheckExcludePattern(@AncestorInPath final AbstractProject<?, ?> project,
                @QueryParameter final String excludePattern) {
            if (!JENKINS.hasPermission(Item.CONFIGURE, project)) {
                return FormValidation.ok();
            }

            return VALIDATION_UTILITIES.doCheckPattern(project, excludePattern);
        }

        /**
         * Validates the example text that will be scanned for open tasks.
         *
         * @param project
         *         the project that is configured
         * @param example
         *         the text to be scanned for open tasks
         * @param high
         *         tag identifiers indicating high priority
         * @param normal
         *         tag identifiers indicating normal priority
         * @param low
         *         tag identifiers indicating low priority
         * @param ignoreCase
         *         if the case should be ignored during matching
         * @param asRegexp
         *         if the identifiers should be treated as regular expression
         *
         * @return validation result
         */
        @SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
        @POST
        public FormValidation doCheckExample(@AncestorInPath final BuildableItem project,
                @QueryParameter final String example,
                @QueryParameter final String high,
                @QueryParameter final String normal,
                @QueryParameter final String low,
                @QueryParameter final boolean ignoreCase,
                @QueryParameter final boolean asRegexp) {
            if (StringUtils.isEmpty(example) || !JENKINS.hasPermission(Item.CONFIGURE, project)) {
                return FormValidation.ok();
            }

            var builder = new TaskScannerBuilder();
            var scanner = builder.setHighTasks(high)
                    .setNormalTasks(normal)
                    .setLowTasks(low)
                    .setCaseMode(ignoreCase ? CaseMode.IGNORE_CASE : CaseMode.CASE_SENSITIVE)
                    .setMatcherMode(asRegexp ? MatcherMode.REGEXP_MATCH : MatcherMode.STRING_MATCH).build();

            if (scanner.isInvalidPattern()) {
                return FormValidation.error(scanner.getErrors());
            }

            try (var reader = new BufferedReader(new StringReader(example)); var issueBuilder = new IssueBuilder()) {
                issueBuilder.setFileName("UI example");
                var tasks = scanner.scanTasks(reader.lines().iterator(), issueBuilder);
                if (tasks.isEmpty()) {
                    return FormValidation.warning(Messages.OpenTasks_Validation_NoTask());
                }
                else if (tasks.size() != 1) {
                    return FormValidation.warning(Messages.OpenTasks_Validation_MultipleTasks(tasks.size()));
                }
                else {
                    var task = tasks.get(0);
                    return FormValidation.ok(Messages.OpenTasks_Validation_OneTask(task.getType(), task.getMessage()));
                }
            }
            catch (IOException e) {
                return FormValidation.error(e.getMessage()); // should never happen
            }
        }
    }
}

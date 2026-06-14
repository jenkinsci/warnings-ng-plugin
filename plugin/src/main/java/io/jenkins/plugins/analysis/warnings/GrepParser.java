package io.jenkins.plugins.analysis.warnings;

import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.analysis.ParsingCanceledException;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Severity;
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
import hudson.util.ListBoxModel;

import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.SymbolIconLabelProvider;
import io.jenkins.plugins.analysis.core.model.Tool;
import io.jenkins.plugins.util.JenkinsFacade;
import io.jenkins.plugins.util.LogHandler;
import io.jenkins.plugins.util.ValidationUtilities;

/**
 * Provides a simple grep-style file scanner that searches for occurrences of a configurable regular expression
 * in the workspace files and reports each matching line as an issue.
 *
 * @author Akash Manna
 * @see <a href="https://issues.jenkins.io/browse/JENKINS-53014">JENKINS-53014: Add Grep Parsing
 * capability to Warnings Plugin</a>
 */
@SuppressWarnings("PMD.DataClass")
public class GrepParser extends Tool {
    @Serial
    private static final long serialVersionUID = -5253946294921794746L;

    private static final String ID = "grep";

    /** Default severity to assign to matches when none is configured. */
    static final String DEFAULT_SEVERITY = Severity.WARNING_NORMAL.getName();

    private String regexp = StringUtils.EMPTY;
    private String message = StringUtils.EMPTY;
    private String severity = DEFAULT_SEVERITY;
    private String includePattern = StringUtils.EMPTY;
    private String excludePattern = StringUtils.EMPTY;

    /**
     * Returns the Ant file-set pattern of files to scan.
     *
     * @return Ant file-set pattern of files to scan
     */
    public String getIncludePattern() {
        return includePattern;
    }

    /**
     * Sets the Ant file-set pattern of files to scan.
     *
     * @param includePattern
     *         Ant file-set pattern of files to scan
     */
    @DataBoundSetter
    public void setIncludePattern(final String includePattern) {
        this.includePattern = includePattern;
    }

    /**
     * Returns the Ant file-set pattern of files to exclude from scanning.
     *
     * @return Ant file-set pattern of files to exclude
     */
    public String getExcludePattern() {
        return excludePattern;
    }

    /**
     * Sets the Ant file-set pattern of files to exclude from scanning.
     *
     * @param excludePattern
     *         Ant file-set pattern of files to exclude
     */
    @DataBoundSetter
    public void setExcludePattern(final String excludePattern) {
        this.excludePattern = excludePattern;
    }

    /**
     * Returns the regular expression used to match lines.
     *
     * @return the regular expression
     */
    public String getRegexp() {
        return regexp;
    }

    /**
     * Sets the regular expression used to match lines.
     *
     * @param regexp
     *         the regular expression
     */
    @DataBoundSetter
    public void setRegexp(final String regexp) {
        this.regexp = regexp;
    }

    /**
     * Returns the optional message template for matched issues. When blank, the matched line is used as-is.
     *
     * @return the message template (may be empty)
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets an optional message template for matched issues.
     *
     * @param message
     *         the message template
     */
    @DataBoundSetter
    public void setMessage(final String message) {
        this.message = message;
    }

    /**
     * Returns the severity to assign to matched issues.
     *
     * @return the severity name (one of {@link Severity#WARNING_HIGH}, {@link Severity#WARNING_NORMAL},
     *         {@link Severity#WARNING_LOW})
     */
    public String getSeverity() {
        return severity;
    }

    /**
     * Sets the severity to assign to matched issues.
     *
     * @param severity
     *         the severity name
     */
    @DataBoundSetter
    public void setSeverity(final String severity) {
        this.severity = Severity.valueOf(severity, Severity.WARNING_NORMAL).getName();
    }

    /**
     * Creates a new instance of {@link GrepParser}.
     */
    @DataBoundConstructor
    public GrepParser() {
        super();
        // empty constructor required for Stapler
    }

    @Override
    public Report scan(final Run<?, ?> run, final FilePath workspace, final Charset sourceCodeEncoding,
            final LogHandler logger) {
        try {
            var report = workspace.act(
                    new AgentGrepScanner(regexp, severity, message, includePattern, excludePattern,
                            sourceCodeEncoding.name()));
            report.setOrigin(getActualId(), getActualName());
            return report;
        }
        catch (IOException e) {
            var report = new Report();
            report.logException(e, "Exception while scanning files for grep pattern '%s':", regexp);
            return report;
        }
        catch (InterruptedException e) {
            throw new ParsingCanceledException(e);
        }
    }

    /** Label provider with customised messages. */
    private static class LabelProvider extends SymbolIconLabelProvider {
        LabelProvider() {
            super(ID, Messages.Warnings_GrepParser_Name(), i -> StringUtils.EMPTY,
                    "symbol-search plugin-ionicons-api");
        }

        @Override
        public String getLinkName() {
            return Messages.Warnings_GrepParser_LinkName();
        }

        @Override
        public String getTrendName() {
            return Messages.Warnings_GrepParser_TrendName();
        }
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("grepParser")
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
            return Messages.Warnings_GrepParser_Name();
        }

        @Override
        public StaticAnalysisLabelProvider getLabelProvider() {
            return new LabelProvider();
        }

        /**
         * Performs on-the-fly validation on the ant pattern for included files.
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
         * Performs on-the-fly validation on the ant pattern for excluded files.
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
         * Performs on-the-fly validation on the regular expression.
         *
         * @param project
         *         the project that is configured
         * @param regexp
         *         the regular expression to validate
         *
         * @return the validation result
         */
        @POST
        public FormValidation doCheckRegexp(@AncestorInPath final BuildableItem project,
                @QueryParameter final String regexp) {
            if (!JENKINS.hasPermission(Item.CONFIGURE, project)) {
                return FormValidation.ok();
            }
            return checkRegexp(regexp);
        }

        FormValidation checkRegexp(final String regexp) {
            if (StringUtils.isBlank(regexp)) {
                return FormValidation.error(Messages.Warnings_GrepParser_Error_Regexp_isEmpty());
            }
            var scanner = new GrepScanner(regexp, Severity.WARNING_NORMAL, StringUtils.EMPTY);
            if (scanner.isInvalidPattern()) {
                return FormValidation.error(scanner.getErrorMessage());
            }
            return FormValidation.ok();
        }

        /**
         * Returns the available severity levels for the drop-down in the UI.
         *
         * @return a list-box model with all predefined severity options
         */
        public ListBoxModel doFillSeverityItems() {
            var items = new ListBoxModel();
            items.add(Messages.Warnings_GrepParser_Severity_High(), Severity.WARNING_HIGH.getName());
            items.add(Messages.Warnings_GrepParser_Severity_Normal(), Severity.WARNING_NORMAL.getName());
            items.add(Messages.Warnings_GrepParser_Severity_Low(), Severity.WARNING_LOW.getName());
            return items;
        }

        /**
         * Validates the example text against the configured regexp (live preview in the UI).
         *
         * @param project
         *         the project that is configured
         * @param example
         *         example text to scan
         * @param regexp
         *         the regular expression to match
         * @param severity
         *         the severity to assign to matches
         *
         * @return validation result
         */
        @POST
        public FormValidation doCheckExample(@AncestorInPath final BuildableItem project,
                @QueryParameter final String example,
                @QueryParameter final String regexp,
                @QueryParameter final String severity) {
            if (StringUtils.isEmpty(example) || !JENKINS.hasPermission(Item.CONFIGURE, project)) {
                return FormValidation.ok();
            }

            var scanner = new GrepScanner(regexp,
                    Severity.valueOf(severity, Severity.WARNING_NORMAL), StringUtils.EMPTY);
            if (scanner.isInvalidPattern()) {
                return FormValidation.error(scanner.getErrorMessage());
            }

            try (var reader = new BufferedReader(new StringReader(example));
                 var issueBuilder = new edu.hm.hafner.analysis.IssueBuilder()) {
                issueBuilder.setFileName("UI example");
                var matches = scanner.scanLines(reader.lines().iterator(), issueBuilder);
                if (matches.isEmpty()) {
                    return FormValidation.warning(Messages.Warnings_GrepParser_Validation_NoMatch());
                }
                else if (matches.size() == 1) {
                    return FormValidation.ok(Messages.Warnings_GrepParser_Validation_OneMatch(
                            matches.get(0).getMessage()));
                }
                else {
                    return FormValidation.ok(Messages.Warnings_GrepParser_Validation_MultipleMatches(
                            matches.size()));
                }
            }
            catch (IOException e) {
                return FormValidation.error(e.getMessage()); // should never happen
            }
        }
    }
}

package io.jenkins.plugins.analysis.core.filter;

import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Report.IssueFilterBuilder;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.io.Serial;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.verb.POST;
import hudson.Extension;
import hudson.model.BuildableItem;
import hudson.model.Item;
import hudson.util.FormValidation;

import io.jenkins.plugins.util.JenkinsFacade;

/**
 * A compound suppression filter that excludes issues where <em>both</em> the file name and the warning message
 * match their respective regular expressions. This is equivalent to a single entry in Buildbot's
 * {@code suppressionFile}, where each rule specifies a filename pattern and a message pattern — and an issue
 * is suppressed only when it satisfies both conditions simultaneously.
 *
 * @author Akash Manna
 * @see <a href="https://github.com/jenkinsci/warnings-ng-plugin/issues/3051">JENKINS-65553</a>
 */
public class SuppressionFilter extends RegexpFilter {
    @Serial
    private static final long serialVersionUID = 2847319283746293847L;

    private String messagePattern;

    /**
     * Creates a new instance of {@link SuppressionFilter}.
     *
     * @param filePattern
     *         the regular expression matched against the file name; may be blank (matches all files)
     */
    @DataBoundConstructor
    public SuppressionFilter(final String filePattern) {
        super(filePattern);
    }

    /**
     * Returns the regular expression matched against the warning message.
     *
     * @return the message regular expression, or blank if not set
     */
    public String getMessagePattern() {
        return StringUtils.defaultString(messagePattern);
    }

    /**
     * Sets the regular expression matched against the warning message.
     *
     * @param messagePattern
     *         the message regular expression; may be blank (matches all messages)
     */
    @DataBoundSetter
    public void setMessagePattern(final String messagePattern) {
        this.messagePattern = messagePattern;
    }

    @Override
    public boolean isActive() {
        return StringUtils.isNotBlank(getPattern()) || StringUtils.isNotBlank(getMessagePattern());
    }

    /**
     * Returns a compound predicate that keeps an issue unless <em>both</em> the file name matches
     * {@link #getPattern()} <em>and</em> the message matches {@link #getMessagePattern()}.
     *
     * @return a predicate that returns {@code true} for issues that should be <em>kept</em>
     */
    @Override
    public Predicate<Issue> getFilterPredicate() {
        var filePatternStr = getPattern();
        var msgPatternStr = getMessagePattern();

        var fileRegex = StringUtils.isNotBlank(filePatternStr)
                ? Pattern.compile(filePatternStr, Pattern.DOTALL) : null;
        var messageRegex = StringUtils.isNotBlank(msgPatternStr)
                ? Pattern.compile(msgPatternStr, Pattern.DOTALL) : null;

        return issue -> {
            boolean fileMatches = fileRegex == null || fileRegex.matcher(issue.getFileName()).find();
            boolean messageMatches = messageRegex == null || messageRegex.matcher(issue.getMessage()).find();
            return !(fileMatches && messageMatches);
        };
    }

    /**
     * Not used — compound filtering is handled via {@link #getFilterPredicate()}.
     *
     * @param builder
     *         the issue filter builder (unused)
     */
    @Override
    public void apply(final IssueFilterBuilder builder) {
        // Intentionally empty: this filter uses getFilterPredicate() instead.
    }

    /**
     * Descriptor for {@link SuppressionFilter}.
     */
    @Extension
    @org.jenkinsci.Symbol("suppress")
    public static class DescriptorImpl extends RegexpFilterDescriptor {
        private final JenkinsFacade jenkinsFacade;

        /** Creates a new descriptor. */
        public DescriptorImpl() {
            this(new JenkinsFacade());
        }

        DescriptorImpl(final JenkinsFacade jenkinsFacade) {
            super(jenkinsFacade);
            this.jenkinsFacade = jenkinsFacade;
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Filter_Suppress();
        }

        /**
         * Validates the message pattern.
         *
         * @param project
         *         the project that is configured
         * @param messagePattern
         *         the pattern to check
         *
         * @return the validation result
         */
        @POST
        public FormValidation doCheckMessagePattern(@AncestorInPath final BuildableItem project,
                @QueryParameter final String messagePattern) {
            if (!jenkinsFacade.hasPermission(Item.CONFIGURE, project)) {
                return FormValidation.ok();
            }
            if (StringUtils.isBlank(messagePattern)) {
                return FormValidation.ok(Messages.pattern_blank());
            }
            try {
                Pattern.compile(messagePattern);
                return FormValidation.ok();
            }
            catch (java.util.regex.PatternSyntaxException exception) {
                return FormValidation.error(Messages.pattern_error(exception.getLocalizedMessage()));
            }
        }
    }
}

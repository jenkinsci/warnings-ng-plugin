package io.jenkins.plugins.analysis.core.filter;

import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Report.IssueFilterBuilder;
import edu.hm.hafner.util.Ensure;
import edu.hm.hafner.util.VisibleForTesting;

import java.io.Serial;
import java.io.Serializable;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.verb.POST;
import hudson.model.AbstractDescribableImpl;
import hudson.model.BuildableItem;
import hudson.model.Descriptor;
import hudson.model.Item;
import hudson.util.FormValidation;

import io.jenkins.plugins.util.JenkinsFacade;

/**
 * Defines a filter criteria based on a regular expression for {@link Report}.
 *
 * @author Ullrich Hafner
 */
public abstract class RegexpFilter extends AbstractDescribableImpl<RegexpFilter> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1892735849628260157L;

    private final String pattern;

    /**
     * Creates a new instance of {@link RegexpFilter}.
     *
     * @param pattern
     *         the regular expression of the filter
     */
    RegexpFilter(final String pattern) {
        super();

        this.pattern = pattern;
    }

    /**
     * Returns the regular expression of the filter.
     *
     * @return the regular expression of the filter
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * Applies the filter on the specified builder.
     *
     * @param builder
     *         the issue filter builder
     */
    public abstract void apply(IssueFilterBuilder builder);

    /** Descriptor for a filter. */
    public abstract static class RegexpFilterDescriptor extends Descriptor<RegexpFilter> {
        private final JenkinsFacade jenkinsFacade;

        /**
         * Creates a new {@link RegexpFilterDescriptor}.
         */
        protected RegexpFilterDescriptor() {
            this(new JenkinsFacade());
        }

        @VisibleForTesting
        RegexpFilterDescriptor(final JenkinsFacade jenkinsFacade) {
            super();

            this.jenkinsFacade = jenkinsFacade;
        }

        /**
         * Performs on-the-fly validation of the regexp pattern.
         *
         * @param project
         *         the project that is configured
         * @param pattern
         *         the pattern to check
         *
         * @return the validation result
         */
        @POST
        public FormValidation doCheckPattern(@AncestorInPath final BuildableItem project,
                @QueryParameter final String pattern) {
            if (!jenkinsFacade.hasPermission(Item.CONFIGURE, project)) {
                return FormValidation.ok();
            }

            try {
                if (StringUtils.isBlank(pattern)) {
                    return FormValidation.ok(Messages.pattern_blank());
                }
                Pattern compiled = Pattern.compile(pattern);
                Ensure.that(compiled).isNotNull();

                return FormValidation.ok();
            }
            catch (PatternSyntaxException exception) {
                return FormValidation.error(Messages.pattern_error(exception.getLocalizedMessage()));
            }
        }
    }
}

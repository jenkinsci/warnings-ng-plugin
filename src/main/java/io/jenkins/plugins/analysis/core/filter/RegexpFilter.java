package io.jenkins.plugins.analysis.core.filter;

import java.io.Serializable;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Report.IssueFilterBuilder;
import edu.hm.hafner.util.Ensure;

import org.kohsuke.stapler.QueryParameter;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;

/**
 * Defines a filter criteria based on a regular expression for {@link Report}.
 *
 * @author Ullrich Hafner
 */
public abstract class RegexpFilter extends AbstractDescribableImpl<RegexpFilter> implements Serializable {
    private static final long serialVersionUID = 1892735849628260157L;

    private final String pattern;

    /**
     * Creates a new instance of {@link RegexpFilter}.
     *
     * @param pattern
     *         the regular expression of the filter
     */
    public RegexpFilter(final String pattern) {
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

    public abstract static class RegexpFilterDescriptor extends Descriptor<RegexpFilter> {
        /**
         * Performs on-the-fly validation on threshold for high warnings.
         *
         * @param pattern
         *         the pattern to check
         *
         * @return the validation result
         */
        public FormValidation doCheckPattern(@QueryParameter final String pattern) {
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
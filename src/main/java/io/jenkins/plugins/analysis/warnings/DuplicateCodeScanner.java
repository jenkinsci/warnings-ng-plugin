package io.jenkins.plugins.analysis.warnings;

import java.io.Serializable;
import java.util.List;

import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.parser.dry.DuplicationGroup;
import static hudson.plugins.warnings.WarningsDescriptor.*;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;
import static j2html.TagCreator.*;
import net.sf.json.JSONArray;

import hudson.util.FormValidation;

/**
 * Provides settings for duplicate code scanners.
 *
 * @author Ullrich Hafner
 */
public abstract class DuplicateCodeScanner extends StaticAnalysisTool {
    private static final long serialVersionUID = -8446643146836067375L;
    private static final String SMALL_ICON_URL = IMAGE_PREFIX + "dry-24x24.png";
    private static final String LARGE_ICON_URL = IMAGE_PREFIX + "dry-48x48.png";

    /** Validates the thresholds user input. */
    private static final ThresholdValidation THRESHOLD_VALIDATION = new ThresholdValidation();

    private int highThreshold = 50;
    private int normalThreshold = 25;

    /**
     * Returns the minimum number of duplicate lines for high priority warnings.
     *
     * @return the minimum number of duplicate lines for high priority warnings
     */
    public int getHighThreshold() {
        return THRESHOLD_VALIDATION.getHighThreshold(normalThreshold, highThreshold);
    }

    /**
     * Sets the minimum number of duplicate lines for high priority warnings.
     *
     * @param highThreshold
     *         the number of lines for priority high
     */
    @DataBoundSetter
    public void setHighThreshold(final int highThreshold) {
        this.highThreshold = highThreshold;
    }

    /**
     * Returns the minimum number of duplicate lines for normal warnings.
     *
     * @return the minimum number of duplicate lines for normal warnings
     */
    public int getNormalThreshold() {
        return THRESHOLD_VALIDATION.getNormalThreshold(normalThreshold, highThreshold);
    }

    /**
     * Sets the minimum number of duplicate lines for normal priority warnings.
     *
     * @param normalThreshold
     *         the number of lines for priority normal
     */
    @DataBoundSetter
    public void setNormalThreshold(final int normalThreshold) {
        this.normalThreshold = normalThreshold;
    }

    /** Provides icons for DRY parsers. */
    static class DryLabelProvider extends StaticAnalysisLabelProvider {
        protected DryLabelProvider(final String id) {
            super(id, Messages.Warnings_Dry_Name());
        }

        @Override
        public String getSmallIconUrl() {
            return SMALL_ICON_URL;
        }

        @Override
        public String getLargeIconUrl() {
            return LARGE_ICON_URL;
        }

        @Override
        public String getDescription(final Issue issue) {
            Serializable properties = issue.getAdditionalProperties();
            if (properties instanceof DuplicationGroup) {
                return pre().with(code(((DuplicationGroup) properties).getCodeFragment())).render();
            }
            else {
                return super.getDescription(issue);
            }
        }

        /**
         * Returns a JSON array that contains the column values for this issue.
         *
         * @param ageBuilder
         *         the builder to compute the age of a build
         *
         * @return the columns of this issue
         */
        @Override
        protected JSONArray toJson(final Issue issue, final AgeBuilder ageBuilder) {
            JSONArray columns = new JSONArray();
            columns.add(formatDetails(issue));
            columns.add(formatFileName(issue));
            columns.add(formatSeverity(issue.getSeverity()));
            columns.add(issue.getLineEnd() - issue.getLineStart() + 1);
            columns.add(formatTargets(issue));
            columns.add(formatAge(issue, ageBuilder));
            return columns;
        }

        private String formatTargets(final Issue issue) {
            Serializable properties = issue.getAdditionalProperties();
            if (properties instanceof DuplicationGroup) {
                List<Issue> duplications = ((DuplicationGroup) properties).getDuplications();
                duplications.remove(issue); // do not show reference to this issue

                return ul(each(duplications, link -> li(a()
                                .withHref(String.format("source.%s/#%d", link.getId(), link.getLineStart()))
                                .withText(String.format("%s:%s", FILE_NAME_FORMATTER.apply(link), link.getLineStart()))
                        ))
                ).render();
            }
            return "-";
        }

        @Override
        public int[] getTableWidths() {
            return new int[]{1, 2, 1, 1, 3, 1};
        }

        @Override
        public String[] getTableHeaders() {
            return new String[]{
                    Messages.DRY_Table_Column_Details(),
                    Messages.DRY_Table_Column_File(),
                    Messages.DRY_Table_Column_Priority(),
                    Messages.DRY_Table_Column_LinesCount(),
                    Messages.DRY_Table_Column_DuplicatedIn(),
                    Messages.DRY_Table_Column_Age()
            };
        }
    }

    /** Descriptor for this static analysis tool. */
    abstract static class DryDescriptor extends StaticAnalysisToolDescriptor {
        private static final ThresholdValidation VALIDATION = new ThresholdValidation();

        /**
         * Creates the descriptor instance.
         *
         * @param id
         *         ID of the tool
         */
        protected DryDescriptor(final String id) {
            super(id);
        }

        /**
         * Performs on-the-fly validation on threshold for high warnings.
         *
         * @param highThreshold
         *         the threshold for high warnings
         * @param normalThreshold
         *         the threshold for normal warnings
         *
         * @return the validation result
         */
        public FormValidation doCheckHighThreshold(@QueryParameter final int highThreshold,
                @QueryParameter final int normalThreshold) {
            return VALIDATION.validateHigh(highThreshold, normalThreshold);
        }

        /**
         * Performs on-the-fly validation on threshold for normal warnings.
         *
         * @param highThreshold
         *         the threshold for high warnings
         * @param normalThreshold
         *         the threshold for normal warnings
         *
         * @return the validation result
         */
        public FormValidation doCheckNormalThreshold(@QueryParameter final int highThreshold,
                @QueryParameter final int normalThreshold) {
            return VALIDATION.validateNormal(highThreshold, normalThreshold);
        }
    }

    /**
     * Validates the number of lines thresholds.
     */
    @SuppressWarnings("ParameterHidesMemberVariable")
    public static class ThresholdValidation {
        /** Minimum number of duplicate lines for a warning with priority high. */
        static final int DEFAULT_HIGH_THRESHOLD = 50;
        /** Minimum number of duplicate lines for a warning with priority normal. */
        static final int DEFAULT_NORMAL_THRESHOLD = 25;

        /**
         * Performs on-the-fly validation on threshold for high warnings.
         *
         * @param highThreshold
         *         the threshold for high warnings
         * @param normalThreshold
         *         the threshold for normal warnings
         *
         * @return the validation result
         */
        public FormValidation validateHigh(final int highThreshold, final int normalThreshold) {
            return validate(highThreshold, normalThreshold, Messages.DRY_ValidationError_HighThreshold());
        }

        /**
         * Performs on-the-fly validation on threshold for normal warnings.
         *
         * @param highThreshold
         *         the threshold for high warnings
         * @param normalThreshold
         *         the threshold for normal warnings
         *
         * @return the validation result
         */
        public FormValidation validateNormal(final int highThreshold, final int normalThreshold) {
            return validate(highThreshold, normalThreshold, Messages.DRY_ValidationError_NormalThreshold());
        }

        /**
         * Performs on-the-fly validation on thresholds for high and normal warnings.
         *
         * @param high
         *         the threshold for high warnings
         * @param normal
         *         the threshold for normal warnings
         * @param message
         *         the validation message
         *
         * @return the validation result
         */
        private FormValidation validate(final int high, final int normal,
                final String message) {
            if (isValid(normal, high)) {
                return FormValidation.ok();
            }
            return FormValidation.error(message);
        }

        /**
         * Returns the minimum number of duplicate lines for a warning with priority high.
         *
         * @param normalThreshold
         *         the normal threshold
         * @param highThreshold
         *         the high threshold
         *
         * @return the minimum number of duplicate lines for a warning with priority high
         */
        public int getHighThreshold(final int normalThreshold, final int highThreshold) {
            if (!isValid(normalThreshold, highThreshold)) {
                return DEFAULT_HIGH_THRESHOLD;
            }
            return highThreshold;
        }

        private boolean isValid(final int normalThreshold, final int highThreshold) {
            return !(highThreshold <= 0 || normalThreshold <= 0 || highThreshold <= normalThreshold);
        }

        /**
         * Returns the minimum number of duplicate lines for a warning with priority normal.
         *
         * @param normalThreshold
         *         the normal threshold
         * @param highThreshold
         *         the high threshold
         *
         * @return the minimum number of duplicate lines for a warning with priority normal
         */
        public int getNormalThreshold(final int normalThreshold, final int highThreshold) {
            if (!isValid(normalThreshold, highThreshold)) {
                return DEFAULT_NORMAL_THRESHOLD;
            }
            return normalThreshold;
        }
    }
}

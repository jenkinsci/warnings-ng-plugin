package io.jenkins.plugins.analysis.warnings;

import java.util.List;

import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import static hudson.plugins.warnings.WarningsDescriptor.*;
import static io.jenkins.plugins.analysis.core.views.IssuesDetail.*;
import static j2html.TagCreator.*;
import net.sf.json.JSONArray;

import hudson.util.FormValidation;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.parser.dry.CodeDuplication;

/**
 * Provides settings for duplicate code scanners.
 *
 * @author Ullrich Hafner
 */
public abstract class DuplicateCodeScanner extends StaticAnalysisTool {
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
        protected DryLabelProvider(final String id, final String name) {
            super(id, name);
        }

        @Override
        public String getSmallIconUrl() {
            return SMALL_ICON_URL;
        }

        @Override
        public String getLargeIconUrl() {
            return LARGE_ICON_URL;
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
            columns.add(formatPriority(issue.getPriority()));
            columns.add(issue.getLineEnd() - issue.getLineStart() + 1);
            columns.add(formatTargets(issue));
            columns.add(formatAge(issue, ageBuilder));
            return columns;
        }

        private String formatTargets(final Issue issue) {
            if (issue instanceof CodeDuplication) {
                List<CodeDuplication> duplications = ((CodeDuplication) issue).getDuplications();
                return ul(each(duplications, link -> li(a()
                                .withHref(String.format("source.%s/#%d", link.getId(), link.getLineStart()))
                                .withText(String.format("%s:%s", FILE_NAME_FORMATTER.apply(link.getFileName()),
                                        link.getLineStart()))
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
                    io.jenkins.plugins.analysis.core.model.Messages.Table_Column_Details(),
                    io.jenkins.plugins.analysis.core.model.Messages.Table_Column_File(),
                    io.jenkins.plugins.analysis.core.model.Messages.Table_Column_Priority(),
                    Messages.DRY_Table_Column_LinesCount(),
                    Messages.DRY_Table_Column_DuplicatedIn(),
                    io.jenkins.plugins.analysis.core.model.Messages.Table_Column_Age()
            };
        }
    }

    /** Descriptor for this static analysis tool. */
    abstract static class DryDescriptor extends StaticAnalysisToolDescriptor {
        private static final ThresholdValidation VALIDATION = new ThresholdValidation();

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
        public FormValidation doCheckHighThreshold(@QueryParameter final String highThreshold,
                @QueryParameter final String normalThreshold) {
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
        public FormValidation doCheckNormalThreshold(@QueryParameter final String highThreshold,
                @QueryParameter final String normalThreshold) {
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
        public FormValidation validateHigh(final String highThreshold, final String normalThreshold) {
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
        public FormValidation validateNormal(final String highThreshold, final String normalThreshold) {
            return validate(highThreshold, normalThreshold, Messages.DRY_ValidationError_NormalThreshold());
        }

        /**
         * Performs on-the-fly validation on thresholds for high and normal warnings.
         *
         * @param highThreshold
         *         the threshold for high warnings
         * @param normalThreshold
         *         the threshold for normal warnings
         * @param message
         *         the validation message
         *
         * @return the validation result
         */
        private FormValidation validate(final String highThreshold, final String normalThreshold,
                final String message) {
            try {
                int high = Integer.parseInt(highThreshold);
                int normal = Integer.parseInt(normalThreshold);
                if (isValid(normal, high)) {
                    return FormValidation.ok();
                }
            }
            catch (NumberFormatException ignored) {
                // ignore and return failure
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

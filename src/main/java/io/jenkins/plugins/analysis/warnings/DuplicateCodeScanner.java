package io.jenkins.plugins.analysis.warnings;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.parser.dry.DuplicationGroup;
import io.jenkins.plugins.analysis.core.model.DescriptionProvider;
import io.jenkins.plugins.analysis.core.model.DetailsTableModel;
import io.jenkins.plugins.analysis.core.model.FileNameRenderer;
import io.jenkins.plugins.analysis.core.model.IconLabelProvider;
import io.jenkins.plugins.analysis.core.model.ReportScanningTool;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider.AgeBuilder;
import static j2html.TagCreator.*;

import hudson.model.Run;
import hudson.util.FormValidation;

/**
 * Provides settings for duplicate code scanners.
 *
 * @author Ullrich Hafner
 */
public abstract class DuplicateCodeScanner extends ReportScanningTool {
    private static final long serialVersionUID = -8446643146836067375L;

    /** Validates the thresholds user input. */
    private static final ThresholdValidation THRESHOLD_VALIDATION = new ThresholdValidation();

    private int highThreshold = 50;
    private int normalThreshold = 25;

    /**
     * Returns the minimum number of duplicate lines for high severity warnings.
     *
     * @return the minimum number of duplicate lines for high severity warnings
     */
    public int getHighThreshold() {
        return THRESHOLD_VALIDATION.getHighThreshold(normalThreshold, highThreshold);
    }

    /**
     * Sets the minimum number of duplicate lines for high severity warnings.
     *
     * @param highThreshold
     *         the number of lines for severity high
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
     * Sets the minimum number of duplicate lines for normal severity warnings.
     *
     * @param normalThreshold
     *         the number of lines for severity normal
     */
    @DataBoundSetter
    public void setNormalThreshold(final int normalThreshold) {
        this.normalThreshold = normalThreshold;
    }

    /** Provides icons for DRY parsers. */
    static class DryLabelProvider extends IconLabelProvider {
        protected DryLabelProvider(final String id, final String name) {
            super(id, name, "dry");
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

        @Override
        public DetailsTableModel getIssuesModel(final Run<?, ?> build, final String url) {
            return new DryTableModel(getAgeBuilder(build, url), getFileNameRenderer(build), this);
        }
   }

    /** Descriptor for this static analysis tool. */
    abstract static class DryDescriptor extends ReportScanningToolDescriptor {
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
        // TODO: see JENKINS-50355
        public FormValidation doCheckHighThreshold(@QueryParameter final int highThreshold,
                @QueryParameter final int normalThreshold) {
//            return VALIDATION.validateHigh(highThreshold, normalThreshold);
            return FormValidation.ok();
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
        // TODO: see JENKINS-50355
        public FormValidation doCheckNormalThreshold(@QueryParameter final int highThreshold,
                @QueryParameter final int normalThreshold) {
//            return VALIDATION.validateNormal(highThreshold, normalThreshold);
            return FormValidation.ok();
        }
    }

    /**
     * Validates the number of lines thresholds.
     */
    @SuppressWarnings("ParameterHidesMemberVariable")
    static class ThresholdValidation {
        /** Minimum number of duplicate lines for a warning with severity high. */
        static final int DEFAULT_HIGH_THRESHOLD = 50;
        /** Minimum number of duplicate lines for a warning with severity normal. */
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
        private FormValidation validate(final int high, final int normal, final String message) {
            if (isValid(normal, high)) {
                return FormValidation.ok();
            }
            return FormValidation.error(message);
        }

        /**
         * Returns the minimum number of duplicate lines for a warning with severity high.
         *
         * @param normalThreshold
         *         the normal threshold
         * @param highThreshold
         *         the high threshold
         *
         * @return the minimum number of duplicate lines for a warning with severity high
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
         * Returns the minimum number of duplicate lines for a warning with severity normal.
         *
         * @param normalThreshold
         *         the normal threshold
         * @param highThreshold
         *         the high threshold
         *
         * @return the minimum number of duplicate lines for a warning with severity normal
         */
        public int getNormalThreshold(final int normalThreshold, final int highThreshold) {
            if (!isValid(normalThreshold, highThreshold)) {
                return DEFAULT_NORMAL_THRESHOLD;
            }
            return normalThreshold;
        }
    }

    /**
     * Provides a table that contains the duplication references as well.
     */
    static class DryTableModel extends DetailsTableModel {
        DryTableModel(final AgeBuilder ageBuilder,
                final FileNameRenderer fileNameRenderer,
                final DescriptionProvider descriptionProvider) {
            super(ageBuilder, fileNameRenderer, descriptionProvider);
        }

        @Override
        public List<Integer> getWidths(final Report report) {
            List<Integer> widths = new ArrayList<>();
            widths.add(1);
            widths.add(2);
            if (report.hasPackages()) {
                widths.add(2);
            }
            widths.add(1);
            widths.add(1);
            widths.add(3);
            widths.add(1);
            return widths;
        }

        @Override
        public List<String> getHeaders(final Report report) {
            List<String> headers = new ArrayList<>();
            headers.add(Messages.DRY_Table_Column_Details());
            headers.add(Messages.DRY_Table_Column_File());
            if (report.hasPackages()) {
                headers.add(Messages.DRY_Table_Column_Package());
            }
            headers.add(Messages.DRY_Table_Column_Severity());
            headers.add(Messages.DRY_Table_Column_LinesCount());
            headers.add(Messages.DRY_Table_Column_DuplicatedIn());
            headers.add(Messages.DRY_Table_Column_Age());
            return headers;
        }

        @Override
        protected List<String> getRow(final Report report, final Issue issue,
                final String description) {
            List<String> columns = new ArrayList<>();
            columns.add(formatDetails(issue, description));
            columns.add(formatFileName(issue));
            if (report.hasPackages()) {
                columns.add(formatProperty("packageName", issue.getPackageName()));
            }
            columns.add(formatSeverity(issue.getSeverity()));
            columns.add(String.valueOf(issue.getLineEnd() - issue.getLineStart() + 1));
            columns.add(formatTargets(issue));
            columns.add(formatAge(issue));
            return columns;
        }

        private String formatTargets(final Issue issue) {
            Serializable properties = issue.getAdditionalProperties();
            if (properties instanceof DuplicationGroup) {
                List<Issue> duplications = ((DuplicationGroup) properties).getDuplications();
                duplications.remove(issue); // do not show reference to this issue

                return ul(each(duplications, link -> li(formatFileName(link)))).render();
            }
            return "-";
        }
    }
}

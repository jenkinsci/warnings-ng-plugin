package io.jenkins.plugins.analysis.warnings;

import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.analysis.DuplicationGroup;
import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.registry.ParserDescriptor.Option;
import edu.hm.hafner.util.VisibleForTesting;

import j2html.tags.UnescapedText;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.verb.POST;
import hudson.model.BuildableItem;
import hudson.model.Item;
import hudson.model.Run;
import hudson.util.FormValidation;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;
import io.jenkins.plugins.analysis.core.model.DescriptionProvider;
import io.jenkins.plugins.analysis.core.model.DetailsTableModel;
import io.jenkins.plugins.analysis.core.model.FileNameRenderer;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider.AgeBuilder;
import io.jenkins.plugins.analysis.core.model.SymbolIconLabelProvider;
import io.jenkins.plugins.datatables.TableColumn;
import io.jenkins.plugins.datatables.TableColumn.ColumnBuilder;
import io.jenkins.plugins.datatables.TableColumn.ColumnCss;
import io.jenkins.plugins.prism.Sanitizer;
import io.jenkins.plugins.util.JenkinsFacade;

import static edu.hm.hafner.analysis.registry.DryDescriptor.*;
import static io.jenkins.plugins.analysis.warnings.DuplicateCodeScanner.DryLabelProvider.*;
import static j2html.TagCreator.*;

/**
 * Provides settings for duplicate code scanners.
 *
 * @author Ullrich Hafner
 */
@SuppressWarnings({"PMD.DataClass", "PMD.ExcessiveImports"})
public abstract class DuplicateCodeScanner extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = -8446643146836067375L;

    /** Validates the thresholds user input. */
    private static final ThresholdValidation THRESHOLD_VALIDATION = new ThresholdValidation();

    private int highThreshold = 50;
    private int normalThreshold = 25;

    @Override
    protected Option[] configureOptions() {
        return new Option[]{
                new Option(HIGH_OPTION_KEY, String.valueOf(getHighThreshold())),
                new Option(NORMAL_OPTION_KEY, String.valueOf(getNormalThreshold()))};
    }

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
    static class DryLabelProvider extends SymbolIconLabelProvider {
        private static final Sanitizer SANITIZER = new Sanitizer();

        protected DryLabelProvider(final String id, final String name) {
            super(id, name, EMPTY_DESCRIPTION, "symbol-regular/clone plugin-font-awesome-api");
        }

        @Override
        public String getDescription(final Issue issue) {
            Serializable properties = issue.getAdditionalProperties();
            if (properties instanceof DuplicationGroup) {
                return pre().with(new UnescapedText(getCodeFragment((DuplicationGroup) properties))).renderFormatted();
            }
            else {
                return super.getDescription(issue);
            }
        }

        private String getCodeFragment(final DuplicationGroup duplicationGroup) {
            return SANITIZER.render(code(duplicationGroup.getCodeFragment()));
        }

        @Override
        public String getSourceCodeDescription(final Run<?, ?> build, final Issue issue) {
            return formatTargets(getFileNameRenderer(build), issue, "../");
        }

        @Override
        public DetailsTableModel getIssuesModel(final Run<?, ?> build, final String url,
                final Report report) {
            return new DryModel(report, getFileNameRenderer(build), getAgeBuilder(build, url), this);
        }

        static String formatTargets(final FileNameRenderer fileNameRenderer, final Issue issue) {
            return formatTargets(fileNameRenderer, issue, StringUtils.EMPTY);
        }

        static String formatTargets(final FileNameRenderer fileNameRenderer, final Issue issue, final String prefix) {
            Serializable properties = issue.getAdditionalProperties();
            if (properties instanceof DuplicationGroup) {
                List<Issue> duplications = ((DuplicationGroup) properties).getDuplications();
                duplications.remove(issue); // do not show reference to this issue

                return ul(
                        each(duplications, link -> li(fileNameRenderer.createAffectedFileLink(link, prefix)))).render();
            }
            return "-";
        }

        @Override
        public String getTrendName() {
            return Messages.DRY_Trend_Name(getName());
        }

        @Override
        public String getLinkName() {
            return Messages.DRY_Link_Name(getName());
        }
    }

    /** Descriptor for this static analysis tool. */
    abstract static class DuplicateCodeDescriptor extends AnalysisModelParserDescriptor {
        private static final JenkinsFacade JENKINS = new JenkinsFacade();
        private static final ThresholdValidation VALIDATION = new ThresholdValidation();

        /**
         * Creates the descriptor instance.
         *
         * @param id
         *         ID of the tool
         */
        DuplicateCodeDescriptor(final String id) {
            super(id);
        }

        /**
         * Performs on-the-fly validation of the threshold for high warnings.
         *
         * @param project
         *         the project that is configured
         * @param highThreshold
         *         the threshold for high warnings
         * @param normalThreshold
         *         the threshold for normal warnings
         *
         * @return the validation result
         */
        @POST
        public FormValidation doCheckHighThreshold(@AncestorInPath final BuildableItem project,
                @QueryParameter("highThreshold") final int highThreshold,
                @QueryParameter("normalThreshold") final int normalThreshold) {
            if (!JENKINS.hasPermission(Item.CONFIGURE, project)) {
                return FormValidation.ok();
            }
            return VALIDATION.validateHigh(highThreshold, normalThreshold);
        }

        /**
         * Performs on-the-fly validation of the threshold for normal warnings.
         *
         * @param project
         *         the project that is configured
         * @param highThreshold
         *         the threshold for high warnings
         * @param normalThreshold
         *         the threshold for normal warnings
         *
         * @return the validation result
         */
        @POST
        public FormValidation doCheckNormalThreshold(@AncestorInPath final BuildableItem project,
                @QueryParameter("highThreshold") final int highThreshold,
                @QueryParameter("normalThreshold") final int normalThreshold) {
            if (!JENKINS.hasPermission(Item.CONFIGURE, project)) {
                return FormValidation.ok();
            }
            return VALIDATION.validateNormal(highThreshold, normalThreshold);
        }
    }

    /**
     * Validates the number of lines thresholds.
     */
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

        @SuppressWarnings("ConditionCoveredByFurtherCondition")
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
    public static class DryModel extends DetailsTableModel {
        DryModel(final Report report, final FileNameRenderer fileNameRenderer, final AgeBuilder ageBuilder,
                final DescriptionProvider descriptionProvider) {
            super(report, fileNameRenderer, ageBuilder, descriptionProvider, new JenkinsFacade());
        }

        @VisibleForTesting
        DryModel(final Report report, final FileNameRenderer fileNameRenderer, final AgeBuilder ageBuilder,
                final DescriptionProvider descriptionProvider, final JenkinsFacade jenkinsFacade) {
            super(report, fileNameRenderer, ageBuilder, descriptionProvider, jenkinsFacade);
        }

        @Override
        public String getId() {
            return "issues";
        }

        @Override
        public List<TableColumn> getColumns() {
            List<TableColumn> columns = new ArrayList<>();

            columns.add(createDetailsColumn());
            columns.add(createFileColumn());
            if (getReport().hasPackages()) {
                columns.add(createPackageColumn());
            }
            TableColumn severity = new ColumnBuilder().withHeaderLabel(Messages.DRY_Table_Column_Severity())
                    .withDataPropertyKey("severity")
                    .withResponsivePriority(100)
                    .build();
            columns.add(severity);
            TableColumn linesCount = new ColumnBuilder().withHeaderLabel(Messages.DRY_Table_Column_LinesCount())
                    .withDataPropertyKey("linesCount")
                    .withResponsivePriority(5)
                    .withHeaderClass(ColumnCss.NUMBER)
                    .build();
            columns.add(linesCount);
            TableColumn duplicatedIn = new ColumnBuilder().withHeaderLabel(Messages.DRY_Table_Column_DuplicatedIn())
                    .withDataPropertyKey("duplicatedIn")
                    .withResponsivePriority(50)
                    .build();
            columns.add(duplicatedIn);
            columns.add(createAgeColumn());
            return columns;
        }

        @Override
        public DuplicationRow getRow(final Issue issue) {
            DuplicationRow row = new DuplicationRow(getAgeBuilder(), getFileNameRenderer(), getDescriptionProvider(),
                    issue, getJenkinsFacade());
            row.setPackageName(issue);
            row.setSeverity(issue);
            row.setLinesCount(String.valueOf(issue.getLineEnd() - issue.getLineStart() + 1));
            row.setDuplicatedIn(formatTargets(getFileNameRenderer(), issue));
            return row;
        }

        /**
         * A table row that shows the properties of a code duplication.
         */
        @SuppressWarnings("PMD.DataClass") // Used to automatically convert to JSON object
        public static class DuplicationRow extends TableRow {
            private String packageName;
            private String severity;
            private String linesCount;
            private String duplicatedIn;

            DuplicationRow(final AgeBuilder ageBuilder, final FileNameRenderer fileNameRenderer,
                    final DescriptionProvider descriptionProvider,
                    final Issue issue, final JenkinsFacade jenkinsFacade) {
                super(ageBuilder, fileNameRenderer, descriptionProvider, issue, jenkinsFacade);
            }

            public String getPackageName() {
                return packageName;
            }

            public String getSeverity() {
                return severity;
            }

            public String getLinesCount() {
                return linesCount;
            }

            public String getDuplicatedIn() {
                return duplicatedIn;
            }

            void setPackageName(final Issue issue) {
                packageName = formatProperty("packageName", issue.getPackageName());
            }

            void setLinesCount(final String linesCount) {
                this.linesCount = linesCount;
            }

            void setDuplicatedIn(final String duplicatedIn) {
                this.duplicatedIn = duplicatedIn;
            }

            void setSeverity(final Issue issue) {
                severity = formatSeverity(issue.getSeverity());
            }
        }
    }
}

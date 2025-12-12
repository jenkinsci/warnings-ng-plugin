package io.jenkins.plugins.analysis.core.model;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.util.VisibleForTesting;

import java.util.ArrayList;
import java.util.List;

import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider.AgeBuilder;
import io.jenkins.plugins.datatables.TableColumn;
import io.jenkins.plugins.datatables.TableColumn.ColumnBuilder;
import io.jenkins.plugins.util.JenkinsFacade;

/**
 * Provides the dynamic model for the details table that shows the issue properties.
 *
 * <p>
 *     This issues model consists of the following columns:
 * </p>
 * <ul>
 * <li>issue details (message and description)</li>
 * <li>file name</li>
 * <li>package name (if there are multiple packages)</li>
 * <li>category (if there are multiple categories)</li>
 * <li>type (if there are multiple types)</li>
 * <li>severity</li>
 * <li>age</li>
 * </ul>
 *
 * @author Ullrich Hafner
 */
public class IssuesModel extends DetailsTableModel {
    IssuesModel(final Report report, final FileNameRenderer fileNameRenderer, final AgeBuilder ageBuilder,
            final DescriptionProvider descriptionProvider) {
        this(report, fileNameRenderer, ageBuilder, descriptionProvider, new JenkinsFacade());
    }

    @VisibleForTesting
    IssuesModel(final Report report, final FileNameRenderer fileNameRenderer, final AgeBuilder ageBuilder,
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
        if (getReport().hasCategories()) {
            var category = new ColumnBuilder().withHeaderLabel(Messages.Table_Column_Category())
                    .withDataPropertyKey("category")
                    .withResponsivePriority(100)
                    .build();
            columns.add(category);
        }
        if (getReport().hasTypes()) {
            var type = new ColumnBuilder().withHeaderLabel(Messages.Table_Column_Type())
                    .withDataPropertyKey("type")
                    .withResponsivePriority(1000)
                    .build();
            columns.add(type);
        }
        columns.add(createSeverityColumn());
        columns.add(createAgeColumn());
        columns.add(createHiddenDetailsColumn());
        return columns;
    }

    @Override
    public IssuesRow getRow(final Issue issue) {
        var row = new IssuesRow(getAgeBuilder(), getFileNameRenderer(), getDescriptionProvider(),
                issue, getJenkinsFacade());
        row.setPackageName(issue);
        row.setCategory(issue);
        row.setType(issue);
        row.setSeverity(issue);
        return row;
    }

    /**
     * A table row that shows the properties of an issue.
     */
    @SuppressWarnings("PMD.DataClass") // Used to automatically convert to JSON object
    public static class IssuesRow extends TableRow {
        private String packageName;
        private String category;
        private String type;
        private String severity;

        IssuesRow(final AgeBuilder ageBuilder, final FileNameRenderer fileNameRenderer,
                final DescriptionProvider descriptionProvider, final Issue issue, final JenkinsFacade jenkinsFacade) {
            super(ageBuilder, fileNameRenderer, descriptionProvider, issue, jenkinsFacade);
        }

        public String getPackageName() {
            return packageName;
        }

        public String getCategory() {
            return category;
        }

        public String getType() {
            return type;
        }

        public String getSeverity() {
            return severity;
        }

        void setPackageName(final Issue issue) {
            packageName = formatProperty("packageName", issue.getPackageName());
        }

        void setCategory(final Issue issue) {
            category = formatPropertyWithUrl("category", issue.getCategory(), issue);
        }

        void setType(final Issue issue) {
            type = formatProperty("type", issue.getType());
        }

        void setSeverity(final Issue issue) {
            severity = formatSeverity(issue.getSeverity());
        }
    }
}

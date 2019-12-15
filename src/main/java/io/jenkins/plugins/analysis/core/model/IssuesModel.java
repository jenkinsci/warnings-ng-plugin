package io.jenkins.plugins.analysis.core.model;

import java.util.ArrayList;
import java.util.List;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Report;

import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider.AgeBuilder;
import io.jenkins.plugins.datatables.TableColumn;

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
        super(report, fileNameRenderer, ageBuilder, descriptionProvider);
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
            columns.add(new TableColumn(Messages.Table_Column_Package(), "packageName").setWidth(2));
        }
        if (getReport().hasCategories()) {
            columns.add(new TableColumn(Messages.Table_Column_Category(), "category"));
        }
        if (getReport().hasTypes()) {
            columns.add(new TableColumn(Messages.Table_Column_Type(), "type"));
        }
        columns.add(createSeverityColumn());
        columns.add(createAgeColumn());
        return columns;
    }

    @Override
    public IssuesRow getRow(final Issue issue) {
        IssuesRow row = new IssuesRow(getAgeBuilder(), getFileNameRenderer(), getDescriptionProvider(), issue);
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
                final DescriptionProvider descriptionProvider, final Issue issue) {
            super(ageBuilder, fileNameRenderer, descriptionProvider, issue);
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
            category = formatProperty("category", issue.getCategory());
        }

        void setType(final Issue issue) {
            type = formatProperty("type", issue.getType());
        }

        void setSeverity(final Issue issue) {
            severity = formatSeverity(issue.getSeverity());
        }
    }
}

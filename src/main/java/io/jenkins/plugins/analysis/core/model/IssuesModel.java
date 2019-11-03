package io.jenkins.plugins.analysis.core.model;

import java.util.ArrayList;
import java.util.List;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Report;

import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider.AgeBuilder;

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
    public List<String> getHeaders() {
        List<String> visibleColumns = new ArrayList<>();
        visibleColumns.add(Messages.Table_Column_Details());
        visibleColumns.add(Messages.Table_Column_File());
        if (getReport().hasPackages()) {
            visibleColumns.add(Messages.Table_Column_Package());
        }
        if (getReport().hasCategories()) {
            visibleColumns.add(Messages.Table_Column_Category());
        }
        if (getReport().hasTypes()) {
            visibleColumns.add(Messages.Table_Column_Type());
        }
        visibleColumns.add(Messages.Table_Column_Severity());
        visibleColumns.add(Messages.Table_Column_Age());
        return visibleColumns;
    }

    @Override
    public List<Integer> getWidths() {
        List<Integer> widths = new ArrayList<>();
        widths.add(1);
        widths.add(1);
        if (getReport().hasPackages()) {
            widths.add(2);
        }
        if (getReport().hasCategories()) {
            widths.add(1);
        }
        if (getReport().hasTypes()) {
            widths.add(1);
        }
        widths.add(1);
        widths.add(1);
        return widths;
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

    @Override
    public void configureColumns(final ColumnDefinitionBuilder builder) {
        builder.add("description").add("fileName", "string");
        if (getReport().hasPackages()) {
            builder.add("packageName");
        }
        if (getReport().hasCategories()) {
            builder.add("category");
        }
        if (getReport().hasTypes()) {
            builder.add("type");
        }
        builder.add("severity").add("age");
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

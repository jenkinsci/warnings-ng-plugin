package io.jenkins.plugins.analysis.core.model;

import java.util.ArrayList;
import java.util.Arrays;
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
    IssuesModel(final AgeBuilder ageBuilder, final FileNameRenderer fileNameRenderer,
            final DescriptionProvider descriptionProvider) {
        super(ageBuilder, fileNameRenderer, descriptionProvider);
    }

    @Override
    public List<String> getHeaders(final Report report) {
        List<String> visibleColumns = new ArrayList<>();
        visibleColumns.add(Messages.Table_Column_Details());
        visibleColumns.add(Messages.Table_Column_File());
        if (report.hasPackages()) {
            visibleColumns.add(Messages.Table_Column_Package());
        }
        if (report.hasCategories()) {
            visibleColumns.add(Messages.Table_Column_Category());
        }
        if (report.hasTypes()) {
            visibleColumns.add(Messages.Table_Column_Type());
        }
        visibleColumns.add(Messages.Table_Column_Severity());
        visibleColumns.add(Messages.Table_Column_Age());
        return visibleColumns;
    }

    @Override
    public List<String> getHeaderClasses(final Report report) {
        return Arrays.asList(
                ANY_HEADER_CLASS,
                ANY_HEADER_CLASS,
                ANY_HEADER_CLASS,
                ANY_HEADER_CLASS,
                ANY_HEADER_CLASS,
                ANY_HEADER_CLASS,
                ANY_HEADER_CLASS);
    }

    @Override
    public List<Integer> getWidths(final Report report) {
        List<Integer> widths = new ArrayList<>();
        widths.add(1);
        widths.add(1);
        if (report.hasPackages()) {
            widths.add(2);
        }
        if (report.hasCategories()) {
            widths.add(1);
        }
        if (report.hasTypes()) {
            widths.add(1);
        }
        widths.add(1);
        widths.add(1);
        return widths;
    }

    @Override
    public IssuesRow getRow(final Report report, final Issue issue) {
        IssuesRow row = new IssuesRow(getAgeBuilder(), getFileNameRenderer(), getDescriptionProvider(), issue);
        row.setPackageName(issue);
        row.setCategory(issue);
        row.setType(issue);
        row.setSeverity(issue);
        return row;
    }

    @Override
    public void configureColumns(final ColumnDefinitionBuilder builder,  final Report report) {
        builder.add("description").add("fileName", "string");
        if (report.hasPackages()) {
            builder.add("packageName");
        }
        if (report.hasCategories()) {
            builder.add("category");
        }
        if (report.hasTypes()) {
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

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
    /**
     * Creates a new instance of {@link IssuesModel}.
     *
     * @param ageBuilder
     *         renders the age column
     * @param fileNameRenderer
     *         renders the file name column
     * @param descriptionProvider
     *         renders the description text
     */
    public IssuesModel(final AgeBuilder ageBuilder, final FileNameRenderer fileNameRenderer,
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

    /**
     * Returns an JSON array that represents the columns of the issues table.
     *
     * @param report
     *         the report to show in the table
     * @param issue
     *         the issue to get the column properties for
     * @param description
     *         description of the issue
     *
     * @return the columns of one row
     */
    @Override
    public IssuesRow getRow(final Report report, final Issue issue, final String description) {
        IssuesRow row = new IssuesRow();
        row.setDescription(formatDetails(issue, description));
        row.setFileName(formatFileName(issue));
        row.setPackageName(formatProperty("packageName", issue.getPackageName()));
        row.setCategory(formatProperty("category", issue.getCategory()));
        row.setType(formatProperty("type", issue.getType()));
        row.setSeverity(formatSeverity(issue.getSeverity()));
        row.setAge(formatAge(issue));
        return row;
    }

    @Override
    public String getColumnsDefinition(final Report report) {
        StringBuilder builder = new StringBuilder("[");
        builder.append("{\"data\": \"description\"},");
        builder.append("{\"data\": \"fileName\"},");
        if (report.hasPackages()) {
            builder.append("{\"data\": \"packageName\"},");
        }
        if (report.hasCategories()) {
            builder.append("{\"data\": \"category\"},");
        }
        if (report.hasTypes()) {
            builder.append("{\"data\": \"type\"},");
        }
        builder.append("{\"data\": \"severity\"},");
        builder.append("{\"data\": \"age\"}");
        builder.append("]");
        return builder.toString();
    }

    public static class IssuesRow {
        private String description;
        private String fileName;
        private String packageName;
        private String category;
        private String type;
        private String severity;
        private String age;

        public String getDescription() {
            return description;
        }

        public String getFileName() {
            return fileName;
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

        public String getAge() {
            return age;
        }

        void setDescription(final String description) {
            this.description = description;
        }

        void setFileName(final String fileName) {
            this.fileName = fileName;
        }

        void setAge(final String age) {
            this.age = age;
        }

        void setPackageName(final String packageName) {
            this.packageName = packageName;
        }

        void setCategory(final String category) {
            this.category = category;
        }

        void setType(final String type) {
            this.type = type;
        }

        void setSeverity(final String severity) {
            this.severity = severity;
        }
    }
}

package io.jenkins.plugins.analysis.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Severity;

import j2html.tags.UnescapedText;

import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider.AgeBuilder;
import io.jenkins.plugins.analysis.core.util.LocalizedSeverity;
import io.jenkins.plugins.analysis.core.util.Sanitizer;

import static edu.hm.hafner.util.IntegerParser.*;
import static j2html.TagCreator.*;

/**
 * Provides the model for the issues details table. The model consists of the following parts:
 *
 * <ul>
 * <li>header name for each column</li>
 * <li>column types for each column (via header class)</li>
 * <li>width for each column</li>
 * <li>content for each row</li>
 * <li>content for whole table</li>
 * </ul>
 *
 * @author Ullrich Hafner
 */
public abstract class DetailsTableModel {
    private final AgeBuilder ageBuilder;
    private final FileNameRenderer fileNameRenderer;
    private final DescriptionProvider descriptionProvider;

    /**
     * An unspecified column.
     */
    protected static final String ANY_HEADER_CLASS = "";
    /**
     * A column with a date in it.
     */
    protected static final String DATE_HEADER_CLASS = "date";

    /**
     * Creates a new instance of {@link DetailsTableModel}.
     *
     * @param ageBuilder
     *         renders the age column
     * @param fileNameRenderer
     *         renders the file name column
     * @param descriptionProvider
     *         renders the description text
     */
    protected DetailsTableModel(final AgeBuilder ageBuilder, final FileNameRenderer fileNameRenderer,
            final DescriptionProvider descriptionProvider) {
        this.ageBuilder = ageBuilder;
        this.fileNameRenderer = fileNameRenderer;
        this.descriptionProvider = descriptionProvider;
    }

    protected AgeBuilder getAgeBuilder() {
        return ageBuilder;
    }

    protected FileNameRenderer getFileNameRenderer() {
        return fileNameRenderer;
    }

    protected DescriptionProvider getDescriptionProvider() {
        return descriptionProvider;
    }

    /**
     * Returns the table headers of the report table.
     *
     * @param report
     *         the report to show
     *
     * @return the table headers
     */
    @SuppressWarnings("unused") // called by Jelly view
    public abstract List<String> getHeaders(Report report);

    /**
     * Returns the column definitions of the report table.
     *
     * @param report
     *         the report to show
     *
     * @return the table headers
     */
    @SuppressWarnings("unused") // called by Jelly view
    public final String getColumnsDefinition(final Report report) {
        ColumnDefinitionBuilder builder = new ColumnDefinitionBuilder();
        configureColumns(builder, report);
        return builder.toString();
    }

    /**
     * Returns the classes for all column headers.
     *
     * @param report
     *         the report to show
     *
     * @return the table header classes
     */
    @SuppressWarnings("unused") //called by Jelly view
    public abstract List<String> getHeaderClasses(Report report);

    /**
     * Configures the columns of the report table.
     *
     * @param builder
     *         the columns definition builder
     * @param report
     *         the report to show
     */
    @SuppressWarnings("unused") // called by Jelly view
    protected abstract void configureColumns(ColumnDefinitionBuilder builder, Report report);

    /**
     * Returns the widths of the table headers of the report table.
     *
     * @param report
     *         the report to show
     *
     * @return the width of the table headers
     */
    @SuppressWarnings("unused") // called by Jelly view
    public abstract List<Integer> getWidths(Report report);

    /**
     * Converts the specified set of issues into a table.
     *
     * @param report
     *         the report to show in the table
     *
     * @return the table as String
     */
    public List<Object> getContent(final Report report) {
        List<Object> rows = new ArrayList<>();
        for (Issue issue : report) {
            rows.add(getRow(report, issue));
        }
        return rows;
    }

    /**
     * Returns a table row for the specified issue.
     *
     * @param report
     *         the report to show
     * @param issue
     *         the issue to show in the row
     *
     * @return a table row for the issue
     */
    public abstract TableRow getRow(Report report, Issue issue);

    /**
     * Base class for table rows. Contains columns that should be used by all tables.
     */
    @SuppressWarnings("PMD.DataClass") // Used to automatically convert to JSON object
    public static class TableRow {
        private static final Sanitizer SANITIZER = new Sanitizer();

        private final String description;
        private final DetailedColumnDefinition fileName;
        private final String age;

        /**
         * Creates a new {@link TableRow}.
         *
         * @param ageBuilder
         *         renders the age column
         * @param fileNameRenderer
         *         renders the file name column
         * @param descriptionProvider
         *         renders the description text
         * @param issue
         *         the issue to show in the row
         */
        protected TableRow(final AgeBuilder ageBuilder,
                final FileNameRenderer fileNameRenderer,
                final DescriptionProvider descriptionProvider, final Issue issue) {
            description = formatDetails(issue, descriptionProvider.getDescription(issue));
            age = ageBuilder.apply(parseInt(issue.getReference()));
            fileName = createFileName(fileNameRenderer, issue);
        }

        private DetailedColumnDefinition createFileName(final FileNameRenderer fileNameRenderer, final Issue issue) {
            return new DetailedColumnDefinition(fileNameRenderer.renderAffectedFileLink(issue),
                    String.format("%s:%07d", issue.getFileName(), issue.getLineStart()));
        }

        /**
         * Formats the text of the details column. The details column is not directly shown, it rather is a hidden
         * element that is expanded if the corresponding button is selected. The actual text value is stored in the
         * {@code data-description} attribute.
         *
         * @param issue
         *         the issue in a table row
         * @param additionalDescription
         *         additional description of the issue
         *
         * @return the formatted column
         */
        private String formatDetails(final Issue issue, final String additionalDescription) {
            UnescapedText details;
            if (StringUtils.isBlank(issue.getMessage())) {
                details = new UnescapedText(additionalDescription);
            }
            else {
                details = join(p(strong().with(new UnescapedText(issue.getMessage()))), additionalDescription);
            }
            return div().withClass("details-control").attr("data-description", render(details)).render();
        }

        /**
         * Formats the text of the severity column.
         *
         * @param severity
         *         the severity of the issue
         *
         * @return the formatted column
         */
        protected String formatSeverity(final Severity severity) {
            return String.format("<a href=\"%s\">%s</a>",
                    severity.getName(), LocalizedSeverity.getLocalizedString(severity));
        }

        /**
         * Formats the text of the specified property column. T he text actually is a link to the UI representation of
         * the property.
         *
         * @param property
         *         the property to format
         * @param value
         *         the value of the property
         *
         * @return the formatted column
         */
        protected String formatProperty(final String property, final String value) {
            return String.format("<a href=\"%s.%d/\">%s</a>", property, value.hashCode(), render(value));
        }

        /**
         * Renders the specified HTML code. Removes unsafe HTML constructs.
         *
         * @param text
         *         the HTML to render
         *
         * @return safe HTML
         */
        protected final String render(final UnescapedText text) {
            return SANITIZER.render(text);
        }

        /**
         * Renders the specified HTML code. Removes unsafe HTML constructs.
         *
         * @param html
         *         the HTML to render
         *
         * @return safe HTML
         */
        protected final String render(final String html) {
            return SANITIZER.render(html);
        }

        public String getDescription() {
            return description;
        }

        public DetailedColumnDefinition getFileName() {
            return fileName;
        }

        public String getAge() {
            return age;
        }

    }

    /**
     * A JQuery DataTables column definition builder. Provides simple columns that extract a given entity property as
     * column value or complex columns that provide different properties to sort and display a column.
     */
    public static class ColumnDefinitionBuilder {
        private final StringJoiner columns = new StringJoiner(",", "[", "]");

        /**
         * Adds a new simple column that maps the specified property of the row entity to the column value.
         *
         * @param dataPropertyName
         *         the property to extract from the entity, it will be shown as column value
         *
         * @return this
         */
        public ColumnDefinitionBuilder add(final String dataPropertyName) {
            columns.add(String.format("{\"data\": \"%s\"}", dataPropertyName));

            return this;
        }

        /**
         * Adds a new complex column that maps the specified property of the row entity to the display and sort
         * attributes of the column. The property {@code dataPropertyName} must be of type {@link
         * DetailedColumnDefinition}.
         *
         * @param dataPropertyName
         *         the property to extract from the entity, it will be shown as column value
         * @param columnDataType
         *         JQuery DataTables data type of the column
         *
         * @return this
         * @see DetailedColumnDefinition
         */
        public ColumnDefinitionBuilder add(final String dataPropertyName, final String columnDataType) {
            columns.add(String.format("{"
                    + "  \"type\": \"%s\","
                    + "  \"data\": \"%s\","
                    + "  \"render\": {"
                    + "     \"_\": \"display\","
                    + "     \"sort\": \"sort\""
                    + "  }"
                    + "}", columnDataType, dataPropertyName));

            return this;
        }

        @Override
        public String toString() {
            return columns.toString();
        }
    }

    /**
     * A column value attribute that provides a {@code display} and {@code sort} property so that a JQuery DataTables
     * can use different properties to sort and display a column.
     */
    public static class DetailedColumnDefinition {
        private final String display;
        private final String sort;

        /**
         * Creates a new {@link DetailedColumnDefinition}.
         *
         * @param display
         *         the entity property that should be used to display the column
         * @param sort
         *         the entity property that should be used to sort the column
         *
         * @see <a href="https://datatables.net/reference/option/columns.type">DataTables Column Types</a>
         */
        public DetailedColumnDefinition(final String display, final String sort) {
            this.display = display;
            this.sort = sort;
        }

        public String getDisplay() {
            return display;
        }

        public String getSort() {
            return sort;
        }
    }
}

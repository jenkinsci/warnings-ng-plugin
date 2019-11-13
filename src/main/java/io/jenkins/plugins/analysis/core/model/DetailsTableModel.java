package io.jenkins.plugins.analysis.core.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Severity;

import j2html.tags.UnescapedText;

import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider.AgeBuilder;
import io.jenkins.plugins.analysis.core.util.LocalizedSeverity;
import io.jenkins.plugins.analysis.core.util.Sanitizer;
import io.jenkins.plugins.datatables.api.TableColumn;
import io.jenkins.plugins.datatables.api.TableModel;

import static edu.hm.hafner.util.IntegerParser.*;
import static j2html.TagCreator.*;

/**
 * Provides the model for the issues details table. The model consists of the following parts:
 *
 * <ul>
 * <li>header name for each column</li>
 * <li>width for each column</li>
 * <li>content for each row</li>
 * <li>content for whole table</li>
 * </ul>
 *
 * @author Ullrich Hafner
 */
public abstract class DetailsTableModel extends TableModel {
    private final AgeBuilder ageBuilder;
    private final FileNameRenderer fileNameRenderer;
    private final DescriptionProvider descriptionProvider;
    private final Report report;

    /**
     * Creates a new instance of {@link DetailsTableModel}.
     *
     * @param report
     *         the report to render
     * @param fileNameRenderer
     *         renders the file name column
     * @param ageBuilder
     *         renders the age column
     * @param descriptionProvider
     *         renders the description text
     */
    protected DetailsTableModel(final Report report, final FileNameRenderer fileNameRenderer,
            final AgeBuilder ageBuilder,
            final DescriptionProvider descriptionProvider) {
        this.report = report;
        this.fileNameRenderer = fileNameRenderer;
        this.ageBuilder = ageBuilder;
        this.descriptionProvider = descriptionProvider;
    }

    protected Report getReport() {
        return report;
    }

    protected FileNameRenderer getFileNameRenderer() {
        return fileNameRenderer;
    }

    protected AgeBuilder getAgeBuilder() {
        return ageBuilder;
    }

    protected DescriptionProvider getDescriptionProvider() {
        return descriptionProvider;
    }

    /**
     * Converts the specified set of issues into a table.
     *
     * @return the table as String
     */
    @Override
    public List<Object> getRows() {
        List<Object> rows = new ArrayList<>();
        for (Issue issue : report) {
            rows.add(getRow(issue));
        }
        return rows;
    }

    protected TableColumn createDetailsColumn() {
        return new TableColumn(Messages.Table_Column_Details(), "description").setHeaderClass("nosort");
    }

    protected TableColumn createFileColumn() {
        return new TableColumn(Messages.Table_Column_File(), "fileName", "string");
    }

    protected TableColumn createAgeColumn() {
        return new TableColumn(Messages.Table_Column_Age(), "age");
    }

    protected TableColumn createSeverityColumn() {
        return new TableColumn(Messages.Table_Column_Severity(), "severity");
    }

    /**
     * Returns a table row for the specified issue.
     *
     * @param issue
     *         the issue to show in the row
     *
     * @return a table row for the issue
     */
    protected abstract TableRow getRow(Issue issue);

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
}

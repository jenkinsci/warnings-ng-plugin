package io.jenkins.plugins.analysis.core.model;

import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Severity;
import edu.hm.hafner.analysis.util.IntegerParser;

import j2html.tags.DomContentJoiner;
import j2html.tags.UnescapedText;
import java.util.ArrayList;
import java.util.List;

import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider.AgeBuilder;
import io.jenkins.plugins.analysis.core.util.LocalizedSeverity;
import io.jenkins.plugins.datatables.DetailedCell;
import io.jenkins.plugins.datatables.TableColumn;
import io.jenkins.plugins.datatables.TableColumn.ColumnBuilder;
import io.jenkins.plugins.datatables.TableColumn.ColumnCss;
import io.jenkins.plugins.datatables.TableColumn.ColumnType;
import io.jenkins.plugins.datatables.TableConfiguration;
import io.jenkins.plugins.datatables.TableModel;
import io.jenkins.plugins.prism.Sanitizer;
import io.jenkins.plugins.util.JenkinsFacade;

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
    private final JenkinsFacade jenkinsFacade;
    private final Report report;

    /**
     * Creates a new instance of {@link DetailsTableModel}.
     *  @param report
     *         the report to render
     * @param fileNameRenderer
     *         renders the file name column
     * @param ageBuilder
     *         renders the age column
     * @param descriptionProvider
     *         renders the description text
     * @param jenkinsFacade
     *         Jenkins facade to replaced with a stub during unit tests
     */
    protected DetailsTableModel(final Report report, final FileNameRenderer fileNameRenderer,
            final AgeBuilder ageBuilder, final DescriptionProvider descriptionProvider,
            final JenkinsFacade jenkinsFacade) {
        super();

        this.report = report;
        this.fileNameRenderer = fileNameRenderer;
        this.ageBuilder = ageBuilder;
        this.descriptionProvider = descriptionProvider;
        this.jenkinsFacade = jenkinsFacade;
    }

    @Override
    public TableConfiguration getTableConfiguration() {
        var tableConfiguration = new TableConfiguration();
        tableConfiguration.responsive();
        return tableConfiguration;
    }

    protected JenkinsFacade getJenkinsFacade() {
        return jenkinsFacade;
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
        return new ColumnBuilder().withHeaderLabel(Messages.Table_Column_Details())
                .withDataPropertyKey("description")
                .withResponsivePriority(1)
                .withHeaderClass(ColumnCss.NO_SORT)
                .build();
    }

    protected TableColumn createHiddenDetailsColumn() {
        return new ColumnBuilder().withHeaderLabel("Hiddendetails")
                .withDataPropertyKey("message")
                .withHeaderClass(ColumnCss.HIDDEN)
                .build();
    }

    protected TableColumn createFileColumn() {
        return new ColumnBuilder().withHeaderLabel(Messages.Table_Column_File())
                .withDataPropertyKey("fileName")
                .withResponsivePriority(1)
                .withDetailedCell()
                .build();
    }

    protected TableColumn createAgeColumn() {
        return new ColumnBuilder().withHeaderLabel(Messages.Table_Column_Age())
                .withDataPropertyKey("age")
                .withType(ColumnType.HTML_NUMBER)
                .withResponsivePriority(10)
                .build();
    }

    protected TableColumn createSeverityColumn() {
        return new ColumnBuilder().withHeaderLabel(Messages.Table_Column_Severity())
                .withDataPropertyKey("severity")
                .withResponsivePriority(5)
                .build();
    }

    protected TableColumn createPackageColumn() {
        return new ColumnBuilder().withHeaderLabel(Messages.Table_Column_Package())
                .withDataPropertyKey("packageName")
                .withResponsivePriority(50_000)
                .build();
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
        private final String message;
        private final DetailedCell<String> fileName;
        private final String age;
        private final JenkinsFacade jenkinsFacade;

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
         * @param jenkinsFacade
         *         Jenkins facade to be replaced with a stub during unit tests
         */
        protected TableRow(final AgeBuilder ageBuilder, final FileNameRenderer fileNameRenderer,
                final DescriptionProvider descriptionProvider, final Issue issue,
                final JenkinsFacade jenkinsFacade) {
            this.jenkinsFacade = jenkinsFacade;
            message = render(issue.getMessage());
            description = formatDetails(issue, descriptionProvider.getDescription(issue));
            age = ageBuilder.apply(IntegerParser.parseInt(issue.getReference()));
            fileName = createFileName(fileNameRenderer, issue);
        }

        private DetailedCell<String> createFileName(final FileNameRenderer fileNameRenderer, final Issue issue) {
            return new DetailedCell<>(fileNameRenderer.renderAffectedFileLink(issue),
                    "%s:%07d".formatted(issue.getFileName(), issue.getLineStart()));
        }

        /**
         * Formats the text of the details' column. The details' column is not directly shown, it rather is a hidden
         * element expanded if the corresponding button is selected. The actual text value is stored in the
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
                details = DomContentJoiner.join(" ", false,
                        p(strong().with(new UnescapedText(issue.getMessage()))), additionalDescription);
            }
            return TableColumn.renderDetailsColumn(render(details), jenkinsFacade);
        }

        /**
         * Formats the text of the severity column.
         *
         * @param severity
         *         the severity of the issue
         *
         * @return the formatted column
         */
        protected final String formatSeverity(final Severity severity) {
            return "<a href=\"%s\">%s</a>".formatted(
                    severity.getName(), LocalizedSeverity.getLocalizedString(severity));
        }

        /**
         * Formats the text of the specified property column. The text actually is a link to the UI representation of
         * the property.
         *
         * @param property
         *         the property to format
         * @param value
         *         the value of the property
         *
         * @return the formatted column
         */
        protected final String formatProperty(final String property, final String value) {
            var renderedValue = render(value);
            if (StringUtils.isBlank(value)) {
                renderedValue = "-";
            }
            return "<a href=\"%s.%d/\">%s</a>".formatted(property, value.hashCode(), renderedValue);
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

        public String getMessage() {
            return message;
        }

        public DetailedCell<String> getFileName() {
            return fileName;
        }

        public String getAge() {
            return age;
        }
    }
}

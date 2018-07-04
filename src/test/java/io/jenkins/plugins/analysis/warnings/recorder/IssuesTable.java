package io.jenkins.plugins.analysis.warnings.recorder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTableBody;
import com.gargoylesoftware.htmlunit.html.HtmlTableCell;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;

import static org.assertj.core.api.Assertions.*;

/**
 * Page Object for a table that shows the issues of a build.
 *
 * @author Ullrich Hafner
 */
public class IssuesTable {
    private final String title;
    private final List<IssueRow> rows = new ArrayList<>();

    /**
     * Creates a new instance of {@link IssuesTable}.
     *
     * @param page
     *         the whole details HTML page
     */
    public IssuesTable(final HtmlPage page) {
        HtmlAnchor content = page.getAnchorByHref("#issuesContent");
        clickOnLink(content);

        title = content.getTextContent();

        DomElement issues = page.getElementById("issues");
        assertThat(issues).isInstanceOf(HtmlTable.class);

        HtmlTable table = (HtmlTable) issues;
        List<HtmlTableRow> tableHeaderRows = table.getHeader().getRows();
        assertThat(tableHeaderRows).hasSize(1);

        HtmlTableRow header = tableHeaderRows.get(0);
        List<HtmlTableCell> cells = header.getCells();
        assertThat(cells).hasSize(7);

        assertThat(getHeaders(cells)).containsExactly(
                "Details", "File", "Package", "Category", "Type", "Priority", "Age");

        List<HtmlTableBody> bodies = table.getBodies();
        assertThat(bodies).hasSize(1);
        
        HtmlTableBody mainBody = bodies.get(0);
        waitForAjaxCall(mainBody);
        List<HtmlTableRow> contentRows = mainBody.getRows();

        for (HtmlTableRow row : contentRows) {
            List<HtmlTableCell> rowCells = row.getCells();
            rows.add(new IssueRow(rowCells));
        }
    }

    private void waitForAjaxCall(final HtmlTableBody body) {
        while ("No data available in table".equals(
                body.getRows().get(0).getCells().get(0).getFirstChild().getTextContent())) {
            System.out.println("Waiting for Ajax call to populate issues table ...");
            body.getPage().getEnclosingWindow().getJobManager().waitForJobs(1000);
        }
    }

    /**
     * Helper-method for clicking on a link.
     *
     * @param element
     *         a {@link DomElement} which will trigger the redirection to a new page.
     *
     * @return the wanted {@link HtmlPage}.
     */
    private HtmlPage clickOnLink(final DomElement element) {
        try {
            return element.click();
        }
        catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    private List<String> getHeaders(final List<HtmlTableCell> cells) {
        return cells.stream().map(HtmlTableCell::getTextContent).collect(Collectors.toList());
    }

    /**
     * Returns the title of the corresponding navigation bar (tab header).
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the table rows.
     *
     * @return the rows
     */
    public List<IssueRow> getRows() {
        return rows;
    }

    /**
     * Simple Java bean that represents an issue row in the issues table.
     */
    public static class IssueRow {
        private static final Pattern WIDTH = Pattern.compile("width:(\\d+)\\.\\d+%");
        private final String fileName;
        private final String packageName;
        private final String category;
        private final String type;
        private final String priority;
        private final int age;

        /**
         * Creates a new row based on the content of a list of three HTML cells.
         *
         * @param columns
         *         the values given as {@link HtmlTableCell}
         */
        public IssueRow(final List<HtmlTableCell> columns) {
            assertThat(columns).hasSize(7);

            fileName = asText(columns, 1);
            packageName = asText(columns, 2);
            category = asText(columns, 3);
            type = asText(columns, 4);
            priority = asText(columns, 5);
            age = asInt(columns, 6);
        }

        int asInt(final List<HtmlTableCell> columns, final int column) {
            return Integer.parseInt(asText(columns, column));
        }

        String asText(final List<HtmlTableCell> columns, final int column) {
            return columns.get(column).getTextContent();
        }

        /**
         * Creates a new row based on the specified properties.
         *
         * @param fileName
         *         the file name
         * @param packageName
         *         the package name
         * @param category
         *         the category
         * @param type
         *         the type
         * @param priority
         *         the priority
         * @param age
         *         the age
         */
        public IssueRow(final String fileName, final String packageName, final String category, final String type,
                final String priority, final int age) {
            this.fileName = fileName;
            this.packageName = packageName;
            this.category = category;
            this.type = type;
            this.priority = priority;
            this.age = age;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            IssueRow issueRow = (IssueRow) o;

            if (age != issueRow.age) {
                return false;
            }
            if (!fileName.equals(issueRow.fileName)) {
                return false;
            }
            if (!packageName.equals(issueRow.packageName)) {
                return false;
            }
            if (!category.equals(issueRow.category)) {
                return false;
            }
            if (!type.equals(issueRow.type)) {
                return false;
            }
            return priority.equals(issueRow.priority);
        }

        @Override
        public int hashCode() {
            int result = fileName.hashCode();
            result = 31 * result + packageName.hashCode();
            result = 31 * result + category.hashCode();
            result = 31 * result + type.hashCode();
            result = 31 * result + priority.hashCode();
            result = 31 * result + age;
            return result;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .append("fileName", fileName)
                    .append("packageName", packageName)
                    .append("category", category)
                    .append("type", type)
                    .append("priority", priority)
                    .append("age", age)
                    .toString();
        }
    }
}

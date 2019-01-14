package io.jenkins.plugins.analysis.warnings.recorder.pageobj;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTableBody;
import com.gargoylesoftware.htmlunit.html.HtmlTableCell;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;

import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import static org.assertj.core.api.Assertions.*;

/**
 * Page Object for a table that shows the issues of a build.
 *
 * @author Ullrich Hafner
 */
public class DuplicationTable {
    private final String title;
    private final List<DuplicationRow> rows = new ArrayList<>();

    /**
     * Creates a new instance of {@link DuplicationTable}.
     *
     * @param page
     *         the whole details HTML page
     * @param hasPackages
     *         determines if the packages column is visible
     */
    @SuppressFBWarnings("BC")
    public DuplicationTable(final HtmlPage page, final boolean hasPackages) {
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
        if (hasPackages) {
            assertThat(cells).hasSize(7);
            assertThat(getHeaders(cells)).containsExactly(
                    "Details", "File", "Package", "Severity", "#Lines", "Duplicated In", "Age");
        }
        else {
            assertThat(cells).hasSize(6);
            assertThat(getHeaders(cells)).containsExactly(
                    "Details", "File", "Severity", "#Lines", "Duplicated In", "Age");
        }

        List<HtmlTableBody> bodies = table.getBodies();
        assertThat(bodies).hasSize(1);

        HtmlTableBody mainBody = bodies.get(0);
        IssuesTable.waitForAjaxCall(mainBody);
        List<HtmlTableRow> contentRows = mainBody.getRows();

        for (HtmlTableRow row : contentRows) {
            List<HtmlTableCell> rowCells = row.getCells();
            rows.add(new DuplicationRow(rowCells, hasPackages));
        }
    }

    /**
     * Helper-method for clicking on a link.
     *
     * @param element
     *         a {@link DomElement} which will trigger the redirection to a new page.
     */
    private static HtmlPage clickOnLink(final DomElement element) {
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
    public List<DuplicationRow> getRows() {
        return rows;
    }

    /**
     * Returns the selected table row.
     *
     * @return the selected row
     */
    public DuplicationRow getRow(final int row) {
        return rows.get(row);
    }

    /**
     * Simple Java bean that represents an issue row in the issues table.
     */
    public static class DuplicationRow {
        private final String fileName;
        private final String packageName;
        private final String priority;
        private final int age;
        private final int lineCount;
        
        @Nullable
        private DomElement fileNameLink;
        @Nullable
        private DomElement priorityLink;

        /**
         * Creates a new row based on the content of a list of three HTML cells.
         *
         * @param columns
         *         the values given as {@link HtmlTableCell}
         * @param hasPackages
         *         determines if the packages column is visible
         */
        public DuplicationRow(final List<HtmlTableCell> columns, final boolean hasPackages) {
            if (hasPackages) {
                assertThat(columns).hasSize(7);
            }
            else {
                assertThat(columns).hasSize(6);
            }
            int column = 1;
            fileNameLink = columns.get(column).getFirstElementChild();
            fileName = asText(columns, column++);
            if (hasPackages) {
                packageName = asText(columns, column++);
            }
            else {
                packageName = "-";
            }
            priorityLink = columns.get(column).getFirstElementChild();
            priority = asText(columns, column++);
            lineCount = asInt(columns, column++);
            column++; // skip links
            age = asInt(columns, column);
        }

        private int asInt(final List<HtmlTableCell> columns, final int column) {
            return Integer.parseInt(asText(columns, column));
        }

        private String asText(final List<HtmlTableCell> columns, final int column) {
            return columns.get(column).getTextContent();
        }

        /**
         * Creates a new row based on the specified properties.
         *
         * @param fileName
         *         the file name
         * @param packageName
         *         the package name
         * @param priority
         *         the priority
         * @param lineCount
         *         the number of duplicated lines
         * @param age
         *         the age
         */
        public DuplicationRow(final String fileName, final String packageName,
                final String priority, final int lineCount, final int age) {
            this.fileName = fileName;
            this.packageName = packageName;
            this.priority = priority;
            this.lineCount = lineCount;
            this.age = age;
        }

        public HtmlPage clickSourceCode() {
            if (fileNameLink == null) {
                throw new AssertionError("No source code column link found");
            }
            else {
                return clickOnLink(fileNameLink);
            }
        }
        
        public HtmlPage clickSeverity() {
            if (priorityLink == null) {
                throw new AssertionError("No severity column link found");
            }
            else {
                return clickOnLink(priorityLink);
            }
        }
        
        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            DuplicationRow that = (DuplicationRow) o;

            if (age != that.age) {
                return false;
            }
            if (lineCount != that.lineCount) {
                return false;
            }
            if (!fileName.equals(that.fileName)) {
                return false;
            }
            if (!packageName.equals(that.packageName)) {
                return false;
            }
            return priority.equals(that.priority);
        }

        @Override
        public int hashCode() {
            int result = fileName.hashCode();
            result = 31 * result + packageName.hashCode();
            result = 31 * result + priority.hashCode();
            result = 31 * result + age;
            result = 31 * result + lineCount;
            return result;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .append("fileName", fileName)
                    .append("packageName", packageName)
                    .append("priority", priority)
                    .append("age", age)
                    .append("lineCount", lineCount)
                    .toString();
        }
    }
}

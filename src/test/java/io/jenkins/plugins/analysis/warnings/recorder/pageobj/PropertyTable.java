package io.jenkins.plugins.analysis.warnings.recorder.pageobj;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTableBody;
import com.gargoylesoftware.htmlunit.html.HtmlTableCell;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import static org.apache.commons.lang3.builder.ToStringStyle.*;
import static org.assertj.core.api.Assertions.*;

/**
 * Page Object for a table that shows the summary and distribution of issues by a given property.
 *
 * @author Ullrich Hafner
 */
public class PropertyTable {
    /**
     * Returns whether the specified property table is visible on the specified page. 
     *
     * @param page
     *         the whole details HTML page
     * @param property
     *         the property table to extract
     * @return {@code true} if the table is visible, {@code false} otherwise
     */
    public static boolean isVisible(final HtmlPage page, final String property) {
        try {
            getTitleOfTable(page, property);
            
            return true;
        }
        catch (ElementNotFoundException e) {
            return false;
        }

    }

    private static String getTitleOfTable(final HtmlPage page, final String property) {
        return page.getAnchorByHref(String.format("#%sContent", property)).getTextContent();
    }

    private final String title;
    private final String propertyName;
    private final List<PropertyRow> rows = new ArrayList<>();

    /**
     * Creates a new instance of {@link PropertyTable}.
     *
     * @param page
     *         the whole details HTML page
     * @param property
     *         the property tab to extract
     */
    @SuppressFBWarnings("BC")
    public PropertyTable(final HtmlPage page, final String property) {
        title = getTitleOfTable(page, property);

        DomElement propertyElement = page.getElementById(property);
        assertThat(propertyElement).isInstanceOf(HtmlTable.class);

        HtmlTable table = (HtmlTable) propertyElement;
        List<HtmlTableRow> tableHeaderRows = table.getHeader().getRows();
        assertThat(tableHeaderRows).hasSize(1);

        HtmlTableRow header = tableHeaderRows.get(0);
        List<HtmlTableCell> cells = header.getCells();
        assertThat(cells).hasSize(3);

        propertyName = cells.get(0).getTextContent();
        assertThat(cells.get(1).getTextContent()).isEqualTo("Total");
        assertThat(cells.get(2).getTextContent()).isEqualTo("Distribution");

        List<HtmlTableBody> bodies = table.getBodies();
        assertThat(bodies).hasSize(1);
        List<HtmlTableRow> contentRows = bodies.get(0).getRows();

        for (HtmlTableRow row : contentRows) {
            List<HtmlTableCell> rowCells = row.getCells();
            rows.add(new PropertyRow(rowCells));
        }
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
     * Returns the column name of the shown property.
     *
     * @return the title
     */
    public String getColumnName() {
        return propertyName;
    }

    /**
     * Returns the table rows.
     *
     * @return the rows
     */
    public List<PropertyRow> getRows() {
        return rows;
    }

    /**
     * Simple Java bean that represents a row in the table. It consists of three columns, the name (value) of the
     * property, the number of warnings for this property and the percentage this size represents.
     */
    public static class PropertyRow {
        private static final Pattern WIDTH = Pattern.compile("width:(\\d+)\\.\\d+%");
        private final int width;
        private final String name;
        private final int size;
        private final boolean ignoreWidth;

        /**
         * Creates a new row based on the content of a list of three HTML cells.
         *
         * @param columns
         *         the values given as {@link HtmlTableCell}
         */
        public PropertyRow(final List<HtmlTableCell> columns) {
            assertThat(columns).hasSize(3);
            name = columns.get(0).getTextContent();
            size = Integer.parseInt(columns.get(1).getTextContent());
            String style = columns.get(2).getFirstElementChild().getFirstElementChild().getAttribute("style");
            Matcher matcher = WIDTH.matcher(style);
            assertThat(matcher.matches()).isTrue();
            width = Integer.parseInt(matcher.group(1));
            ignoreWidth = false;
        }

        private PropertyRow(final String name, final int size, final int width, final boolean ignoreWidth) {
            this.name = name;
            this.size = size;
            this.width = width;
            this.ignoreWidth = ignoreWidth;
        }
        
        /**
         * Creates a new {@link PropertyRow}.
         *
         * @param name
         *         the name of the row
         * @param size
         *         the number of issues in this row
         * @param width
         *         the percentage this size represents
         */
        public PropertyRow(final String name, final int size, final int width) {
            this(name, size, width, false);
        }
        
        /**
         * Creates a new {@link PropertyRow}.
         *
         * @param name
         *         the name of the row
         * @param size
         *         the number of issues in this row
         */
        public PropertyRow(final String name, final int size) {
            this(name, size, 0, true);
        }

        public int getSize() {
            return size;
        }

        @SuppressWarnings("PMD.CollapsibleIfStatements")
        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            PropertyRow that = (PropertyRow) o;

            if (!ignoreWidth && !that.ignoreWidth) {
                if (width != that.width) {
                    return false;
                }
            }
            if (size != that.size) {
                return false;
            }
            return name.equals(that.name);
        }

        @Override
        public int hashCode() {
            int result = width;
            result = 31 * result + name.hashCode();
            result = 31 * result + size;
            return result;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this, NO_CLASS_NAME_STYLE)
                    .append("width", width)
                    .append("name", name)
                    .append("size", size)
                    . toString();
        }
    }
}

package io.jenkins.plugins.analysis.warnings;

import java.util.List;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import edu.umd.cs.findbugs.annotations.CheckForNull;

/**
 * Representation of a table row displaying the duplicate code warnings details.
 *
 * @author Stephan Plöderl
 */
public class DryTableRow extends AbstractSeverityTableRow {
    private static final String DUPLICATED_IN = "Duplicated In";
    private static final String AMOUNT_OF_LINES = "#Lines";

    private final List<String> duplicatedIn;
    private final int lines;

    /**
     * Creates an instance representing a duplicate code warnings table row.
     *
     * @param element
     *         the WebElement representing the row
     * @param table
     *         the issues table in which this row is displayed in
     */
    DryTableRow(final WebElement element, final DryTable table) {
        super(element, table);

        duplicatedIn = getCell(DUPLICATED_IN)
                .findElements(By.tagName("li"))
                .stream()
                .map(WebElement::getText)
                .collect(Collectors.toList());
        lines = Integer.parseInt(getCellContent(AMOUNT_OF_LINES));
    }

    /**
     * Returns the number of duplicated code lines.
     *
     * @return the number of lines
     */
    public int getLines() {
        return lines;
    }

    /**
     * Returns the other files that duplicate the same code fragment.
     *
     * @return the duplications
     */
    public List<String> getDuplicatedIn() {
        return duplicatedIn;
    }

    @Override
    public boolean equals(@CheckForNull final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        DryTableRow that = (DryTableRow) o;

        if (lines != that.lines) {
            return false;
        }
        return duplicatedIn.equals(that.duplicatedIn);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + duplicatedIn.hashCode();
        result = 31 * result + lines;
        return result;
    }
}

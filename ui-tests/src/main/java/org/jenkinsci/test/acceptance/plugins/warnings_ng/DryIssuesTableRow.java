package org.jenkinsci.test.acceptance.plugins.warnings_ng;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Class representing a duplicate code warnings issue table row in the issues table.
 */
public class DryIssuesTableRow extends AbstractNonDetailsIssuesTableRow {
    private static final String DUPLICATED_IN = "Duplicated In";
    private static final String AMOUNT_OF_LINES = "#Lines";

    /**
     * Creates an instance representing a duplicate code warnings table row.
     *
     * @param element
     *         the WebElement representing the row
     * @param issuesTable
     *         the issues table in which this row is displayed in
     */
    DryIssuesTableRow(final WebElement element, final IssuesTable issuesTable) {
        super(element, issuesTable);
    }

    /**
     * Returns the number of duplicated code lines.
     *
     * @return the number of lines
     */
    public int getLines() {
        return Integer.parseInt(getCellContent(AMOUNT_OF_LINES));
    }

    /**
     * Returns the other files that duplicate the same code fragment.
     *
     * @return the duplications
     */
    public List<String> getDuplicatedIn() {
        return getCells().get(getHeaders().indexOf(DUPLICATED_IN))
                .findElements(By.tagName("li"))
                .stream()
                .map(WebElement::getText)
                .collect(Collectors.toList());
    }

    /**
     * Performs a click on a specific link of the detected duplications.
     *
     * @param number
     *         the number of the link which shall be clicked
     *
     * @return the representation of the source code page.
     */
    public SourceView clickOnDuplicatedInLink(final int number) {
        return clickOnLink(findAllLinks(getCell(DUPLICATED_IN)).get(number), SourceView.class);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        DryIssuesTableRow that = (DryIssuesTableRow) o;

        EqualsBuilder builder = new EqualsBuilder();
        builder.append(getLines(), that.getLines())
                .append(getDuplicatedIn(), that.getDuplicatedIn());
        return builder.isEquals();
    }

    @Override
    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder();

        builder.append(getLines())
                .append(getDuplicatedIn());

        return Objects.hash(super.hashCode(), builder.toHashCode());
    }
}

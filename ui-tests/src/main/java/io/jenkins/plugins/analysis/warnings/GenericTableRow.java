package io.jenkins.plugins.analysis.warnings;

/**
 * Abstract representation of a table row of the issues-table.
 *
 * @author Stephan Plöderl
 */
public class GenericTableRow {
    /**
     * Returns this row as an instance of a specific sub class of {@link GenericTableRow}.
     *
     * @param actualClass
     *         the class to which the table row shall be converted to
     * @param <T>
     *         actual type of the row
     *
     * @return the row
     */
    public <T extends GenericTableRow> T getAs(final Class<T> actualClass) {
        return actualClass.cast(this);
    }
}

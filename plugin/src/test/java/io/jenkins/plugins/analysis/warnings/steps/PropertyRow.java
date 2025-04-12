package io.jenkins.plugins.analysis.warnings.steps;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;
import java.util.stream.Collectors;

import io.jenkins.plugins.analysis.core.model.ResultAction;

import static org.apache.commons.lang3.builder.ToStringStyle.*;

/**
 * Simple Java bean that represents a row in the table. It consists of three columns, the name (value) of the property,
 * the number of warnings for this property and the percentage this size represents.
 */
public class PropertyRow {
    /**
     * Returns the rows of the table model.
     *
     * @param result
     *         the whole details HTML page
     * @param property
     *         the property tab to extract
     *
     * @return the rows
     */
    public static List<PropertyRow> getRows(final ResultAction result, final String property) {
        var details = result.getTarget().getDetails(property);
        return details.getKeys().stream()
                .map(key -> new PropertyRow(
                        details.getDisplayName(key),
                        (int) details.getCount(key),
                        (int) (details.getCount(key) * 100 / details.getMax()))).collect(Collectors.toList());
    }

    private final long percentage;
    private final String name;
    private final long total;
    private final boolean ignorePercentage;

    private PropertyRow(final String name, final long total, final long percentage, final boolean ignorePercentage) {
        this.name = name;
        this.total = total;
        this.percentage = percentage;
        this.ignorePercentage = ignorePercentage;
    }

    /**
     * Creates a new {@link PropertyRow}.
     *
     * @param name
     *         the name of the row
     * @param total
     *         the number of issues in this row
     * @param percentage
     *         the percentage this size represents
     */
    public PropertyRow(final String name, final long total, final long percentage) {
        this(name, total, percentage, false);
    }

    /**
     * Creates a new {@link PropertyRow}.
     *
     * @param name
     *         the name of the row
     * @param total
     *         the number of issues in this row
     */
    public PropertyRow(final String name, final long total) {
        this(name, total, 0, true);
    }

    public long getTotal() {
        return total;
    }

    @SuppressWarnings({"PMD.CollapsibleIfStatements", "PMD.SimplifyBooleanReturns"})
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        var that = (PropertyRow) o;

        if (!ignorePercentage && !that.ignorePercentage) {
            if (percentage != that.percentage) {
                return false;
            }
        }
        if (total != that.total) {
            return false;
        }
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        long result = percentage;
        result = 31 * result + name.hashCode();
        result = 31 * result + total;
        return (int) result;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, NO_CLASS_NAME_STYLE)
                .append("width", percentage)
                .append("name", name)
                .append("size", total)
                .toString();
    }
}

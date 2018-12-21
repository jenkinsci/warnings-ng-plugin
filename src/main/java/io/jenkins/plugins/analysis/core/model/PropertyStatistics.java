package io.jenkins.plugins.analysis.core.model;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Severity;
import edu.hm.hafner.util.NoSuchElementException;

/**
 * Groups issue by a specified property, like package name or origin. Provides statistics for this property in order to
 * draw graphs or show the result in tables.
 *
 * @author Ullrich Hafner
 */
public class PropertyStatistics {
    private final Map<String, ? extends Report> issuesByProperty;
    private final Function<String, String> propertyFormatter;
    private final String property;
    private final int total;

    /**
     * Creates a new instance of {@link PropertyStatistics}.
     *
     * @param report
     *         the issues that should be grouped by property
     * @param property
     *         the property to show the details for
     * @param propertyFormatter
     *         the formatter that show the property
     */
    PropertyStatistics(final Report report,
            final String property, final Function<String, String> propertyFormatter) {
        this.property = property;
        this.propertyFormatter = propertyFormatter;
        issuesByProperty = report.groupByProperty(property);
        total = report.size();
    }

    /**
     * Returns the total number of issues.
     *
     * @return total number of issues
     */
    public int getTotal() {
        return total;
    }

    /**
     * Returns the name of this property. E.g., 'package name', 'module name', ect.
     *
     * @return the name
     */
    public String getProperty() {
        return property;
    }

    /**
     * Returns a display name for the specified property instance.
     *
     * @param key
     *         the property instance
     *
     * @return the display name
     */
    public String getDisplayName(final String key) {
        return propertyFormatter.apply(key);
    }

    /**
     * Returns all instances for this property.
     *
     * @return the property instances
     */
    public Set<String> getKeys() {
        return issuesByProperty.keySet();
    }

    /**
     * Returns the maximum number of issues for each property instance.
     *
     * @return the maximum number of issues
     */
    public int getMax() {
        return issuesByProperty.values().stream().mapToInt(Report::size).max().orElse(0);
    }

    /**
     * Returns the maximum number of issues for the specified property instance.
     *
     * @param key
     *         the property instance
     *
     * @return the maximum number of issues
     */
    public long getCount(final String key) {
        return getReportFor(key).size();
    }

    /**
     * Returns the number of issues with severity {@link Severity#ERROR} for the specified property instance.
     *
     * @param key
     *         the property instance
     *
     * @return the number of high severity issues
     */
    public long getErrorsCount(final String key) {
        return getReportFor(key).getSizeOf(Severity.ERROR);
    }

    /**
     * Returns the number of issues with severity {@link Severity#WARNING_HIGH} for the specified property instance.
     *
     * @param key
     *         the property instance
     *
     * @return the number of high severity issues
     */
    public long getHighCount(final String key) {
        return getReportFor(key).getSizeOf(Severity.WARNING_HIGH);
    }

    /**
     * Returns the number of issues with severity {@link Severity#WARNING_NORMAL} for the specified property instance.
     *
     * @param key
     *         the property instance
     *
     * @return the number of normal severity issues
     */
    public long getNormalCount(final String key) {
        return getReportFor(key).getSizeOf(Severity.WARNING_NORMAL);
    }

    /**
     * Returns the number of issues with severity {@link Severity#WARNING_LOW} for the specified property instance.
     *
     * @param key
     *         the property instance
     *
     * @return the number of low severity issues
     */
    public long getLowCount(final String key) {
        return getReportFor(key).getSizeOf(Severity.WARNING_LOW);
    }

    private Report getReportFor(final String key) {
        if (issuesByProperty.containsKey(key)) {
            return issuesByProperty.get(key);
        }
        throw new NoSuchElementException("There is no report for key '%s'", key);
    }
}


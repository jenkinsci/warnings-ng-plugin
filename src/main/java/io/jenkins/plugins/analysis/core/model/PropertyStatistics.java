package io.jenkins.plugins.analysis.core.model;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import edu.hm.hafner.analysis.Issues;
import edu.hm.hafner.analysis.Priority;

/**
 * Groups issue by a specified property, like package name or origin. Provides statistics for this property in order to
 * draw graphs or show the result in tables.
 *
 * @author Ulli Hafner
 */
public class PropertyStatistics {
    private final Map<String, ? extends Issues<?>> issuesByProperty;
    private final Function<String, String> propertyFormatter;
    private final String property;
    private final int total;

    /**
     * Creates a new instance of {@link PropertyStatistics}.
     *
     * @param issues
     *         the issues that should be grouped by property
     * @param property
     *         the property to show the details for
     * @param propertyFormatter
     *         the formatter that show the property
     */
    public PropertyStatistics(final Issues<?> issues,
            final String property, final Function<String, String> propertyFormatter) {
        this.property = property;
        this.propertyFormatter = propertyFormatter;
        issuesByProperty = issues.groupByProperty(property);
        total = issues.size();
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
        return issuesByProperty.values().stream().mapToInt(issues -> issues.size()).max().orElse(0);
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
        return issuesByProperty.get(key).size();
    }

    /**
     * Returns the number of issues with priority {@link Priority#HIGH} for the specified property instance.
     *
     * @param key
     *         the property instance
     *
     * @return the number of high priority issues
     */
    public long getHighCount(final String key) {
        return issuesByProperty.get(key).getHighPrioritySize();
    }

    /**
     * Returns the number of issues with priority {@link Priority#NORMAL} for the specified property instance.
     *
     * @param key
     *         the property instance
     *
     * @return the number of normal priority issues
     */
    public long getNormalCount(final String key) {
        return issuesByProperty.get(key).getNormalPrioritySize();
    }

    /**
     * Returns the number of issues with priority {@link Priority#LOW} for the specified property instance.
     *
     * @param key
     *         the property instance
     *
     * @return the number of low priority issues
     */
    public long getLowCount(final String key) {
        return issuesByProperty.get(key).getLowPrioritySize();
    }
}


package io.jenkins.plugins.analysis.core.views;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.apache.commons.beanutils.PropertyUtils;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Issues;
import io.jenkins.plugins.analysis.core.steps.Messages;

import hudson.model.ModelObject;
import hudson.model.Run;

/**
 * Dynamic tab that shows the issue counts for a specified property.
 *
 * @author Ulli Hafner
 */
public class PropertyCountTab extends IssuesDetail {
    private final Map<String, Long> propertyCount;
    private final Function<String, String> propertyFormatter;
    private final String property;

    /**
     * Creates a new instance of {@link PropertyCountTab}.
     *
     * @param property the property to show the details for
     * @param owner
     *         current build as owner of this action.
     * @param issues
     *         the module to show the details for
     */
    public PropertyCountTab(final Run<?, ?> owner, final Issues issues, final String defaultEncoding,
            final ModelObject parent, final String property, final Function<String, String> propertyFormatter) {
        super(owner, issues, new Issues(), new Issues(), defaultEncoding, parent, Messages._Default_Name());

        this.property = property;
        propertyCount = getIssues().getPropertyCount(getIssueStringFunction(property));
        this.propertyFormatter = propertyFormatter;
    }

    private Function<Issue, String> getIssueStringFunction(final String property) {
        return issue -> {
            try {
                return PropertyUtils.getProperty(issue, property).toString();
            }
            catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException ignored) {
                return property;
            }
        };
    }

    public String getColumnHeader() {
        try {
            return PropertyUtils.getProperty(new TabLabelProvider(getIssues()), property).toString();
        }
        catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            return "Element";
        }
    }

    public String getProperty() {
        return property;
    }

    public long getMax() {
        return Collections.max(propertyCount.values());
    }

    public String getDisplayName(final String key) {
        return propertyFormatter.apply(key);
    }

    public Set<String> getKeys() {
        return propertyCount.keySet();
    }

    public long getCount(final String key) {
        return propertyCount.get(key);
    }
}


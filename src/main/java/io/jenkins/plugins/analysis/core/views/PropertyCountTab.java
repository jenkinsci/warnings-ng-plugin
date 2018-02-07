package io.jenkins.plugins.analysis.core.views;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.apache.commons.beanutils.PropertyUtils;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Issues;
import io.jenkins.plugins.analysis.core.model.BuildIssue;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;

import hudson.model.Run;

/**
 * Dynamic tab that shows the issue counts for a specified property.
 *
 * @author Ulli Hafner
 */
public class PropertyCountTab extends IssuesDetail {
    private final Map<String, Issues<BuildIssue>> issuesByProperty;
    private final Function<String, String> propertyFormatter;
    private final String property;

    /**
     * Creates a new instance of {@link PropertyCountTab}.
     *  @param owner
     *         current build as owner of this action.
     * @param issues
     * @param property
     *         the property to show the details for
     * @param labelProvider
     */
    public PropertyCountTab(final Run<?, ?> owner, final Issues issues, final String defaultEncoding,
            final String property, final Function<String, String> propertyFormatter,
            final StaticAnalysisLabelProvider labelProvider, final String url) {
        super(owner, issues, NO_ISSUES, NO_ISSUES, NO_ISSUES, propertyFormatter.apply(property), url, labelProvider,
                defaultEncoding
        );

        this.property = property;
        this.propertyFormatter = propertyFormatter;

        issuesByProperty = issues.groupByProperty(getIssueStringFunction(property));
    }

    static Function<Issue, String> getIssueStringFunction(final String property) {
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
        Issues issues = getIssues();
        String property = this.property;
        return getColumnHeaderFor(issues, property);
    }

    static String getColumnHeaderFor(final Issues issues, final String property) {
        try {
            return PropertyUtils.getProperty(new TabLabelProvider(issues), property).toString();
        }
        catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException ignored) {
            return "Element";
        }
    }

    public String getProperty() {
        return property;
    }

    public int getMax() {
        return issuesByProperty.values().stream().mapToInt(issues -> issues.size()).max().orElse(0);
    }

    public String getDisplayName(final String key) {
        return propertyFormatter.apply(key);
    }

    @Override
    public String getDisplayName() {
        return getDisplayName(property);
    }

    public Set<String> getKeys() {
        return issuesByProperty.keySet();
    }

    public long getCount(final String key) {
        return issuesByProperty.get(key).size();
    }
    public long getLowCount(final String key) {
        return issuesByProperty.get(key).getLowPrioritySize();
    }
    public long getHighCount(final String key) {
        return issuesByProperty.get(key).getHighPrioritySize();
    }
    public long getNormalCount(final String key) {
        return issuesByProperty.get(key).getNormalPrioritySize();
    }
}


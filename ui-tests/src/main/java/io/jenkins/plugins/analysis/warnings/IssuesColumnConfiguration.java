package io.jenkins.plugins.analysis.warnings;

import org.jenkinsci.test.acceptance.po.AbstractListViewColumn;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.ListView;

/**
 * Configuration page object of a list view column that shows the number of static analysis issues.
 *
 * @author Ullrich Hafner
 */
@Describable("Number of static analysis issues")
public class IssuesColumnConfiguration extends AbstractListViewColumn {
    private final Control name = control("/name");
    private final Control type = control("/type");
    private final Control selectTools = control("/selectTools");
    private final Control tools = control("/tools/id");

    /**
     * Creates a new issue column configuration page object.
     *
     * @param parent
     *         the list view that contains this column
     * @param path
     *         the URL of the view
     */
    public IssuesColumnConfiguration(final ListView parent, final String path) {
        super(parent, path);
    }

    /**
     * Sets the name of the column.
     *
     * @param name
     *         the name to show as column header
     */
    public void setName(final String name) {
        this.name.set(name);
    }

    /**
     * Selects the static analysis tool for which the results should be shown.
     *
     * @param toolId
     *         the ID of the static analysis tool
     */
    public void filterByTool(final String toolId) {
        selectTools.check(true);
        tools.set(toolId);
    }

    /**
     * Disables the filtering by static analysis tool.
     */
    public void disableToolFilter() {
        selectTools.check(false);
    }

    /**
     * Selects the type of the totals to show.
     *
     * @param properties
     *         the property that should be shown
     */
    public void setType(final StatisticProperties properties) {
        this.type.select(properties.getDisplayName());
    }
}

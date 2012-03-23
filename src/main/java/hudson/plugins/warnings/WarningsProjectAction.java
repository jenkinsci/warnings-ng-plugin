package hudson.plugins.warnings;

import hudson.model.AbstractProject;
import hudson.plugins.analysis.core.AbstractProjectAction;
import hudson.plugins.warnings.parser.ParserRegistry;

/**
 * Entry point to visualize the warnings trend graph in the project screen.
 * Drawing of the graph is delegated to the associated
 * {@link WarningsResultAction}.
 *
 * @author Ulli Hafner
 */
public class WarningsProjectAction extends AbstractProjectAction<WarningsResultAction> {
    private final String group;

    /**
     * Creates a new instance of {@link WarningsProjectAction}.
     *
     * @param project
     *            the project that owns this action
     * @param group
     *            the group of the parsers that share this action
     */
    public WarningsProjectAction(final AbstractProject<?, ?> project, final String group) {
        super(project, WarningsResultAction.class,
                ParserRegistry.getParser(group).getLinkName(), ParserRegistry.getParser(group).getTrendName(),
                getUrl(WarningsDescriptor.PLUGIN_ID, group),
                WarningsDescriptor.ICON_URL,
                getUrl(WarningsDescriptor.RESULT_URL, group));
        this.group = group;
    }

    /**
     * Creates a unique URL for this action. Uses the provided parser group to
     * get a unique ID from the parser registry.
     *
     * @param baseUrl
     *            the base URL
     * @param group
     *            the parser group
     * @return a unique URL
     */
    private static String getUrl(final String baseUrl, final String group) {
        return baseUrl + ParserRegistry.getUrl(group);
    }
}


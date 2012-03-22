package hudson.plugins.warnings;

import hudson.model.AbstractProject;
import hudson.plugins.analysis.core.AbstractProjectAction;
import hudson.plugins.warnings.parser.AbstractWarningsParser;
import hudson.plugins.warnings.parser.ParserRegistry;

import java.util.List;

import org.jvnet.localizer.Localizable;

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
        super(project, WarningsResultAction.class, getActionName(group), getTrendName(group),
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

    private static Localizable getActionName(final String group) {
        List<AbstractWarningsParser> parsers = ParserRegistry.getParsers(group);
        if (parsers.isEmpty()) {
            return Messages._Warnings_ProjectAction_Name();
        }
        else {
            return parsers.get(0).getLinkName();
        }
    }

    private static Localizable getTrendName(final String group) {
        List<AbstractWarningsParser> parsers = ParserRegistry.getParsers(group);
        if (parsers.isEmpty()) {
            return Messages._Warnings_Trend_Name();
        }
        else {
            return parsers.get(0).getTrendName();
        }
    }
}


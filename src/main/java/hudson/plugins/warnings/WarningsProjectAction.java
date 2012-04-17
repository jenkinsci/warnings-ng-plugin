package hudson.plugins.warnings;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.plugins.analysis.core.BuildHistory;
import hudson.plugins.analysis.core.NullBuildHistory;
import hudson.plugins.analysis.core.AbstractProjectAction;
import hudson.plugins.analysis.graph.BuildResultGraph;
import hudson.plugins.analysis.graph.DefaultGraphConfigurationView;
import hudson.plugins.analysis.graph.GraphConfigurationView;
import hudson.plugins.warnings.parser.ParserRegistry;

import java.util.List;

import javax.annotation.CheckForNull;

import org.apache.commons.lang.StringUtils;

/**
 * Entry point to visualize the warnings trend graph in the project screen.
 * Drawing of the graph is delegated to the associated
 * {@link WarningsResultAction}.
 *
 * @author Ulli Hafner
 */
public class WarningsProjectAction extends AbstractProjectAction<WarningsResultAction> {
    private final String parser;

    /**
     * Returns all the graphs.
     *
     * @return the graphs
     */
    public static List<BuildResultGraph> getAllGraphs() {
        return new WarningsProjectAction(null, null).getAvailableGraphs();
    }

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
                WarningsDescriptor.getProjectUrl(group),
                ParserRegistry.getParser(group).getSmallImage(),
                WarningsDescriptor.getResultUrl(group));
        parser = group;
    }

    @Override
    protected GraphConfigurationView createDefaultConfiguration() {
        return new DefaultGraphConfigurationView(createConfiguration(getAvailableGraphs()), getProject(),
                WarningsDescriptor.getProjectUrl(parser),
                createBuildHistory(), WarningsDescriptor.getProjectUrl(null));
    }

    /**
     * Returns whether the specified parser is the parser group of this action.
     *
     * @param group
     *            the group to check
     * @return <code>true</code> if the parser is in the same group,
     *         <code>false</code> otherwise
     */
    public boolean isInGroup(@CheckForNull final String group) {
        return StringUtils.equals(parser, group);
    }

    @Override
    protected WarningsResultAction getResultAction(final AbstractBuild<?, ?> lastBuild) {
        return createHistory(lastBuild).getResultAction(lastBuild);
    }

    /**
     * Creates the build history.
     *
     * @return build history
     */
    @Override
    protected BuildHistory createBuildHistory() {
        AbstractBuild<?, ?> lastFinishedBuild = getLastFinishedBuild();
        if (lastFinishedBuild == null) {
            return new NullBuildHistory();
        }
        else {
            return createHistory(lastFinishedBuild);
        }
    }

    private WarningsBuildHistory createHistory(final AbstractBuild<?, ?> build) {
        return new WarningsBuildHistory(build, parser);
    }
}


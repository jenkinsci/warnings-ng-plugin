package hudson.plugins.warnings;

import javax.annotation.CheckForNull;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.StaplerRequest;

import hudson.model.Job;
import hudson.model.Run;
import hudson.plugins.analysis.core.AbstractProjectAction;
import hudson.plugins.analysis.core.BuildHistory;
import hudson.plugins.analysis.core.NullBuildHistory;
import hudson.plugins.analysis.graph.BuildResultGraph;
import hudson.plugins.analysis.graph.DefaultGraphConfigurationView;
import hudson.plugins.analysis.graph.GraphConfigurationView;
import hudson.plugins.analysis.graph.UserGraphConfigurationView;
import hudson.plugins.warnings.parser.ParserRegistry;

/**
 * Entry point to visualize the warnings trend graph in the job screen.
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
     * @param job
     *            the job that owns this action
     * @param group
     *            the group of the parsers that share this action
     */
    public WarningsProjectAction(final Job<?, ?> job, final String group) {
        super(job, WarningsResultAction.class,
                ParserRegistry.getParser(group).getLinkName(), ParserRegistry.getParser(group).getTrendName(),
                WarningsDescriptor.getProjectUrl(group),
                ParserRegistry.getParser(group).getSmallImage(),
                WarningsDescriptor.getResultUrl(group));
        parser = group;
    }

    @Override
    public boolean isTrendVisible(final StaplerRequest request) {
        GraphConfigurationView configuration = createUserConfiguration(request);

        boolean canShow = configuration.isVisible() && configuration.hasMeaningfulGraph();

        return !createUserConfiguration(request, WarningsDescriptor.PLUGIN_ID).isDeactivated() && canShow;
    }

    @Override
    protected GraphConfigurationView createUserConfiguration(final StaplerRequest request) {
        return createUserConfiguration(request, WarningsDescriptor.getProjectUrl(parser));
    }

    private UserGraphConfigurationView createUserConfiguration(final StaplerRequest request, final String urlName) {
        return new UserGraphConfigurationView(
                createConfiguration(getAvailableGraphs()), getOwner(),
                urlName, WarningsDescriptor.getProjectUrl(null),
                request.getCookies(), createBuildHistory());
    }

    @Override
    protected GraphConfigurationView createDefaultConfiguration() {
        return new DefaultGraphConfigurationView(
                createConfiguration(getAvailableGraphs()), getOwner(),
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
    protected WarningsResultAction getResultAction(final Run<?, ?> lastBuild) {
        return createHistory(lastBuild).getResultAction((Run<?, ?>) lastBuild);
    }

    /**
     * Creates the build history.
     *
     * @return build history
     */
    @Override
    protected BuildHistory createBuildHistory() {
        Run<?, ?> lastFinishedBuild = getLastFinishedRun();
        if (lastFinishedBuild == null) {
            return new NullBuildHistory();
        }
        else {
            return createHistory(lastFinishedBuild);
        }
    }

    private WarningsBuildHistory createHistory(final Run<?, ?> build) {
        return new WarningsBuildHistory(build, parser, false, false);
    }
}


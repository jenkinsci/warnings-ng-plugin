package hudson.plugins.warnings;

import org.kohsuke.stapler.StaplerRequest;

import hudson.model.AbstractProject;

import hudson.plugins.analysis.core.ResultAction;
import hudson.plugins.analysis.core.AbstractProjectAction;

/**
 * Aggregated warnings results. This action shows the results of all active parsers. Currently, the aggregated results
 * are not shown in the UI.
 *
 * @author Ulli Hafner
 */
public class AggregatedWarningsProjectAction extends AbstractProjectAction<ResultAction<AggregatedWarningsResult>> {
    /**
     * Instantiates a new {@link AggregatedWarningsProjectAction}.
     *
     * @param project
     *            the project that owns this action
     */
    public AggregatedWarningsProjectAction(final AbstractProject<?, ?> project) {
        this(project, AggregatedWarningsResultAction.class);
    }

    /**
     * Instantiates a new {@link AggregatedWarningsProjectAction}.
     *
     * @param project
     *            the project that owns this action
     * @param type
     *            the result action type
     */
    public AggregatedWarningsProjectAction(final AbstractProject<?, ?> project,
            final Class<? extends ResultAction<AggregatedWarningsResult>> type) {
        super(project, type, Messages._Warnings_ProjectAction_Name(), Messages._Warnings_Trend_Name(),
                WarningsDescriptor.PLUGIN_ID, null, WarningsDescriptor.RESULT_URL);
    }

    /** {@inheritDoc} */
    @Override
    public String getIconFileName() {
        return null; // do not show aggregation in UI
    }

    /** {@inheritDoc} */
    @Override
    public boolean isTrendVisible(final StaplerRequest request) {
        return false;
    }
}


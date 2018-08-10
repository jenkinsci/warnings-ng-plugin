package hudson.plugins.warnings;

import org.kohsuke.stapler.StaplerRequest;

import hudson.model.Job;
import hudson.plugins.analysis.core.AbstractProjectAction;

/**
 * Aggregated warnings results. This action shows the aggregated results of all active parsers.
 * Currently, these aggregated results are not shown in the UI.
 *
 * @author Ullrich Hafner
 */
public class AggregatedWarningsProjectAction extends AbstractProjectAction<AggregatedWarningsResultAction> {
    /**
     * Instantiates a new {@link AggregatedWarningsProjectAction}.
     *
     * @param job
     *            the run that owns this action
     */
    public AggregatedWarningsProjectAction(final Job<?, ?> job) {
        this(job, AggregatedWarningsResultAction.class);
    }

    /**
     * Instantiates a new {@link AggregatedWarningsProjectAction}.
     *
     * @param job
     *            the job that owns this action
     * @param type
     *            the result action type
     */
    public AggregatedWarningsProjectAction(final Job<?, ?> job,
            final Class<? extends AggregatedWarningsResultAction> type) {
        super(job, type,
                Messages._Warnings_ProjectAction_Name(), Messages._Warnings_Trend_Name(),
                WarningsDescriptor.PLUGIN_ID, null, WarningsDescriptor.RESULT_URL);
    }

    @Override
    public String getIconFileName() {
        return null; // do not show aggregation in UI
    }

    @Override
    public boolean isTrendVisible(final StaplerRequest request) {
        return false;
    }
}


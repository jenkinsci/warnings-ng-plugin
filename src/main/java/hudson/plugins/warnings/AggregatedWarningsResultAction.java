package hudson.plugins.warnings;

import hudson.model.AbstractBuild;

import hudson.plugins.analysis.core.NullHealthDescriptor;
import hudson.plugins.analysis.core.AbstractResultAction;
import hudson.plugins.analysis.core.PluginDescriptor;

/**
 * Aggregated warnings results. This action shows the results of all active parsers. Currently, the aggregated results
 * are not shown in the UI.
 *
 * @author Marvin Schütz
 * @author Sebastian Hansbauer
 */
public class AggregatedWarningsResultAction extends AbstractResultAction<AggregatedWarningsResult> {
    private static final NullHealthDescriptor NULL_HEALTH_DESCRIPTOR = new NullHealthDescriptor();

    /**
     * Creates a new instance of <code>WarningsResultAction</code>.
     *
     * @param owner
     *            the associated build of this action
     * @param result
     *            the result in this build
     */
    public AggregatedWarningsResultAction(final AbstractBuild<?, ?> owner, final AggregatedWarningsResult result) {
        super(owner, NULL_HEALTH_DESCRIPTOR, result);
    }

    @Override
    public String getUrlName() {
        return WarningsDescriptor.RESULT_URL;
    }

    @Override
    public String getDisplayName() {
        return Messages.Warnings_ProjectAction_Name();
    }

    @Override
    public String getIconFileName() {
        return null; // do not show aggregation in UI
    }

    @Override
    protected PluginDescriptor getDescriptor() {
        return new WarningsDescriptor();
    }
}

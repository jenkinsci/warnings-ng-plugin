package hudson.plugins.warnings;

import java.util.Collection;

import hudson.model.Action;
import hudson.model.Run;
import hudson.plugins.analysis.core.AbstractResultAction;
import hudson.plugins.analysis.core.NullHealthDescriptor;
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
    public AggregatedWarningsResultAction(final Run<?, ?> owner, final AggregatedWarningsResult result) {
        super(owner, NULL_HEALTH_DESCRIPTOR, result);
    }

    /**
     * Returns the associated project action for this result.
     *
     * @return the project action for this result
     */
    @Override
    public Collection<? extends Action> getProjectActions() {
        return asSet(new AggregatedWarningsProjectAction(getJob()));
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

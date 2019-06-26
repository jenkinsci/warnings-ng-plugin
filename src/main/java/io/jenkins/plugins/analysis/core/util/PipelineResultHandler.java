package io.jenkins.plugins.analysis.core.util;

import org.jenkinsci.plugins.workflow.actions.WarningAction;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import hudson.model.Result;
import hudson.model.Run;

/**
 * {@link StageResultHandler} that sets the overall build result of the {@link Run} and annotates the given Pipeline
 * step with a {@link WarningAction}.
 */
public class PipelineResultHandler implements StageResultHandler {
    private final Run<?, ?> run;
    private final FlowNode flowNode;

    /**
     * Creates a new instance of {@link io.jenkins.plugins.analysis.core.util.PipelineResultHandler}.
     *
     * @param run
     *         the run to set the result for
     * @param flowNode
     *         the flow node to add a warning to
     */
    public PipelineResultHandler(final Run<?, ?> run, final FlowNode flowNode) {
        this.run = run;
        this.flowNode = flowNode;
    }

    @Override
    public void setResult(final Result result, final String message) {
        run.setResult(result);
        WarningAction existing = flowNode.getPersistentAction(WarningAction.class);
        if (existing == null || existing.getResult().isBetterThan(result)) {
            flowNode.addOrReplaceAction(new WarningAction(result).withMessage(message));
        }
    }
}

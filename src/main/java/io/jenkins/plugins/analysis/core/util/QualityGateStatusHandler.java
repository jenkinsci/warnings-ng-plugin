package io.jenkins.plugins.analysis.core.util;

import hudson.model.Result;
import hudson.model.Run;
import org.jenkinsci.plugins.workflow.actions.WarningAction;
import org.jenkinsci.plugins.workflow.graph.FlowNode;

/**
 * Interface used to handle {@link QualityGateStatus}. Default implementation is {@link SetBuildResultStatusHandler}
 */
public interface QualityGateStatusHandler {

    /**
     * Called to handle the {@link QualityGateStatus} created by a
     * {@link QualityGateEvaluator#evaluate(IssuesStatistics, FormattedLogger)} call.
     *
     * @param status
     *         the status to handle
     */
    void handleStatus(QualityGateStatus status);

    /**
     * {@link QualityGateStatusHandler} that sets the overall build result if the status is unsuccessful.
     */
    class SetBuildResultStatusHandler implements QualityGateStatusHandler {
        private final Run<?, ?> run;

        /**
         * Creates a new instance of {@link SetBuildResultStatusHandler}.
         *
         * @param run
         *         the run to set the result for
         */
        public SetBuildResultStatusHandler(final Run<?, ?> run) {
            this.run = run;
        }

        @Override
        public void handleStatus(final QualityGateStatus status) {
            if (!status.isSuccessful()) {
                run.setResult(status.getResult());
            }
        }
    }

    /**
     * {@link QualityGateStatusHandler} that sets the overall build result and annotates the given
     * Pipeline step with a {@link WarningAction} if the status is unsuccessful.
     */
    class PipelineStatusHandler implements QualityGateStatusHandler {
        private final Run<?, ?> run;
        private final FlowNode flowNode;

        /**
         * Creates a new instance of {@link PipelineStatusHandler}.
         *
         * @param run
         *         the run to set the result for
         * @param flowNode
         *         the flow node to add a warning to
         */
        public PipelineStatusHandler(final Run<?, ?> run, final FlowNode flowNode) {
            this.run = run;
            this.flowNode = flowNode;
        }

        @Override
        public void handleStatus(final QualityGateStatus status) {
            if (!status.isSuccessful()) {
                Result result = status.getResult();
                run.setResult(result);
                WarningAction existing = flowNode.getPersistentAction(WarningAction.class);
                if (existing == null || existing.getResult().isBetterThan(result)) {
                    flowNode.addOrReplaceAction(new WarningAction(result)
                            .withMessage("Some quality gates have been missed: overall result is " + status));
                }
            }
        }
    }
}

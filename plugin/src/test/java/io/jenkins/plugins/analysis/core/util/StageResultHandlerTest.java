package io.jenkins.plugins.analysis.core.util;

import org.junit.jupiter.api.Test;

import org.jenkinsci.plugins.workflow.actions.WarningAction;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import hudson.model.Result;
import hudson.model.Run;

import static org.mockito.Mockito.*;

/**
 * Tests the class {@link StageResultHandler}.
 */
class StageResultHandlerTest {
    private static final String MESSAGE = "message";

    @Test
    void defaultHandlerShouldSetBuildResult() {
        Run<?, ?> run = mock(Run.class);
        StageResultHandler defaultHandler = new RunResultHandler(run);
        defaultHandler.setResult(Result.UNSTABLE, "something");
        verify(run).setResult(Result.UNSTABLE);
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void pipelineHandlerShouldSetBuildResultAndAddWarningAction() {
        Run<?, ?> run = mock(Run.class);
        FlowNode flowNode = mock(FlowNode.class);
        StageResultHandler pipelineHandler = new PipelineResultHandler(run, flowNode);
        pipelineHandler.setResult(Result.UNSTABLE, MESSAGE);
        verify(run).setResult(Result.UNSTABLE);
        verify(flowNode).addOrReplaceAction(refEq(new WarningAction(Result.UNSTABLE).withMessage(MESSAGE)));
        pipelineHandler.setResult(Result.FAILURE, MESSAGE);
        verify(flowNode).addOrReplaceAction(refEq(new WarningAction(Result.FAILURE).withMessage(MESSAGE)));
    }
}

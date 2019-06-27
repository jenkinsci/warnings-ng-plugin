package io.jenkins.plugins.analysis.core.util;

import java.lang.reflect.Field;
import java.util.concurrent.CopyOnWriteArrayList;

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
        Run run = mock(Run.class);
        StageResultHandler defaultHandler = new RunResultHandler(run);
        defaultHandler.setResult(Result.UNSTABLE, "something");
        verify(run).setResult(Result.UNSTABLE);
    }

    @Test
    void pipelineHandlerShouldSetBuildResultAndAddWarningAction()
            throws IllegalAccessException, IllegalArgumentException, NoSuchFieldException {
        Run run = mock(Run.class);
        FlowNode flowNode = mock(FlowNode.class);
        // Needed to keep `FlowNode.getPersistentAction` from failing. We can't mock the method directly because it's final.
        Field actions = FlowNode.class.getDeclaredField("actions");
        actions.setAccessible(true);
        actions.set(flowNode, new CopyOnWriteArrayList<>());
        StageResultHandler pipelineHandler = new PipelineResultHandler(run, flowNode);
        pipelineHandler.setResult(Result.UNSTABLE, MESSAGE);
        verify(run).setResult(Result.UNSTABLE);
        verify(flowNode).addOrReplaceAction(refEq(new WarningAction(Result.UNSTABLE).withMessage(MESSAGE)));
        pipelineHandler.setResult(Result.FAILURE, MESSAGE);
        verify(flowNode).addOrReplaceAction(refEq(new WarningAction(Result.FAILURE).withMessage(MESSAGE)));
    }
}

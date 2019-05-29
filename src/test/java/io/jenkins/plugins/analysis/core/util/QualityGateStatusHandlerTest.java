package io.jenkins.plugins.analysis.core.util;

import hudson.model.Result;
import hudson.model.Run;
import java.lang.reflect.Field;
import java.util.concurrent.CopyOnWriteArrayList;
import org.jenkinsci.plugins.workflow.actions.WarningAction;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Tests the class {@link QualityGateStatusHandler}.
 */
class QualityGateStatusHandlerTest {
    @Test
    void defaultHandlerShouldSetBuildResult() {
        Run run = mock(Run.class);
        QualityGateStatusHandler defaultHandler = new QualityGateStatusHandler.SetBuildResultStatusHandler(run);
        defaultHandler.handleStatus(QualityGateStatus.PASSED);
        verify(run, never()).setResult(any());
        defaultHandler.handleStatus(QualityGateStatus.INACTIVE);
        verify(run, never()).setResult(any());
        defaultHandler.handleStatus(QualityGateStatus.WARNING);
        verify(run).setResult(Result.UNSTABLE);
        defaultHandler.handleStatus(QualityGateStatus.FAILED);
        verify(run).setResult(Result.FAILURE);
    }

    @Test
    void pipelineHandlerShouldSetBuildResultAndAddWarningAction() throws Exception {
        Run run = mock(Run.class);
        FlowNode flowNode = mock(FlowNode.class);
        // Needed to keep `FlowNode.getPersistentAction` from failing. We can't mock the method directly because it's final.
        Field actions = FlowNode.class.getDeclaredField("actions");
        actions.setAccessible(true);
        actions.set(flowNode, new CopyOnWriteArrayList<>());
        QualityGateStatusHandler pipelineHandler = new QualityGateStatusHandler.PipelineStatusHandler(run, flowNode);
        pipelineHandler.handleStatus(QualityGateStatus.PASSED);
        verify(run, never()).setResult(any());
        verify(flowNode, never()).addOrReplaceAction(any());
        pipelineHandler.handleStatus(QualityGateStatus.INACTIVE);
        verify(run, never()).setResult(any());
        verify(flowNode, never()).addOrReplaceAction(any());
        pipelineHandler.handleStatus(QualityGateStatus.WARNING);
        verify(run).setResult(Result.UNSTABLE);
        verify(flowNode).addOrReplaceAction(refEq(new WarningAction(Result.UNSTABLE)
                .withMessage("Some quality gates have been missed: overall result is WARNING")));
        pipelineHandler.handleStatus(QualityGateStatus.FAILED);
        verify(flowNode).addOrReplaceAction(refEq(new WarningAction(Result.FAILURE)
                .withMessage("Some quality gates have been missed: overall result is FAILED")));
    }
}

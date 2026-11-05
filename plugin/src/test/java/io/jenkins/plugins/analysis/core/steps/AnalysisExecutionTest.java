package io.jenkins.plugins.analysis.core.steps;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import hudson.model.Run;
import java.io.IOException;
import java.io.Serial;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link AnalysisExecution}.
 */
class AnalysisExecutionTest {
    @Test
    void shouldReturnFlowNodeWhenPresentInContext() throws IOException, InterruptedException {
        var context = mock(StepContext.class);
        var node = mock(FlowNode.class);
        when(context.get(FlowNode.class)).thenReturn(node);

        var execution = new TestAnalysisExecution(context);

        assertThat(execution.getFlowNode()).isSameAs(node);
    }

    @Test
    void shouldReturnNullWhenResolvingFlowNodeThrowsException() throws IOException, InterruptedException {
        var context = mock(StepContext.class);
        when(context.get(FlowNode.class)).thenThrow(new IOException("Context error"));

        var execution = new TestAnalysisExecution(context);

        assertThat(execution.getFlowNode()).isNull();
    }

    @Test
    void shouldThrowIOExceptionWhenRequiredFlowNodeIsMissing() throws IOException, InterruptedException {
        var context = mock(StepContext.class);
        when(context.get(Run.class)).thenReturn(mock(Run.class));
        when(context.get(FlowNode.class)).thenReturn(null);

        var execution = new TestAnalysisExecution(context);

        assertThatThrownBy(execution::createResultHandler)
                .isInstanceOf(IOException.class)
                .hasMessageContaining("FlowNode is not defined in the context");
    }

    private static class TestAnalysisExecution extends AnalysisExecution<Void> {
        @Serial
        private static final long serialVersionUID = 1L;

        TestAnalysisExecution(final StepContext context) {
            super(context);
        }

        @Override
        protected Void run() {
            return null;
        }
    }
}

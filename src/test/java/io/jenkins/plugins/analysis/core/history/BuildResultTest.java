package io.jenkins.plugins.analysis.core.history;

import org.junit.jupiter.api.Test;

import io.jenkins.plugins.analysis.core.steps.AnalysisResult;
import io.jenkins.plugins.analysis.core.steps.PipelineResultAction;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import hudson.model.Run;

/**
 * Tests the class {@link BuildHistory.BuildResultIterator}.
 *
 * @author Ullrich Hafner
 */
class BuildResultTest {
    /** Verifies that the iterator for a single run without previous runs contains just a single element. */
    @Test
    void shouldHaveOneElementForOneTotalRun() {
        PipelineResultAction action = mock(PipelineResultAction.class);

        AnalysisResult buildResult = mock(AnalysisResult.class);
        when(action.getResult()).thenReturn(buildResult);

        ResultSelector selector = mock(ResultSelector.class);

        Run run = mock(Run.class);
        when(selector.get(run)).thenReturn(action);

        BuildHistory history = new BuildHistory(run, selector);

        assertThat(history).hasSize(1);
        assertThat(history).containsExactly(buildResult);
    }
}
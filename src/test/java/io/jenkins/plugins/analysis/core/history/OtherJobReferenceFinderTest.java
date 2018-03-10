package io.jenkins.plugins.analysis.core.history;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.views.ResultAction;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import hudson.model.Run;

import edu.hm.hafner.analysis.Issues;

/**
 * Tests the class {@link OtherJobReferenceFinder}.
 *
 * @author Ullrich Hafner
 */
class OtherJobReferenceFinderTest {
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    void shouldReturnBaselineIfThereAlreadyIsAResult() {
        Run run = mock(Run.class);
        ResultSelector selector = mock(ResultSelector.class);

        ResultAction action = mock(ResultAction.class);

        AnalysisResult result = mock(AnalysisResult.class);
        when(result.getIssues()).thenReturn(new Issues<>());

        when(action.getOwner()).thenReturn(run);
        when(action.getResult()).thenReturn(result);

        when(selector.get(run)).thenReturn(Optional.of(action));

        OtherJobReferenceFinder finder = new OtherJobReferenceFinder(run, selector, false, false);

        assertThat(finder.getReferenceAction()).hasValue(action);
        assertThat(finder.getIssues()).hasSize(0);
        assertThat(finder.getAnalysisRun()).hasValue(run);
    }
}
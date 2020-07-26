package io.jenkins.plugins.analysis.core.model;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import hudson.model.Job;
import hudson.model.Run;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link JobAction}.
 * 
 * @author Kasper Heyndrickx
 */
class JobActionTest {
    private static final String LINK_NAME = "link-name";
    private static final String TREND_NAME = "trend-name";
    private static final String ID = "jobaction-id";
    private static final String ANALYSIS_ID = "analysis-id";

    @Test
    void shouldUseLabelProviderLinkNameAsDisplayName() {
        Job<?, ?> job = mock(Job.class);
        StaticAnalysisLabelProvider labelProvider = mock(StaticAnalysisLabelProvider.class);
        when(labelProvider.getLinkName()).thenReturn(LINK_NAME);
        JobAction action = new JobAction(job, labelProvider, 1);
        assertThat(action.getDisplayName()).isEqualTo(LINK_NAME);
    }
    
    @Test
    void shouldUseLabelProviderTrendNameAsTrendName() {
        Job<?, ?> job = mock(Job.class);
        StaticAnalysisLabelProvider labelProvider = mock(StaticAnalysisLabelProvider.class);
        when(labelProvider.getTrendName()).thenReturn(TREND_NAME); 
        JobAction action = new JobAction(job, labelProvider, 1);
        assertThat(action.getTrendName()).isEqualTo(TREND_NAME); 
    } 

    @Test
    void shouldUseLabelProviderIDAsID() {
        Job<?, ?> job = mock(Job.class);
        StaticAnalysisLabelProvider labelProvider = mock(StaticAnalysisLabelProvider.class);
        when(labelProvider.getId()).thenReturn(ID); 
        JobAction action = new JobAction(job, labelProvider, 1);
        assertThat(action.getId()).isEqualTo(ID); 
    } 

    @Test
    void shouldSetOwner() {
        Job<?, ?> job = mock(Job.class);
        StaticAnalysisLabelProvider labelProvider = mock(StaticAnalysisLabelProvider.class);
        JobAction action = new JobAction(job, labelProvider, 1);
        assertThat(action.getOwner()).isEqualTo(job); 
    }

    @SuppressWarnings("rawtypes")
    @Test
    void shouldShowIconIfThereIsABuildResultAvailable() {
        Job job = mock(Job.class);
        StaticAnalysisLabelProvider labelProvider = mock(StaticAnalysisLabelProvider.class);
        when(labelProvider.getId()).thenReturn(ANALYSIS_ID);

        JobAction action = new JobAction(job, labelProvider, 1);

        assertThat(action.getIconFileName()).isNull();

        Run<?, ?> reference = createValidReferenceBuild();
        when(job.getLastCompletedBuild()).thenReturn(reference);

        assertThat(action.getIconFileName()).isNotEmpty();
    }

    private Run<?, ?> createValidReferenceBuild() {
        Run<?, ?> reference = mock(Run.class);
        ResultAction result = mock(ResultAction.class);
        when(result.getResult()).thenReturn(mock(AnalysisResult.class));
        when(result.getId()).thenReturn(ANALYSIS_ID);
        when(reference.getActions(ResultAction.class)).thenReturn(Collections.singletonList(result));
        return reference;
    }
}

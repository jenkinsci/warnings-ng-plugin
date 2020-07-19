package io.jenkins.plugins.analysis.core.model;

import org.junit.jupiter.api.Test;

import hudson.model.Job;

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
} 
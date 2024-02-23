package io.jenkins.plugins.analysis.core.model;

import java.io.IOException;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import hudson.model.Job;
import hudson.model.Run;

import io.jenkins.plugins.analysis.core.util.TrendChartType;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link JobAction}.
 *
 * @author Kasper Heyndrickx
 * @author Ullrich Hafner
 */
class JobActionTest {
    private static final String LINK_NAME = "link-name";
    private static final String TREND_NAME = "trend-name";
    private static final String ID = "jobaction-id";
    private static final String ANALYSIS_ID = "analysis-id";
    private static final String ICON = "icon";

    @Test
    void shouldUseLabelProvider() {
        StaticAnalysisLabelProvider labelProvider = mock(StaticAnalysisLabelProvider.class);
        when(labelProvider.getLinkName()).thenReturn(LINK_NAME);
        when(labelProvider.getLinkName()).thenReturn(LINK_NAME);
        when(labelProvider.getTrendName()).thenReturn(TREND_NAME);
        when(labelProvider.getId()).thenReturn(ID);

        Job<?, ?> job = mock(Job.class);

        JobAction action = new JobAction(job, labelProvider, 1);
        assertThat(action.getDisplayName()).isEqualTo(LINK_NAME);
        assertThat(action.getTrendName()).isEqualTo(TREND_NAME);
        assertThat(action.getId()).isEqualTo(ID);
        assertThat(action.getUrlName()).isEqualTo(ID);
        assertThat(action.getOwner()).isEqualTo(job);
    }

    @Test
    void shouldShowIconIfThereIsABuildResultAvailable() throws IOException {
        StaticAnalysisLabelProvider labelProvider = mock(StaticAnalysisLabelProvider.class);
        when(labelProvider.getId()).thenReturn(ANALYSIS_ID);
        when(labelProvider.getSmallIconUrl()).thenReturn(ICON);

        Job<?, ?> job = mock(Job.class);
        JobAction action = new JobAction(job, labelProvider, 1);
        assertThat(action.getIconFileName()).isNull();

        Run<?, ?> reference = createValidReferenceBuild(0);
        when(job.getLastCompletedBuild()).thenAnswer(i -> reference);

        assertThat(action.getIconFileName()).isEqualTo(ICON);
        assertThat(action.isTrendVisible()).isFalse();
        assertThat(action.isTrendEmpty()).isTrue();

        Run<?, ?> referenceWithIssues = createValidReferenceBuild(1);
        when(reference.getPreviousBuild()).thenAnswer(i -> referenceWithIssues);

        assertThat(action.isTrendEmpty()).isFalse();
        assertThat(action.isTrendVisible()).isTrue();

        StaplerResponse response = mock(StaplerResponse.class);
        action.doIndex(mock(StaplerRequest.class), response);

        verify(response).sendRedirect2("../0/" + ANALYSIS_ID);

        JobAction hiddenAction = new JobAction(job, labelProvider, 1, TrendChartType.NONE);
        assertThat(hiddenAction.isTrendVisible()).isFalse();
    }

    @Test
    void shouldRedirect() throws IOException {
        StaticAnalysisLabelProvider labelProvider = mock(StaticAnalysisLabelProvider.class);
        Job<?, ?> job = mock(Job.class);
        JobAction action = new JobAction(job, labelProvider, 1);

        StaplerRequest request = mock(StaplerRequest.class);
        action.doIndex(request, mock(StaplerResponse.class));

        verifyNoInteractions(request);
    }

    private Run<?, ?> createValidReferenceBuild(final int issuesSize) {
        Run<?, ?> reference = mock(Run.class);
        ResultAction action = mock(ResultAction.class);
        when(action.getOwner()).thenAnswer(i -> reference);
        AnalysisResult result = mock(AnalysisResult.class);
        when(result.getTotalSize()).thenReturn(issuesSize);
        when(action.getResult()).thenReturn(result);
        when(action.getId()).thenReturn(ANALYSIS_ID);
        when(reference.getActions(ResultAction.class)).thenReturn(Collections.singletonList(action));
        return reference;
    }
}

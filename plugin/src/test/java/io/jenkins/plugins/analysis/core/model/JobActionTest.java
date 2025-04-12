package io.jenkins.plugins.analysis.core.model;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;

import org.kohsuke.stapler.StaplerRequest2;
import org.kohsuke.stapler.StaplerResponse2;
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

        var action = createJobAction(job, labelProvider);
        assertThat(action.getDisplayName()).isEqualTo(LINK_NAME);
        assertThat(action.getTrendName()).isEqualTo(TREND_NAME);
        assertThat(action.getId()).isEqualTo(ID);
        assertThat(action.getUrlName()).isEqualTo(ID);
        assertThat(action.getOwner()).isEqualTo(job);
    }

    private JobAction createJobAction(final Job<?, ?> job, final StaticAnalysisLabelProvider labelProvider) {
        return new JobAction(job, labelProvider, 1, TrendChartType.TOOLS_ONLY, labelProvider.getId());
    }

    @Test
    void shouldShowIconIfThereIsABuildResultAvailable() throws IOException {
        StaticAnalysisLabelProvider labelProvider = mock(StaticAnalysisLabelProvider.class);
        when(labelProvider.getId()).thenReturn(ANALYSIS_ID);
        when(labelProvider.getSmallIconUrl()).thenReturn(ICON);

        Job<?, ?> job = mock(Job.class);
        var action = createJobAction(job, labelProvider);
        assertThat(action.getIconFileName()).isEqualTo(ICON); // a JobAction should always show an icon

        Run<?, ?> reference = createValidReferenceBuild(0);
        when(job.getLastCompletedBuild()).thenAnswer(i -> reference);

        assertThat(action.getIconFileName()).isEqualTo(ICON);
        assertThat(action.isTrendVisible()).isFalse();
        assertThat(action.isTrendEmpty()).isTrue();

        Run<?, ?> referenceWithIssues = createValidReferenceBuild(1);
        when(reference.getPreviousBuild()).thenAnswer(i -> referenceWithIssues);

        assertThat(action.isTrendEmpty()).isFalse();
        assertThat(action.isTrendVisible()).isTrue();

        StaplerResponse2 response = mock(StaplerResponse2.class);
        action.doIndex(mock(StaplerRequest2.class), response);

        verify(response).sendRedirect2("../0/" + ANALYSIS_ID);

        var url = "something";
        var hiddenAction = new JobAction(job, labelProvider, 1, TrendChartType.NONE, url);
        assertThat(hiddenAction.isTrendVisible()).isFalse();
        assertThat(hiddenAction.getUrlName()).isEqualTo(url);
    }

    @Test
    void shouldRedirect() throws IOException {
        StaticAnalysisLabelProvider labelProvider = mock(StaticAnalysisLabelProvider.class);
        Job<?, ?> job = mock(Job.class);
        var action = createJobAction(job, labelProvider);

        StaplerRequest2 request = mock(StaplerRequest2.class);
        action.doIndex(request, mock(StaplerResponse2.class));

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

package io.jenkins.plugins.analysis.core.model;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.collections.impl.factory.Lists;
import org.junit.jupiter.api.Test;

import hudson.model.Action;
import hudson.model.Api;
import hudson.model.Run;

import io.jenkins.plugins.analysis.core.restapi.AggregationApi;
import io.jenkins.plugins.analysis.core.restapi.ToolApi;
import io.jenkins.plugins.analysis.core.testutil.JobStubs;
import io.jenkins.plugins.analysis.core.testutil.SoftAssertions;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link AggregationAction}.
 *
 * @author Florian Hageneder
 */
class AggregationActionTest {
    private static final int SIZE = 1;

    @Test
    void shouldNotReturnIconFileName() {
        AggregationAction action = new AggregationAction();

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(action.getIconFileName()).isNull();
            softly.assertThat(action.getDisplayName()).isEqualTo(Messages.Aggregation_Name());
            softly.assertThat(action.getUrlName()).isEqualTo("warnings-ng");
        });
    }

    @Test
    void shouldNeverReturnMultipleProjectActions() {
        AggregationAction action = new AggregationAction();
        action.onLoad(mock(Run.class));

        Collection<? extends Action> projectActions = action.getProjectActions();

        assertThat(projectActions).hasSize(1);
        assertThat(projectActions.iterator().next()).isInstanceOf(AggregatedTrendAction.class);
    }

    @Test
    void shouldReturnAggregationApi() {
        Run<?, ?> owner = mock(Run.class);
        when(owner.getActions(any())).thenReturn(Collections.emptyList());

        AggregationAction action = new AggregationAction();
        action.onAttached(owner);

        Api api = action.getApi();
        assertThat(api.bean).isInstanceOf(AggregationApi.class);
    }

    @Test
    void shouldCreateCompleteApi() {
        Run<?, ?> owner = mock(Run.class);
        List<ResultAction> actions = Lists.fixedSize.of(
                createAction(JobStubs.SPOT_BUGS_ID, JobStubs.SPOT_BUGS_NAME, SIZE),
                createAction(JobStubs.CHECK_STYLE_NAME, JobStubs.CHECK_STYLE_NAME, SIZE)
        );
        when(owner.getActions(any())).thenAnswer(i -> actions);
        AggregationAction action = new AggregationAction();
        action.onLoad(owner);

        Api api = action.getApi();
        AggregationApi aggregationApi = (AggregationApi) api.bean;

        assertThat(aggregationApi.getTools()).hasSize(2);

        List<ToolApi> actually = action.getTools();

        assertThat(actually).hasSize(2);
    }

    private ResultAction createAction(final String id, final String name, final int size) {
        ResultAction resultAction = mock(ResultAction.class);

        AnalysisResult result = mock(AnalysisResult.class);
        when(result.getTotalSize()).thenReturn(size);

        when(resultAction.getId()).thenReturn(id);
        when(resultAction.getDisplayName()).thenReturn(name);
        when(resultAction.getAbsoluteUrl()).thenReturn("http://example.com/");
        when(resultAction.getUrlName()).thenReturn("job/build/" + id);
        when(resultAction.getResult()).thenReturn(result);

        return resultAction;
    }
}

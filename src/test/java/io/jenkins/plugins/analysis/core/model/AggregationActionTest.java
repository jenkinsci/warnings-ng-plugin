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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link AggregationAction}.
 *
 * @author Florian Hageneder
 */
class AggregationActionTest {

    @Test
    void shouldNotReturnIconFileName() {
        AggregationAction sut = new AggregationAction();

        String fileName = sut.getIconFileName();

        assertThat(fileName).isNull();
    }

    @Test
    void shouldReturnDisplayName() {
        AggregationAction sut = new AggregationAction();

        String displayName = sut.getDisplayName();

        assertThat(displayName).isEqualTo(Messages.Aggregation_Name());
    }

    @Test
    void shouldReturnUrlName() {
        AggregationAction sut = new AggregationAction();

        String url = sut.getUrlName();

        assertThat(url).isEqualTo("warnings-ng");
    }

    @Test
    void shouldNeverReturnMultipleProjectActions() {
        AggregationAction sut = new AggregationAction();
        sut.onLoad(mock(Run.class));

        Collection<? extends Action> projectActions = sut.getProjectActions();

        assertThat(projectActions).hasSize(1);
    }

    @Test
    void shouldReturnAggregatedTrendActionAsProjectActions() {
        AggregationAction sut = new AggregationAction();
        sut.onAttached(mock(Run.class));

        Collection<? extends Action> projectActions = sut.getProjectActions();

        assertThat(projectActions.iterator().next()).isInstanceOf(AggregatedTrendAction.class);
    }

    @Test
    void shouldReturnAggregationApi() {
        Run<?, ?> owner = mock(Run.class);
        when(owner.getActions(any())).thenReturn(Collections.emptyList());
        AggregationAction sut = new AggregationAction();
        sut.onAttached(owner);

        Api api = sut.getApi();

        assertThat(api.bean).isInstanceOf(AggregationApi.class);
    }

    @Test
    void shouldCreateCompleteApi() {
        Run<?, ?> owner = mock(Run.class);
        List<ResultAction> actions = Lists.fixedSize.of(
                createAction(JobStubs.SPOT_BUGS_ID, JobStubs.SPOT_BUGS_NAME, 42),
                createAction(JobStubs.CHECK_STYLE_NAME, JobStubs.CHECK_STYLE_NAME, 42)
        );
        when(owner.getActions(any())).thenAnswer(i -> actions);
        AggregationAction sut = new AggregationAction();
        sut.onLoad(owner);

        Api api = sut.getApi();
        AggregationApi aggregationApi = (AggregationApi) api.bean;

        assertThat(aggregationApi.getTools()).hasSize(2);
    }

    @Test
    void shouldReturnAllUsedTools() {
        Run<?, ?> owner = mock(Run.class);
        List<ResultAction> actions = Lists.fixedSize.of(
                createAction(JobStubs.SPOT_BUGS_ID, JobStubs.SPOT_BUGS_NAME, 42),
                createAction(JobStubs.CHECK_STYLE_NAME, JobStubs.CHECK_STYLE_NAME, 69)
        );
        when(owner.getActions(any())).thenAnswer(i -> actions);
        AggregationAction sut = new AggregationAction();
        sut.onLoad(owner);

        List<ToolApi> actually = sut.getTools();

        assertThat(actually).hasSize(2);
    }

    ResultAction createAction(final String id, final String name, final int size) {
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

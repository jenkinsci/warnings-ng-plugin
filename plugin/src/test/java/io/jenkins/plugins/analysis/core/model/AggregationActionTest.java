package io.jenkins.plugins.analysis.core.model;

import org.eclipse.collections.impl.factory.Lists;
import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Severity;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hudson.model.Action;
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
    private static final int SIZE = 1;

    @Test
    void shouldNotReturnIconFileName() {
        var action = new AggregationAction();

        assertThat(action.getIconFileName()).isNull();
        assertThat(action.getDisplayName()).isEqualTo(Messages.Aggregation_Name());
        assertThat(action.getUrlName()).isEqualTo("warnings-ng");
    }

    @Test
    void shouldNeverReturnMultipleProjectActions() {
        var action = new AggregationAction();
        action.onLoad(mock(Run.class));

        Collection<? extends Action> projectActions = action.getProjectActions();

        assertThat(projectActions).hasSize(1);
        assertThat(projectActions.iterator().next()).isInstanceOf(AggregatedTrendAction.class);
    }

    @Test
    void shouldReturnAggregationApi() {
        Run<?, ?> owner = mock(Run.class);
        when(owner.getActions(any())).thenReturn(Collections.emptyList());

        var action = new AggregationAction();
        action.onAttached(owner);

        var api = action.getApi();
        assertThat(api.bean).isInstanceOf(AggregationApi.class);
    }

    @Test
    void shouldCreateCompleteApi() {
        Run<?, ?> owner = mock(Run.class);
        List<ResultAction> actions = Lists.fixedSize.of(
                createAction(JobStubs.SPOT_BUGS_ID, JobStubs.SPOT_BUGS_NAME, SIZE, Severity.ERROR),
                createAction(JobStubs.CHECK_STYLE_NAME, JobStubs.CHECK_STYLE_NAME, SIZE, Severity.WARNING_HIGH)
        );
        when(owner.getActions(any())).thenAnswer(i -> actions);
        var action = new AggregationAction();
        action.onLoad(owner);

        var api = action.getApi();
        var aggregationApi = (AggregationApi) api.bean;

        assertThat(aggregationApi.getTools()).hasSize(2);

        List<ToolApi> actually = action.getTools();

        assertThat(actually).hasSize(2);

        assertThat(actually.get(0).getErrorSize()).isEqualTo(SIZE);
        assertThat(actually.get(0).getHighSize()).isEqualTo(0);
        assertThat(actually.get(0).getNormalSize()).isEqualTo(0);
        assertThat(actually.get(0).getLowSize()).isEqualTo(0);

        assertThat(actually.get(1).getErrorSize()).isEqualTo(0);
        assertThat(actually.get(1).getHighSize()).isEqualTo(SIZE);
        assertThat(actually.get(1).getNormalSize()).isEqualTo(0);
        assertThat(actually.get(1).getLowSize()).isEqualTo(0);
    }

    private ResultAction createAction(final String id, final String name, final int size, final Severity severity) {
        ResultAction resultAction = mock(ResultAction.class);

        Map<Severity, Integer> sizesPerSeverity = new HashMap<>();
        sizesPerSeverity.put(severity, size);

        AnalysisResult result = mock(AnalysisResult.class);
        when(result.getTotalSize()).thenReturn(size);
        when(result.getSizePerSeverity()).thenReturn(sizesPerSeverity);

        when(resultAction.getId()).thenReturn(id);
        when(resultAction.getDisplayName()).thenReturn(name);
        when(resultAction.getUrlName()).thenReturn("job/build/" + id);
        when(resultAction.getResult()).thenReturn(result);

        return resultAction;
    }
}

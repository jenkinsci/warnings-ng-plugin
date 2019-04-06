package io.jenkins.plugins.analysis.core.model;

import java.util.Collection;

import org.junit.jupiter.api.Test;

import hudson.model.Action;
import hudson.model.Run;

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
        sut.onLoad(mock(Run.class));

        Collection<? extends Action> projectActions = sut.getProjectActions();

        assertThat(projectActions.iterator().next()).isInstanceOf(AggregatedTrendAction.class);
    }

}

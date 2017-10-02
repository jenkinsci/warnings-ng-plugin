package io.jenkins.plugins.analysis.core.graphs;

import java.util.List;

import org.jfree.data.category.CategoryDataset;
import org.junit.jupiter.api.Test;

import com.google.common.collect.Lists;

import io.jenkins.plugins.analysis.core.steps.BuildResult;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import hudson.model.Run;

/**
 * Tests the class {@link PriorityGraph}.
 *
 * @author Ullrich Hafner
 */
class PriorityGraphTest {
    /**
     * FIXME: write comment.
     */
    @Test
    void should() {
        PrioritySeriesBuilder builder = new PrioritySeriesBuilder();
        GraphConfiguration configuration = mock(GraphConfiguration.class);

        CategoryDataset dataSet = builder.createDataSet(configuration, Lists.newArrayList());

        assertThat(dataSet.getColumnCount()).isEqualTo(0);
        assertThat(dataSet.getRowCount()).isEqualTo(0);
    }
    /**
     * FIXME: write comment.
     */
    @Test
    void shouldNot() {
        PrioritySeriesBuilder builder = new PrioritySeriesBuilder();
        GraphConfiguration configuration = mock(GraphConfiguration.class);

        BuildResult buildResult = mock(BuildResult.class);
        Run run = mock(Run.class);
        when(run.getNumber()).thenReturn(1);
        when(buildResult.getOwner()).thenReturn(run);

        List<BuildResult> results = Lists.newArrayList(buildResult);
        CategoryDataset dataSet = builder.createDataSet(configuration, results);

        assertThat(dataSet.getColumnCount()).isEqualTo(1);
        assertThat(dataSet.getRowCount()).isEqualTo(3);
    }
}
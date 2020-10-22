package io.jenkins.plugins.analysis.core.steps;

import org.junit.jupiter.api.Test;

import hudson.Launcher;
import hudson.matrix.MatrixAggregator;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixProject;
import hudson.model.BuildListener;
import hudson.util.DescribableList;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link MatrixBridge}.
 *
 * @author Naveen Sundar
 */
class MatrixBridgeTest {

    @Test
    void constructMatrixAggregatorWithoutRecorder() {
        MatrixBuild build = createBuild(false);
        MatrixAggregator aggregator = createAggregator(build);

        assertThat(aggregator).isNull();
    }

    @Test
    void constructMatrixAggregatorWithRecorder() {
        MatrixBuild build = createBuild(true);
        MatrixAggregator aggregator = createAggregator(build);

        assertThat(aggregator).isNotNull();
    }

    private MatrixBuild createBuild(final boolean withIssueRecorder) {
        MatrixBuild build = mock(MatrixBuild.class);
        MatrixProject matrixProject = mock(MatrixProject.class);
        DescribableList describableList = mock(DescribableList.class);

        when(build.getParent()).thenReturn(matrixProject);
        when(matrixProject.getPublishersList()).thenReturn(describableList);

        if (withIssueRecorder) {
            when(describableList.get(IssuesRecorder.class)).thenReturn(mock(IssuesRecorder.class));
        }
        else {
            when(describableList.get(IssuesRecorder.class)).thenReturn(null);
        }

        return build;
    }

    private MatrixAggregator createAggregator(final MatrixBuild build) {
        return new MatrixBridge().createAggregator(build, mock(Launcher.class), mock(BuildListener.class));
    }
}

package io.jenkins.plugins.analysis.core.steps;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import hudson.Launcher;
import hudson.matrix.MatrixAggregator;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixProject;
import hudson.model.BuildListener;
import hudson.model.Saveable;
import hudson.tasks.Publisher;
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
    void shouldNotAggregateIfNoPublisherRegistered() {
        MatrixBuild build = createBuildWithPublishers();

        assertThat(createAggregator(build)).isNull();
    }

    @Test
    void constructMatrixAggregatorWithRecorder() {
        MatrixBuild build = createBuildWithPublishers(mock(IssuesRecorder.class));

        assertThat(createAggregator(build)).isNotNull();
    }

    @Test
    void constructMatrixAggregatorWithRecorderAndSomethingElse() {
        MatrixBuild build = createBuildWithPublishers(mock(Publisher.class), mock(IssuesRecorder.class));

        assertThat(createAggregator(build)).isNotNull();
    }

    private MatrixBuild createBuildWithPublishers(final Publisher... publisher) {
        MatrixBuild build = mock(MatrixBuild.class);

        MatrixProject matrixProject = mock(MatrixProject.class);
        when(matrixProject.getPublishersList()).thenReturn(
                new DescribableList<>(Saveable.NOOP, Arrays.asList(publisher)));

        when(build.getParent()).thenReturn(matrixProject);

        return build;
    }

    private MatrixAggregator createAggregator(final MatrixBuild build) {
        return new MatrixBridge().createAggregator(build, mock(Launcher.class), mock(BuildListener.class));
    }
}

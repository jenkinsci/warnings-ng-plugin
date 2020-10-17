package io.jenkins.plugins.analysis.core.steps;

import org.junit.Before;
import org.junit.Test;

import hudson.Launcher;
import hudson.matrix.MatrixAggregator;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixProject;
import hudson.model.BuildListener;
import hudson.util.DescribableList;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link MatrixBridge}.
 *
 * @author Naveen Sundar
 */
public class MatrixBridgeTest {
    // to be tested
    private MatrixBridge matrixBridge;

    // Dependencies
    private MatrixBuild build;
    private Launcher launcher;
    private BuildListener listener;
    private IssuesRecorder issuesRecorder;

    private MatrixProject matrixProject;
    private DescribableList describableList;

    @Before
    public void setup() {
        matrixBridge = new MatrixBridge();

        build = mock(MatrixBuild.class);
        launcher = mock(Launcher.class);
        listener = mock(BuildListener.class);
        issuesRecorder = mock(IssuesRecorder.class);

        matrixProject = mock(MatrixProject.class);
        describableList = mock(DescribableList.class);

        // stubbing
        when(build.getParent()).thenReturn(matrixProject);
        when(matrixProject.getPublishersList()).thenReturn(describableList);
    }

    @Test
    public void constructMatrixAggregatorWithoutRecorder() {
        when(describableList.get(IssuesRecorder.class)).thenReturn(null);

        MatrixAggregator aggregator = matrixBridge.createAggregator(build, launcher, listener);

        assertNull(aggregator);
    }

    @Test
    public void constructMatrixAggregatorWithRecorder() {
        when(describableList.get(IssuesRecorder.class)).thenReturn(issuesRecorder);

        MatrixAggregator aggregator = matrixBridge.createAggregator(build, launcher, listener);

        assertNotNull(aggregator);
    }
}

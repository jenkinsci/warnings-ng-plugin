package io.jenkins.plugins.analysis.core.util;

import org.junit.jupiter.api.Test;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link BuildResultNavigator}.
 *
 * @author Ullrich Hafner
 */
class BuildResultNavigatorTest {
    @Test
    void shouldNavigateToTheSelectedBuild() {
        var navigator = new BuildResultNavigator();

        assertThat(navigator.getSameUrlForOtherBuild(createBuild(),
                "http://localhost:8080/job/pipeline-analysis-model/30/spotbugs/something",
                "spotbugs", "111"))
                .contains("http://localhost:8080/job/pipeline-analysis-model/111/spotbugs");
        assertThat(navigator.getSameUrlForOtherBuild(createBuild(),
                "http://localhost:8080/job/pipeline-analysis-model/30/spotbugs/something/else",
                "spotbugs", "111"))
                .contains("http://localhost:8080/job/pipeline-analysis-model/111/spotbugs");
        assertThat(navigator.getSameUrlForOtherBuild(createBuild(),
                "http://localhost:8080/job/pipeline-analysis-model/30/spotbugs",
                "spotbugs", "111"))
                .contains("http://localhost:8080/job/pipeline-analysis-model/111/spotbugs");
    }

    @Test
    void shouldSkipMissingBuild() {
        var navigator = new BuildResultNavigator();

        FreeStyleProject job = mock(FreeStyleProject.class);
        FreeStyleBuild currentBuild = mock(FreeStyleBuild.class);
        when(currentBuild.getNumber()).thenReturn(30);
        when(currentBuild.getParent()).thenReturn(job);

        assertThat(navigator.getSameUrlForOtherBuild(currentBuild,
                "http://localhost:8080/job/pipeline-analysis-model/30/spotbugs/something",
                "spotbugs", "111"))
                .isEmpty();
    }

    @Test
    void shouldSkipBrokenNextBuildNumber() {
        var navigator = new BuildResultNavigator();

        assertThat(navigator.getSameUrlForOtherBuild(mock(FreeStyleBuild.class),
                "http://localhost:8080/job/pipeline-analysis-model/30/spotbugs/something",
                "spotbugs", "##"))
                .isEmpty();
    }

    @Test
    void shouldSkipOtherUrl() {
        var navigator = new BuildResultNavigator();

        assertThat(navigator.getSameUrlForOtherBuild(createBuild(),
                "http://localhost:8080/job/pipeline-analysis-model/",
                "spotbugs", "111"))
                .isEmpty();
    }

    private FreeStyleBuild createBuild() {
        FreeStyleProject job = mock(FreeStyleProject.class);
        when(job.getBuildByNumber(111)).thenReturn(mock(FreeStyleBuild.class));

        FreeStyleBuild currentBuild = mock(FreeStyleBuild.class);
        when(currentBuild.getNumber()).thenReturn(30);
        when(currentBuild.getParent()).thenReturn(job);
        return currentBuild;
    }
}

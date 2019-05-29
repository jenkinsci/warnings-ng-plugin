package io.jenkins.plugins.analysis.warnings;

import java.util.Arrays;
import java.util.Collections;

import org.jenkins_ci.plugins.flexible_publish.ConditionalPublisher;
import org.jenkins_ci.plugins.flexible_publish.FlexiblePublisher;
import org.jenkins_ci.plugins.run_condition.BuildStepRunner.Run;
import org.jenkins_ci.plugins.run_condition.core.AlwaysRun;
import org.junit.Test;

import hudson.model.FreeStyleProject;
import hudson.model.Result;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;

/**
 * Test the flexible publish plugin in combination with the warnings-ng-plugin.
 *
 * @author Tobias Redl
 * @author Andreas Neumeier
 */
public class FlexiblePublishITest extends IntegrationTestWithJenkinsPerSuite {
    private static final String JAVA_FILE = "java2Warnings.txt";

    /**
     * Test that the same tool can be used twice with different configuration.
     */
    @Test
    public void shouldAnalyseJavaTwice() {
        FreeStyleProject project = createFreeStyleProject();
        copySingleFileToWorkspace(project, JAVA_FILE, JAVA_FILE);

        Java java = new Java();
        java.setPattern(JAVA_FILE);

        IssuesRecorder publisher = new IssuesRecorder();
        publisher.setId("j");
        publisher.setTools(java);
        publisher.setHealthy(1);
        publisher.setUnhealthy(10);
        //project.getPublishersList().add(publisher);

        Java java2 = new Java();
        java2.setPattern(JAVA_FILE);
        IssuesRecorder publisher2 = new IssuesRecorder();
        publisher2.setId("j2");
        publisher2.setTools(java2);
        publisher2.setHealthy(1);
        publisher2.setUnhealthy(2);
        //project.getPublishersList().add(publisher2);

        project.getPublishersList().add(new FlexiblePublisher(Arrays.asList(
                new ConditionalPublisher(
                        new AlwaysRun(),
                        Collections.singletonList(
                                publisher
                        ),
                        new Run(),
                        false,
                        null,
                        null,
                        null
                ),

                new ConditionalPublisher(
                        new AlwaysRun(),
                        Collections.singletonList(
                                publisher2
                        ),
                        new Run(),
                        false,
                        null,
                        null,
                        null
                )
        )));

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        //assertThat(result.getInfoMessages().contains(HEALTH_REPORT_ENABLED_MESSAGE));

        //InfoPage infoPage = new InfoPage(project, 1);
        //assertThat(result.getInfoMessages()).isEqualTo(infoPage.getInfoMessages());
        //assertThat(result.getErrorMessages()).isEqualTo(infoPage.getErrorMessages());
    }
}

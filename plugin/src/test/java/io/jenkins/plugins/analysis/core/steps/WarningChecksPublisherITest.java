package io.jenkins.plugins.analysis.core.steps;

import org.junit.Test;

import hudson.model.FreeStyleProject;
import hudson.model.Run;

import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerTest;
import io.jenkins.plugins.checks.api.ChecksAnnotation.ChecksAnnotationBuilder;
import io.jenkins.plugins.checks.api.ChecksAnnotation.ChecksAnnotationLevel;
import io.jenkins.plugins.checks.api.ChecksConclusion;
import io.jenkins.plugins.checks.api.ChecksDetails;
import io.jenkins.plugins.checks.api.ChecksDetails.ChecksDetailsBuilder;
import io.jenkins.plugins.checks.api.ChecksOutput;
import io.jenkins.plugins.checks.api.ChecksOutput.ChecksOutputBuilder;
import io.jenkins.plugins.checks.api.ChecksStatus;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

public class WarningChecksPublisherITest extends IntegrationTestWithJenkinsPerTest {
    private static final String OLD_REPORT_FILE = "checkstyle.xml";
    private static final String NEW_REPORT_FILE = "checkstyle1.xml";

    /**
     * Verifies that {@link WarningChecksPublisher} constructs the {@link ChecksDetails} correctly
     * with only new issues.
     */
    @Test
    public void shouldCreateChecksDetailsWithNewIssuesAsAnnotations() {
        FreeStyleProject project = createFreeStyleProjectWithWorkspaceFiles(OLD_REPORT_FILE);
        enableCheckStyleWarnings(project);

        // trigger a build using the report file with 4 issues
        Run<?, ?> run = buildSuccessfully(project);
        assertThat(getAnalysisResult(run))
                .hasTotalSize(4)
                .hasNewSize(0);

        // trigger a build using the report file with 2 new issues
        copyMultipleFilesToWorkspaceWithSuffix(project, NEW_REPORT_FILE);
        run = buildSuccessfully(project);
        assertThat(getAnalysisResult(run))
                .hasTotalSize(6)
                .hasNewSize(2);

        // extract details from result
        WarningChecksPublisher publisher = new WarningChecksPublisher(getResultAction(run));
        ChecksDetails details = publisher.extractChecksDetails();

        // verify extracted details
        ChecksDetails expectedDetails = createChecksDetailsBasedOnReportFile();
        assertThat(details)
                .usingRecursiveComparison()
                .ignoringFields("detailsURL")
                .isEqualTo(expectedDetails);
    }

    private ChecksDetails createChecksDetailsBasedOnReportFile() {
        ChecksDetailsBuilder builder = new ChecksDetailsBuilder()
                .withName("CheckStyle")
                .withStatus(ChecksStatus.COMPLETED)
                .withConclusion(ChecksConclusion.SUCCESS)
                .withDetailsURL("http://localhost:39121/jenkins/job/test0/2/");


        ChecksOutput output = new ChecksOutputBuilder()
                .withTitle("CheckStyle Warnings")
                .withSummary("## 6 issues in total:\n"
                        + "- ### 2 new issues\n"
                        + "- ### 4 outstanding Issues\n"
                        + "- ### 2 delta issues\n"
                        + "- ### 0 fixed issues")
                .withText("## Total Issue Statistics:\n* Error: 6\n* High: 0\n* Normal: 0\n* Low: 0\n"
                        + "## New Issue Statistics:\n* Error: 2\n* High: 0\n* Normal: 0\n* Low: 0\n"
                        + "## Delta Issue Statistics:\n* Error: 2\n* High: 0\n* Normal: 0\n* Low: 0\n")
                .addAnnotation(new ChecksAnnotationBuilder()
                        .withPath("X:/Build/Results/jobs/Maven/workspace/tasks/src/main/java/hudson/plugins"
                                + "/tasks/parser/CsharpNamespaceDetector.java")
                        .withTitle("RightCurlyCheck")
                        .withAnnotationLevel(ChecksAnnotationLevel.WARNING)
                        .withMessage("ERROR: '}' sollte in derselben Zeile stehen.")
                        .withLine(30)
                        .withStartColumn(21)
                        .withEndColumn(21)
                        .withRawDetails("")
                        .build())
                .addAnnotation(new ChecksAnnotationBuilder()
                        .withPath("X:/Build/Results/jobs/Maven/workspace/tasks/src/main/java/hudson/plugins"
                                + "/tasks/parser/CsharpNamespaceDetector.java")
                        .withTitle("RightCurlyCheck")
                        .withAnnotationLevel(ChecksAnnotationLevel.WARNING)
                        .withMessage("ERROR: '}' sollte in derselben Zeile stehen.")
                        .withLine(37)
                        .withStartColumn(9)
                        .withEndColumn(9)
                        .withRawDetails("")
                        .build())
                .build();

        return builder
                .withOutput(output)
                .build();
    }
}

package io.jenkins.plugins.analysis.core.steps;

import java.util.Optional;
import java.util.function.Consumer;

import org.junit.Test;

import com.google.errorprone.annotations.CanIgnoreReturnValue;

import hudson.model.AbstractProject;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.model.Run;

import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerTest;
import io.jenkins.plugins.analysis.core.util.QualityGate.QualityGateResult;
import io.jenkins.plugins.analysis.core.util.QualityGate.QualityGateType;
import io.jenkins.plugins.analysis.warnings.checkstyle.CheckStyle;
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

        Run<?, ?> reference = buildSuccessfully(project);
        assertThat(getAnalysisResult(reference))
                .hasTotalSize(4)
                .hasNewSize(0);

        copyMultipleFilesToWorkspaceWithSuffix(project, NEW_REPORT_FILE);
        Run<?, ?> run = buildSuccessfully(project);
        assertThat(getAnalysisResult(run))
                .hasTotalSize(6)
                .hasNewSize(2);

        WarningChecksPublisher publisher = new WarningChecksPublisher(getResultAction(run));
        assertThat(publisher.extractChecksDetails())
                .hasFieldOrPropertyWithValue("detailsURL", Optional.of(getResultAction(run).getAbsoluteUrl()))
                .usingRecursiveComparison()
                .ignoringFields("detailsURL")
                .isEqualTo(createExpectedDetails());
    }

    @Test
    public void shouldConcludeChecksAsSuccessWhenQualityGateIsPassed() {
        FreeStyleProject project = createFreeStyleProjectWithWorkspaceFiles(NEW_REPORT_FILE);
        enableAndConfigureCheckstyle(project,
                recorder -> recorder.addQualityGate(10, QualityGateType.TOTAL, QualityGateResult.UNSTABLE));

        Run<?, ?> build = buildSuccessfully(project);
        WarningChecksPublisher publisher = new WarningChecksPublisher(getResultAction(build));

        assertThat(publisher.extractChecksDetails().getConclusion())
                .isEqualTo(ChecksConclusion.SUCCESS);
    }

    @Test
    public void shouldConcludeChecksAsFailureWhenQualityGateIsFailed() {
        FreeStyleProject project = createFreeStyleProjectWithWorkspaceFiles(NEW_REPORT_FILE);
        enableAndConfigureCheckstyle(project,
                recorder -> recorder.addQualityGate(1, QualityGateType.TOTAL, QualityGateResult.FAILURE));

        Run<?, ?> build = buildWithResult(project, Result.FAILURE);
        WarningChecksPublisher publisher = new WarningChecksPublisher(getResultAction(build));

        assertThat(publisher.extractChecksDetails().getConclusion())
                .isEqualTo(ChecksConclusion.FAILURE);
    }

    private ChecksDetails createExpectedDetails() {
        ChecksDetailsBuilder builder = new ChecksDetailsBuilder()
                .withName("CheckStyle")
                .withStatus(ChecksStatus.COMPLETED)
                .withConclusion(ChecksConclusion.SUCCESS);

        ChecksOutput output = new ChecksOutputBuilder()
                .withTitle("CheckStyle Warnings")
                .withSummary("## 6 issues in total:\n"
                        + "- ### 2 new issues\n"
                        + "- ### 4 outstanding issues\n"
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

    @CanIgnoreReturnValue
    private IssuesRecorder enableAndConfigureCheckstyle(final AbstractProject<?, ?> job,
            final Consumer<IssuesRecorder> configuration) {
        IssuesRecorder item = new IssuesRecorder();
        item.setTools(createTool(new CheckStyle(), "**/*issues.txt"));
        job.getPublishersList().add(item);
        configuration.accept(item);
        return item;
    }
}

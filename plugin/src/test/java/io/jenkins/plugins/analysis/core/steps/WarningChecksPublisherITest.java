package io.jenkins.plugins.analysis.core.steps;

import java.util.Optional;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import com.google.errorprone.annotations.CanIgnoreReturnValue;

import hudson.model.AbstractProject;
import hudson.model.FreeStyleProject;
import hudson.model.Run;
import hudson.model.TaskListener;

import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.core.util.QualityGate.QualityGateResult;
import io.jenkins.plugins.analysis.core.util.QualityGate.QualityGateType;
import io.jenkins.plugins.analysis.warnings.PVSStudio;
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
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link WarningChecksPublisher}.
 *
 * @author Kezhi Xiong
 */
public class WarningChecksPublisherITest extends IntegrationTestWithJenkinsPerSuite {
    private static final String OLD_CHECKSTYLE_REPORT = "checkstyle.xml";
    private static final String NEW_CHECKSTYLE_REPORT = "checkstyle1.xml";

    /**
     * Verifies that {@link WarningChecksPublisher} constructs the {@link ChecksDetails} correctly
     * with only new issues.
     */
    @Test
    public void shouldCreateChecksDetailsWithNewIssuesAsAnnotations() {
        FreeStyleProject project = createFreeStyleProjectWithWorkspaceFiles(OLD_CHECKSTYLE_REPORT);
        enableCheckStyleWarnings(project);

        Run<?, ?> reference = buildSuccessfully(project);
        assertThat(getAnalysisResult(reference))
                .hasTotalSize(4)
                .hasNewSize(0);

        copyMultipleFilesToWorkspaceWithSuffix(project, NEW_CHECKSTYLE_REPORT);
        Run<?, ?> run = buildSuccessfully(project);
        assertThat(getAnalysisResult(run))
                .hasTotalSize(6)
                .hasNewSize(2);

        WarningChecksPublisher publisher = new WarningChecksPublisher(getResultAction(run), mock(TaskListener.class));
        assertThat(publisher.extractChecksDetails())
                .hasFieldOrPropertyWithValue("detailsURL", Optional.of(getResultAction(run).getAbsoluteUrl()))
                .usingRecursiveComparison()
                .ignoringFields("detailsURL")
                .isEqualTo(createExpectedCheckStyleDetails());
    }

    @Test
    public void shouldConcludeChecksAsSuccessWhenQualityGateIsPassed() {
        FreeStyleProject project = createFreeStyleProjectWithWorkspaceFiles(NEW_CHECKSTYLE_REPORT);
        enableAndConfigureCheckstyle(project,
                recorder -> recorder.addQualityGate(10, QualityGateType.TOTAL, QualityGateResult.UNSTABLE));

        Run<?, ?> build = buildSuccessfully(project);
        WarningChecksPublisher publisher = new WarningChecksPublisher(getResultAction(build), mock(TaskListener.class));

        assertThat(publisher.extractChecksDetails().getConclusion())
                .isEqualTo(ChecksConclusion.SUCCESS);
    }

    @Test
    public void shouldConcludeChecksAsFailureWhenQualityGateIsFailed() {
        assertChecksConclusionIsFailureWithQualityGateResult(QualityGateResult.FAILURE);
    }

    @Test
    public void shouldConcludeChecksAsFailureWhenQualityGateResultIsUnstable() {
        assertChecksConclusionIsFailureWithQualityGateResult(QualityGateResult.UNSTABLE);
    }

    @Test
    public void shouldParseHtmlMessage() {
        FreeStyleProject project = createFreeStyleProject();
        enableWarnings(project, new PVSStudio());

        buildSuccessfully(project);

        copySingleFileToWorkspace(project, "PVSReport.xml", "PVSReport.plog");
        Run<?, ?> run = buildSuccessfully(project);

        WarningChecksPublisher publisher = new WarningChecksPublisher(getResultAction(run), mock(TaskListener.class));
        ChecksDetails details = publisher.extractChecksDetails();

        assertThat(details.getOutput().get().getChecksAnnotations())
                .usingElementComparatorOnFields("message")
                .containsOnly(new ChecksAnnotationBuilder()
                        .withMessage("ERROR:\n"
                                + "Some diagnostic messages may contain incorrect line number.\n"
                                + "V002:https://www.viva64.com/en/w/v002/")
                        .build());
    }

    @Test
    public void shouldReportNoIssuesInTitle() {
        FreeStyleProject project = createFreeStyleProject();
        enableCheckStyleWarnings(project);

        Run<?, ?> run = buildSuccessfully(project);
        assertThat(getAnalysisResult(run))
                .hasTotalSize(0)
                .hasNewSize(0);

        assertThat(new WarningChecksPublisher(getResultAction(run), mock(TaskListener.class))
                .extractChecksDetails().getOutput())
                .isPresent()
                .get()
                .hasFieldOrPropertyWithValue("title", Optional.of("No issues."));
    }

    @Test
    public void shouldReportOnlyTotalIssuesInTitleWhenNoNewIssues() {
        FreeStyleProject project = createFreeStyleProjectWithWorkspaceFiles(OLD_CHECKSTYLE_REPORT);
        enableCheckStyleWarnings(project);

        Run<?, ?> run = buildSuccessfully(project);
        assertThat(getAnalysisResult(run))
                .hasTotalSize(4)
                .hasNewSize(0);

        assertThat(new WarningChecksPublisher(getResultAction(run), mock(TaskListener.class))
                .extractChecksDetails().getOutput())
                .isPresent()
                .get()
                .hasFieldOrPropertyWithValue("title", Optional.of("No new issues, 4 total."));
    }

    @Test
    public void shouldReportOnlyNewIssuesInTitleWhenAllIssuesAreNew() {
        FreeStyleProject project = createFreeStyleProject();
        enableCheckStyleWarnings(project);

        Run<?, ?> reference = buildSuccessfully(project);
        assertThat(getAnalysisResult(reference))
                .hasTotalSize(0)
                .hasNewSize(0);

        copyMultipleFilesToWorkspaceWithSuffix(project, NEW_CHECKSTYLE_REPORT);
        Run<?, ?> run = buildSuccessfully(project);
        assertThat(getAnalysisResult(run))
                .hasTotalSize(6)
                .hasNewSize(6);

        assertThat(new WarningChecksPublisher(getResultAction(run), mock(TaskListener.class))
                .extractChecksDetails().getOutput())
                .isPresent()
                .get()
                .hasFieldOrPropertyWithValue("title", Optional.of("6 new issues."));
    }

    private ChecksDetails createExpectedCheckStyleDetails() {
        ChecksDetailsBuilder builder = new ChecksDetailsBuilder()
                .withName("CheckStyle")
                .withStatus(ChecksStatus.COMPLETED)
                .withConclusion(ChecksConclusion.SUCCESS);

        ChecksOutput output = new ChecksOutputBuilder()
                .withTitle("2 new issues, 6 total.")
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
                        .withMessage("ERROR:\n'}' sollte in derselben Zeile stehen.")
                        .withLine(30)
                        .withStartColumn(21)
                        .withEndColumn(21)
                        .withRawDetails(StringUtils.normalizeSpace("<p>Since Checkstyle 3.0</p><p>\n"
                                + "Checks the placement of right curly braces (<code>'}'</code>)\n"
                                + "for if-else, try-catch-finally blocks, while-loops, for-loops,\n"
                                + "method definitions, class definitions, constructor definitions,\n"
                                + "instance and static initialization blocks.\n"
                                + "The policy to verify is specified using the property <code> option</code>.\n"
                                + "For right curly brace of expression blocks please follow issue\n"
                                + "<a href=\"https://github.com/checkstyle/checkstyle/issues/5945\">#5945</a>.\n"
                                + "</p>"))
                        .build())
                .addAnnotation(new ChecksAnnotationBuilder()
                        .withPath("X:/Build/Results/jobs/Maven/workspace/tasks/src/main/java/hudson/plugins"
                                + "/tasks/parser/CsharpNamespaceDetector.java")
                        .withTitle("RightCurlyCheck")
                        .withAnnotationLevel(ChecksAnnotationLevel.WARNING)
                        .withMessage("ERROR:\n'}' sollte in derselben Zeile stehen.")
                        .withLine(37)
                        .withStartColumn(9)
                        .withEndColumn(9)
                        .withRawDetails(StringUtils.normalizeSpace("<p>Since Checkstyle 3.0</p><p>\n"
                                + "Checks the placement of right curly braces (<code>'}'</code>)\n"
                                + "for if-else, try-catch-finally blocks, while-loops, for-loops,\n"
                                + "method definitions, class definitions, constructor definitions,\n"
                                + "instance and static initialization blocks.\n"
                                + "The policy to verify is specified using the property <code> option</code>.\n"
                                + "For right curly brace of expression blocks please follow issue\n"
                                + "<a href=\"https://github.com/checkstyle/checkstyle/issues/5945\">#5945</a>.\n"
                                + "</p>"))
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

    private void assertChecksConclusionIsFailureWithQualityGateResult(final QualityGateResult qualityGateResult) {
        FreeStyleProject project = createFreeStyleProjectWithWorkspaceFiles(NEW_CHECKSTYLE_REPORT);
        enableAndConfigureCheckstyle(project,
                recorder -> recorder.addQualityGate(1, QualityGateType.TOTAL, qualityGateResult));

        Run<?, ?> build = buildWithResult(project, qualityGateResult.getStatus().getResult());
        assertThat(getAnalysisResult(build))
                .hasTotalSize(6)
                .hasQualityGateStatus(qualityGateResult.getStatus());

        WarningChecksPublisher publisher = new WarningChecksPublisher(getResultAction(build), mock(TaskListener.class));
        assertThat(publisher.extractChecksDetails().getConclusion())
                .isEqualTo(ChecksConclusion.FAILURE);
    }
}

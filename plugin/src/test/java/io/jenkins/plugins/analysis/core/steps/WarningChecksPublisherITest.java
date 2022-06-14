package io.jenkins.plugins.analysis.core.steps;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.TestExtension;

import com.google.errorprone.annotations.CanIgnoreReturnValue;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import hudson.model.AbstractProject;
import hudson.model.FreeStyleProject;
import hudson.model.Run;
import hudson.model.TaskListener;

import io.jenkins.plugins.analysis.core.steps.WarningChecksPublisher.AnnotationScope;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.core.util.QualityGate.QualityGateResult;
import io.jenkins.plugins.analysis.core.util.QualityGate.QualityGateType;
import io.jenkins.plugins.analysis.warnings.CheckStyle;
import io.jenkins.plugins.analysis.warnings.PVSStudio;
import io.jenkins.plugins.analysis.warnings.Pmd;
import io.jenkins.plugins.checks.api.ChecksAnnotation.ChecksAnnotationBuilder;
import io.jenkins.plugins.checks.api.ChecksAnnotation.ChecksAnnotationLevel;
import io.jenkins.plugins.checks.api.ChecksConclusion;
import io.jenkins.plugins.checks.api.ChecksDetails;
import io.jenkins.plugins.checks.api.ChecksDetails.ChecksDetailsBuilder;
import io.jenkins.plugins.checks.api.ChecksOutput;
import io.jenkins.plugins.checks.api.ChecksOutput.ChecksOutputBuilder;
import io.jenkins.plugins.checks.api.ChecksPublisherFactory;
import io.jenkins.plugins.checks.api.ChecksStatus;
import io.jenkins.plugins.checks.util.CapturingChecksPublisher;
import io.jenkins.plugins.checks.util.CapturingChecksPublisher.Factory;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

/**
 * Tests the class {@link WarningChecksPublisher}.
 *
 * @author Kezhi Xiong
 */
class WarningChecksPublisherITest extends IntegrationTestWithJenkinsPerSuite {
    private static final String OLD_CHECKSTYLE_REPORT = "checkstyle.xml";
    private static final String NEW_CHECKSTYLE_REPORT = "checkstyle1.xml";

    /**
     * Verifies that {@link WarningChecksPublisher} constructs the {@link ChecksDetails} correctly with only new
     * issues.
     */
    @Test
    void shouldCreateChecksDetailsWithNewIssuesAsAnnotations() {
        WorkflowJob project = createPipelineWithWorkspaceFilesWithSuffix(OLD_CHECKSTYLE_REPORT, NEW_CHECKSTYLE_REPORT);

        configureScanner(project, "checkstyle", "");

        Run<?, ?> reference = buildSuccessfully(project);
        assertThat(getAnalysisResult(reference))
                .hasTotalSize(4)
                .hasNewSize(0);

        configureScanner(project, "checkstyle1", "");
        Run<?, ?> run = buildSuccessfully(project);
        assertThat(getAnalysisResult(run))
                .hasTotalSize(6)
                .hasNewSize(2);

        WarningChecksPublisher publisher = new WarningChecksPublisher(getResultAction(run), TaskListener.NULL, null);
        assertThat(publisher.extractChecksDetails(AnnotationScope.PUBLISH_NEW_ISSUES))
                .hasFieldOrPropertyWithValue("detailsURL", Optional.of(getResultAction(run).getAbsoluteUrl()))
                .usingRecursiveComparison()
                .ignoringFields("detailsURL", "output.value.summary.value")
                .isEqualTo(createExpectedCheckStyleDetails());
        assertThat(publisher.extractChecksDetails(AnnotationScope.PUBLISH_NEW_ISSUES).getOutput()).isPresent()
                .get()
                .satisfies(
                        output -> {
                            assertThat(output.getSummary()).isPresent().get().asString()
                                    .startsWith("|Total|New|Outstanding|Fixed|Trend\n"
                                            + "|:-:|:-:|:-:|:-:|:-:\n"
                                            + "|6|2|4|0|:-1:\n\n"
                                            + "Reference build: <a href=\"http://localhost:")
                                    .endsWith("#1</a>");
                            assertThat(output.getChecksAnnotations()).hasSize(2);
                        });
        assertThat(publisher.extractChecksDetails(AnnotationScope.PUBLISH_ALL_ISSUES).getOutput()).isPresent().get()
                .satisfies(
                        output -> assertThat(output.getChecksAnnotations()).hasSize(6));
    }

    private void configureScanner(final WorkflowJob job, final String fileName, final String parameters) {
        job.setDefinition(new CpsFlowDefinition("node {\n"
                + "  stage ('Integration Test') {\n"
                + "         recordIssues tool: checkStyle(pattern: '**/" + fileName + "-*') "
                + parameters
                + "\n"
                + "  }\n"
                + "}", true));
    }

    /**
     * Verifies that {@link WarningChecksPublisher} correctly reports a successful quality gate.
     */
    @Test
    void shouldConcludeChecksAsSuccessWhenQualityGateIsPassed() {
        FreeStyleProject project = createFreeStyleProjectWithWorkspaceFilesWithSuffix(NEW_CHECKSTYLE_REPORT);
        enableAndConfigureCheckstyle(project,
                recorder -> recorder.addQualityGate(10, QualityGateType.TOTAL, QualityGateResult.UNSTABLE));

        Run<?, ?> build = buildSuccessfully(project);
        WarningChecksPublisher publisher = new WarningChecksPublisher(getResultAction(build), TaskListener.NULL, null);

        assertThat(publisher.extractChecksDetails(AnnotationScope.PUBLISH_NEW_ISSUES).getConclusion())
                .isEqualTo(ChecksConclusion.SUCCESS);
    }

    /**
     * Verifies that {@link WarningChecksPublisher} correctly reports a failed quality gate.
     */
    @Test
    void shouldConcludeChecksAsFailureWhenQualityGateIsFailed() {
        assertChecksConclusionIsFailureWithQualityGateResult(QualityGateResult.FAILURE);
    }

    /**
     * Verifies that {@link WarningChecksPublisher} correctly reports an unstable quality gate.
     */
    @Test
    void shouldConcludeChecksAsFailureWhenQualityGateResultIsUnstable() {
        assertChecksConclusionIsFailureWithQualityGateResult(QualityGateResult.UNSTABLE);
    }

    /**
     * Verifies that {@link WarningChecksPublisher} correctly handles HTML tags in messages.
     */
    @Test
    void shouldParseHtmlMessage() {
        FreeStyleProject project = createFreeStyleProject();
        enableWarnings(project, new PVSStudio());

        buildSuccessfully(project);

        copySingleFileToWorkspace(project, "PVSReport.xml", "PVSReport.plog");
        Run<?, ?> run = buildSuccessfully(project);

        WarningChecksPublisher publisher = new WarningChecksPublisher(getResultAction(run), TaskListener.NULL, null);
        ChecksDetails details = publisher.extractChecksDetails(AnnotationScope.PUBLISH_NEW_ISSUES);

        assertThat(details.getOutput().get().getChecksAnnotations())
                .usingRecursiveFieldByFieldElementComparatorOnFields("message")
                .containsOnly(new ChecksAnnotationBuilder()
                        .withMessage("ERROR:\n"
                                + "Some diagnostic messages may contain incorrect line number.\n"
                                + "V002:https://pvs-studio.com/en/docs/warnings/v002/")
                        .build());
    }

    /**
     * Verifies that {@link WarningChecksPublisher} correctly handles the special case of zero issues.
     */
    @Test
    void shouldReportNoIssuesInTitle() {
        FreeStyleProject project = createFreeStyleProject();
        enableCheckStyleWarnings(project);

        Run<?, ?> run = buildSuccessfully(project);
        assertThat(getAnalysisResult(run))
                .hasTotalSize(0)
                .hasNewSize(0);

        assertThat(new WarningChecksPublisher(getResultAction(run), TaskListener.NULL, null)
                .extractChecksDetails(AnnotationScope.PUBLISH_NEW_ISSUES).getOutput())
                .isPresent()
                .get()
                .hasFieldOrPropertyWithValue("title", Optional.of("No issues."));
    }

    /**
     * Verifies that {@link WarningChecksPublisher} correctly shows only the totals in the title if there are no new
     * issues.
     */
    @Test
    void shouldReportOnlyTotalIssuesInTitleWhenNoNewIssues() {
        FreeStyleProject project = createFreeStyleProjectWithWorkspaceFilesWithSuffix(OLD_CHECKSTYLE_REPORT);
        enableCheckStyleWarnings(project);

        Run<?, ?> run = buildSuccessfully(project);
        assertThat(getAnalysisResult(run))
                .hasTotalSize(4)
                .hasNewSize(0);

        assertThat(new WarningChecksPublisher(getResultAction(run), TaskListener.NULL, null)
                .extractChecksDetails(AnnotationScope.PUBLISH_NEW_ISSUES).getOutput())
                .isPresent()
                .get()
                .hasFieldOrPropertyWithValue("title", Optional.of("No new issues, 4 total."));
    }

    /**
     * Verifies that {@link WarningChecksPublisher} correctly shows the totals and the new issues in the title.
     */
    @Test
    void shouldReportOnlyNewIssuesInTitleWhenAllIssuesAreNew() {
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

        assertThat(new WarningChecksPublisher(getResultAction(run), TaskListener.NULL, null)
                .extractChecksDetails(AnnotationScope.PUBLISH_NEW_ISSUES).getOutput())
                .isPresent()
                .get()
                .hasFieldOrPropertyWithValue("title", Optional.of("6 new issues."));
    }

    /**
     * Verifies that {@link WarningChecksPublisher} correctly ignores the columns if the issue refers to several lines.
     */
    @Test
    void shouldIgnoreColumnsWhenBuildMultipleLineAnnotation() {
        FreeStyleProject project = createFreeStyleProject();
        enableWarnings(project, new Pmd());

        buildSuccessfully(project);

        copySingleFileToWorkspace(project, "pmd.xml");
        Run<?, ?> run = buildSuccessfully(project);

        WarningChecksPublisher publisher = new WarningChecksPublisher(getResultAction(run), TaskListener.NULL, null);
        ChecksDetails details = publisher.extractChecksDetails(AnnotationScope.PUBLISH_NEW_ISSUES);

        assertThat(details.getOutput().get().getChecksAnnotations().get(0))
                .hasFieldOrPropertyWithValue("startLine", Optional.of(123))
                .hasFieldOrPropertyWithValue("endLine", Optional.of(125))
                .hasFieldOrPropertyWithValue("startColumn", Optional.empty())
                .hasFieldOrPropertyWithValue("endColumn", Optional.empty());
    }

    /**
     * Tests that {@code publishIssues} uses correct default name.
     */
    @Test
    void shouldUseDefaultChecksNamePublishIssues() {
        WorkflowJob project = createPipelineWithWorkspaceFilesWithSuffix(NEW_CHECKSTYLE_REPORT);
        project.setDefinition(asStage(createScanForIssuesStep(new CheckStyle()), PUBLISH_ISSUES_STEP));
        buildSuccessfully(project);

        List<ChecksDetails> publishedChecks = getPublishedChecks();

        assertThat(publishedChecks).hasSize(1);

        assertThat(publishedChecks.get(0).getName()).isPresent().get().isEqualTo("CheckStyle");

        assertThat(publishedChecks.get(0).getOutput()).isPresent().hasValueSatisfying(
                output -> assertThat(output.getTitle()).isPresent().get().isEqualTo("No new issues, 6 total."));
    }

    /**
     * Tests that {@code recordIssues} either reports all or only new issues.
     */
    @Test
    void shouldReportSelectedIssues() {
        buildCheckForNewAndOutstandingWarnings(AnnotationScope.PUBLISH_NEW_ISSUES, 2);
        buildCheckForNewAndOutstandingWarnings(AnnotationScope.PUBLISH_ALL_ISSUES, 6);
    }

    private void buildCheckForNewAndOutstandingWarnings(final AnnotationScope scope, final int expectedSize) {
        WorkflowJob project = createPipelineWithWorkspaceFilesWithSuffix(OLD_CHECKSTYLE_REPORT, NEW_CHECKSTYLE_REPORT);
        configureScanner(project, "checkstyle", ", publishAllIssues: "
                + (scope == AnnotationScope.PUBLISH_ALL_ISSUES));
        buildSuccessfully(project);
        resetCapturedChecks();

        configureScanner(project, "checkstyle1", ", publishAllIssues: "
                + (scope == AnnotationScope.PUBLISH_ALL_ISSUES));
        buildSuccessfully(project);

        List<ChecksDetails> publishedChecks = getPublishedChecks();

        assertThat(publishedChecks).hasSize(1);

        ChecksDetails details = publishedChecks.get(0);
        assertThat(details.getName()).isPresent().get().isEqualTo("CheckStyle");
        assertThat(details.getOutput()).isPresent().hasValueSatisfying(
                output -> {
                    assertThat(output.getTitle()).isPresent().get().isEqualTo("2 new issues, 6 total.");
                    assertThat(output.getChecksAnnotations()).hasSize(expectedSize);
                });
    }

    /**
     * Tests that {@code recordIssues} uses correct default name.
     */
    @Test
    void shouldUseDefaultChecksNameRecordIssues() {
        WorkflowJob project = createPipelineWithWorkspaceFilesWithSuffix(NEW_CHECKSTYLE_REPORT);
        project.setDefinition(asStage(createRecordIssuesStep(new CheckStyle())));
        buildSuccessfully(project);

        List<ChecksDetails> publishedChecks = getPublishedChecks();

        assertThat(publishedChecks).hasSize(1);

        ChecksDetails details = publishedChecks.get(0);
        assertThat(details.getName()).isPresent().get().isEqualTo("CheckStyle");
        assertThat(details.getOutput()).isPresent().hasValueSatisfying(
                output -> assertThat(output.getTitle()).isPresent().get().isEqualTo("No new issues, 6 total."));
    }

    /**
     * Tests that {@code publishIssues} honors the checks name provided by a {@code withChecks} context.
     */
    @Test
    void shouldHonorWithChecksContextPublishIssues() {
        WorkflowJob project = createPipelineWithWorkspaceFilesWithSuffix(NEW_CHECKSTYLE_REPORT);
        project.setDefinition(asStage("withChecks('Custom Checks Name') {", createScanForIssuesStep(new CheckStyle()),
                PUBLISH_ISSUES_STEP, "}"));
        buildSuccessfully(project);

        List<ChecksDetails> publishedChecks = getPublishedChecks();

        assertThat(publishedChecks).hasSize(
                2);  // First from 'In progress' check provided by withChecks, second from publishIssues

        publishedChecks.forEach(check -> assertThat(check.getName()).isPresent().get().isEqualTo("Custom Checks Name"));

        assertThat(publishedChecks.get(1).getOutput()).isPresent().hasValueSatisfying(
                output -> assertThat(output.getTitle()).isPresent().get().isEqualTo("No new issues, 6 total."));
    }

    /**
     * Tests that {@code recordIssues} honors the checks name provided by a {@code withChecks} context.
     */
    @Test
    void shouldHonorWithChecksContextRecordIssues() {
        WorkflowJob project = createPipelineWithWorkspaceFilesWithSuffix(NEW_CHECKSTYLE_REPORT);
        project.setDefinition(
                asStage("withChecks('Custom Checks Name') {", createRecordIssuesStep(new CheckStyle()), "}"));
        buildSuccessfully(project);

        List<ChecksDetails> publishedChecks = getPublishedChecks();

        assertThat(publishedChecks).hasSize(
                2);  // First from 'In progress' check provided by withChecks, second from recordIssues

        publishedChecks.forEach(check -> assertThat(check.getName()).isPresent().get().isEqualTo("Custom Checks Name"));

        assertThat(publishedChecks.get(1).getOutput()).isPresent().hasValueSatisfying(
                output -> assertThat(output.getTitle()).isPresent().get().isEqualTo("No new issues, 6 total."));
    }

    private ChecksDetails createExpectedCheckStyleDetails() {
        ChecksDetailsBuilder builder = new ChecksDetailsBuilder()
                .withName("CheckStyle")
                .withStatus(ChecksStatus.COMPLETED)
                .withConclusion(ChecksConclusion.SUCCESS);

        ChecksOutput output = new ChecksOutputBuilder()
                .withTitle("2 new issues, 6 total.")
                .withSummary("") // summary value is checked directly since it is using a random port
                .withText("## Severity distribution of new issues\n"
                        + "|Error|Warning High|Warning Normal|Warning Low\n"
                        + "|:-:|:-:|:-:|:-:\n"
                        + "|2|0|0|0\n")
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
        FreeStyleProject project = createFreeStyleProjectWithWorkspaceFilesWithSuffix(NEW_CHECKSTYLE_REPORT);
        enableAndConfigureCheckstyle(project,
                recorder -> recorder.addQualityGate(1, QualityGateType.TOTAL, qualityGateResult));

        Run<?, ?> build = buildWithResult(project, qualityGateResult.getStatus().getResult());
        assertThat(getAnalysisResult(build))
                .hasTotalSize(6)
                .hasQualityGateStatus(qualityGateResult.getStatus());

        WarningChecksPublisher publisher = new WarningChecksPublisher(getResultAction(build), TaskListener.NULL, null);
        assertThat(publisher.extractChecksDetails(AnnotationScope.PUBLISH_NEW_ISSUES).getConclusion())
                .isEqualTo(ChecksConclusion.FAILURE);
    }

    private List<ChecksDetails> getPublishedChecks() {
        return getFactory().getPublishedChecks();
    }

    private CapturingChecksPublisher.Factory getFactory()  {
        return getJenkins().getInstance().getExtensionList(ChecksPublisherFactory.class)
                .stream()
                .filter(f -> f instanceof Factory)
                .map(f -> (Factory) f)
                .findAny()
                .orElseThrow(() -> new AssertionError("No CapturingChecksPublisher registered as @TestExtension?"));
    }

    @AfterEach
    void resetCapturedChecks() {
        getPublishedChecks().clear();
    }

    /**
     * Capturing checks publisher for inspection of checks created during a run.
     */
    @TestExtension
    public static class CapturingChecksPublisherTestExtension extends CapturingChecksPublisher.Factory {
        // activate test extension
    }
}

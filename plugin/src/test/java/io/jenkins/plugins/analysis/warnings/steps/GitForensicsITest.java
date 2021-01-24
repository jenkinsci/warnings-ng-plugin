package io.jenkins.plugins.analysis.warnings.steps;

import org.junit.Test;

import org.jenkinsci.plugins.workflow.job.WorkflowJob;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.forensics.blame.FileBlame;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

/**
 * Verifies that Git blamer and miner are correctly called.
 *
 * @author Ullrich Hafner
 */
public class GitForensicsITest extends IntegrationTestWithJenkinsPerSuite {
    private static final String JAVA_ONE_WARNING = "java-start-rev0.txt";
    private static final String PUBLISH_ISSUES_STEP = "publishIssues issues:[issues]";
    private static final String CHECKOUT_FORENSICS_API = "checkout([$class: 'GitSCM', "
            + "branches: [[name: 'a6d0ef09ab3c418e370449a884da99b8190ae950' ]],\n"
            + "userRemoteConfigs: [[url: 'https://github.com/jenkinsci/forensics-api-plugin.git']],\n"
            + "extensions: [[$class: 'RelativeTargetDirectory', \n"
            + "            relativeTargetDir: 'forensics-api']]])";
    private static final String MINE_REPOSITORY = "mineRepository()";
    private static final String SCM_RESOLVER = "src/main/java/io/jenkins/plugins/forensics/util/ScmResolver.java";
    private static final int AFFECTED_LINE = 20;

    /**
     * Checks out an existing Git repository and starts a pipeline with the scan and publish steps. Verifies that the
     * Git forensics plugin is correctly invoked.
     */
    @Test
    public void shouldObtainBlamesAndForensicsWithScanAndPublishIssuesSteps() {
        runStepAndVerifyBlamesAndForensics("def issues = scanForIssues "
                + "sourceDirectory: 'forensics-api', "
                + "tool: java(pattern:'**/*issues.txt', reportEncoding:'UTF-8')\n"
                + PUBLISH_ISSUES_STEP);
    }

    /**
     * Checks out an existing Git repository and starts a pipeline with the record step. Verifies that the Git forensics
     * plugin is correctly invoked.
     */
    @Test
    public void shouldObtainBlamesAndForensicsWithRecordIssuesStep() {
        runStepAndVerifyBlamesAndForensics("recordIssues "
                + "sourceDirectory: 'forensics-api', "
                + "tool: java(pattern:'**/*issues.txt', reportEncoding:'UTF-8')");
    }

    private void runStepAndVerifyBlamesAndForensics(final String step) {
        WorkflowJob job = createPipelineWithWorkspaceFiles();

        createFileInWorkspace(job, "java-issues.txt", createJavaWarning(SCM_RESOLVER, AFFECTED_LINE));

        job.setDefinition(asStage(CHECKOUT_FORENSICS_API, MINE_REPOSITORY, step));

        AnalysisResult result = scheduleSuccessfulBuild(job);

        assertThat(result).hasTotalSize(1).hasNewSize(0).hasFixedSize(0);
        assertThat(result.getBlames().contains(SCM_RESOLVER)).isTrue();

        FileBlame blame = result.getBlames().getBlame(SCM_RESOLVER);
        assertThat(blame.getFileName()).isEqualTo(SCM_RESOLVER);
        assertThat(blame.getEmail(AFFECTED_LINE)).isEqualTo("ullrich.hafner@gmail.com");
        assertThat(blame.getCommit(AFFECTED_LINE)).isEqualTo("43dde5d4f7a06122216494a896c51830ed684572");

        assertThat(getConsoleLog(result)).contains(
                "Invoking Git blamer to create author and commit information for 1 affected files",
                "Git commit ID = 'a6d0ef09ab3c418e370449a884da99b8190ae950'",
                "-> blamed authors of issues in 1 files",
                "Extracting repository forensics for 1 affected files (files in repository: 121)",
                "-> 1 affected files processed");
    }

    /**
     * Checks out an existing Git repository and starts a pipeline with the scan and publish steps. Verifies that the
     * Git forensics plugin is correctly invoked.
     */
    @Test
    public void shouldSkipBlamesAndForensicsWithScanAndPublishIssuesSteps() {
        runStepAndVerifyScmSkipping("def issues = scanForIssues "
                + "sourceDirectory: 'forensics-api',"
                + "scm: 'nothing', "
                + "tool: java(pattern:'**/*issues.txt', reportEncoding:'UTF-8')\n"
                + PUBLISH_ISSUES_STEP);
    }

    /**
     * Checks out an existing Git repository and starts a pipeline with the record step. Verifies that the Git forensics
     * plugin is correctly invoked.
     */
    @Test
    public void shouldSkipBlamesAndForensicsWithRecordIssuesStep() {
        runStepAndVerifyScmSkipping("recordIssues "
                + "sourceDirectory: 'forensics-api',"
                + "scm: 'nothing', "
                + "tool: java(pattern:'**/*issues.txt', reportEncoding:'UTF-8')");
    }

    private void runStepAndVerifyScmSkipping(final String step) {
        WorkflowJob job = createPipelineWithWorkspaceFiles(JAVA_ONE_WARNING);
        job.setDefinition(asStage(CHECKOUT_FORENSICS_API, MINE_REPOSITORY, step));

        AnalysisResult result = scheduleSuccessfulBuild(job);

        assertThat(result).hasTotalSize(1).hasNewSize(0).hasFixedSize(0);

        assertThat(getConsoleLog(result)).contains(
                "Creating SCM blamer to obtain author and commit information for affected files",
                "-> Filtering SCMs by key 'nothing'",
                "-> no SCM found",
                "Extracting repository forensics for 1 affected files (files in repository: 0)",
                "-> 0 affected files processed");
    }
}

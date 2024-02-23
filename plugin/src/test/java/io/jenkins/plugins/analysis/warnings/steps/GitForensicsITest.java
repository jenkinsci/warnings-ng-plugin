package io.jenkins.plugins.analysis.warnings.steps;

import java.io.IOException;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import hudson.model.FreeStyleProject;
import hudson.plugins.git.BranchSpec;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.extensions.impl.RelativeTargetDirectory;
import jenkins.model.ParameterizedJobMixIn.ParameterizedJob;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.warnings.Java;
import io.jenkins.plugins.forensics.blame.FileBlame;
import io.jenkins.plugins.forensics.miner.RepositoryMinerStep;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

/**
 * Verifies that Git blamer and miner are correctly called.
 *
 * @author Ullrich Hafner
 */
class GitForensicsITest extends IntegrationTestWithJenkinsPerSuite {
    private static final String JAVA_ONE_WARNING = "java-start-rev0.txt";
    private static final String PUBLISH_ISSUES_STEP = "publishIssues issues:[issues]";
    private static final String FORENSICS_API_PLUGIN = "https://github.com/jenkinsci/forensics-api-plugin.git";
    private static final String OLD_COMMIT = "3097ea1037fb809c84d2752d7f98f71eedcfe97a";
    private static final String COMMIT = "a6d0ef09ab3c418e370449a884da99b8190ae950";
    private static final String CHECKOUT_FORENSICS_API = checkout(COMMIT);
    private static final String MODIFIED_FILE = "src/main/java/io/jenkins/plugins/forensics/blame/Blames.java";

    private static String checkout(final String commit) {
        return "checkout([$class: 'GitSCM', "
                + "branches: [[name: '" + commit
                + "' ]],\n"
                + "userRemoteConfigs: [[url: '" + FORENSICS_API_PLUGIN + "']],\n"
                + "extensions: [[$class: 'RelativeTargetDirectory', \n"
                + "            relativeTargetDir: 'forensics-api']]])";
    }

    private static final String MINE_REPOSITORY = "mineRepository()";
    private static final String SCM_RESOLVER = "src/main/java/io/jenkins/plugins/forensics/util/ScmResolver.java";
    private static final int AFFECTED_LINE = 20;

    /**
     * Checks out an existing Git repository and starts a pipeline with the scan and publish steps. Verifies that the
     * Git forensics plugin is correctly invoked.
     */
    @Test
    void shouldObtainBlamesAndForensicsWithScanAndPublishIssuesSteps() {
        runStepAndVerifyBlamesAndForensics(createScanForIssuesStep("sourceDirectories: [[path: 'forensics-api']]"));
        runStepAndVerifyBlamesAndForensics(
                createScanForIssuesStep("sourceDirectories: [[path: 'does-not-exist'], [path: 'forensics-api']]"));
    }

    private String createScanForIssuesStep(final String sourceDirectories) {
        return "def issues = scanForIssues "
                + sourceDirectories
                + ", tool: java(pattern:'**/*issues.txt', reportEncoding:'UTF-8')\n"
                + PUBLISH_ISSUES_STEP;
    }

    /**
     * Checks out an existing Git repository and starts a pipeline with the record step. Verifies that the Git forensics
     * plugin is correctly invoked.
     */
    @Test
    void shouldObtainBlamesAndForensicsWithRecordIssuesStep() {
        runStepAndVerifyBlamesAndForensics(createRecordIssuesStep("sourceDirectories: [[path: 'forensics-api']]"));
        runStepAndVerifyBlamesAndForensics(createRecordIssuesStep("sourceDirectories: [[path: 'does-not-exist'], [path: 'forensics-api']]"));
    }

    private String createRecordIssuesStep(final String sourceDirectories) {
        return "recordIssues "
                + sourceDirectories
                + ", tool: java(pattern:'**/*issues.txt', reportEncoding:'UTF-8')";
    }

    /**
     * Checks out an existing Git repository and starts a freestyle job. Verifies that the Git forensics plugin is
     * correctly invoked.
     */
    @Test
    void shouldObtainBlamesAndForensicsInFreestyleJob() throws IOException {
        FreeStyleProject job = createFreeStyleProject();

        createFileInWorkspace(job, "java-issues.txt", createJavaWarning(SCM_RESOLVER, AFFECTED_LINE));

        GitSCM scm = new GitSCM(GitSCM.createRepoList(FORENSICS_API_PLUGIN, null),
                Collections.singletonList(new BranchSpec(COMMIT)), null, null,
                Collections.singletonList(new RelativeTargetDirectory("forensics-api")));
        job.setScm(scm);
        job.getPublishersList().add(new RepositoryMinerStep());
        enableGenericWarnings(job, new Java());

        verifyBlaming(job);
    }

    private void runStepAndVerifyBlamesAndForensics(final String step) {
        WorkflowJob job = createPipelineWithWorkspaceFilesWithSuffix();

        createFileInWorkspace(job, "java-issues.txt",
                createJavaWarning(MODIFIED_FILE, 111)
                        + createJavaWarning(SCM_RESOLVER, AFFECTED_LINE));

        job.setDefinition(asStage(checkout(OLD_COMMIT), MINE_REPOSITORY, step));

        buildSuccessfully(job); // reference build

        createFileInWorkspace(job, "java-issues.txt",
                createJavaWarning(MODIFIED_FILE, 111)    // outstanding and modified
                        + createJavaWarning(MODIFIED_FILE, 112)  // new and modified
                        + createJavaWarning(MODIFIED_FILE, 113)  // new and modified
                        + createJavaWarning(SCM_RESOLVER, AFFECTED_LINE)   // outstanding (not modified)
                        + createJavaWarning(MODIFIED_FILE, 2));  // new (not modified)

        job.setDefinition(asStage(CHECKOUT_FORENSICS_API, MINE_REPOSITORY, "discoverReferenceBuild()", step));

        var result = verifyBlamingWithModifiedFiles(job);

        assertThat(getConsoleLog(result)).contains(
                "Detect all issues that are part of modified code",
                "-> Using commit 'a6d0ef0' as latest commit for build",
                "-> Using commit '3097ea1' as latest commit for build",
                "-> Invoking Git delta calculator for determining the changes between commits 'a6d0ef0' and '3097ea1'",
                "-> Start scanning for differences between commits...",
                "-> 121 files contain changes",
                "-> Creating the Git diff file",
                "-> Git code delta successfully calculated",
                "Issues in modified code: 3 (new: 2, outstanding: 1)");
    }

    private AnalysisResult verifyBlaming(final ParameterizedJob<?, ?> job) {
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

        return result;
    }

    private AnalysisResult verifyBlamingWithModifiedFiles(final ParameterizedJob<?, ?> job) {
        AnalysisResult result = scheduleSuccessfulBuild(job);

        assertThat(result).hasTotalSize(5).hasNewSize(3).hasFixedSize(0);
        assertThat(result.getTotals()).hasNewModifiedSize(2).hasTotalModifiedSize(3);

        assertThat(result.getBlames().contains(SCM_RESOLVER)).isTrue();

        FileBlame blame = result.getBlames().getBlame(SCM_RESOLVER);
        assertThat(blame.getFileName()).isEqualTo(SCM_RESOLVER);
        assertThat(blame.getEmail(AFFECTED_LINE)).isEqualTo("ullrich.hafner@gmail.com");
        assertThat(blame.getCommit(AFFECTED_LINE)).isEqualTo("43dde5d4f7a06122216494a896c51830ed684572");

        assertThat(getConsoleLog(result)).contains(
                "Invoking Git blamer to create author and commit information for 2 affected files",
                "Git commit ID = 'a6d0ef09ab3c418e370449a884da99b8190ae950'",
                "-> blamed authors of issues in 2 files",
                "Extracting repository forensics for 2 affected files (files in repository: 121)",
                "-> 2 affected files processed");

        return result;
    }

    /**
     * Checks out an existing Git repository and starts a pipeline with the scan and publish steps. Verifies that the
     * Git forensics plugin is correctly skipped.
     */
    @Test
    void shouldSkipBlamesAndForensicsWithScanAndPublishIssuesSteps() {
        runStepAndVerifyScmSkipping("def issues = scanForIssues "
                + "sourceDirectories: [[path: 'forensics-api']],"
                + "scm: 'nothing', "
                + "tool: java(pattern:'**/*issues.txt', reportEncoding:'UTF-8')\n"
                + PUBLISH_ISSUES_STEP);
    }

    /**
     * Checks out an existing Git repository and starts a pipeline with the record step. Verifies that the Git forensics
     * plugin is correctly skipped.
     */
    @Test
    void shouldSkipBlamesAndForensicsWithRecordIssuesStep() {
        runStepAndVerifyScmSkipping("recordIssues "
                + "sourceDirectories: [[path: 'forensics-api']],"
                + "scm: 'nothing', "
                + "tool: java(pattern:'**/*issues.txt', reportEncoding:'UTF-8')");
    }

    /**
     * Checks out an existing Git repository and starts a freestyle job. Verifies that the Git forensics plugin is
     * correctly skipped.
     */
    @Test
    void shouldSkipBlamesAndForensicsInFreestyleJob() throws IOException {
        FreeStyleProject job = createFreeStyleProject();

        createFileInWorkspace(job, "java-issues.txt", createJavaWarning(SCM_RESOLVER, AFFECTED_LINE));

        GitSCM scm = new GitSCM(GitSCM.createRepoList(FORENSICS_API_PLUGIN, null),
                Collections.singletonList(new BranchSpec(COMMIT)), null, null,
                Collections.singletonList(new RelativeTargetDirectory("forensics-api")));
        job.setScm(scm);
        job.getPublishersList().add(new RepositoryMinerStep());
        enableGenericWarnings(job, recorder -> recorder.setScm("nothing"), new Java());

        verifySkippedScm(job);
    }

    private void runStepAndVerifyScmSkipping(final String step) {
        WorkflowJob job = createPipelineWithWorkspaceFilesWithSuffix(JAVA_ONE_WARNING);
        job.setDefinition(asStage(CHECKOUT_FORENSICS_API, MINE_REPOSITORY, step));

        verifySkippedScm(job);
    }

    private void verifySkippedScm(final ParameterizedJob<?, ?> job) {
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

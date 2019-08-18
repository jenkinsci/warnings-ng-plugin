package io.jenkins.plugins.analysis.core.scm;

import org.junit.Rule;
import org.junit.Test;

import org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import hudson.plugins.git.GitSCM;
import jenkins.plugins.git.GitSampleRepoRule;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerTest;
import io.jenkins.plugins.forensics.miner.FileStatistics;
import io.jenkins.plugins.forensics.miner.RepositoryMiner;
import io.jenkins.plugins.forensics.miner.RepositoryStatistics;

import static io.jenkins.plugins.forensics.assertions.Assertions.*;

/**
 * Tests the {@link RepositoryMiner GitRepositoryMiner} in a pipeline that uses a real Git repository.
 *
 * @author Ullrich Hafner
 */
@SuppressWarnings("PMD.SignatureDeclareThrowsException")
public class GitMinerITest extends IntegrationTestWithJenkinsPerTest {
    private static final String FILE_NAME = "Test.java";

    /** The Git repository for the test. */
    @Rule
    public GitSampleRepoRule gitRepo = new GitSampleRepoRule();

    /**
     * Shows the statistics of an actual git repository with a single file in a pipeline script.
     *
     * @throws Exception
     *         if there is a problem with the git repository
     */
    @Test
    public void shouldShowStatisticsOfOneIssue() throws Exception {
        WorkflowJob job = buildPipeline("");

        AnalysisResult result = scheduleSuccessfulBuild(job);
        RepositoryStatistics statistics = result.getForensics();
        assertThat(statistics).isNotEmpty();
        String absoluteFileName = getAbsolutePathOfWorkspaceFile(job, FILE_NAME);
        assertThat(statistics).hasFiles(absoluteFileName);

        FileStatistics fileStatistics = statistics.get(absoluteFileName);
        assertThat(fileStatistics).hasNumberOfCommits(1);
        assertThat(fileStatistics).hasNumberOfAuthors(1);
        assertThat(fileStatistics).hasNumberOfAuthors(1);
        assertThat(fileStatistics).hasAgeInDays(0);
        assertThat(fileStatistics).hasLastModifiedInDays(0);
    }

    /**
     * Shows  statistics of an actual git repository with a single file in a pipeline script.
     *
     * @throws Exception
     *         if there is a problem with the git repository
     */
    @Test
    public void shouldShowDisableStatistics() throws Exception {
        WorkflowJob job = buildPipeline("forensicsDisabled: 'true', ");
        AnalysisResult result = scheduleSuccessfulBuild(job);
        RepositoryStatistics statistics = result.getForensics();

        assertThat(statistics).isEmpty();
    }

    private WorkflowJob buildPipeline(final String disableForensicsParameter) throws Exception {
        gitRepo.init();
        createAndCommitFile(FILE_NAME, "public class Test {}");

        createAndCommitFile("Jenkinsfile", "node {\n"
                + "  stage ('Checkout') {\n"
                + "    checkout scm\n"
                + "  }\n"
                + "  stage ('Build and Analysis') {"
                + "    echo '[javac] Test.java:1: warning: Test Warning for Jenkins'\n"
                + "    recordIssues " + disableForensicsParameter + "tools: [java()]\n"
                + "  }\n"
                + "}");

        WorkflowJob job = createPipeline();
        job.setDefinition(new CpsScmFlowDefinition(new GitSCM(gitRepo.toString()), "Jenkinsfile"));

        return job;
    }

    private void createAndCommitFile(final String fileName, final String content) throws Exception {
        gitRepo.write(fileName, content);
        gitRepo.git("add", fileName);
        gitRepo.git("commit", "--message=" + fileName + " created");
    }
}

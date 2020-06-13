package core;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import org.jenkinsci.test.acceptance.docker.DockerContainerHolder;
import org.jenkinsci.test.acceptance.docker.fixtures.GitContainer;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.DockerTest;
import org.jenkinsci.test.acceptance.junit.WithCredentials;
import org.jenkinsci.test.acceptance.junit.WithDocker;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.git.GitScm;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.WorkflowJob;

import io.jenkins.plugins.analysis.warnings.AnalysisResult;
import io.jenkins.plugins.analysis.warnings.AnalysisSummary;
import io.jenkins.plugins.analysis.warnings.ForensicsTable;
import io.jenkins.plugins.analysis.warnings.ForensicsTableRow;
import io.jenkins.plugins.analysis.warnings.IssuesRecorder;

import static org.assertj.core.api.Assertions.*;

@WithDocker
@Category(DockerTest.class)
@WithPlugins({"git", "git-forensics"})
@WithCredentials(credentialType = WithCredentials.SSH_USERNAME_PRIVATE_KEY, values = {"gitplugin", "/org/jenkinsci/test/acceptance/docker/fixtures/GitContainer/unsafe"})
public class GitForensicsUITest extends AbstractJUnitTest {

    private static final String USERNAME = "gitplugin";

    @Inject
    DockerContainerHolder<GitContainer> gitServer;

    private GitContainer container;
    private String repoUrl;
    private String host;
    private int port;

    @Before
    public void init() {
        container = gitServer.get();
        repoUrl = container.getRepoUrl();
        host = container.host();
        port = container.port();
    }

    @Test
    public void shouldShowGitForensicsOneIssue() {
        GitRepo repo = GitUtils.setupInitialGitRepository();
        repo.commitFileWithMessage("commit", "Test.java", "public class Test {}");
        repo.commitFileWithMessage("commit", "warnings.txt",
                "[javac] Test.java:1: warning: Test Warning for Jenkins");

        Build build = generateFreeStyleJob(repo);
        build.open();

        AnalysisSummary summary = new AnalysisSummary(build, "java");
        AnalysisResult result = summary.openOverallResult();
        ForensicsTable forensicsTable = result.openForensicsTable();
        ForensicsTableRow row = forensicsTable.getRowAs(0, ForensicsTableRow.class);

        assertThat(forensicsTable.getTableRows()).hasSize(1);
        assertThat(forensicsTable.getHeaders()).containsExactly("Details", "File", "Age", "#Authors", "#Commits", "Last Commit", "Added");
        assertColumnsOfRow(row, "Test.java", 1);
    }

    @Test
    public void shouldShowGitForensicsMultipleIssuesWithPipeline() {
        GitRepo repo = new GitRepo();
        GitUtils.commitDifferentFilesToGitRepository(repo);
        repo.commitFileWithMessage("commit", "Jenkinsfile",
                "node {\n"
                        + "  stage ('Checkout') {\n"
                        + "    checkout scm\n"
                        + "  }\n"
                        + "  stage ('Build and Analysis') {"
                        + "    echo '[javac] Test.java:1: warning: Test Warning for Jenkins'\n"
                        + "    echo '[javac] Test.java:2: warning: Test Warning for Jenkins'\n"
                        + "    echo '[javac] Test.java:3: warning: Test Warning for Jenkins'\n"
                        + "    echo '[javac] LoremIpsum.java:1: warning: Another Warning for Jenkins'\n"
                        + "    echo '[javac] LoremIpsum.java:2: warning: Another Warning for Jenkins'\n"
                        + "    echo '[javac] LoremIpsum.java:3: warning: Another Warning for Jenkins'\n"
                        + "    echo '[javac] LoremIpsum.java:4: warning: Another Warning for Jenkins'\n"
                        + "    echo '[javac] Bob.java:1: warning: Bobs Warning for Jenkins'\n"
                        + "    echo '[javac] Bob.java:2: warning: Bobs Warning for Jenkins'\n"
                        + "    echo '[javac] Bob.java:3: warning: Bobs Warning for Jenkins'\n"
                        + "    recordIssues tools: [java()]\n"
                        + "  }\n"
                        + "}"
        );

        Build build = generateWorkflowJob(repo);
        build.open();

        AnalysisSummary summary = new AnalysisSummary(build, "java");
        AnalysisResult result = summary.openOverallResult();
        ForensicsTable forensicsTable = result.openForensicsTable();

        assertThat(forensicsTable.getTableRows()).hasSize(10);
        assertThat(forensicsTable.getHeaders()).containsExactly("Details", "File", "Age", "#Authors", "#Commits", "Last Commit", "Added");
        assertMultipleIssues(forensicsTable, 1);
    }

    @Test
    public void shouldShowGitForensicsMultipleIssuesWithFreestyle() {
        GitRepo repo = new GitRepo();
        GitUtils.commitDifferentFilesToGitRepository(repo);
        repo.commitFileWithMessage("commit", "warnings.txt",
                "[javac] Test.java:1: warning: Test Warning for Jenkins\n"
                        + "[javac] Test.java:2: warning: Test Warning for Jenkins\n"
                        + "[javac] Test.java:3: warning: Test Warning for Jenkins\n"
                        + "[javac] LoremIpsum.java:1: warning: Another Warning for Jenkins\n"
                        + "[javac] LoremIpsum.java:2: warning: Another Warning for Jenkins\n"
                        + "[javac] LoremIpsum.java:3: warning: Another Warning for Jenkins\n"
                        + "[javac] LoremIpsum.java:4: warning: Another Warning for Jenkins\n"
                        + "[javac] Bob.java:1: warning: Bobs Warning for Jenkins\n"
                        + "[javac] Bob.java:2: warning: Bobs Warning for Jenkins\n"
                        + "[javac] Bob.java:3: warning: Bobs Warning for Jenkins");

        Build build = generateFreeStyleJob(repo);
        build.open();

        AnalysisSummary summary = new AnalysisSummary(build, "java");
        AnalysisResult result = summary.openOverallResult();
        ForensicsTable forensicsTable = result.openForensicsTable();

        assertThat(forensicsTable.getTableRows()).hasSize(10);
        assertThat(forensicsTable.getHeaders()).containsExactly("Details", "File", "Age", "#Authors", "#Commits", "Last Commit", "Added");
        assertMultipleIssues(forensicsTable, 1);
    }

    @Test
    public void shouldShowGitForensicsMultipleIssuesWithMultipleCommits() {
        GitRepo repo = new GitRepo();
        GitUtils.commitDifferentFilesToGitRepository(repo);
        repo.setIdentity("Alice Miller", "alice@miller");
        repo.commitFileWithMessage("commit", "LoremIpsum.java", "public class LoremIpsum {\n"
                + "    public LoremIpsum() {\n"
                + "        Log.log(\"Lorem ipsum dolor sit amet\");"
                + "    }\n"
                + "}");
        repo.commitFileWithMessage("commit", "warnings.txt",
                "[javac] Test.java:1: warning: Test Warning for Jenkins\n"
                        + "[javac] Test.java:2: warning: Test Warning for Jenkins\n"
                        + "[javac] Test.java:3: warning: Test Warning for Jenkins\n"
                        + "[javac] LoremIpsum.java:1: warning: Another Warning for Jenkins\n"
                        + "[javac] LoremIpsum.java:2: warning: Another Warning for Jenkins\n"
                        + "[javac] LoremIpsum.java:3: warning: Another Warning for Jenkins\n"
                        + "[javac] LoremIpsum.java:4: warning: Another Warning for Jenkins\n"
                        + "[javac] Bob.java:1: warning: Bobs Warning for Jenkins\n"
                        + "[javac] Bob.java:2: warning: Bobs Warning for Jenkins\n"
                        + "[javac] Bob.java:3: warning: Bobs Warning for Jenkins");

        Build build = generateFreeStyleJob(repo);
        build.open();

        AnalysisSummary summary = new AnalysisSummary(build, "java");
        AnalysisResult result = summary.openOverallResult();
        ForensicsTable forensicsTable = result.openForensicsTable();

        assertThat(forensicsTable.getTableRows()).hasSize(10);
        assertThat(forensicsTable.getHeaders()).containsExactly("Details", "File", "Age", "#Authors", "#Commits", "Last Commit", "Added");
        assertMultipleIssues(forensicsTable, 2);
    }

    private void assertColumnsOfRow(final ForensicsTableRow row, final String filename, final int commits) {
        assertThat(row.getFileName()).isEqualTo(filename);
        assertThat(row.getAge()).isEqualTo(1);
        assertThat(row.getAuthors()).isEqualTo(1);
        assertThat(row.getCommits()).isEqualTo(commits);
        assertThat(row.getLastCommit()).isNotNull();
        assertThat(row.getAdded()).isNotNull();
    }

    private void assertMultipleIssues(final ForensicsTable forensicsTable, final int commits) {
        assertColumnsOfRow(forensicsTable.getRowAs(0, ForensicsTableRow.class), "Bob.java", 1);
        assertColumnsOfRow(forensicsTable.getRowAs(1, ForensicsTableRow.class), "Bob.java", 1);
        assertColumnsOfRow(forensicsTable.getRowAs(2, ForensicsTableRow.class), "Bob.java", 1);
        assertColumnsOfRow(forensicsTable.getRowAs(3, ForensicsTableRow.class), "LoremIpsum.java", commits);
        assertColumnsOfRow(forensicsTable.getRowAs(5, ForensicsTableRow.class), "LoremIpsum.java", commits);
        assertColumnsOfRow(forensicsTable.getRowAs(6, ForensicsTableRow.class), "LoremIpsum.java", commits);
        assertColumnsOfRow(forensicsTable.getRowAs(7, ForensicsTableRow.class), "Test.java", 1);
        assertColumnsOfRow(forensicsTable.getRowAs(8, ForensicsTableRow.class), "Test.java", 1);
        assertColumnsOfRow(forensicsTable.getRowAs(9, ForensicsTableRow.class), "Test.java", 1);
    }

    private Build generateFreeStyleJob(final GitRepo repo) {
        FreeStyleJob freestyleJob = jenkins.jobs.create();
        freestyleJob.configure();
        repo.transferToDockerContainer(host, port);
        freestyleJob.useScm(GitScm.class)
                .url(repoUrl)
                .credentials(USERNAME);

        addRecorder(freestyleJob);
        freestyleJob.save();

        return freestyleJob.startBuild().waitUntilFinished();
    }

    private void addRecorder(final FreeStyleJob job) {
        job.addPublisher(IssuesRecorder.class, recorder -> {
            recorder.setTool("Java").setPattern("warnings.txt");
            recorder.setEnabledForFailure(true);
        });
    }

    private Build generateWorkflowJob(final GitRepo repo) {
        WorkflowJob workflowJob = jenkins.jobs.create(WorkflowJob.class);
        workflowJob.configure();
        repo.transferToDockerContainer(host, port);
        workflowJob.setJenkinsFileRepository(repoUrl, USERNAME);
        workflowJob.save();
        return workflowJob.startBuild().waitUntilFinished();
    }

}

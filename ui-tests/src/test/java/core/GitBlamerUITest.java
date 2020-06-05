package core;

import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

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
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.Scm;

import io.jenkins.plugins.analysis.warnings.AnalysisResult;
import io.jenkins.plugins.analysis.warnings.AnalysisResult.Tab;
import io.jenkins.plugins.analysis.warnings.AnalysisSummary;
import io.jenkins.plugins.analysis.warnings.BlamesTable;
import io.jenkins.plugins.analysis.warnings.BlamesTableRow;
import io.jenkins.plugins.analysis.warnings.IssuesRecorder;
import io.jenkins.plugins.analysis.warnings.IssuesTable;
import io.jenkins.plugins.analysis.warnings.IssuesTable.IssuesTableRowType;

import static io.jenkins.plugins.analysis.warnings.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

@WithDocker
@Category(DockerTest.class)
@WithPlugins({"git", "git-forensics"})
@WithCredentials(credentialType = WithCredentials.SSH_USERNAME_PRIVATE_KEY, values = {"gitplugin", "/org/jenkinsci/test/acceptance/docker/fixtures/GitContainer/unsafe"})
public class GitBlamerUITest extends AbstractJUnitTest {

    @Inject
    DockerContainerHolder<GitContainer> gitServer;

    private static final String USERNAME = "gitplugin";
    private Job job;
    private GitContainer container;
    private String repoUrl;
    private String host;
    private int port;

    private static final String DETAILS = "Details";
    private static final String FILE = "File";
    private static final String AGE = "Age";
    private static final String AUTHOR = "Author";
    private static final String EMAIL = "Email";
    private static final String COMMIT = "Commit";
    private static final String ADDED = "Added";

    @Before
    public void initGitRepository() {
        container = gitServer.get();
        repoUrl = container.getRepoUrl();
        host = container.host();
        port = container.port();
        job = jenkins.jobs.create();    // creates freestyle job
        job.configure();
    }

    @Test
    public void shouldBlameOneIssueWithFreestyle() {
        GitRepo repo = setupInitialGitRepository();
        // Map<String, String> commits = commitDifferentFilesToGitRepository(repo);
        repo.commitFileWithMessage("commit", "Test.java",
                "public class Test {}");
        String commitId = repo.getLastSha1();
        repo.commitFileWithMessage("commit", "warnings.txt",
                "[javac] Test.java:1: warning: Test Warning for Jenkins");

        Build build = generate(repo);
        build.open();

        AnalysisSummary blame = new AnalysisSummary(build, "java");
        // assertThat(blame).isDisplayed().hasTitleText("Java: One warning");

        AnalysisResult resultPage = blame.openOverallResult();
        BlamesTable blamesTable = resultPage.openBlamesTable();
        BlamesTableRow row = blamesTable.getRowAs(0, BlamesTableRow.class);

        assertThat(blamesTable.getTableRows()).hasSize(1);
        assertColumnHeader(blamesTable);
        assertColumnsOfTest(row, commitId);
    }

    private void assertColumnsOfTest(final BlamesTableRow row, final String commit) {
        assertThat(row.getAuthor()).isEqualTo("Git SampleRepoRule");
        assertThat(row.getEmail()).isEqualTo("gits@mplereporule");
        assertThat(row.getFileName()).isEqualTo("Test.java");
        assertThat(row.getCommit()).isEqualTo(commit);
        assertThat(row.getAge()).isEqualTo(1);
    }

    private void assertColumnHeader(final BlamesTable table) {
        assertThat(table.getHeaders()).containsExactly(DETAILS, FILE, AGE, AUTHOR, EMAIL, COMMIT, ADDED);
    }

    private Build generate(final GitRepo repo) {
        // Transfer to docker git repository
        repo.transferToDockerContainer(host, port);
        job.useScm(GitScm.class)
                .url(repoUrl)
                .credentials(USERNAME);

        addRecorder((FreeStyleJob) job);
        job.save();

        return job.startBuild().waitUntilFinished();
    }

    private void addRecorder(final FreeStyleJob job) {
        job.addPublisher(IssuesRecorder.class, recorder -> {
            recorder.setTool("Java").setPattern("warnings.txt");
            recorder.setEnabledForFailure(true);
        });
    }

    private GitRepo setupInitialGitRepository() {
        GitRepo repo = new GitRepo();
        repo.setIdentity("Git SampleRepoRule", "gits@mplereporule");
        repo.commitFileWithMessage("init", "file", "");
        return repo;
    }

    private Map<String, String> commitDifferentFilesToGitRepository(GitRepo repo) {
        Map<String, String> commits = new HashMap<>();

        repo.setIdentity("Git SampleRepoRule", "gits@mplereporule");
        repo.commitFileWithMessage("commit", "Test.java", "public class Test {\n"
                + "    public Test() {\n"
                + "        System.out.println(\"Test\");"
                + "    }\n"
                + "}");
        commits.put("Test", repo.getLastSha1());

        repo.setIdentity("John Doe", "john@doe");
        repo.commitFileWithMessage("commit", "LoremIpsum.java", "public class LoremIpsum {\n"
                + "    public LoremIpsum() {\n"
                + "        Log.log(\"Lorem ipsum dolor sit amet\");"
                + "    }\n"
                + "}");
        commits.put("LoremIpsum", repo.getLastSha1());

        repo.setIdentity("Alice Miller", "alice@miller");
        repo.commitFileWithMessage("commit", "Bob.java", "public class Bob {\n"
                + "    public Bob() {\n"
                + "        Log.log(\"Bob: 'Where are you?'\");"
                + "    }\n"
                + "}");
        commits.put("Bob", repo.getLastSha1());

        return commits;
    }

}

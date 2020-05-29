package core;

import java.io.File;
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
import org.jenkinsci.test.acceptance.plugins.git.GitRepo;
import org.jenkinsci.test.acceptance.plugins.git.GitScm;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.Job;

import io.jenkins.plugins.analysis.warnings.AnalysisResult;
import io.jenkins.plugins.analysis.warnings.AnalysisSummary;
import io.jenkins.plugins.analysis.warnings.IssuesRecorder;

@WithDocker
@Category(DockerTest.class)
@WithPlugins({"warnings-ng", "git"})
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
        Build referenceBuild = generate();
        referenceBuild.open();
        AnalysisSummary blame = new AnalysisSummary(referenceBuild, "java");
        AnalysisResult result = blame.openOverallResult();
    }

    private Build generate() {
        GitRepo repo = setupGitRepositoryCommits();
        buildGitRepo(repo);
        repo.transferToDockerContainer(host, port);

        job.useScm(GitScm.class)
                .url(repoUrl)
                .credentials(USERNAME);

        addRecorder((FreeStyleJob) job);

        job.save();
        return job.startBuild().waitUntilFinished();
    }

    private IssuesRecorder addRecorder(final FreeStyleJob job) {
        return job.addPublisher(IssuesRecorder.class, recorder -> {
            recorder.setTool("Java").setPattern("warnings.txt");
        });
    }

    private void buildGitRepo(GitRepo repo) {
        // repo.commitFileWithMessage("commit", "Test.java", "public class Test {}");
        repo.commitFileWithMessage("initial commit", "warnings.txt",
                "[javac] Test.java:1: warning: Test Warning for Jenkins\n"
                        + "[javac] Test.java:2: warning: Test Warning for Jenkins\n"
                        + "[javac] Test.java:3: warning: Test Warning for Jenkins\n"
                        + "[javac] Test.java:4: warning: Test Warning for Jenkins\n"
                        + "[javac] LoremIpsum.java:1: warning: Another Warning for Jenkins\n"
                        + "[javac] LoremIpsum.java:2: warning: Another Warning for Jenkins\n"
                        + "[javac] LoremIpsum.java:3: warning: Another Warning for Jenkins\n"
                        + "[javac] LoremIpsum.java:4: warning: Another Warning for Jenkins\n"
                        + "[javac] Bob.java:1: warning: Bobs Warning for Jenkins\n"
                        + "[javac] Bob.java:2: warning: Bobs Warning for Jenkins\n"
                        + "[javac] Bob.java:3: warning: Bobs Warning for Jenkins");
    }

    private GitRepo setupGitRepositoryCommits() {
        GitRepo repo = new GitRepo();
        repo.commitFileWithMessage("commit", "Test.java", "public class Test {\n"
                + "    public Test() {\n"
                + "        System.out.println(\"Test\");"
                + "    }\n"
                + "}");

        // Change identity
        repo.setIdentity("John Doe", "john@doe");
        repo.commitFileWithMessage("commit", "LoremIpsum.java", "public class LoremIpsum {\n"
                + "    public LoremIpsum() {\n"
                + "        Log.log(\"Lorem ipsum dolor sit amet\");"
                + "    }\n"
                + "}");

        repo.setIdentity("Alice Miller", "alice@miller");
        repo.commitFileWithMessage("commit", "Bob.java", "public class Bob {\n"
                + "    public Bob() {\n"
                + "        Log.log(\"Bob: 'Where are you?'\");"
                + "    }\n"
                + "}");

        return repo;
    }

}

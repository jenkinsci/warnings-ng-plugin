package io.jenkins.plugins.analysis.warnings;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;

import org.jenkinsci.test.acceptance.docker.DockerContainerHolder;
import org.jenkinsci.test.acceptance.docker.fixtures.GitContainer;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithDocker;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.git.GitRepo;
import org.jenkinsci.test.acceptance.plugins.git.GitScm;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.WorkflowJob;

@WithDocker
@WithPlugins("warnings-ng")
public class GitBlamerUITest extends AbstractJUnitTest {

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
    public void shouldBlame() {
        WorkflowJob job = jenkins.jobs.create(WorkflowJob.class);

        buildGitRepo("Jenkinsfile", "node {\n"
                + "  stage ('Checkout') {\n"
                + "    checkout scm\n"
                + "  }\n"
                + "  stage ('Build and Analysis') {"
                + "    echo '[javac] Test.java:1: warning: Test Warning for Jenkins'\n"
                + "    echo '[javac] Test.java:2: warning: Test Warning for Jenkins'\n"
                + "    echo '[javac] Test.java:3: warning: Test Warning for Jenkins'\n"
                + "    echo '[javac] Test.java:4: warning: Test Warning for Jenkins'\n"
                + "    echo '[javac] LoremIpsum.java:1: warning: Another Warning for Jenkins'\n"
                + "    echo '[javac] LoremIpsum.java:2: warning: Another Warning for Jenkins'\n"
                + "    echo '[javac] LoremIpsum.java:3: warning: Another Warning for Jenkins'\n"
                + "    echo '[javac] LoremIpsum.java:4: warning: Another Warning for Jenkins'\n"
                + "    echo '[javac] Bob.java:1: warning: Bobs Warning for Jenkins'\n"
                + "    echo '[javac] Bob.java:2: warning: Bobs Warning for Jenkins'\n"
                + "    echo '[javac] Bob.java:3: warning: Bobs Warning for Jenkins'\n"
                + "    recordIssues tools: [java()]\n"
                + "  }\n"
                + "}")
                .transferToDockerContainer(host, port);

        job.useScm(GitScm.class)
                .url(repoUrl)
                .credentials(USERNAME);

        job.save();

        Build referenceBuild = buildJob(job);
    }

    private GitRepo buildGitRepo(final String fileName, final String content) {
        GitRepo repo = new GitRepo();
        //Init
        repo.changeAndCommitFoo("Initial commit");
        //Add File
        try {
            try(FileWriter o = new FileWriter(new File(repo.dir, fileName), true)) {
                o.write(content);
            }
            repo.git("add", fileName);
            repo.commit("Create " + fileName);
        } catch (IOException e) {
            throw new AssertionError("Cant't append line to file", e);
        }

        return repo;
    }

    private Build buildJob(final Job job) {
        return job.startBuild().waitUntilFinished();
    }

}

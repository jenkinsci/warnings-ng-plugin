package io.jenkins.plugins.analysis.warnings.recorder;

import org.junit.ClassRule;
import org.junit.Test;

import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import jenkins.plugins.git.GitSampleRepoRule;

import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;

/**
 * Integration test for the git blame functionality.
 *
 */
public class GitBlameITest extends IntegrationTestWithJenkinsPerSuite {

    @ClassRule
    public static GitSampleRepoRule repository = new GitSampleRepoRule();

    @Test
    public void should() throws Exception {
        String fileName = "README";

        repository.init();
        repository.git("config", "user.name", "Author User Name");
        repository.git("config", "user.email", "author.user.name@mail.example.com");
        repository.git("checkout", "master");
        repository.write(fileName, "Hello World!");
        repository.git("add", fileName);
        repository.git("commit", "-m", "Adding " + fileName, fileName);



    }
}

package io.jenkins.plugins.analysis.core.scm;

import org.junit.ClassRule;
import org.junit.Test;

import jenkins.plugins.git.GitSampleRepoRule;

import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;

public class GitBlameITest extends IntegrationTestWithJenkinsPerSuite {

    @ClassRule
    public static GitSampleRepoRule sampleRepo = new GitSampleRepoRule();

    @Test
    public void shouldShowGitBlameHistory() throws Exception {
        sampleRepo.init();
        sampleRepo.git("log");
    }
}

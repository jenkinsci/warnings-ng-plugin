package io.jenkins.plugins.analysis.core.scm;

import org.junit.ClassRule;
import org.junit.Test;

import jenkins.plugins.git.GitSampleRepoRule;

import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerTest;

/**
 * Tests the Git Blame Functionality.
 *
 * @author Artem Polovyi
 */
public class GitBlameTest extends IntegrationTestWithJenkinsPerTest {
    @ClassRule
    public static GitSampleRepoRule repository = new GitSampleRepoRule();

    @Test
    public void shouldCreateBlameWarning() {
    }

}

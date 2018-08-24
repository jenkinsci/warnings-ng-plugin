package io.jenkins.plugins.analysis.core.testutil;

import org.junit.ClassRule;
import org.jvnet.hudson.test.JenkinsRule;

/**
 * Base class for integration tests in Jenkins. Sub classes will get a new and fresh Jenkins instance for each
 * test case.
 *
 * @author Ullrich Hafner
 */
public class IntegrationTestWithJenkinsPerTest extends IntegrationTest {
    @ClassRule
    public final static JenkinsRule JENKINS_PER_SUITE = new JenkinsRule();

    @Override
    protected JenkinsRule getJenkins() {
        return JENKINS_PER_SUITE;
    }
}

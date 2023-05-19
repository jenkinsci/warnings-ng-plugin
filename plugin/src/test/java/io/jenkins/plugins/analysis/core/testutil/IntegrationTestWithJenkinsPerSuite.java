package io.jenkins.plugins.analysis.core.testutil;

import org.junit.jupiter.api.BeforeAll;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

/**
 * Base class for integration tests in Jenkins. Subclasses will get a new and fresh Jenkins instance for each test
 * case.
 *
 * @author Ullrich Hafner
 */
@WithJenkins
public abstract class IntegrationTestWithJenkinsPerSuite extends WarningsIntegrationTest {
    private static JenkinsRule jenkinsPerSuite;

    @BeforeAll
    static void initializeJenkins(final JenkinsRule rule) {
        jenkinsPerSuite = rule;
    }

    @Override
    protected JenkinsRule getJenkins() {
        return jenkinsPerSuite;
    }
}

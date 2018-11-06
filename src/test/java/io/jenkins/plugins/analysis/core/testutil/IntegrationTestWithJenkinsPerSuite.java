package io.jenkins.plugins.analysis.core.testutil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.junit.ClassRule;
import org.jvnet.hudson.test.JenkinsRule;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;

/**
 * Base class for integration tests in Jenkins. Sub classes will get a new and fresh Jenkins instance for each
 * test case.
 *
 * @author Ullrich Hafner
 */
public abstract class IntegrationTestWithJenkinsPerSuite extends IntegrationTest {
    /** Jenkins rule per suite. */
    @ClassRule
    public static final JenkinsRule JENKINS_PER_SUITE = new JenkinsRule();

    @Override
    protected JenkinsRule getJenkins() {
        return JENKINS_PER_SUITE;
    }

    protected String getConsoleLog(final AnalysisResult result) {
        try {
            return FileUtils.readFileToString(result.getOwner().getLogFile(), StandardCharsets.UTF_8);
        }
        catch (IOException e) {
            throw new AssertionError(e);
        }
    }
}

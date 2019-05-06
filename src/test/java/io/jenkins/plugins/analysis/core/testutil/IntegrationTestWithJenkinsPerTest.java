package io.jenkins.plugins.analysis.core.testutil;

import org.junit.Before;
import org.junit.Rule;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.JenkinsRule.WebClient;

/**
 * Base class for integration tests in Jenkins. Sub classes will get a new and fresh Jenkins instance for each
 * test case.
 *
 * @author Ullrich Hafner
 */
public abstract class IntegrationTestWithJenkinsPerTest extends IntegrationTest {
    /** Jenkins rule per test. */
    @Rule
    public final JenkinsRule jenkinsPerTest = new JenkinsRule();

    @Override
    protected JenkinsRule getJenkins() {
        return jenkinsPerTest;
    }

    private WebClient noJsWebClient;
    private WebClient jsEnabledClient;

    @Before
    public void createWebClients() {
        noJsWebClient = create(false);
        jsEnabledClient = create(true);
    }

    @Override
    protected WebClient getWebClient(final JavaScriptSupport javaScriptSupport) {
        return javaScriptSupport == JavaScriptSupport.JS_DISABLED ? noJsWebClient : jsEnabledClient;
    }
}

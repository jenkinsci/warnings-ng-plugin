package io.jenkins.plugins.analysis.core.testutil;

import org.junit.jupiter.api.AfterEach;

/**
 * Base class for integration tests in Jenkins. Subclasses will get a new and fresh Jenkins instance for each
 * test case.
 *
 * @author Ullrich Hafner
 */
public abstract class IntegrationTestWithJenkinsPerTest extends IntegrationTestWithJenkinsPerSuite {
    @SuppressWarnings("checkstyle:IllegalThrows")
    @AfterEach
    void tearDown() throws Throwable {
        getJenkins().after();
        getJenkins().before();
    }
}

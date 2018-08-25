package io.jenkins.plugins.analysis.warnings.recorder;

import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerTest;

/**
 * Base class for integration tests of the warnings plug-in in freestyle jobs. 
 * Tests the new recorder {@link IssuesRecorder}.
 *
 * @author Ullrich Hafner
 */
public class AbstractIssuesRecorderITest extends IntegrationTestWithJenkinsPerTest {
}
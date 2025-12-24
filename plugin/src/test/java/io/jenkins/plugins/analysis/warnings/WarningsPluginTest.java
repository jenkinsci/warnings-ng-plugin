package io.jenkins.plugins.analysis.warnings;

import org.junit.jupiter.api.Test;

import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

/**
 * Tests the class {@link WarningsPlugin}.
 *
 * @author Akash Manna
 */
class WarningsPluginTest extends IntegrationTestWithJenkinsPerSuite {
    /** Tests that the descriptor's canScanConsoleLog() method returns true. */
    @Test
    void descriptorMethodCanScanConsoleLogReturnsTrue() {
        var descriptor = new WarningsPlugin.Descriptor();

        assertThat(descriptor.canScanConsoleLog()).isTrue();
    }
}

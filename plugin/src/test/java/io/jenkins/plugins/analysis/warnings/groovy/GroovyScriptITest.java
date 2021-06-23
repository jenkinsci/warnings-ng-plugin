package io.jenkins.plugins.analysis.warnings.groovy;

import static io.jenkins.plugins.analysis.core.testutil.Assertions.*;

import org.junit.Test;

import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;

/**
 * Tests the class {@link GroovyScript}.
 */
public class GroovyScriptITest extends IntegrationTestWithJenkinsPerSuite {

    @Test
    void descriptorMethodCanScanConsoleLogReturnsTrueIfConfigurationSaysConsoleLogScanningPermittedIsTrue() {
        testDescriptorCanScanConsoleLog(true);
    }

    @Test
    void descriptorMethodCanScanConsoleLogReturnsFalsefConfigurationSaysConsoleLogScanningPermittedIsFalse() {
        testDescriptorCanScanConsoleLog(false);
    }

    private void testDescriptorCanScanConsoleLog(final boolean expected) {
        // Given
        ParserConfiguration.getInstance().setConsoleLogScanningPermitted(expected);
        final GroovyScript.Descriptor descriptor = new GroovyScript.Descriptor();

        // When
        final boolean actual = descriptor.canScanConsoleLog();

        // Then
        assertThat(actual).isEqualTo(expected);
    }
}

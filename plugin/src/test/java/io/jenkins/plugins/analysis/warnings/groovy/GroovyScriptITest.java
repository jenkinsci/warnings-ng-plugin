package io.jenkins.plugins.analysis.warnings.groovy;

import java.util.Collections;

import org.junit.Test;
import org.jvnet.hudson.test.Issue;

import io.jenkins.plugins.analysis.core.model.Tool;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;

import static io.jenkins.plugins.analysis.core.testutil.Assertions.*;

/**
 * Tests the class {@link GroovyScript}.
 */
public class GroovyScriptITest extends IntegrationTestWithJenkinsPerSuite {

    /** Tests that the descriptor's canScanConsoleLog method returns the same as the configured permission ... for the "true" case */
    @Test
    public void descriptorMethodCanScanConsoleLogReturnsTrueIfConfigurationSaysConsoleLogScanningPermittedIsTrue() {
        testDescriptorCanScanConsoleLog(true);
    }

    /** Tests that the descriptor's canScanConsoleLog method returns the same as the configured permission ... for the "false" case */
    @Test
    public void descriptorMethodCanScanConsoleLogReturnsFalsefConfigurationSaysConsoleLogScanningPermittedIsFalse() {
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

    /** Verifies that the Groovy parser does not accept illegal IDs. */
    @Test
    @Issue("SECURITY-2090")
    public void setIdShouldThrowExceptionIfCustomIdHasInvalidPattern() {
        ParserConfiguration configuration = ParserConfiguration.getInstance();
        configuration.setParsers(Collections.singletonList(new GroovyParser("groovy", "", "", "", "")));
        Tool groovyScript = new GroovyScript("groovy");

        assertThatIllegalArgumentException().isThrownBy(() -> groovyScript.setId("../../invalid-id"));
    }
}

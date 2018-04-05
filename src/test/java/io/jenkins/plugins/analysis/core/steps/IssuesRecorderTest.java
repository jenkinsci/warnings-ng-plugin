package io.jenkins.plugins.analysis.core.steps;

import org.junit.jupiter.api.Test;

import io.jenkins.plugins.analysis.core.steps.IssuesRecorder.Descriptor;
import static io.jenkins.plugins.analysis.core.testutil.FormValidationAssert.*;

import hudson.util.FormValidation;

/**
 * Tests the class {@link IssuesRecorder}.
 *
 * @author Ullrich Hafner
 */
class IssuesRecorderTest {
    @Test
    void shouldBeOkIfIfEncodingIsEmpty() {
        Descriptor descriptor = new Descriptor();

        FormValidation actualResult = descriptor.doCheckSourceCodeEncoding("");

        assertThat(actualResult).isOk();
    }
}
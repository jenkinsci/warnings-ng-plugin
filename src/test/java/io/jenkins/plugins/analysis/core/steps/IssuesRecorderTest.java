package io.jenkins.plugins.analysis.core.steps;

import hudson.util.FormValidation;
import org.junit.jupiter.api.Test;
import io.jenkins.plugins.analysis.core.steps.IssuesRecorder.Descriptor;

import static io.jenkins.plugins.analysis.core.testutil.FormValidationAssert.assertThat;

/**
 * Tests the class {@link IssuesRecorder}
 *
 * @author Manuel Hampp
 */
class IssuesRecorderTest {

    @Test
    void shouldBeOkIfEncodingIsEmpty() {
        // given
        Descriptor descriptor = new Descriptor();

        // when
        FormValidation actionResult = descriptor.doCheckSourceCodeEncoding("");

        // then
        assertThat(actionResult).isOk();
    }


}
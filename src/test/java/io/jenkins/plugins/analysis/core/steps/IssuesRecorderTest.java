package io.jenkins.plugins.analysis.core.steps;

import org.junit.jupiter.api.Test;

import io.jenkins.plugins.analysis.core.steps.IssuesRecorder.Descriptor;
import static io.jenkins.plugins.analysis.core.testutil.FormValidationAssert.assertThat;

import hudson.util.FormValidation;

/**
 * @author Arne Schöntag
 */
class IssuesRecorderTest {

    @Test
    void shouldBeOkIfEncodingIsEmpty(){
        //given
        Descriptor descriptor = new Descriptor();

        //when
        FormValidation acturalResult = descriptor.doCheckSourceCodeEncoding("");

        //then
        assertThat(acturalResult).isOk();
    }

}
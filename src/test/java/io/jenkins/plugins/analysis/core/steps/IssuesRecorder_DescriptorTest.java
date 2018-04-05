package io.jenkins.plugins.analysis.core.steps;

import org.junit.jupiter.api.Test;

import io.jenkins.plugins.analysis.core.steps.IssuesRecorder.Descriptor;
import static io.jenkins.plugins.analysis.core.testutil.FormValidationAssert.assertThat;

import hudson.util.FormValidation;

/**
 * @author Arne Schöntag
 */
class IssuesRecorder_DescriptorTest {

    // doCheckSourceCodeEncoding
    @Test
    void shouldBeOkIfEncodingIsEmpty(){
        Descriptor descriptor = new Descriptor();
        FormValidation actualResult = descriptor.doCheckSourceCodeEncoding("");
        assertThat(actualResult).isOk();
    }

    @Test
    void shouldBeOkIfEncodingIsValid(){
        Descriptor descriptor = new Descriptor();
        FormValidation actualResult = descriptor.doCheckSourceCodeEncoding("UTF-8");
        assertThat(actualResult).isOk();
    }

    @Test
    void shouldNotBeOkIfEncodingIsInvalid(){
        Descriptor descriptor = new Descriptor();
        FormValidation actualResult = descriptor.doCheckSourceCodeEncoding("Some wrong text");
        assertThat(actualResult).isError();
    }

    // doCheckHealthy

    @Test
    void doCheckHealthyShouldBeOkWithValidValues(){
        // healthy = 0 = unhealthy
        Descriptor descriptor = new Descriptor();
        FormValidation actualResult = descriptor.doCheckHealthy(0,0);
        assertThat(actualResult).isOk();

        // healthy < unhealthy
        actualResult = descriptor.doCheckHealthy(3,10);
        assertThat(actualResult).isOk();
    }

    @Test
    void doCheckHealthyShouldBeNotOkWithInvalidValues(){
        // healthy < 0
        Descriptor descriptor = new Descriptor();
        FormValidation actualResult = descriptor.doCheckHealthy(-1,0);
        assertThat(actualResult).isError();

        // healthy = 0 , unhealthy > 0
        actualResult = descriptor.doCheckHealthy(0,1);
        assertThat(actualResult).isError();

        // healthy > 0 , unhealthy > healthy
        actualResult = descriptor.doCheckHealthy(10,3);
        assertThat(actualResult).isError();
    }

    // doCheckUnHealthy

    @Test
    void doCheckUnHealthyShouldBeOkWithValidValues(){
        // unhealthy > healthy > 0
        Descriptor descriptor = new Descriptor();
        FormValidation actualResult = descriptor.doCheckUnHealthy(2,4);
        assertThat(actualResult).isOk();

        // unhealthy > healthy = 0
        actualResult = descriptor.doCheckUnHealthy(0,4);
        assertThat(actualResult).isOk();
    }

    @Test
    void doCheckUnHealthyShouldBeNotOkWithInvalidValues(){
        // healthy > unhealthy = 0
        Descriptor descriptor = new Descriptor();
        FormValidation actualResult = descriptor.doCheckUnHealthy(3,0);
        assertThat(actualResult).isError();

        // healthy > unhealthy > 0
        actualResult = descriptor.doCheckUnHealthy(3,1);
        assertThat(actualResult).isError();

        // unhealthy < 0
        actualResult = descriptor.doCheckUnHealthy(0,-1);
        assertThat(actualResult).isError();
    }
}
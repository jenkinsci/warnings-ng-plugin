package io.jenkins.plugins.analysis.core.steps;

import org.junit.jupiter.api.Test;

import io.jenkins.plugins.analysis.core.steps.IssuesRecorder.Descriptor;
import static io.jenkins.plugins.analysis.core.testutil.FormValidationAssert.assertThat;

import hudson.util.ComboBoxModel;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;

/**
 * Tests the class {@link IssuesRecorder}.
 *
 * @author Ullrich Hafner
 */
class IssuesRecorderTest {

    @Test
    void shouldBeOkWithValidEncodings() {
        Descriptor empty = new Descriptor();
        FormValidation emptyResult = empty.doCheckSourceCodeEncoding("");

        Descriptor valid = new Descriptor();
        FormValidation validResult = valid.doCheckSourceCodeEncoding("UTF-8");

        Descriptor invalid = new Descriptor();
        FormValidation invalidResult = invalid.doCheckSourceCodeEncoding("Some wrong text");

        assertThat(emptyResult).isOk();
        assertThat(validResult).isOk();
        assertThat(invalidResult).hasMessage(invalid.createWrongEncodingErrorMessage());
    }

    // doFill
    @Test
    void doFillSourceCodeEncodingItemsShouldBeNotEmpty(){
        Descriptor descriptor = new Descriptor();
        ComboBoxModel boxModel = descriptor.doFillSourceCodeEncodingItems();
        edu.hm.hafner.analysis.assertj.Assertions.assertThat(boxModel).isNotEmpty();
    }

    @Test
    void doFillMinimumPriorityItemsShouldBeNotEmpty(){
        Descriptor descriptor = new Descriptor();
        ListBoxModel boxModel = descriptor.doFillMinimumPriorityItems();
        edu.hm.hafner.analysis.assertj.Assertions.assertThat(boxModel).isNotEmpty();
    }

    /* TODO
    @Test
    void doFillReferenceJobItemsShouldBeNotEmpty(){
        Descriptor descriptor = new Descriptor();
        ComboBoxModel boxModel = descriptor.doFillReferenceJobItems();
        edu.hm.hafner.analysis.assertj.Assertions.assertThat(boxModel).isNotEmpty();
    }*/

    // doCheckHealthy

    @Test
    void doCheckHealthyShouldBeOkWithValidValues(){
        // healthy = 0 = unhealthy
        Descriptor descriptor = new Descriptor();
        FormValidation actualResult = descriptor.doCheckHealthy(0,0);
        assertThat(actualResult).isOk();

        // healthy < unhealthy
        actualResult = descriptor.doCheckHealthy(1,2);
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
        actualResult = descriptor.doCheckHealthy(2,1);
        assertThat(actualResult).isError();
    }

    // doCheckUnHealthy

    @Test
    void doCheckUnHealthyShouldBeOkWithValidValues(){
        // unhealthy > healthy > 0
        Descriptor descriptor = new Descriptor();
        FormValidation actualResult = descriptor.doCheckUnHealthy(1,2);
        assertThat(actualResult).isOk();

        // unhealthy > healthy = 0
        actualResult = descriptor.doCheckUnHealthy(0,1);
        assertThat(actualResult).isOk();
    }

    @Test
    void doCheckUnHealthyShouldBeNotOkWithInvalidValues(){
        // healthy > unhealthy = 0
        Descriptor descriptor = new Descriptor();
        FormValidation actualResult = descriptor.doCheckUnHealthy(1,0);
        assertThat(actualResult).isError();

        // healthy > unhealthy > 0
        actualResult = descriptor.doCheckUnHealthy(1,1);
        assertThat(actualResult).isError();

        // unhealthy < 0
        actualResult = descriptor.doCheckUnHealthy(0,-1);
        assertThat(actualResult).isError();
    }
}
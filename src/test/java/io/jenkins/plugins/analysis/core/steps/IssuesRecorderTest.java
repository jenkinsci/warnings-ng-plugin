package io.jenkins.plugins.analysis.core.steps;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import io.jenkins.plugins.analysis.core.JenkinsFacade;
import io.jenkins.plugins.analysis.core.steps.IssuesRecorder.Descriptor;
import static io.jenkins.plugins.analysis.core.testutil.Assertions.assertThat;
import static io.jenkins.plugins.analysis.core.testutil.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.*;

import hudson.model.Job;
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
        Descriptor descriptor = new Descriptor();

        FormValidation emptyResult = descriptor.doCheckSourceCodeEncoding("");
        FormValidation validResult = descriptor.doCheckSourceCodeEncoding("UTF-8");
        FormValidation invalidResult = descriptor.doCheckSourceCodeEncoding("Some wrong text");

        assertSoftly(softly -> {
            softly.assertThat(emptyResult).isOk();
            softly.assertThat(validResult).isOk();
            softly.assertThat(invalidResult).hasMessage(descriptor.createWrongEncodingErrorMessage());
        });
    }

    // doFill
    @Test
    void doFillSourceCodeEncodingItemsShouldBeNotEmpty() {
        Descriptor descriptor = new Descriptor();
        ComboBoxModel boxModel = descriptor.doFillSourceCodeEncodingItems();
        edu.hm.hafner.analysis.assertj.Assertions.assertThat(boxModel).isNotEmpty();
    }

    @Test
    void doFillMinimumPriorityItemsShouldBeNotEmpty() {
        Descriptor descriptor = new Descriptor();
        ListBoxModel boxModel = descriptor.doFillMinimumPriorityItems();
        edu.hm.hafner.analysis.assertj.Assertions.assertThat(boxModel).isNotEmpty();
    }

    @Test
    void doFillReferenceJobItemsShouldBeNotEmpty(){
        JenkinsFacade mock = mock(JenkinsFacade.class);
        when(mock.getAllJobs()).thenReturn(new HashSet<>());
        Descriptor descriptor = new Descriptor(mock);
        ComboBoxModel boxModel = descriptor.doFillReferenceJobItems();
        edu.hm.hafner.analysis.assertj.Assertions.assertThat(boxModel).isNotEmpty();
    }

    @Test
    void doCheckReferenceJobShouldBeOkWithValidValues(){
        JenkinsFacade mock = mock(JenkinsFacade.class);
        String string = "referenceJob";
        Job<?,?> job = mock(Job.class);
        Optional<Job<?,?>> op = Optional.of(job);
        when(mock.getJob(string)).thenReturn(op);
        Descriptor descriptor = new Descriptor(mock);

        FormValidation formValidation1 = descriptor.doCheckReferenceJob(string);
        FormValidation formValidation2 = descriptor.doCheckReferenceJob("-");
        FormValidation formValidation3 = descriptor.doCheckReferenceJob("");

        assertSoftly(softly -> {
            softly.assertThat(formValidation1).isOk();
            softly.assertThat(formValidation2).isOk();
            softly.assertThat(formValidation3).isOk();
        });
    }

    @Test
    void doCheckReferenceJobShouldBeNotOkWithInvalidValues(){
        JenkinsFacade mock = mock(JenkinsFacade.class);
        String string = "referenceJob";
        Job<?,?> job = mock(Job.class);
        Optional<Job<?,?>> optional = Optional.of(job);
        when(mock.getJob(string)).thenReturn(optional);
        Descriptor descriptor = new Descriptor(mock);

        FormValidation formValidation1 = descriptor.doCheckReferenceJob("not referenceJob");

        optional = Optional.empty();
        when(mock.getJob(string)).thenReturn(optional);

        FormValidation formValidation2 = descriptor.doCheckReferenceJob("not referenceJob");

        assertSoftly(softly -> {
            softly.assertThat(formValidation1).hasMessage(Messages.FieldValidator_Error_ReferenceJobDoesNotExist());
            softly.assertThat(formValidation2).hasMessage(Messages.FieldValidator_Error_ReferenceJobDoesNotExist());
        });
    }

    // doCheckHealthy

    @Test
    void doCheckHealthyShouldBeOkWithValidValues() {
        // healthy = 0 = unhealthy
        Descriptor descriptor = new Descriptor();
        FormValidation actualResult = descriptor.doCheckHealthy(0, 0);
        assertThat(actualResult).isOk();

        // healthy < unhealthy
        actualResult = descriptor.doCheckHealthy(1, 2);
        assertThat(actualResult).isOk();
    }

    @Test
    void doCheckHealthyShouldBeNotOkWithInvalidValues() {
        // healthy < 0
        Descriptor descriptor = new Descriptor();
        FormValidation actualResult = descriptor.doCheckHealthy(-1, 0);
        assertThat(actualResult).isError();

        // healthy = 0 , unhealthy > 0
        actualResult = descriptor.doCheckHealthy(0, 1);
        assertThat(actualResult).isError();

        // healthy > 0 , unhealthy > healthy
        actualResult = descriptor.doCheckHealthy(2, 1);
        assertThat(actualResult).isError();
    }

    // doCheckUnHealthy

    @Test
    void doCheckUnHealthyShouldBeOkWithValidValues() {
        // unhealthy > healthy > 0
        Descriptor descriptor = new Descriptor();
        FormValidation actualResult = descriptor.doCheckUnHealthy(1, 2);
        assertThat(actualResult).isOk();

        // unhealthy > healthy = 0
        actualResult = descriptor.doCheckUnHealthy(0, 1);
        assertThat(actualResult).isOk();
    }

    @Test
    void doCheckUnHealthyShouldBeNotOkWithInvalidValues() {
        // healthy > unhealthy = 0
        Descriptor descriptor = new Descriptor();
        FormValidation actualResult = descriptor.doCheckUnHealthy(1, 0);
        assertThat(actualResult).isError();

        // healthy > unhealthy > 0
        actualResult = descriptor.doCheckUnHealthy(1, 1);
        assertThat(actualResult).isError();

        // unhealthy < 0
        actualResult = descriptor.doCheckUnHealthy(0, -1);
        assertThat(actualResult).isError();
    }

    @Test
    void shouldContainEmptyJobPlaceHolder() {
        JenkinsFacade jenkins = mock(JenkinsFacade.class);
        Descriptor descriptor = new Descriptor(jenkins);

        ComboBoxModel actualModel = descriptor.doFillReferenceJobItems();

        assertThat(actualModel).hasSize(1);
        assertThat(actualModel).containsExactly(IssuesRecorder.NO_REFERENCE_JOB);
    }

    @Test
    void shouldContainSingleElementAndPlaceHolder() {
        JenkinsFacade jenkins = mock(JenkinsFacade.class);
        Job job = mock(Job.class);
        String name = "Job Name";
        when(jenkins.getFullNameOf(job)).thenReturn(name);
        when(jenkins.getAllJobs()).thenReturn(Collections.singleton(name));

        Descriptor descriptor = new Descriptor(jenkins);

        ComboBoxModel actualModel = descriptor.doFillReferenceJobItems();

        assertThat(actualModel).hasSize(2);
        assertThat(actualModel).containsExactly("-", name);
    }
}
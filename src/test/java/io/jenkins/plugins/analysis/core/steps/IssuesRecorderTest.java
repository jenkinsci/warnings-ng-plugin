package io.jenkins.plugins.analysis.core.steps;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import io.jenkins.plugins.analysis.core.JenkinsFacade;
import io.jenkins.plugins.analysis.core.steps.IssuesRecorder.Descriptor;
import static io.jenkins.plugins.analysis.core.steps.IssuesRecorder.NO_REFERENCE_JOB;
import static io.jenkins.plugins.analysis.core.testutil.Assertions.*;
import static io.jenkins.plugins.analysis.core.testutil.SoftAssertions.*;
import static org.mockito.Mockito.*;

import hudson.model.Job;
import hudson.util.ComboBoxModel;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;

/**
 * Tests the class {@link IssuesRecorder}.
 *
 * @author Arne Schöntag
 * @author Ullrich Hafner
 */
class IssuesRecorderTest {
    @Test
    void shouldBeOkWithValidEncodings() {
        Descriptor descriptor = new Descriptor();

        assertSoftly(softly -> {
            softly.assertThat(descriptor.doCheckSourceCodeEncoding(""))
                    .isOk();
            softly.assertThat(descriptor.doCheckSourceCodeEncoding("UTF-8"))
                    .isOk();
            softly.assertThat(descriptor.doCheckSourceCodeEncoding("Some wrong text"))
                    .isError()
                    .hasMessage(descriptor.createWrongEncodingErrorMessage());
        });
    }

    @Test
    void doFillSourceCodeEncodingItemsShouldBeNotEmpty() {
        Descriptor descriptor = new Descriptor();

        assertThat(descriptor.doFillSourceCodeEncodingItems())
                .isNotEmpty()
                .contains("UTF-8", "ISO-8859-1");
    }

    @Test
    void doFillMinimumPriorityItemsShouldBeNotEmpty() {
        Descriptor descriptor = new Descriptor();
        ListBoxModel boxModel = descriptor.doFillMinimumPriorityItems();
        assertThat(boxModel).isNotEmpty();
    }

    @Test
    void doFillReferenceJobItemsShouldBeNotEmpty() {
        JenkinsFacade jenkins = mock(JenkinsFacade.class);
        when(jenkins.getAllJobs()).thenReturn(new HashSet<>());

        Descriptor descriptor = new Descriptor(jenkins);

        assertThat(descriptor.doFillReferenceJobItems()).containsExactly(NO_REFERENCE_JOB);
    }

    @Test
    void doCheckReferenceJobShouldBeOkWithValidValues() {
        JenkinsFacade jenkins = mock(JenkinsFacade.class);
        Job<?, ?> job = mock(Job.class);
        String jobName = "referenceJob";
        when(jenkins.getJob(jobName)).thenReturn(Optional.of(job));
        Descriptor descriptor = new Descriptor(jenkins);

        assertSoftly(softly -> {
            softly.assertThat(descriptor.doCheckReferenceJob(jobName)).isOk();
            softly.assertThat(descriptor.doCheckReferenceJob(NO_REFERENCE_JOB)).isOk();
            softly.assertThat(descriptor.doCheckReferenceJob("")).isOk();
        });
    }

    @Test
    void doCheckReferenceJobShouldBeNotOkWithInvalidValues() {
        JenkinsFacade mock = mock(JenkinsFacade.class);
        String string = "referenceJob";
        Job<?, ?> job = mock(Job.class);
        Optional<Job<?, ?>> optional = Optional.of(job);
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
        assertThat(actualModel).containsExactly(NO_REFERENCE_JOB);
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
        assertThat(actualModel).containsExactly(NO_REFERENCE_JOB, name);
    }
}
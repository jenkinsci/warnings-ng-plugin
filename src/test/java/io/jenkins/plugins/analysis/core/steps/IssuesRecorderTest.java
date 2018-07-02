package io.jenkins.plugins.analysis.core.steps;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Priority;
import io.jenkins.plugins.analysis.core.JenkinsFacade;
import static io.jenkins.plugins.analysis.core.steps.IssuesRecorder.*;
import static io.jenkins.plugins.analysis.core.testutil.Assertions.*;
import io.jenkins.plugins.analysis.core.testutil.LocalizedMessagesTest;
import static io.jenkins.plugins.analysis.core.testutil.SoftAssertions.*;
import static org.mockito.Mockito.*;

import hudson.model.Job;
import hudson.util.ComboBoxModel;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import hudson.util.ListBoxModel.Option;

/**
 * Tests the class {@link IssuesRecorder}.
 *
 * @author Arne Schöntag
 * @author Stephan Plöderl
 * @author Ullrich Hafner
 */
class IssuesRecorderTest extends LocalizedMessagesTest {
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

        ComboBoxModel sourceCodeEncodingItems = descriptor.doFillSourceCodeEncodingItems();
        assertThat(sourceCodeEncodingItems)
                .isNotEmpty()
                .contains("UTF-8", "ISO-8859-1");
        assertThat(descriptor.doFillReportEncodingItems())
                .isEqualTo(sourceCodeEncodingItems);
    }

    @Test
    void doFillMinimumPriorityItemsShouldBeNotEmpty() {
        Descriptor descriptor = new Descriptor();
        ListBoxModel boxModel = descriptor.doFillMinimumPriorityItems();

        assertThat(boxModel.size()).isEqualTo(3);

        Option actualHighOption = boxModel.get(0);
        Option actualNormalOption = boxModel.get(1);
        Option actualLowOption = boxModel.get(2);

        assertThat(actualHighOption.value).isEqualTo(Priority.HIGH.name());
        assertThat(actualHighOption.name).isEqualTo(Messages.PriorityFilter_High());
        assertThat(actualNormalOption.value).isEqualTo(Priority.NORMAL.name());
        assertThat(actualNormalOption.name).isEqualTo(Messages.PriorityFilter_Normal());
        assertThat(actualLowOption.value).isEqualTo(Priority.LOW.name());
        assertThat(actualLowOption.name).isEqualTo(Messages.PriorityFilter_Low());
    }

    @Test
    void doFillReferenceJobItemsShouldBeNotEmpty() {
        JenkinsFacade jenkins = mock(JenkinsFacade.class);
        when(jenkins.getAllJobs()).thenReturn(new HashSet<>());

        Descriptor descriptor = new Descriptor(jenkins);

        assertThat(descriptor.doFillReferenceJobNameItems()).containsExactly(NO_REFERENCE_JOB);
    }

    @Test
    void doCheckReferenceJobShouldBeOkWithValidValues() {
        JenkinsFacade jenkins = mock(JenkinsFacade.class);
        Job<?, ?> job = mock(Job.class);
        String jobName = "referenceJob";
        when(jenkins.getJob(jobName)).thenReturn(Optional.of(job));
        Descriptor descriptor = new Descriptor(jenkins);

        assertSoftly(softly -> {
            softly.assertThat(descriptor.doCheckReferenceJobName(jobName)).isOk();
            softly.assertThat(descriptor.doCheckReferenceJobName(NO_REFERENCE_JOB)).isOk();
            softly.assertThat(descriptor.doCheckReferenceJobName("")).isOk();
        });
    }

    @Test
    void doCheckReferenceJobShouldBeNOkWithInvalidValue() {
        String referenceJob = "referenceJob";
        JenkinsFacade jenkins = mock(JenkinsFacade.class);
        when(jenkins.getJob(referenceJob)).thenReturn(Optional.empty());
        Descriptor descriptor = new Descriptor(jenkins);

        assertThat(descriptor.doCheckReferenceJobName(referenceJob))
                .isError()
                .hasMessage("There is no such job - maybe the job has been renamed?");
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

        ComboBoxModel actualModel = descriptor.doFillReferenceJobNameItems();

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

        ComboBoxModel actualModel = descriptor.doFillReferenceJobNameItems();

        assertThat(actualModel).hasSize(2);
        assertThat(actualModel).containsExactly(NO_REFERENCE_JOB, name);
    }
}
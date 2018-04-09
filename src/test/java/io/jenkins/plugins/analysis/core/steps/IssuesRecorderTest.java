package io.jenkins.plugins.analysis.core.steps;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import io.jenkins.plugins.analysis.core.JenkinsFacade;
import io.jenkins.plugins.analysis.core.steps.IssuesRecorder.Descriptor;
import static io.jenkins.plugins.analysis.core.testutil.Assertions.*;
import static org.mockito.Mockito.*;

import hudson.model.Job;
import hudson.util.ComboBoxModel;
import hudson.util.FormValidation;

/**
 * Tests the class {@link IssuesRecorder}.
 *
 * @author Ullrich Hafner
 */
class IssuesRecorderTest {
    @Test
    void shouldBeOkIfEncodingIsEmpty() {
        Descriptor descriptor = new Descriptor();

        FormValidation actualResult = descriptor.doCheckSourceCodeEncoding("");

        assertThat(actualResult).isOk();
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
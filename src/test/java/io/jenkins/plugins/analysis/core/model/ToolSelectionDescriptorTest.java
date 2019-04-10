package io.jenkins.plugins.analysis.core.model;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;

import hudson.model.Job;
import hudson.util.ComboBoxModel;

import io.jenkins.plugins.analysis.core.model.ToolSelection.ToolSelectionDescriptor;
import io.jenkins.plugins.analysis.core.util.JenkinsFacade;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link ToolSelectionDescriptor}.
 *
 * @author Fabian Janker
 */

class ToolSelectionDescriptorTest {

    @Test
    void shouldFillIDItems() {
        ToolSelectionDescriptor toolSelectionDescriptor = new ToolSelectionDescriptor();

        JobAction jobAction1 = mock(JobAction.class);
        when(jobAction1.getId()).thenReturn("1");

        JobAction jobAction2 = mock(JobAction.class);
        when(jobAction2.getId()).thenReturn("2");

        Job job = mock(Job.class);
        when(job.getActions(JobAction.class)).thenReturn(Lists.list(jobAction1, jobAction2));

        JenkinsFacade jenkinsFacade = mock(JenkinsFacade.class);
        when(jenkinsFacade.getAllJobs()).thenReturn(Lists.list(job));
        ToolSelectionDescriptor.setJenkinsFacade(jenkinsFacade);

        ComboBoxModel model = toolSelectionDescriptor.doFillIdItems();

        assertThat(model.contains("1")).isTrue();
        assertThat(model.contains("2")).isTrue();
    }
}

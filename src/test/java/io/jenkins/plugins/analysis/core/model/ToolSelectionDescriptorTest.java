package io.jenkins.plugins.analysis.core.model;

import java.util.ArrayList;
import java.util.List;

import org.assertj.core.util.Lists;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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
    @ParameterizedTest(name = "{index} => Existing Job IDs = <{0}>")
    @ValueSource(strings = {"", "1", "1, 2", "1, 2, 3"})
    void shouldFillIDItems(final String ids) {
        String[] elements = ids.split(",", -1);

        ToolSelectionDescriptor toolSelectionDescriptor = new ToolSelectionDescriptor();

        List<JobAction> actions = new ArrayList<>();
        for (String element : elements) {
            JobAction jobAction = mock(JobAction.class);
            when(jobAction.getId()).thenReturn(element);
            actions.add(jobAction);
        }

        Job job = mock(Job.class);
        when(job.getActions(JobAction.class)).thenReturn(actions);

        JenkinsFacade jenkinsFacade = mock(JenkinsFacade.class);
        when(jenkinsFacade.getAllJobs()).thenReturn(Lists.list(job));
        ToolSelectionDescriptor.setJenkinsFacade(jenkinsFacade);

        ComboBoxModel model = toolSelectionDescriptor.doFillIdItems();

        assertThat(model).containsExactly(elements);
    }
}

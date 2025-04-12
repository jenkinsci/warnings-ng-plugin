package io.jenkins.plugins.analysis.core.model;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;

import hudson.model.BuildableItem;
import hudson.model.Item;
import hudson.model.Job;

import io.jenkins.plugins.analysis.core.model.ToolSelection.ToolSelectionDescriptor;
import io.jenkins.plugins.util.JenkinsFacade;

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
        var elements = ids.split(",", -1);

        var model = createDescriptor(elements, true).doFillIdItems(null);
        assertThat(model).containsExactly(elements);

        var prohibited = createDescriptor(elements, false).doFillIdItems(null);
        assertThat(prohibited).isEmpty();
    }

    @Test
    void shouldReturnEmptyListIfNoPermission() {
        String[] elements = {"1", "3"};

        var model = createDescriptor(elements, true).doFillIdItems(null);
        assertThat(model).containsExactly(elements);

        var prohibited = createDescriptor(elements, false).doFillIdItems(null);
        assertThat(prohibited).isEmpty();
    }

    private ToolSelectionDescriptor createDescriptor(final String[] elements, final boolean hasPermission) {
        var toolSelectionDescriptor = new ToolSelectionDescriptor();

        List<JobAction> actions = new ArrayList<>();
        for (String element : elements) {
            JobAction jobAction = mock(JobAction.class);
            when(jobAction.getId()).thenReturn(element);
            actions.add(jobAction);
        }

        Job<?, ?> job = mock(Job.class);
        when(job.getActions(JobAction.class)).thenReturn(actions);

        JenkinsFacade jenkinsFacade = mock(JenkinsFacade.class);
        when(jenkinsFacade.getAllJobs()).thenReturn(Lists.list(job));
        ToolSelectionDescriptor.setJenkinsFacade(jenkinsFacade);
        when(jenkinsFacade.hasPermission(Item.CONFIGURE, (BuildableItem) null)).thenReturn(hasPermission);
        return toolSelectionDescriptor;
    }
}

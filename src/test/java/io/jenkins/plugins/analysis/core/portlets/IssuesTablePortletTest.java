package io.jenkins.plugins.analysis.core.portlets;

import org.junit.jupiter.api.Test;

import hudson.model.Job;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link IssuesTablePortlet}.
 *
 * @author Ullrich Hafner
 */
class IssuesTablePortletTest {
    @Test
    void shouldName() {
        IssuesTablePortlet issues = new IssuesTablePortlet("issues");

        assertThatIllegalStateException()
                .as("Mapping is generated in getToolNames and must be called first")
                .isThrownBy(() -> issues.getTotals(mock(Job.class)));
    }
}
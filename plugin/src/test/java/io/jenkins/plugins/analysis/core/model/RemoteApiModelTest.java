package io.jenkins.plugins.analysis.core.model;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.assertThat;
import static org.mockito.Mockito.*;

import hudson.model.Run;
import io.jenkins.plugins.analysis.core.model.RemoteApiModel.ToolEndpoints;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Tests the class {@link RemoteApiModel}.
 *
 * @author Ullrich Hafner
 */
class RemoteApiModelTest {
    /**
     * Verifies that the model provides the endpoints of all tools in the order of the results.
     */
    @Test
    void shouldProvideEndpointsOfAllTools() {
        Run<?, ?> run = mock(Run.class);
        var checkstyle = createAction("checkstyle", "CheckStyle");
        var pmd = createAction("pmd", "PMD");

        var model = new RemoteApiModel(run, List.of(checkstyle, pmd));

        assertThat(model).hasDisplayName("Remote API").hasUrlName("remote-api").hasAggregationId("warnings-ng");
        assertThat(model.getObject()).isSameAs(run);
        assertThat(model.getTab().getObject()).isSameAs(run);
        assertThat(model.hasTools()).isTrue();
        assertThat(model.getTools()).extracting(ToolEndpoints::getId).containsExactly("checkstyle", "pmd");
        assertThat(model.getTools().get(1)).hasId("pmd").hasDisplayName("PMD").hasSmallImage("pmd-icon");
    }

    /**
     * Verifies that a run without results provides no tools.
     */
    @Test
    void shouldHandleRunWithoutTools() {
        var model = new RemoteApiModel(mock(Run.class), List.of());

        assertThat(model).hasNoTools();
        assertThat(model.hasTools()).isFalse();
    }

    /**
     * Creates a stub for the results of a tool.
     *
     * @param id
     *         the ID of the tool
     * @param displayName
     *         the name of the tool
     *
     * @return the results of the tool
     */
    private ResultAction createAction(final String id, final String displayName) {
        ResultAction action = mock(ResultAction.class);

        when(action.getId()).thenReturn(id);
        when(action.getDisplayName()).thenReturn(displayName);
        when(action.getSmallImage()).thenReturn(id + "-icon");

        return action;
    }
}

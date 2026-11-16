package io.jenkins.plugins.analysis.core.model;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.assertThat;
import static org.mockito.Mockito.*;

import hudson.model.Run;
import io.jenkins.plugins.analysis.core.model.LogModel.ToolLog;
import java.util.List;
import org.eclipse.collections.impl.factory.Lists;
import org.junit.jupiter.api.Test;

/**
 * Tests the class {@link LogModel}.
 *
 * @author Ullrich Hafner
 */
class LogModelTest {
    /**
     * Verifies that the model provides the messages of all tools, including the tools without errors, and that
     * the first tool with errors is shown by default.
     */
    @Test
    void shouldProvideMessagesOfAllTools() {
        Run<?, ?> run = mock(Run.class);
        var checkstyle = createAction("checkstyle", "CheckStyle", List.of("Parsing checkstyle.xml"), List.of());
        var pmd = createAction("pmd", "PMD", List.of("Parsing pmd.xml", "Found 2 issues"), List.of("No blame"));
        var spotbugs = createAction("spotbugs", "SpotBugs", List.of(), List.of("No files found", "No blame"));

        var model = new LogModel(run, List.of(checkstyle, pmd, spotbugs));

        assertThat(model).hasDisplayName("Log Messages").hasUrlName("log").hasDefaultToolId("pmd");
        assertThat(model.getObject()).isSameAs(run);
        assertThat(model.hasTools()).isTrue();
        assertThat(model.getTools()).extracting(ToolLog::getId).containsExactly("checkstyle", "pmd", "spotbugs");
        assertThat(model.getTools())
                .extracting(ToolLog::getLabel)
                .containsExactly("CheckStyle", "PMD (1 error)", "SpotBugs (2 errors)");

        var tools = model.getTools();
        assertThat(tools.get(0))
                .hasDisplayName("CheckStyle")
                .hasIconFileName("checkstyle-icon")
                .hasInfoMessages("Parsing checkstyle.xml")
                .hasNoErrorMessages();
        assertThat(tools.get(0).hasErrors()).isFalse();
        assertThat(tools.get(1))
                .hasDisplayName("PMD")
                .hasInfoMessages("Parsing pmd.xml", "Found 2 issues")
                .hasErrorMessages("No blame");
        assertThat(tools.get(1).hasErrors()).isTrue();
        assertThat(tools.get(2)).hasNoInfoMessages().hasErrorMessages("No files found", "No blame");
    }

    /**
     * Verifies that the first tool is shown by default if no tool has errors.
     */
    @Test
    void shouldSelectFirstToolIfThereAreNoErrors() {
        var checkstyle = createAction("checkstyle", "CheckStyle", List.of("Parsing checkstyle.xml"), List.of());
        var pmd = createAction("pmd", "PMD", List.of("Parsing pmd.xml"), List.of());

        var model = new LogModel(mock(Run.class), List.of(checkstyle, pmd));

        assertThat(model).hasDefaultToolId("checkstyle");
        assertThat(model.getTools()).extracting(ToolLog::getLabel).containsExactly("CheckStyle", "PMD");
    }

    /**
     * Verifies that a run without results provides no tools and no default tool.
     */
    @Test
    void shouldHandleRunWithoutTools() {
        var model = new LogModel(mock(Run.class), List.of());

        assertThat(model).hasNoTools().hasDefaultToolId("");
        assertThat(model.hasTools()).isFalse();
    }

    /**
     * Creates a stub for the results of a tool.
     *
     * @param id
     *         the ID of the tool
     * @param displayName
     *         the name of the tool
     * @param infoMessages
     *         the information messages of the tool
     * @param errorMessages
     *         the error messages of the tool
     *
     * @return the results of the tool
     */
    private ResultAction createAction(
            final String id,
            final String displayName,
            final List<String> infoMessages,
            final List<String> errorMessages) {
        ResultAction action = mock(ResultAction.class);
        AnalysisResult result = mock(AnalysisResult.class);

        when(action.getId()).thenReturn(id);
        when(action.getDisplayName()).thenReturn(displayName);
        when(action.getIconFileName()).thenReturn(id + "-icon");
        when(action.getResult()).thenReturn(result);
        when(result.getInfoMessages()).thenReturn(Lists.immutable.withAll(infoMessages));
        when(result.getErrorMessages()).thenReturn(Lists.immutable.withAll(errorMessages));

        return action;
    }
}

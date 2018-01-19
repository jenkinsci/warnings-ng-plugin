package io.jenkins.plugins.analysis.core.model;

import org.junit.Test;
import org.jvnet.hudson.test.TestExtension;

import edu.hm.hafner.analysis.IssueParser;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTest;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link ToolRegistry}. This integration test starts Jenkins and inquires
 * the registered extensions of {@link StaticAnalysisTool}.
 *
 * @author Ullrich Hafner
 */
public class ToolRegistryITest extends IntegrationTest {
    private static final String ANNOTATED_ID = "annotatedTool";
    private static final String TOOL_ID = "tool";

    @Test
    public void shouldHaveOneElementWhenCreated() {
        ToolRegistry registry = new ToolRegistry();

        assertThat(registry.getIds()).hasSize(1);
        assertThat(registry.find(ANNOTATED_ID)).isNotNull();
        assertThat(registry.find(ANNOTATED_ID).getId()).isEqualTo(ANNOTATED_ID);
    }

    @Test
    public void shouldAlsoAllowManuallyRegisteredTools() {
        ToolRegistry registry = new ToolRegistry();

        StaticAnalysisTool tool = createTool();
        registry.register(tool);

        assertThat(registry.getIds()).containsExactlyInAnyOrder(TOOL_ID, ANNOTATED_ID);
        assertThat(registry.find(TOOL_ID)).isSameAs(tool);
        assertThat(registry.find(ANNOTATED_ID)).isNotSameAs(tool);
    }

    private StaticAnalysisTool createTool() {
        StaticAnalysisTool tool = mock(StaticAnalysisTool.class);
        when(tool.getId()).thenReturn(TOOL_ID);
        return tool;
    }

    @TestExtension
    public static class TestTool extends StaticAnalysisTool {
        @Override
        public String getId() {
            return ToolRegistryITest.ANNOTATED_ID;
        }

        @Override
        public StaticAnalysisLabelProvider getLabelProvider() {
            return new DefaultLabelProvider();
        }

        @Override
        public IssueParser createParser() {
            return null;
        }
    }
}
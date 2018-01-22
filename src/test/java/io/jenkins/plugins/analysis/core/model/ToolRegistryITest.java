package io.jenkins.plugins.analysis.core.model;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.Test;
import org.jvnet.hudson.test.TestExtension;

import edu.hm.hafner.analysis.IssueParser;
import io.jenkins.plugins.analysis.core.model.ToolRegistry.StaticAnalysisToolFactory;
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
    private static final String PROVIDER_ID = "provider";

    /** Verifies that the tool registry throws an exception if the ID does not exist. */
    @Test
    public void shouldThrowExceptionIfFindIsCalledWithNoExistingId() {
        ToolRegistry registry = new ToolRegistry();

        String id = "nothing";
        assertThatExceptionOfType(NoSuchElementException.class)
                .as("Searching with non-existing ID '%s'", id)
                .isThrownBy(() -> registry.find(id))
                .withMessageContaining(id);
    }

    /**
     * Verifies that the tool registry is correctly filled with static analysis tool instances that are either
     * registered using the extension point {@link StaticAnalysisTool} or dynamically returned by instances of
     * the extension point {@link StaticAnalysisToolFactory}.
     */
    @Test
    public void shouldHaveOneElementWhenCreated() {
        ToolRegistry registry = new ToolRegistry();

        assertThat(registry.getIds()).containsExactlyInAnyOrder(ANNOTATED_ID, PROVIDER_ID);
        assertThatToolExistsWithId(registry, ANNOTATED_ID);
        assertThatToolExistsWithId(registry, PROVIDER_ID);
    }

    private void assertThatToolExistsWithId(final ToolRegistry registry, final String id) {
        assertThat(registry.find(id)).isNotNull();
        assertThat(registry.find(id).getId()).isEqualTo(id);
    }

    private static StaticAnalysisTool createTool(final String id) {
        StaticAnalysisTool tool = mock(StaticAnalysisTool.class);
        when(tool.getId()).thenReturn(id);
        return tool;
    }

    @TestExtension @SuppressWarnings("unused")
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

    @TestExtension @SuppressWarnings("unused")
    public static class TestFactory implements StaticAnalysisToolFactory {
        @Override
        public List<StaticAnalysisTool> getTools() {
            return Collections.singletonList(createTool(PROVIDER_ID));
        }
    }
}
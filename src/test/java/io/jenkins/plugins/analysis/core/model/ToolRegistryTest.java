package io.jenkins.plugins.analysis.core.model;

import java.io.File;
import java.nio.charset.Charset;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.TestExtension;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Issues;
import edu.hm.hafner.analysis.ParsingCanceledException;
import edu.hm.hafner.analysis.ParsingException;
import io.jenkins.plugins.analysis.core.JenkinsFacade;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link ToolRegistry}.
 *
 * @author Ullrich Hafner
 */
class ToolRegistryTest {
    private static final String TOOL_ID = "tool";

    @Test
    void shouldBeEmptyWhenCreated() {
        ToolRegistry registry = createRegistryUnderTest();

        assertThat(registry.getIds()).isEmpty();

        String id = "nothing";
        assertThatExceptionOfType(NoSuchElementException.class)
                .as("Searching with non-existing ID '%s'", id)
                .isThrownBy(() -> registry.find(id))
                .withMessageContaining(id);
    }

    @Test
    void shouldFindASingleTool() {
        ToolRegistry registry = createRegistryUnderTest();

        StaticAnalysisTool tool = createTool();
        registry.register(tool);

        assertThat(registry.getIds()).containsExactly(TOOL_ID);
        assertThat(registry.find(TOOL_ID)).isSameAs(tool);
    }

    @Test
    void shouldNotAllowToRegisterAnotherToolWithSameId() {
        ToolRegistry registry = createRegistryUnderTest();

        registry.register(createTool());
        assertThatExceptionOfType(AssertionError.class)
                .as("Registering duplicate id '%s'", TOOL_ID)
                .isThrownBy(() -> registry.register(createTool()))
                .withMessageContaining(TOOL_ID);
    }

    private ToolRegistry createRegistryUnderTest() {
        return new ToolRegistry(mock(JenkinsFacade.class));
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
            return "annotatedTool";
        }

        @Override
        public Issues<Issue> parse(final File file, final Charset charset, final IssueBuilder builder)
                throws ParsingException, ParsingCanceledException {
            return null;
        }
    }
}
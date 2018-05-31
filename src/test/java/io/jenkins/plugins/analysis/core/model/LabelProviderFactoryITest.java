package io.jenkins.plugins.analysis.core.model;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.jvnet.hudson.test.TestExtension;

import edu.hm.hafner.analysis.IssueParser;
import edu.umd.cs.findbugs.annotations.NonNull;
import io.jenkins.plugins.analysis.core.model.LabelProviderFactory.StaticAnalysisToolFactory;
import static io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProviderAssert.*;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTest;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link LabelProviderFactory}.
 *
 * @author Ullrich Hafner
 */
public class LabelProviderFactoryITest extends IntegrationTest {
    private static final String ANNOTATED_ID = "annotatedTool";
    private static final String PROVIDER_ID = "provider";
    private static final String UNDEFINED_ID = "undefined-id";
    private static final String TOOL_NAME = "Tool Name";

    /**
     * Verifies that the name of the label provider is obtained from the registered tool, if the {@code name} parameter
     * is empty. If the tool with the specified ID is not found, then the default name is returned.
     */
    @Test
    public void shouldUseNameOfStaticAnalysisToolIfParameterNameIsBlank() {
        LabelProviderFactory factory = new LabelProviderFactory();

        String[] ids = {ANNOTATED_ID, PROVIDER_ID};
        String[] names = {"", null};

        for (String name : names) {
            for (String id : ids) {
                StaticAnalysisLabelProvider registered = factory.create(id, name);
                assertThat(registered).as("Tool %s and name '%s'", id, name).hasId(id);
                assertThat(registered).as("Tool %s and name '%s'", id, name).hasName(id);
            }
        }

        StaticAnalysisLabelProvider notRegistered = factory.create(UNDEFINED_ID, "");
        assertThat(notRegistered).hasId(UNDEFINED_ID);
        assertThat(notRegistered).hasName(new StaticAnalysisLabelProvider(UNDEFINED_ID).getDefaultName());
    }

    /**
     * Verifies that the name of the registered tool is not used, if the {@code name} parameter is provided.
     */
    @Test
    public void shouldParameterNameIfNotBlank() {
        LabelProviderFactory factory = new LabelProviderFactory();

        String[] ids = {ANNOTATED_ID, PROVIDER_ID, UNDEFINED_ID};

        for (String id : ids) {
            StaticAnalysisLabelProvider registered = factory.create(id, TOOL_NAME);
            assertThat(registered).as("Tool %s and name '%s'", id, TOOL_NAME).hasId(id);
            assertThat(registered).as("Tool %s and name '%s'", id, TOOL_NAME).hasName(TOOL_NAME);
        }
    }

    /**
     * Static analysis tool that that implements the extension point.
     */
    @SuppressWarnings("unused")
    public static class TestTool extends StaticAnalysisTool {
        @Override
        public String getId() {
            return ANNOTATED_ID;
        }

        @Override
        public IssueParser createParser() {
            return null;
        }

        /**
         * Required descriptor for the tool.
         */
        @TestExtension
        public static final class TestToolDescriptor extends StaticAnalysisToolDescriptor {
            TestToolDescriptor() {
                super(ANNOTATED_ID);
            }

            @NonNull
            @Override
            public String getDisplayName() {
                return ANNOTATED_ID;
            }
        }
    }

    /**
     * Factory that returns a stub for all calls.
     */
    @TestExtension
    @SuppressWarnings("unused")
    public static class TestFactory implements StaticAnalysisToolFactory {
        @Override
        public List<StaticAnalysisTool> getTools() {
            return Collections.singletonList(createTool(PROVIDER_ID));
        }

        private StaticAnalysisTool createTool(final String id) {
            StaticAnalysisTool tool = mock(StaticAnalysisTool.class);
            when(tool.getId()).thenReturn(id);
            when(tool.getName()).thenReturn(id);
            when(tool.getLabelProvider()).thenReturn(new StaticAnalysisLabelProvider(id, id));
            return tool;
        }

    }
}
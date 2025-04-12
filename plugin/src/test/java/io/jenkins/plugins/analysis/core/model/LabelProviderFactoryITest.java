package io.jenkins.plugins.analysis.core.model;

import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.TestExtension;

import edu.hm.hafner.analysis.IssueParser;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.Optional;

import io.jenkins.plugins.analysis.core.model.LabelProviderFactory.StaticAnalysisToolFactory;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;

import static io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProviderAssert.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link LabelProviderFactory}.
 *
 * @author Ullrich Hafner
 */
class LabelProviderFactoryITest extends IntegrationTestWithJenkinsPerSuite {
    private static final String ANNOTATED_ID = "annotated-tool";
    private static final String PROVIDER_ID = "provider";
    private static final String UNDEFINED_ID = "undefined-id";
    private static final String TOOL_NAME = "Tool Name";

    /**
     * Verifies that the name of the label provider is obtained from the registered tool, if the {@code name} parameter
     * is empty. If the tool with the specified ID is not found, then the default name is returned.
     */
    @Test
    void shouldUseNameOfStaticAnalysisToolIfParameterNameIsBlank() {
        var factory = new LabelProviderFactory();

        String[] ids = {ANNOTATED_ID, PROVIDER_ID};
        String[] names = {"", null};

        for (String name : names) {
            for (String id : ids) {
                var registered = factory.create(id, name);
                assertThat(registered).as("Tool %s and name '%s'", id, name).hasId(id);
                assertThat(registered).as("Tool %s and name '%s'", id, name).hasName(id);
            }
        }

        var notRegistered = factory.create(UNDEFINED_ID, "");
        assertThat(notRegistered).hasId(UNDEFINED_ID);
        assertThat(notRegistered).hasName(new StaticAnalysisLabelProvider(UNDEFINED_ID).getDefaultName());
    }

    /**
     * Verifies that the name of the registered tool is not used, if the {@code name} parameter is provided.
     */
    @Test
    void shouldParameterNameIfNotBlank() {
        var factory = new LabelProviderFactory();

        String[] ids = {ANNOTATED_ID, PROVIDER_ID, UNDEFINED_ID};

        for (String id : ids) {
            var registered = factory.create(id, TOOL_NAME);
            assertThat(registered).as("Tool %s and name '%s'", id, TOOL_NAME).hasId(id);
            assertThat(registered).as("Tool %s and name '%s'", id, TOOL_NAME).hasName(TOOL_NAME);
        }
    }

    /**
     * Static analysis tool that that implements the extension point.
     */
    @SuppressWarnings("unused")
    public static class TestTool extends ReportScanningTool {
        private static final long serialVersionUID = 8456938025794683739L;

        @Override
        public String getId() {
            return ANNOTATED_ID;
        }

        @Override
        public IssueParser createParser() {
            return mock(IssueParser.class);
        }

        /**
         * Required descriptor for the tool.
         */
        @TestExtension
        public static final class TestToolDescriptor extends ReportScanningToolDescriptor {
            /**
             * Creates a new descriptor.
             */
            @SuppressWarnings("checkstyle:RedundantModifier")
            public TestToolDescriptor() {
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
        public Optional<StaticAnalysisLabelProvider> getLabelProvider(final String id) {
            if (PROVIDER_ID.equals(id)) {
                return Optional.of(new StaticAnalysisLabelProvider(id, id));
            }
            return Optional.empty();
        }
    }
}

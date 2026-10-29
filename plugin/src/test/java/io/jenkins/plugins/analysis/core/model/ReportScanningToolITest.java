package io.jenkins.plugins.analysis.core.model;

import static io.jenkins.plugins.analysis.core.testutil.Assertions.*;
import static org.mockito.Mockito.*;

import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.Report;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import hudson.FilePath;
import hudson.model.Run;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.util.LogHandler;
import java.io.File;
import java.io.Serial;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.junit.jupiter.api.Test;

/**
 * Tests the class {@link ReportScanningTool}.
 */
class ReportScanningToolITest extends IntegrationTestWithJenkinsPerSuite {
    /** Tests that the descriptor's canScanConsoleLog() method returns true by default. */
    @Test
    void descriptorMethodCanScanConsoleLogReturnsTrue() {
        final var descriptor = new ReportScanningTool.ReportScanningToolDescriptor("someId");

        assertThat(descriptor.canScanConsoleLog()).isTrue();
    }

    /** Tests that, when the descriptor's canScanConsoleLog() method returns false and there's no default pattern, a null pattern is treated as an error. */
    @Test
    void descriptorMethodDoCheckPatternWhenCalledWithNoPatternReturnsErrorIfCanScanConsoleLogIsFalse() {
        final var descriptor = makeDescriptor(false, "");
        final String givenPattern = null;

        assertThat(descriptor.doCheckPattern(null, givenPattern)).isError();
    }

    /** Tests that, when the descriptor's canScanConsoleLog() method returns false and there's no default pattern, an empty pattern is treated as an error. */
    @Test
    void descriptorMethodDoCheckPatternWhenCalledWithEmptyPatternReturnsErrorIfCanScanConsoleLogIsFalse() {
        final var descriptor = makeDescriptor(false, "");
        final var givenPattern = " ";

        assertThat(descriptor.doCheckPattern(null, givenPattern)).isError();
    }

    /** Tests that, when the descriptor's canScanConsoleLog() method returns false and there's a default pattern, an empty pattern is valid. */
    @Test
    void descriptorMethodDoCheckPatternWhenCalledWithDefaultPatternReturnsOkIfCanScanConsoleLogIsFalse() {
        final var descriptor = makeDescriptor(false, "someDefaultPattern");
        final var givenPattern = " ";

        assertThat(descriptor.doCheckPattern(null, givenPattern)).isOk();
    }

    /** Tests that, when the descriptor's canScanConsoleLog() method returns true, an empty pattern is valid. */
    @Test
    void descriptorMethodDoCheckPatternWhenCalledWithEmptyPatternReturnsOkIfCanScanConsoleLogIsTrue() {
        final var descriptor = makeDescriptor(true, "");
        final var givenPattern = "";

        assertThat(descriptor.doCheckPattern(null, givenPattern)).isOk();
    }

    /** Tests that a negative linesLookAhead value is rejected with an error. */
    @Test
    void descriptorMethodDoCheckLinesLookAheadWhenCalledWithNegativeValueReturnsError() {
        final var descriptor = new ReportScanningTool.ReportScanningToolDescriptor("someId");

        assertThat(descriptor.doCheckLinesLookAhead(-1)).isError();
    }

    /** Tests that linesLookAhead value of zero is valid. */
    @Test
    void descriptorMethodDoCheckLinesLookAheadWhenCalledWithZeroReturnsOk() {
        final var descriptor = new ReportScanningTool.ReportScanningToolDescriptor("someId");

        assertThat(descriptor.doCheckLinesLookAhead(0)).isOk();
    }

    /** Tests that a positive linesLookAhead value is valid. */
    @Test
    void descriptorMethodDoCheckLinesLookAheadWhenCalledWithPositiveValueReturnsOk() {
        final var descriptor = new ReportScanningTool.ReportScanningToolDescriptor("someId");

        assertThat(descriptor.doCheckLinesLookAhead(3)).isOk();
    }

    /** Tests that setting a negative linesLookAhead on a tool clamps it to zero. */
    @Test
    void setLinesLookAheadClampsNegativeValueToZero() {
        var tool = new ReportScanningToolStubForTesting(null);
        tool.setLinesLookAhead(-5);
        assertThat(tool.getLinesLookAhead()).isEqualTo(0);
    }

    /** Tests that setting a zero linesLookAhead is stored as-is. */
    @Test
    void setLinesLookAheadStoresZeroValue() {
        var tool = new ReportScanningToolStubForTesting(null);
        tool.setLinesLookAhead(0);
        assertThat(tool.getLinesLookAhead()).isEqualTo(0);
    }

    /** Tests that setting a positive linesLookAhead is stored as-is. */
    @Test
    void setLinesLookAheadStoresPositiveValue() {
        var tool = new ReportScanningToolStubForTesting(null);
        tool.setLinesLookAhead(5);
        assertThat(tool.getLinesLookAhead()).isEqualTo(5);
    }

    /** Tests that scan without flowNode delegates to scan with null flowNode. */
    @Test
    void shouldDelegateScanWithoutFlowNode() {
        var tool = new DelegatingReportScanningTool();

        var report =
                tool.scan(mock(Run.class), new FilePath(new File(".")), StandardCharsets.UTF_8, mock(LogHandler.class));

        assertThat(report).isNotNull();
        assertThat(tool.hasDelegatedWithNullFlowNode()).isTrue();
    }

    private ReportScanningTool.ReportScanningToolDescriptor makeDescriptor(
            final boolean canScanConsoleLog, final String getPattern) {
        return new ReportScanningToolStubForTesting.ReportScanningToolDescriptorStubForTesting(
                "someId", canScanConsoleLog, getPattern);
    }

    /** Tests that scan with flowNode delegates to scan without flowNode in base Tool class. */
    @Test
    void shouldDelegateScanWithFlowNodeInBaseTool() {
        var tool = new TestTool();
        var report = tool.scan(
                mock(Run.class),
                mock(FlowNode.class),
                new FilePath(new File(".")),
                StandardCharsets.UTF_8,
                mock(LogHandler.class));

        assertThat(report).isNotNull();
    }

    private static class DelegatingReportScanningTool extends ReportScanningToolStubForTesting {
        @Serial
        private static final long serialVersionUID = 1L;

        private boolean delegatedWithNullFlowNode;

        DelegatingReportScanningTool() {
            super(mock(IssueParser.class));
        }

        @Override
        public Report scan(
                final Run<?, ?> run,
                @CheckForNull final FlowNode flowNode,
                final FilePath workspace,
                final Charset sourceCodeEncoding,
                final LogHandler logger) {
            delegatedWithNullFlowNode = flowNode == null;
            return new Report();
        }

        boolean hasDelegatedWithNullFlowNode() {
            return delegatedWithNullFlowNode;
        }
    }

    private static class TestTool extends Tool {
        @Serial
        private static final long serialVersionUID = 1L;

        @Override
        public Report scan(
                final Run<?, ?> run,
                final FilePath workspace,
                final Charset sourceCodeEncoding,
                final LogHandler logger) {
            return new Report();
        }
    }
}

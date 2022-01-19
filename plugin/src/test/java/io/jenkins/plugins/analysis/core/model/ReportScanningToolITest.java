package io.jenkins.plugins.analysis.core.model;

import static io.jenkins.plugins.analysis.core.testutil.Assertions.*;

import org.junit.Test;

import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;

/**
 * Tests the class {@link ReportScanningTool}.
 */
public class ReportScanningToolITest extends IntegrationTestWithJenkinsPerSuite {

    /** Tests that the descriptor's canScanConsoleLog() method returns true by default. */
    @Test
    public void descriptorMethodCanScanConsoleLogReturnsTrue() {
        final ReportScanningTool.ReportScanningToolDescriptor descriptor = new ReportScanningTool.ReportScanningToolDescriptor(
                "someId");

        assertThat(descriptor.canScanConsoleLog()).isTrue();
    }

    /** Tests that, when the descriptor's canScanConsoleLog() method returns false and there's no default pattern, a null pattern is treated as an error. */
    @Test
    public void descriptorMethodDoCheckPatternWhenCalledWithNoPatternReturnsErrorIfCanScanConsoleLogIsFalse() {
        final ReportScanningTool.ReportScanningToolDescriptor descriptor = makeDescriptor(false, "");
        final String givenPattern = null;

        assertThat(descriptor.doCheckPattern(null, givenPattern)).isError();
    }

    /** Tests that, when the descriptor's canScanConsoleLog() method returns false and there's no default pattern, an empty pattern is treated as an error. */
    @Test
    public void descriptorMethodDoCheckPatternWhenCalledWithEmptyPatternReturnsErrorIfCanScanConsoleLogIsFalse() {
        final ReportScanningTool.ReportScanningToolDescriptor descriptor = makeDescriptor(false, "");
        final String givenPattern = " ";

        assertThat(descriptor.doCheckPattern(null, givenPattern)).isError();
    }

    /** Tests that, when the descriptor's canScanConsoleLog() method returns false and there's a default pattern, an empty pattern is valid. */
    @Test
    public void descriptorMethodDoCheckPatternWhenCalledWithDefaultPatternReturnsOkIfCanScanConsoleLogIsFalse() {
        final ReportScanningTool.ReportScanningToolDescriptor descriptor = makeDescriptor(false, "someDefaultPattern");
        final String givenPattern = " ";

        assertThat(descriptor.doCheckPattern(null, givenPattern)).isOk();
    }

    /** Tests that, when the descriptor's canScanConsoleLog() method returns true, an empty pattern is valid. */
    @Test
    public void descriptorMethodDoCheckPatternWhenCalledWithEmptyPatternReturnsOkIfCanScanConsoleLogIsTrue() {
        final ReportScanningTool.ReportScanningToolDescriptor descriptor = makeDescriptor(true, "");
        final String givenPattern = "";

        assertThat(descriptor.doCheckPattern(null, givenPattern)).isOk();
    }

    private ReportScanningTool.ReportScanningToolDescriptor makeDescriptor(final boolean canScanConsoleLog, final String getPattern) {
        return new ReportScanningToolStubForTesting.ReportScanningToolDescriptorStubForTesting("someId",
                canScanConsoleLog, getPattern);
    }
}

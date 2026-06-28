package io.jenkins.plugins.analysis.core.model;

import org.junit.jupiter.api.Test;

import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;

import static io.jenkins.plugins.analysis.core.testutil.Assertions.*;

/**
 * Tests the class {@link ReportScanningTool}.
 */
class ReportScanningToolITest extends IntegrationTestWithJenkinsPerSuite {
    /** Tests that the descriptor's canScanConsoleLog() method returns true by default. */
    @Test
    void descriptorMethodCanScanConsoleLogReturnsTrue() {
        final var descriptor = new ReportScanningTool.ReportScanningToolDescriptor(
                "someId");

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

    private ReportScanningTool.ReportScanningToolDescriptor makeDescriptor(final boolean canScanConsoleLog, final String getPattern) {
        return new ReportScanningToolStubForTesting.ReportScanningToolDescriptorStubForTesting("someId",
                canScanConsoleLog, getPattern);
    }
}

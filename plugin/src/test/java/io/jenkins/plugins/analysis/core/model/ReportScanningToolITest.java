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

    private ReportScanningTool.ReportScanningToolDescriptor makeDescriptor(final boolean canScanConsoleLog, final String getPattern) {
        return new ReportScanningToolStubForTesting.ReportScanningToolDescriptorStubForTesting("someId",
                canScanConsoleLog, getPattern);
    }
}

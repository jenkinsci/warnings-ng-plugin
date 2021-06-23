package io.jenkins.plugins.analysis.core.model;

import static io.jenkins.plugins.analysis.core.testutil.Assertions.*;

import org.junit.Test;

import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;

public class ReportScanningToolITest extends IntegrationTestWithJenkinsPerSuite {

    @Test
    public void descriptorMethodCanScanConsoleLogReturnsTrue() {
        final ReportScanningTool.ReportScanningToolDescriptor descriptor = new ReportScanningTool.ReportScanningToolDescriptor(
                "someId");

        assertThat(descriptor.canScanConsoleLog()).isTrue();
    }

    @Test
    public void descriptorMethodDoCheckPatternWhenCalledWithNoPatternReturnsErrorIfCanScanConsoleLogIsFalse() {
        final ReportScanningTool.ReportScanningToolDescriptor descriptor = makeDescriptor(false);
        final String givenPattern = null;

        assertThat(descriptor.doCheckPattern(null, givenPattern)).isError();
    }

    @Test
    public void descriptorMethodDoCheckPatternWhenCalledWithEmptyPatternReturnsErrorIfCanScanConsoleLogIsFalse() {
        final ReportScanningTool.ReportScanningToolDescriptor descriptor = makeDescriptor(false);
        final String givenPattern = "";

        assertThat(descriptor.doCheckPattern(null, givenPattern)).isError();
    }

    @Test
    public void descriptorMethodDoCheckPatternWhenCalledWithEmptyPatternReturnsOkIfCanScanConsoleLogIsTrue() {
        final ReportScanningTool.ReportScanningToolDescriptor descriptor = makeDescriptor(true);
        final String givenPattern = "";

        assertThat(descriptor.doCheckPattern(null, givenPattern)).isOk();
    }

    private ReportScanningTool.ReportScanningToolDescriptor makeDescriptor(final boolean canScanConsoleLog) {
        final ReportScanningTool.ReportScanningToolDescriptor descriptor = new StubReportScanningTool.StubReportScanningToolDescriptor(
                canScanConsoleLog);
        return descriptor;
    }
}

abstract class StubReportScanningTool extends ReportScanningTool {
    private static final long serialVersionUID = 3729285280522413163L;

    public static class StubReportScanningToolDescriptor extends ReportScanningTool.ReportScanningToolDescriptor {
        private final boolean canScanConsoleLog;

        protected StubReportScanningToolDescriptor(boolean canScanConsoleLog) {
            super("someId");
            this.canScanConsoleLog = canScanConsoleLog;
        }

        @Override
        public boolean canScanConsoleLog() {
            return canScanConsoleLog;
        }
    }
}
package io.jenkins.plugins.analysis.core.model;

import edu.hm.hafner.analysis.IssueParser;

/**
 * Stub class used for testing {@link ReportScanningTool} and its descriptor.
 */
public class ReportScanningToolStubForTesting extends ReportScanningTool {
    private static final long serialVersionUID = 3729285280522413163L;
    private final IssueParser valueForCreateParser;

    ReportScanningToolStubForTesting(IssueParser valueForCreateParser) {
        this.valueForCreateParser = valueForCreateParser;
    }

    @Override
    public IssueParser createParser() {
        return valueForCreateParser;
    }

    public static class ReportScanningToolDescriptorStubForTesting
            extends ReportScanningTool.ReportScanningToolDescriptor {
        private final boolean valueForCanScanConsoleLog;
        private final String valueForGetPattern;

        ReportScanningToolDescriptorStubForTesting(final String id, final boolean valueForCanScanConsoleLog,
                final String valueForGetPattern) {
            super(id);
            this.valueForCanScanConsoleLog = valueForCanScanConsoleLog;
            this.valueForGetPattern = valueForGetPattern;
        }

        @Override
        public boolean canScanConsoleLog() {
            return valueForCanScanConsoleLog;
        }

        @Override
        public String getPattern() {
            return valueForGetPattern;
        }
    }
}
package io.jenkins.plugins.analysis.core.model;

import edu.hm.hafner.analysis.IssueParser;

import java.io.Serial;

/**
 * Stub class used for testing {@link ReportScanningTool}.
 */
public class ReportScanningToolStubForTesting extends ReportScanningTool {
    @Serial
    private static final long serialVersionUID = 3729285280522413163L;
    private final IssueParser valueForCreateParser;

    ReportScanningToolStubForTesting(final IssueParser valueForCreateParser) {
        super();
        this.valueForCreateParser = valueForCreateParser;
    }

    @Override
    public IssueParser createParser() {
        return valueForCreateParser;
    }

    /**
     * Stub class used for testing {@link ReportScanningTool.ReportScanningToolDescriptor}.
     */
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

package io.jenkins.plugins.analysis.core.model;

abstract class ReportScanningToolStubForTesting extends ReportScanningTool {
    private static final long serialVersionUID = 3729285280522413163L;

    public static class ReportScanningToolDescriptorStubForTesting
            extends ReportScanningTool.ReportScanningToolDescriptor {
        private final boolean canScanConsoleLog;
        private final String getPattern;

        ReportScanningToolDescriptorStubForTesting(final String id, final boolean answerForCanScanConsoleLog,
                final String answerForGetPattern) {
            super(id);
            this.canScanConsoleLog = answerForCanScanConsoleLog;
            this.getPattern = answerForGetPattern;
        }

        @Override
        public boolean canScanConsoleLog() {
            return canScanConsoleLog;
        }

        @Override
        public String getPattern() {
            return getPattern;
        }
    }
}
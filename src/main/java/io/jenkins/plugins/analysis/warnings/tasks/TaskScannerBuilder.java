package io.jenkins.plugins.analysis.warnings.tasks;

import io.jenkins.plugins.analysis.warnings.tasks.TaskScanner.CaseMode;
import io.jenkins.plugins.analysis.warnings.tasks.TaskScanner.MatcherMode;

public class TaskScannerBuilder {
    private String high;
    private String normal;
    private String low;
    private CaseMode caseMode;
    private MatcherMode matcherMode;

    public TaskScannerBuilder setHigh(final String high) {
        this.high = high;
        return this;
    }

    public TaskScannerBuilder setNormal(final String normal) {
        this.normal = normal;
        return this;
    }

    public TaskScannerBuilder setLow(final String low) {
        this.low = low;
        return this;
    }

    public TaskScannerBuilder setCaseMode(final CaseMode caseMode) {
        this.caseMode = caseMode;
        return this;
    }

    public TaskScannerBuilder setMatcherMode(final MatcherMode matcherMode) {
        this.matcherMode = matcherMode;
        return this;
    }

    public TaskScanner build() {
        return new TaskScanner(high, normal, low, caseMode, matcherMode);
    }
}
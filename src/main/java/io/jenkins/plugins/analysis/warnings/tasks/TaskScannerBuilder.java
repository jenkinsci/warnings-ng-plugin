package io.jenkins.plugins.analysis.warnings.tasks;

import io.jenkins.plugins.analysis.warnings.tasks.TaskScanner.CaseMode;
import io.jenkins.plugins.analysis.warnings.tasks.TaskScanner.MatcherMode;

/**
 * Builder pattern for a {@link TaskScanner}.
 */
public class TaskScannerBuilder {
    private String high;
    private String normal;
    private String low;
    private CaseMode caseMode;
    private MatcherMode matcherMode;

    /**
     * Sets the tag identifiers indicating tasks with high severity.
     *
     * @param high
     *         the identifiers
     *
     * @return this
     */
    public TaskScannerBuilder setHigh(final String high) {
        this.high = high;
        return this;
    }

    /**
     * Sets the tag identifiers indicating tasks with normal severity.
     *
     * @param normal
     *         the identifiers
     *
     * @return this
     */
    public TaskScannerBuilder setNormal(final String normal) {
        this.normal = normal;
        return this;
    }

    /**
     * Sets the tag identifiers indicating tasks with low severity.
     *
     * @param low
     *         the identifiers
     *
     * @return this
     */
    public TaskScannerBuilder setLow(final String low) {
        this.low = low;
        return this;
    }

    /**
     * Defines whether the tag identifiers are case sensitive.
     *
     * @param caseMode
     *         case sensitive mode property
     *
     * @return this
     */
    public TaskScannerBuilder setCaseMode(final CaseMode caseMode) {
        this.caseMode = caseMode;
        return this;
    }

    /**
     * Defines whether the tag identifiers are strings or regular expressions.
     *
     * @param matcherMode
     *         matcher mode property
     *
     * @return this
     */
    public TaskScannerBuilder setMatcherMode(final MatcherMode matcherMode) {
        this.matcherMode = matcherMode;
        return this;
    }

    /**
     * Creates a new {@link TaskScanner} based on the set properties.
     *
     * @return a new {@link TaskScanner}
     */
    public TaskScanner build() {
        return new TaskScanner(high, normal, low, caseMode, matcherMode);
    }
}
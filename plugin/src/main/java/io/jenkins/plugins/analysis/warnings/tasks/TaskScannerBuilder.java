package io.jenkins.plugins.analysis.warnings.tasks;

import io.jenkins.plugins.analysis.warnings.tasks.TaskScanner.CaseMode;
import io.jenkins.plugins.analysis.warnings.tasks.TaskScanner.MatcherMode;

/**
 * Builder pattern for a {@link TaskScanner}.
 *
 * @author Ullrich Hafner
 */
class TaskScannerBuilder {
    private String highTasks;
    private String normalTasks;
    private String low;
    private CaseMode caseMode;
    private MatcherMode matcherMode;

    /**
     * Sets the tag identifiers indicating tasks with highTasks severity.
     *
     * @param high
     *         the identifiers
     *
     * @return this
     */
    TaskScannerBuilder setHighTasks(final String high) {
        this.highTasks = high;
        return this;
    }

    /**
     * Sets the tag identifiers indicating tasks with normalTasks severity.
     *
     * @param normal
     *         the identifiers
     *
     * @return this
     */
    TaskScannerBuilder setNormalTasks(final String normal) {
        this.normalTasks = normal;
        return this;
    }

    /**
     * Sets the tag identifiers indicating tasks with low severity.
     *
     * @param lowTasks
     *         the identifiers
     *
     * @return this
     */
    TaskScannerBuilder setLowTasks(final String lowTasks) {
        this.low = lowTasks;
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
    TaskScannerBuilder setCaseMode(final CaseMode caseMode) {
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
    TaskScannerBuilder setMatcherMode(final MatcherMode matcherMode) {
        this.matcherMode = matcherMode;
        return this;
    }

    /**
     * Creates a new {@link TaskScanner} based on the set properties.
     *
     * @return a new {@link TaskScanner}
     */
    TaskScanner build() {
        return new TaskScanner(highTasks, normalTasks, low, caseMode, matcherMode);
    }
}
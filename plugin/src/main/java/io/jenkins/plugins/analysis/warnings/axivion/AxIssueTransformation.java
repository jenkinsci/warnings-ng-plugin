package io.jenkins.plugins.analysis.warnings.axivion;

import edu.hm.hafner.analysis.Issue;

import java.io.Serializable;

/**
 * Transformation function which converts Axivion-Dashboard violations to warnings-ng {@link Issue} ones.
 */
@FunctionalInterface
interface AxIssueTransformation extends Serializable {
    /**
     * Transforms raw json-based Axivion-Dashboard violations to {@link Issue}'s.
     *
     * @param raw
     *         payload of a single dashboard violation
     *
     * @return warnings-plugins view of a violation
     */
    Issue transform(AxRawIssue raw);
}

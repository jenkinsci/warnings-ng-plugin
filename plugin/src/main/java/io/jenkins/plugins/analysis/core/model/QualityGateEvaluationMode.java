package io.jenkins.plugins.analysis.core.model;

/**
 * Determines how the evaluation of the quality gates is taken into account when the previous result is searched for.
 */
public enum QualityGateEvaluationMode {
    /**
     * The quality gate result is ignored. The previous build with results of the same type is selected.
     */
    IGNORE_QUALITY_GATE,
    /**
     * The quality gate result must be successful. I.e., the history is searched for a build that either passed the
     * quality gate or has deactivated the quality gate.
     */
    SUCCESSFUL_QUALITY_GATE
}

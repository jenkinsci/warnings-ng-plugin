package io.jenkins.plugins.analysis.core.util;

// Reference Build Selection Strategy
public enum ReferenceBuildSelectionStrategy {
    LAST_SUCCESSFUL_BUILD,
    PARENT_COMMIT_BUILD
}
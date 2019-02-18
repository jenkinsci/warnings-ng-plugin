package io.jenkins.plugins.analysis.core.graphs;

import hudson.util.ChartUtil;

import io.jenkins.plugins.analysis.core.util.AnalysisBuild;

/**
 * Duplication of {@link ChartUtil.NumberOnlyBuildLabel} that rather does use methods on a build (instead of fields).
 */
public class NumberOnlyBuildLabel implements Comparable<NumberOnlyBuildLabel> {
    private final AnalysisBuild build;

    /**
     * Creates a new label.
     *
     * @param build
     *         the build to show the label for
     */
    public NumberOnlyBuildLabel(final AnalysisBuild build) {
        this.build = build;
    }

    public AnalysisBuild getBuild() {
        return build;
    }

    @Override
    public int compareTo(final NumberOnlyBuildLabel o) {
        return build.getNumber() - o.build.getNumber();
    }

    @SuppressWarnings("all")
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        NumberOnlyBuildLabel that = (NumberOnlyBuildLabel) o;

        return build != null ? build.equals(that.build) : that.build == null;
    }

    @SuppressWarnings("all")
    @Override
    public int hashCode() {
        return build != null ? build.hashCode() : 0;
    }

    @Override
    public String toString() {
        return build.getDisplayName();
    }
}
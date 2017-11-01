package io.jenkins.plugins.analysis.core.graphs;

import io.jenkins.plugins.analysis.core.quality.AnalysisBuild;

public class NumberOnlyBuildLabel implements Comparable<NumberOnlyBuildLabel> {
    private final AnalysisBuild build;

    public NumberOnlyBuildLabel(final AnalysisBuild build) {
        this.build = build;
    }

    public AnalysisBuild getBuild() {
        return build;
    }

    @Override
    public int compareTo(NumberOnlyBuildLabel o) {
        return build.getNumber() - o.build.getNumber();
    }

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

    @Override
    public int hashCode() {
        return build != null ? build.hashCode() : 0;
    }

    @Override
    public String toString() {
        return build.getDisplayName();
    }
}
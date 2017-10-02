package io.jenkins.plugins.analysis.core.graphs;

import hudson.model.Run;

public class NumberOnlyBuildLabel implements Comparable<NumberOnlyBuildLabel> {
    private final Run<?, ?> run;

    public NumberOnlyBuildLabel(Run<?, ?> run) {
        this.run = run;
    }

    public Run<?, ?> getRun() {
        return run;
    }

    public int compareTo(NumberOnlyBuildLabel that) {
        return this.run.getNumber() - that.run.getNumber();
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

        return run != null ? run.equals(that.run) : that.run == null;
    }

    @Override
    public int hashCode() {
        return run != null ? run.hashCode() : 0;
    }

    @Override
    public String toString() {
        return run.getDisplayName();
    }
}
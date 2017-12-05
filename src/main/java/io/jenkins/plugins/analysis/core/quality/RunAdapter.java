package io.jenkins.plugins.analysis.core.quality;

import hudson.model.Run;

/**
 * Wraps a Jenkins {@link Run} instance into an {@link AnalysisBuild}.
 *
 * @author Ullrich Hafner
 */
public class RunAdapter implements AnalysisBuild {
    private final Run<?, ?> run;

    /**
     * Creates a new instance of {@link RunAdapter}.
     *
     * @param run
     *         the run to wrap
     */
    public RunAdapter(final Run<?, ?> run) {
        this.run = run;
    }

    @Override
    public long getTimeInMillis() {
        return run.getTimeInMillis();
    }

    @Override
    public int getNumber() {
        return run.getNumber();
    }

    @Override
    public String getDisplayName() {
        return run.getDisplayName();
    }

    @Override
    public int compareTo(final AnalysisBuild o) {
        return getNumber() - o.getNumber();
    }
}

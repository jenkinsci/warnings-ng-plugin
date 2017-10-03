package io.jenkins.plugins.analysis.core.history;

import java.util.Calendar;
import java.util.GregorianCalendar;

import io.jenkins.plugins.analysis.core.steps.BuildResult;

/**
 * Empty build history.
 *
 * @author Ulli Hafner
 */
// FIXME: still required?
public class NullBuildHistory extends BuildHistory {
    /**
     * Creates a new instance of {@link NullBuildHistory}.
     */
    public NullBuildHistory() {
        super(null, null);
    }

    @Override
    public Calendar getTimestamp() {
        return new GregorianCalendar();
    }

    @Override
    public boolean hasPrevious() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public BuildResult getPrevious() {
        return null;
    }
}


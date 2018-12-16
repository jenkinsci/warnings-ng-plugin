package io.jenkins.plugins.analysis.core.scm;

import edu.hm.hafner.analysis.Report;

/**
 * A blamer that does nothing.
 *
 * @author Ullrich Hafner
 */
public class NullBlamer implements Blamer {
    private static final long serialVersionUID = -338286497290046470L;

    @Override
    public Blames blame(final Report report) {
        report.logInfo("Skipping blaming as requested in the job configuration");
        return new Blames();
    }
}

package io.jenkins.plugins.analysis.core.scm;

import edu.hm.hafner.analysis.Report;

/**
 * A blamer that does nothing.
 *
 * @author Ullrich Hafner
 */
public class NullBlamer implements Blamer {
    private static final long serialVersionUID = -338286497290046470L;
    /** A message that explains that no blaming information will be collected. **/
    static final String BLAMING_SKIPPED = "Skipping blaming as requested in the job configuration";

    @Override
    public Blames blame(final Report report) {
        report.logInfo(BLAMING_SKIPPED);
        return new Blames();
    }
}

package io.jenkins.plugins.analysis.core.scm;

import edu.hm.hafner.analysis.Report;

/**
 * A blamer that does nothing.
 *
 * @author Ullrich Hafner
 */
public class NullBlamer implements Blamer {
    @Override
    public Blames blame(final Report report) {
        Blames blames = new Blames();
        blames.logInfo("Skipping blaming as requested in the job configuration");
        return blames;
    }
}

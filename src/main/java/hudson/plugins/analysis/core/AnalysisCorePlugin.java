package hudson.plugins.analysis.core;

import hudson.Plugin;

/**
 * Provides unique keys for jelly scripts which can be accessed globally with
 * <code>${app.getPlugin('analysis-core')}</code>.
 */
public class AnalysisCorePlugin extends Plugin {
    /**
     * A number that can be used to generate unique ids within a request. This
     * will not work for repeatable blocks, but should work for view elements
     * that are lazy loaded.
     */
    private transient int counter;

    /**
     * Returns a new key. This number can be used to generate unique ids within
     * a request.
     *
     * @return a new key
     */
    @SuppressWarnings("PMD.AvoidSynchronizedAtMethodLevel")
    public synchronized int getNewId() {
        if (counter == Integer.MAX_VALUE) {
            counter = 0;
        }
        return counter++;
    }
}

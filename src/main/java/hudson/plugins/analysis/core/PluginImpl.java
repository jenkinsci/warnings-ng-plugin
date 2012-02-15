package hudson.plugins.analysis.core;

import hudson.Plugin;

/**
 * Class that represents the plugin (replacing Plugin$DummyImpl) and can be accessed globally with ${app.getPlugin('analysis-core')}
 */
public class PluginImpl extends Plugin {

    /**
     * A number that can be used to generate unique ids within a request
     *
     * This will not work for repeatable blocks, but should work for view elements that are lazy loaded
     */
    private transient int counter = 0;

    public synchronized int getNewId() {
        if (counter == Integer.MAX_VALUE)
            counter = 0;
        return ++counter;
    }

}

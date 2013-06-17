package hudson.plugins.analysis.util;

/**
 * FIXME: Document type LoggerFactory.
 *
 * @author Ulli Hafner
 */
public final class LoggerFactory {
    private LoggerFactory() {

    }

    public static PluginLogger getLogger() {
        if (logIsQuiet()) {
           return new NullLogger();
        }
        else {
            return new PluginLogger("");
        }
    }

    private static boolean logIsQuiet() {
        //TODO
        return false;
    }
}


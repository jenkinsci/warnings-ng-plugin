package hudson.plugins.analysis.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * A logger that prints to a string buffer. The logged message are available
 * using the {@link #toString()} method.
 *
 * @author Ulli Hafner
 */
public class StringPluginLogger extends PluginLogger {
    private final ByteArrayOutputStream stream = new ByteArrayOutputStream();

    /**
     * Creates a new instance of {@link StringPluginLogger}.
     *
     * @param pluginName
     *            the name of the plug-in
     */
    public StringPluginLogger(final String pluginName) {
        super(pluginName);

        setLogger(new PrintStream(stream));
    }

    @Override
    public String toString() {
        return stream.toString();
    }
}


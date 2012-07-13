package hudson.plugins.analysis.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

/**
 * A logger that prints to a string buffer. The logged message are available
 * using the {@link #toString()} method.
 *
 * @author Ulli Hafner
 */
@edu.umd.cs.findbugs.annotations.SuppressWarnings("DM")
public class StringPluginLogger extends PluginLogger {
    private static final String ENCODING = "UTF-8";
    private final ByteArrayOutputStream stream = new ByteArrayOutputStream();

    /**
     * Creates a new instance of {@link StringPluginLogger}.
     *
     * @param pluginName
     *            the name of the plug-in
     */
    public StringPluginLogger(final String pluginName) {
        super(pluginName);

        try {
            setLogger(new PrintStream(stream, true, ENCODING));
        }
        catch (UnsupportedEncodingException exception) {
            setLogger(new PrintStream(stream));
        }
    }

    @Override
    public String toString() {
        try {
            return stream.toString(ENCODING);
        }
        catch (UnsupportedEncodingException exception) {
            return stream.toString();
        }
    }
}


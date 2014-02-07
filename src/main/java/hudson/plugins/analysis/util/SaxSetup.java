package hudson.plugins.analysis.util;

import org.apache.xerces.parsers.SAXParser;

/**
 * Registers the correct SAX driver if the environment variable is set.
 */
public class SaxSetup {
    /** Property of SAX parser factory. */
    public static final String SAX_DRIVER_PROPERTY = "org.xml.sax.driver";

    private final String oldProperty;

    /**
     * Creates a new instance of {@link SaxSetup}.
     * <p/>
     * Registers a valid SAX driver.
     */
    public SaxSetup() {
        oldProperty = System.getProperty(SAX_DRIVER_PROPERTY);
        if (oldProperty != null) {
            System.setProperty(SAX_DRIVER_PROPERTY, SAXParser.class.getName());
        }
    }

    /**
     * Removes the registered SAX driver.
     */
    public void cleanup() {
        if (oldProperty != null) {
            System.setProperty(SAX_DRIVER_PROPERTY, oldProperty);
        }
        else {
            System.clearProperty(SAX_DRIVER_PROPERTY);
        }
    }
}

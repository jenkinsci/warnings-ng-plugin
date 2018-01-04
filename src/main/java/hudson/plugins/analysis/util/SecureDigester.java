package hudson.plugins.analysis.util;

import java.io.IOException;

import org.apache.commons.digester3.Digester;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * A secure {@link Digester} implementation that does not resolve external entities.
 *
 * @author Ullrich Hafner
 */
public class SecureDigester extends Digester {
    /**
     * Creates a new {@link Digester} instance that does not resolve external entities.
     *
     * @param classWithClassLoader the class to get the class loader from
     */
    public SecureDigester(Class<?> classWithClassLoader) {
        setClassLoader(classWithClassLoader.getClassLoader());
        setValidating(false);
        disableFeature("external-general-entities");
        disableFeature("external-parameter-entities");
        setEntityResolver(new EntityResolver() {
            @Override
            public InputSource resolveEntity(final String publicId, final String systemId) throws SAXException, IOException {
                return new InputSource();
            }
        });
    }

    @SuppressWarnings("all")
    private void disableFeature(final String feature) {
        try {
            setFeature("http://xml.org/sax/features/" + feature, false);
        }
        catch (Exception ignored) {
            // ignore and continue
        }
    }
}

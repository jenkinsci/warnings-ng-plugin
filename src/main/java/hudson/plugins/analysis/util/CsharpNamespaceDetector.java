package hudson.plugins.analysis.util;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;

/**
 * Detects the namespace of a C# workspace file.
 *
 * @author Ulli Hafner
 */
// CHECKSTYLE:CONSTANTS-OFF
public class CsharpNamespaceDetector extends AbstractPackageDetector {
    @Override
    public boolean accepts(final String fileName) {
        return fileName.endsWith(".cs");
    }

    /** {@inheritDoc}*/
    @Override
    public String detectPackageName(final InputStream stream) {
        try {
            LineIterator iterator = IOUtils.lineIterator(stream, "UTF-8");
            while (iterator.hasNext()) {
                String line = iterator.nextLine();
                if (line.matches("^namespace .*$")) {
                    if (line.contains("{")) {
                        return StringUtils.substringBetween(line, " ", "{").trim();
                    }
                    else {
                        return StringUtils.substringAfter(line, " ").trim();
                    }
                }
            }
        }
        catch (IOException exception) {
            // ignore
        }
        finally {
            IOUtils.closeQuietly(stream);
        }
        return UNKNOWN_PACKAGE;
    }
}


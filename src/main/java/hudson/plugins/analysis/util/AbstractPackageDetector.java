package hudson.plugins.analysis.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.apache.commons.io.IOUtils;

/**
 * Base class for package detectors.
 *
 * @author Ulli Hafner
 */
public abstract class AbstractPackageDetector implements PackageDetector {
    /** Identifies an unknown package. */
    protected static final String UNKNOWN_PACKAGE = "-";

    @Override
    public String detectPackageName(final String fileName) {
        FileInputStream input = null;
        try {
            if (accepts(fileName)) {
                input = new FileInputStream(new File(fileName));
                return detectPackageName(input);
            }
        }
        catch (FileNotFoundException exception) {
            // ignore and return empty string
        }
        finally {
            IOUtils.closeQuietly(input);
        }
        return UNKNOWN_PACKAGE;
    }
}

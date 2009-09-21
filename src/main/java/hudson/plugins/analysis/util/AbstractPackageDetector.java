package hudson.plugins.analysis.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Base class for package detectors.
 *
 * @author Ulli Hafner
 */
public abstract class AbstractPackageDetector implements PackageDetector {
    /** Identifies an unknown package. */
    protected static final String UNKNOWN_PACKAGE = "-";

    /** {@inheritDoc} */
    public String detectPackageName(final String fileName) {
        try {
            if (accepts(fileName)) {
                return detectPackageName(new FileInputStream(new File(fileName)));
            }
        }
        catch (FileNotFoundException exception) {
            // ignore and return empty string
        }
        return UNKNOWN_PACKAGE;
    }
}

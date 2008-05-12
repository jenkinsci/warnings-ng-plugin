package hudson.plugins.warnings.util;

import java.io.InputStream;

/**
 * Detects the package or namespace name of a file.
 *
 * @author Ulli Hafner
 */
public interface PackageDetector {
    /**
     * Detects the package or namespace name of the specified input stream. The
     * stream must be closed afterwards.
     *
     * @param stream
     *            the content of the file to scan
     * @return the detected package or namespace name
     */
    String detectPackageName(final InputStream stream);

    /**
     * Detects the package or namespace name of the specified input stream. The
     * stream must be closed afterwards.
     *
     * @param fileName
     *            the file name of the file to scan
     * @return the detected package or namespace name
     */
    String detectPackageName(final String fileName);

    /**
     * Returns whether this classifier accepts the specified file for
     * processing.
     *
     * @param fileName
     *            the file name
     * @return <code>true</code> if the classifier accepts the specified file
     *         for processing.
     */
    boolean accepts(String fileName);
}

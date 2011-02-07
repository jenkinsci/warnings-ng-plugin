package hudson.plugins.analysis.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

/**
 * Provides convenient methods to determine the package or namespace names of a
 * file.
 *
 * @author Ulli Hafner
 */
public final class PackageDetectors {
    /** The detectors to use. */
    private static final List<AbstractPackageDetector> DETECTORS = Arrays.asList(
            new JavaPackageDetector(), new CsharpNamespaceDetector());

    /**
     * Detects the package name of the specified file based on several detector
     * strategies.
     *
     * @param fileName
     *            the filename of the file to scan
     * @param content
     *            the content of the file
     * @return the package name or an empty string
     * @throws IOException
     *             if the file could not be read
     */
    public static String detectPackage(final String fileName, final InputStream content) throws IOException {
        for (PackageDetector detector : DETECTORS) {
            if (detector.accepts(fileName)) {
                return detector.detectPackageName(content);
            }
        }
        return "undefined";
    }

    /**
     * Creates a new instance of {@link PackageDetectors}.
     */
    private PackageDetectors() {
        // prevents instantiation
    }
}


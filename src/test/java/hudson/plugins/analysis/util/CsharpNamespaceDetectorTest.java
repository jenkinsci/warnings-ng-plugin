package hudson.plugins.analysis.util;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

/**
 * Tests the class {@link CsharpNamespaceDetector}.
 */
public class CsharpNamespaceDetectorTest {
    /** The classifier under test. */
    private final CsharpNamespaceDetector classifier = new CsharpNamespaceDetector();

    /**
     * Checks whether we correctly detect the namespace of the specified file.
     *
     * @throws IOException
     *             in case of an error
     */
    @Test
    public void checkClassificationJavaFormatting() throws IOException {
        String fileName = "ActionBinding.cs";
        InputStream stream = CsharpNamespaceDetectorTest.class.getResourceAsStream(fileName);

        try {
            assertEquals("Wrong namespace name guessed.", "Avaloq.SmartClient.Utilities",
                    classifier.detectPackageName(stream));
        }
        finally {
            IOUtils.closeQuietly(stream);
        }
    }

    /**
     * Checks whether we correctly detect the namespace of the specified file.
     *
     * @throws IOException
     *             in case of an error
     */
    @Test
    public void checkClassificationOriginalFormatting() throws IOException {
        String fileName = "ActionBinding-Original-Formatting.cs";
        InputStream stream = CsharpNamespaceDetectorTest.class.getResourceAsStream(fileName);

        try {
            assertEquals("Wrong namespace name guessed.", "Avaloq.SmartClient.Utilities",
                    classifier.detectPackageName(stream));
        }
        finally {
            IOUtils.closeQuietly(stream);
        }
    }

    /**
     * Checks whether we do not detect a namespace in a text file.
     *
     * @throws IOException
     *             in case of an error
     */
    @Test
    public void checkEmptyPackageName() throws IOException {
        String fileName = "pom.xml";
        InputStream stream = CsharpNamespaceDetectorTest.class.getResourceAsStream(fileName);

        try {
            assertEquals("Wrong namespace name guessed.", "-", classifier.detectPackageName(stream));
        }
        finally {
            IOUtils.closeQuietly(stream);
        }
    }

    /**
     * Checks whether we correctly accept C# files.
     */
    @Test
    public void testFileSuffix() {
        assertTrue("Does not accept a C# file.", classifier.accepts("ActionBinding.cs"));
        assertFalse("Accepts a non-C# file.", classifier.accepts("ActionBinding.cs.c"));
    }
}


package hudson.plugins.warnings.parser;

import hudson.plugins.warnings.util.ParserResult;
import hudson.plugins.warnings.util.model.FileAnnotation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import junit.framework.Assert;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

/**
 * Tests the class {@link ParserRegistry}.
 */
public class ParserRegistryTest {
    /** Total number of expected warnings. */
    private static final int TOTAL_WARNINGS = 163;
    /** Error message. */
    private static final String WRONG_NUMBER_OF_ANNOTATIONS_PARSED = "Wrong number of annotations parsed";

    /**
     * Checks whether we correctly find all warnings in the log file.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testAllParsersOnOneFile() throws IOException {
        ParserRegistry parserRegistry = createRegistryUnderTest("all.txt", StringUtils.EMPTY);

        Collection<FileAnnotation> annotations = parserRegistry.parse(new File(""));
        Assert.assertEquals(WRONG_NUMBER_OF_ANNOTATIONS_PARSED, TOTAL_WARNINGS, annotations.size());

        ParserResult result = new ParserResult();
        result.addAnnotations(annotations);
        Assert.assertEquals(WRONG_NUMBER_OF_ANNOTATIONS_PARSED, TOTAL_WARNINGS, result.getNumberOfAnnotations());

        result.addAnnotations(annotations);
        Assert.assertEquals(WRONG_NUMBER_OF_ANNOTATIONS_PARSED, TOTAL_WARNINGS, result.getNumberOfAnnotations());
    }

    /**
     * Checks whether we correctly find all warnings in the log file. The file
     * contains 18 additional ANT warnings, 8 of them should be excluded by the single
     * pattern.
     *
     * @throws IOException
     *             if the file could not be read
     * @see <a href="https://hudson.dev.java.net/issues/show_bug.cgi?id=2359">Issue 2359</a>
     */
    @Test
    public void issue2359() throws IOException {
        ParserRegistry parserRegistry = createRegistryUnderTest("all.txt", "/tmp/clover*/**");

        Collection<FileAnnotation> annotations = parserRegistry.parse(new File(""));
        Assert.assertEquals(WRONG_NUMBER_OF_ANNOTATIONS_PARSED, TOTAL_WARNINGS - 8, annotations.size());
    }

    /**
     * The file contains 18 additional ANT warnings, 15 of them should be excluded by the
     * two patterns.
     *
     * @throws IOException
     *             if the file could not be read
     * @see <a href="https://hudson.dev.java.net/issues/show_bug.cgi?id=2359">Issue 2359</a>
     */
    @Test
    public void multiplePatternsIssue2359() throws IOException {
        ParserRegistry parserRegistry = createRegistryUnderTest("all.txt", "/tmp/clover*/**, **/renderers/*");

        Collection<FileAnnotation> annotations = parserRegistry.parse(new File(""));
        Assert.assertEquals(WRONG_NUMBER_OF_ANNOTATIONS_PARSED, TOTAL_WARNINGS - 18 + 3, annotations.size());
    }


    /**
     * Creates the registry under test.
     *
     * @param fileName
     *            file name with the warnings
     * @param excludePattern
     *            Ant file-set pattern of files to exclude from report,
     *            <code>null</code> or an empty string do not filter the output
     * @return the registry
     */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("SIC")
    private ParserRegistry createRegistryUnderTest(final String fileName, final String excludePattern) {
        String empty = excludePattern;
        ParserRegistry parserRegistry = new ParserRegistry(empty) {
            /** {@inheritDoc} */
            @Override
            protected InputStream createInputStream(final File file) throws FileNotFoundException {
                return ParserRegistryTest.class.getResourceAsStream(fileName);
            }
        };
        return parserRegistry;
    }
}


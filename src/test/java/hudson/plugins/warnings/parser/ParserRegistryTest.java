package hudson.plugins.warnings.parser;

import hudson.plugins.warnings.util.model.FileAnnotation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Tests the class {@link ParserRegistry}.
 */
public class ParserRegistryTest {
    /**
     * Checks whether we correctly find all warnings in the log file.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("SIC")
    public void testAllParsersOnOneFile() throws IOException {
        ParserRegistry parserRegistry = new ParserRegistry() {
            /** {@inheritDoc} */
            @Override
            protected InputStream createInputStream(final File file) throws FileNotFoundException {
                return ParserRegistryTest.class.getResourceAsStream("all.txt");
            }
        };

        Collection<FileAnnotation> annotations = parserRegistry.parse(new File(""));

        Assert.assertEquals("Wrong number of annotations parsed", 122, annotations.size());
    }
}


package hudson.plugins.warnings.parser;

import static junit.framework.Assert.*;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Test;

/**
 * Tests the class {@link P4Parser}.
 */
public class P4ParserTest extends ParserTester {
    /**
     * Parses a file with four Perforce warnings.
     *
     * @throws IOException
     *      if the file could not be read
     */
    @Test
    public void testWarningsParser() throws IOException {
        Collection<FileAnnotation> warnings = new P4Parser().parse(openFile());

        assertEquals("Wrong number of warnings detected.", 4, warnings.size());

        Iterator<FileAnnotation> iterator = warnings.iterator();
        FileAnnotation annotation = iterator.next();
        checkWarning(annotation,
                0,
                "//depot/file1.txt",
                "//depot/file1.txt",
                P4Parser.WARNING_TYPE, "can't add existing file", Priority.NORMAL);
        annotation = iterator.next();
        checkWarning(annotation,
                0,
                "//depot/file2.txt",
                "//depot/file2.txt",
                P4Parser.WARNING_TYPE, "warning: add of existing file", Priority.NORMAL);
        annotation = iterator.next();
        checkWarning(annotation,
                0,
                "//depot/file3.txt",
                "//depot/file3.txt",
                P4Parser.WARNING_TYPE, "can't add (already opened for edit)", Priority.LOW);
        annotation = iterator.next();
        checkWarning(annotation,
                0,
                "//depot/file4.txt",
                "//depot/file4.txt",
                P4Parser.WARNING_TYPE, "nothing changed", Priority.LOW);        
    }

    /** {@inheritDoc} */
    @Override
    protected String getWarningsFile() {
        return "perforce.txt";
    }
}


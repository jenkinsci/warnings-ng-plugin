package hudson.plugins.warnings.parser;

import static junit.framework.Assert.*;
import hudson.plugins.warnings.util.model.FileAnnotation;
import hudson.plugins.warnings.util.model.Priority;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Test;

/**
 * Tests the class {@link IntelCParserTest}.
 */
public class IntelCParserTest extends ParserTester {
    /**
     * Creates a new instance of {@link IntelCParserTest}.
     */
    public IntelCParserTest() {
        super(IntelCParser.class);
    }

    /**
     * Parses a file with two Intel warnings.
     *
     * @throws IOException
     *      if the file could not be read
     */
    @Test
    public void testWarningsParser() throws IOException {
        Collection<FileAnnotation> warnings = new IntelCParser().parse(openFile());

        assertEquals("Wrong number of warnings detected.", 4, warnings.size());

        Iterator<FileAnnotation> iterator = warnings.iterator();
        FileAnnotation annotation = iterator.next();
        checkWarning(annotation,
                1460,
                "LOOP WAS VECTORIZED.",
                "D:/Hudson/workspace/foo/busdates.cpp",
                IntelCParser.WARNING_TYPE, "Intel remark", Priority.NORMAL);
        annotation = iterator.next();
        // remark
        checkWarning(annotation,
                2630,
                "FUSED LOOP WAS VECTORIZED.",
                "D:/Hudson/workspace/foo/hols.cpp",
                IntelCParser.WARNING_TYPE, "Intel remark", Priority.NORMAL);
        annotation = iterator.next();
        checkWarning(annotation,
                721,
                "last line of file ends without a newline",
                "D:/Hudson/workspace/zoo/oppdend2d_slv_strip_utils.cpp",
                IntelCParser.WARNING_TYPE, "Intel remark", Priority.NORMAL);
        annotation = iterator.next();
        checkWarning(annotation,
                17,
                "external function definition with no prior declaration",
                "D:/Hudson/workspace/boo/serviceif.cpp",
                IntelCParser.WARNING_TYPE, "Intel remark", Priority.NORMAL);
    }

    /** {@inheritDoc} */
    @Override
    protected String getWarningsFile() {
        return "intelc.txt";
    }
}


package hudson.plugins.warnings.parser;

import static junit.framework.Assert.*;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Test;

/**
 * Tests the class {@link DiabCParser}.
 */
public class DiabCParserTest extends ParserTester {
    private static final String TYPE = new DiabCParser().getGroup();
    /**
     * Parses a file with 5 warnings.
     *
     * @throws IOException
     *      if the file could not be read
     */
    @Test
    public void parseDiabCpp() throws IOException {
        Collection<FileAnnotation> warnings = new DiabCParser().parse(openFile());

        assertEquals("Wrong number of warnings detected.", 5, warnings.size());

        Iterator<FileAnnotation> iterator = warnings.iterator();
        FileAnnotation annotation = iterator.next();
        checkWarning(annotation,
                7,
                "missing return expression",
                "lint.c",
                TYPE, "1521", Priority.NORMAL);
        annotation = iterator.next();
        checkWarning(annotation,
                22,
                "narrowing or signed-to-unsigned type conversion found: int to unsigned char",
                "lint.c",
                TYPE, "1643", Priority.NORMAL);
        annotation = iterator.next();
        checkWarning(annotation,
                28,
                "constant out of range",
                "lint.c",
                TYPE, "1243", Priority.NORMAL);
        annotation = iterator.next();
        checkWarning(annotation,
                4,
                "function f4 is never used",
                "lint.c",
                TYPE, "1517", Priority.NORMAL);
        annotation = iterator.next();
        checkWarning(annotation,
                11,
                "function f5 is not found",
                "lint.c",
                TYPE, "1378", Priority.HIGH);
    }

    @Override
    protected String getWarningsFile() {
        return "diabc.txt";
    }
}


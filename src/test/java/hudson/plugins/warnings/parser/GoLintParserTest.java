package hudson.plugins.warnings.parser;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Test;

import static org.junit.Assert.*;

import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;

/**
 * Tests the class {@link GoLintParser}.
 */
public class GoLintParserTest extends ParserTester {
    private static final String CATEGORY = DEFAULT_CATEGORY;

    /**
     * Parses a file with multiple golint warnings
     *
     * @throws IOException
     *             if the file could not be read
     */
    @Test
    public void testWarningsParser() throws IOException {
        Collection<FileAnnotation> warnings = new GoLintParser().parse(openFile());

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 7, warnings.size());

        Iterator<FileAnnotation> iterator = warnings.iterator();
        FileAnnotation annotation = iterator.next();
        checkWarning(annotation, 64, "exported var ErrCloseSent should have comment or be unexported", "conn.go", CATEGORY,
                Priority.NORMAL);
        annotation = iterator.next();

        checkWarning(annotation, 104, "should replace pos += 1 with pos++", "conn.go", CATEGORY, Priority.NORMAL);
        annotation = iterator.next();

        checkWarning(annotation, 305, "should replace c.writeSeq += 1 with c.writeSeq++", "conn.go", CATEGORY,
                Priority.NORMAL);
        annotation = iterator.next();

        checkWarning(annotation, 360, "should replace c.writeSeq += 1 with c.writeSeq++", "conn.go", CATEGORY,
                Priority.NORMAL);
        annotation = iterator.next();

        checkWarning(annotation, 669, "should replace c.readSeq += 1 with c.readSeq++", "conn.go", CATEGORY, Priority.NORMAL);
        annotation = iterator.next();

        checkWarning(annotation, 706, "should replace r.c.readSeq += 1 with r.c.readSeq++", "conn.go", CATEGORY,
                Priority.NORMAL);
        annotation = iterator.next();

        checkWarning(
                annotation,
                18,
                "should omit type net.Error from declaration of var timeoutErrImplementsNetError; it will be inferred from the right-hand side",
                "conn_test.go", CATEGORY, Priority.NORMAL);

    }


    @Override
    protected String getWarningsFile() {
        return "golint.txt";
    }
}

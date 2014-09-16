package hudson.plugins.warnings.parser;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Test;

import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;

/**
 * Tests the class {@link GoLintParser}.
 */
public class GoVetParserTest extends ParserTester {

    /**
     * Parses a file with multiple go vet warnings
     *
     * @throws IOException
     *             if the file could not be read
     */
    @Test
    public void testWarningsParser() throws IOException {
        Collection<FileAnnotation> warnings = new GoVetParser().parse(openFile());


        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 2, warnings.size());

        Iterator<FileAnnotation> iterator = warnings.iterator();
        FileAnnotation annotation = iterator.next();


        checkWarning(annotation, 46, "missing argument for Fatalf(\"%#v\"): format reads arg 2, have only 1 args", "ui_colored_test.go", "",
                Priority.NORMAL);
        annotation = iterator.next();

        checkWarning(annotation, 59, "missing argument for Fatalf(\"%#v\"): format reads arg 2, have only 1 args", "ui_colored_test.go", "",
                Priority.NORMAL);

    }

    @Override
    protected String getWarningsFile() {
        return "govet.txt";
    }
}

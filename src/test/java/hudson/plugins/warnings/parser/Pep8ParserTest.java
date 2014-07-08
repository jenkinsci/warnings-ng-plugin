package hudson.plugins.warnings.parser;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Test;

import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;

/**
 * Tests the class {@link Pep8Parser}.
 *
 * @author Marvin Schütz
 */
public class Pep8ParserTest extends ParserTester {
    private static final String WARNING_TYPE = "Pep8";

    /**
     * Parses a file with a simple and a complex warning and 2 complex warnings with lower Priority.
     *
     * @throws IOException
     *             if the file could not be read
     */
    @Test
    public void testParseSimpleAndComplexMessage() throws IOException {
        Pep8Parser parser = new Pep8Parser();

        Collection<FileAnnotation> warnings = parser.parse(openFile());
        Iterator<FileAnnotation> iterator = warnings.iterator();
        FileAnnotation warning = iterator.next();

        checkWarning(warning, 1, 2, "trailing whitespace", "trunk/src/python/file.py",
                    WARNING_TYPE, "W291", Priority.HIGH);

        warning = iterator.next();

        checkWarning(warning, 98, 11, "Message #has! 12special-_ chars|?.",
                    "trunk/src/python/file.py", WARNING_TYPE, "E111", Priority.HIGH);

        warning = iterator.next();

        checkWarning(warning, 98, 11, "Message #has! 12special-_ chars|?.",
                "trunk2/src/python/file.py", WARNING_TYPE, "R111", Priority.NORMAL);

        warning = iterator.next();

        checkWarning(warning, 98, 11, "Message #has! 12special-_ chars|?.",
                "trunk3/src/python/file.py", WARNING_TYPE, "C111", Priority.LOW);
    }

    @Override
    protected String getWarningsFile() {
        return "pep8Test.txt";
    }
}

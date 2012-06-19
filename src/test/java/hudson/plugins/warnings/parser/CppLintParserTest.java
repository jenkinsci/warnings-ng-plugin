package hudson.plugins.warnings.parser;

import static junit.framework.Assert.*;
import hudson.plugins.analysis.core.ParserResult;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Test;

/**
 * Tests the class {@link CppLintParser}.
 *
 * @author Ulli Hafner
 */
public class CppLintParserTest extends ParserTester {
    /** {@inheritDoc} */
    @Override
    protected String getWarningsFile() {
        return "cpplint.txt";
    }

    /**
     * Parses a file with 1031 warnings.
     *
     * @throws IOException
     *      if the file could not be read
     */
    @Test
    public void testParser() throws IOException {
        Collection<FileAnnotation> warnings = new CppLintParser().parse(openFile());

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 1031, warnings.size());

        ParserResult result = new ParserResult(warnings);
        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 81, result.getNumberOfAnnotations(Priority.HIGH));
        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 201, result.getNumberOfAnnotations(Priority.NORMAL));
        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 749, result.getNumberOfAnnotations(Priority.LOW));

        Iterator<FileAnnotation> iterator = warnings.iterator();
        FileAnnotation annotation = iterator.next();
        checkWarning(annotation,
                824,
                "Tab found; better to use spaces",
                "c:/Workspace/Trunk/Project/P1/class.cpp",
                "whitespace/tab", Priority.LOW);
    }
}


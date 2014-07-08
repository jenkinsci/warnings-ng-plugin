package hudson.plugins.warnings.parser;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Test;

import hudson.plugins.analysis.core.ParserResult;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;

/**
 * Tests the class {@link CppLintParser}.
 *
 * @author Ulli Hafner
 */
public class CppLintParserTest extends ParserTester {
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

    /**
     * Parses a file with CPP Lint warnings in the new format.
     *
     * @throws IOException
     *      if the file could not be read
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-18290">Issue 18290</a>
     */
    @Test
    public void issue18290() throws IOException {
        Collection<FileAnnotation> warnings = new CppLintParser().parse(openFile("issue18290.txt"));

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 2, warnings.size());
        Iterator<FileAnnotation> iterator = warnings.iterator();
        FileAnnotation annotation = iterator.next();
        checkWarning(annotation, 399, "Missing space before {",
                "/opt/ros/fuerte/stacks/Mule/Mapping/Local_map/src/LocalCostMap.cpp",
                "whitespace/braces", Priority.HIGH);
        annotation = iterator.next();
        checkWarning(annotation, 400, "Tab found; better to use spaces",
                "/opt/ros/fuerte/stacks/Mule/Mapping/Local_map/src/LocalCostMap.cpp",
                "whitespace/tab", Priority.LOW);
    }
}


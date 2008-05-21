package hudson.plugins.warnings.parser;

import static junit.framework.Assert.*;
import hudson.plugins.warnings.util.model.FileAnnotation;
import hudson.plugins.warnings.util.model.Priority;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Test;

/**
 * Tests the class {@link JavacParser}.
 */
public class AntJavacParserTest extends ParserTester {
    /**
     * Parses a file with two deprecation warnings.
     *
     * @throws IOException
     *      if the file could not be read
     */
    @Test
    public void parseDeprecation() throws IOException {
        Collection<FileAnnotation> warnings = new AntJavacParser().parse(AntJavacParserTest.class.getResourceAsStream("ant-javac.txt"));

        assertEquals("Wrong number of warnings detected.", 1, warnings.size());

        Iterator<FileAnnotation> iterator = warnings.iterator();
        FileAnnotation annotation = iterator.next();
        checkWarning(annotation,
                28,
                "begrussen() in ths.types.IGruss has been deprecated",
                "C:/Users/tiliven/.hudson/jobs/Hello THS Trunk - compile/workspace/HelloTHSTest/src/ths/Hallo.java",
                AntJavacParser.WARNING_TYPE, "Deprecation", Priority.NORMAL);
    }
}


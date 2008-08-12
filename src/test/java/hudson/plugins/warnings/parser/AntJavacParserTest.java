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

    /**
     * Parses a warning log with 2 ANT warnings.
     *
     * @throws IOException
     *      if the file could not be read
     * @see <a href="https://hudson.dev.java.net/issues/show_bug.cgi?id=2133">Issue 2133</a>
     */
    @Test
    public void issue2133() throws IOException {
        Collection<FileAnnotation> warnings = new AntJavacParser().parse(AntJavacParserTest.class.getResourceAsStream("issue2133.txt"));

        assertEquals("Wrong number of warnings detected.", 2, warnings.size());

        Iterator<FileAnnotation> iterator = warnings.iterator();
        checkWarning(iterator.next(),
                86,
                "non-varargs call of varargs method with inexact argument type for last parameter;",
                "/home/hudson/hudson/data/jobs/Mockito/workspace/trunk/test/org/mockitousage/misuse/DescriptiveMessagesOnMisuseTest.java",
                AntJavacParser.WARNING_TYPE, "", Priority.NORMAL);
        checkWarning(iterator.next(),
                51,
                "<T>stubVoid(T) in org.mockito.Mockito has been deprecated",
                "/home/hudson/hudson/data/jobs/Mockito/workspace/trunk/test/org/mockitousage/stubbing/StubbingWithThrowablesTest.java",
                AntJavacParser.WARNING_TYPE, RegexpParser.DEPRECATION, Priority.NORMAL);
    }
}


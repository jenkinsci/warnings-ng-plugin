package hudson.plugins.warnings.parser;

import static junit.framework.Assert.*;
import hudson.plugins.warnings.util.model.FileAnnotation;
import hudson.plugins.warnings.util.model.Priority;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Test;

/**
 * Tests the class {@link GccParser}.
 */
public class GccParserTest extends ParserTester {
    /**
     * Parses a file with two GCC warnings.
     *
     * @throws IOException
     *      if the file could not be read
     */
    @Test
    public void testWarningsParser() throws IOException {
        Collection<FileAnnotation> warnings = new GccParser().parse(GccParserTest.class.getResourceAsStream("gcc.txt"));

        assertEquals("Wrong number of warnings detected.", 4, warnings.size());

        Iterator<FileAnnotation> iterator = warnings.iterator();
        FileAnnotation annotation = iterator.next();
        checkWarning(annotation,
                451,
                "`void yyunput(int, char*)' defined but not used",
                "testhist.l",
                GccParser.WARNING_TYPE, "GCC warning", Priority.NORMAL);
        annotation = iterator.next();
        checkWarning(annotation,
                73,
                "implicit typename is deprecated, please see the documentation for details",
                "/u1/drjohn/bfdist/packages/RegrTest/V00-03-01/RgtAddressLineScan.cc",
                GccParser.WARNING_TYPE, "GCC error", Priority.HIGH);
        annotation = iterator.next();
        checkWarning(annotation,
                4,
                "foo.h: No such file or directory",
                "foo.cc",
                GccParser.WARNING_TYPE, "GCC error", Priority.HIGH);
        annotation = iterator.next();
        checkWarning(annotation,
                0,
                "'missing_symbol'",
                "foo.so",
                GccParser.WARNING_TYPE, "GCC error", Priority.HIGH);
    }
}


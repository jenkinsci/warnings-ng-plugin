package hudson.plugins.warnings.parser;

import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;
import org.junit.Test;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import static junit.framework.Assert.assertEquals;

/**
 * Tests the class {@link ArmccCompilerParser}.
 */
public class ArmccCompilerParserTest extends ParserTester {

    /**
     * Error message.
     */
    private static final String WRONG_NUMBER_OF_WARNINGS_DETECTED = "Wrong number of warnings detected.";
    /**
     * The category.
     */
    private static final String WARNING_CATEGORY = ArmccCompilerParser.WARNING_CATEGORY;
    /**
     * The type. *
     */
    private static final String WARNING_TYPE = ArmccCompilerParser.WARNING_TYPE;

    @Test
    public void testWarningsParser() throws IOException {
        Collection<FileAnnotation> warnings = new ArmccCompilerParser().parse(openFile());

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 3, warnings.size());

        Iterator<FileAnnotation> iterator = warnings.iterator();
        checkWarning(iterator.next(),
                21,
                "5 - cannot open source input file \"somefile.h\": No such file or directory",
                "/home/test/main.cpp",
                WARNING_TYPE, WARNING_CATEGORY, Priority.HIGH);
        checkWarning(iterator.next(),
                23,
                "5 - cannot open source input file \"somefile.h\": No such file or directory",
                "C:/home/test/main.cpp",
                WARNING_TYPE, WARNING_CATEGORY, Priority.HIGH);
        checkWarning(iterator.next(),
                25,
                "550 - something bad happened here",
                "/home/test/main.cpp",
                WARNING_TYPE, WARNING_CATEGORY, Priority.NORMAL);
    }

    @Override
    protected String getWarningsFile() {
        return "armcc.txt";
    }
}

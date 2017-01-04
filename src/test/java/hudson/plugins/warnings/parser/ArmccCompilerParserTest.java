package hudson.plugins.warnings.parser;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Test;

import static org.junit.Assert.*;

import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;

/**
 * Tests the class {@link ArmccCompilerParser}.
 */
public class ArmccCompilerParserTest extends ParserTester {
    private static final String WARNING_CATEGORY = DEFAULT_CATEGORY;
    private static final String WARNING_TYPE = new ArmccCompilerParser().getGroup();

    /**
     * Detects three ARMCC warnings.
     *
     * @throws IOException
     *             if file could not be read
     */
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

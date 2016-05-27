package hudson.plugins.warnings.parser;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Test;

import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;

/**
 * Tests the class {@link TaskingVXCompilerParser}.
 */
public class TaskingVXCompilerParserTest extends ParserTester {

    private static final String INFO_CATEGORY = "Info";
    private static final String WARNING_CATEGORY = "Warning";
    private static final String ERROR_CATEGORY = "ERROR";
    private static final String LICERROR_CATEGORY = "License issue";

    private static final String WARNING_TYPE = new TaskingVXCompilerParser().getGroup();

    /**
     * Parses a file with TASKING VX compiler warnings.
     *
     * @throws IOException
     *      if the file could not be read
     */
    @Test
    public void testWarningsParser() throws IOException {
        Collection<FileAnnotation> warnings = new TaskingVXCompilerParser().parse(openFile());

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 8, warnings.size());

        Iterator<FileAnnotation> iterator = warnings.iterator();
        FileAnnotation annotation = iterator.next();
        checkWarning(annotation,
                8796,
                "condition is always true",
                "C:/Projects/a/b/c/d/e/f/g/h/i/src/StbM.c",
                WARNING_TYPE, WARNING_CATEGORY, Priority.NORMAL);
        annotation = iterator.next();
        checkWarning(annotation,
                50,
                "previous definition of macro \"TS_RELOCATABLE_CFG_ENABLE\"",
                "C:/Projects/a/b/c/d/e/f/g/TcpIp_Int.h",
                WARNING_TYPE, INFO_CATEGORY, Priority.LOW);
        annotation = iterator.next();
        checkWarning(annotation,
                36,
                "macro \"TS_RELOCATABLE_CFG_ENABLE\" redefined",
                "C:/Projects/a/b/c/d/e/f/g/h/include/ComM_Types_Int.h",
                WARNING_TYPE, WARNING_CATEGORY, Priority.NORMAL);
        annotation = iterator.next();
        checkWarning(annotation,
                2221,
                "conversion of integer to pointer at argument #3",
                "C:/Projects/a/b/c/d/e/f/g/h/src/SoAd.c",
                WARNING_TYPE, WARNING_CATEGORY, Priority.NORMAL);
        annotation = iterator.next();
        checkWarning(annotation,
                860,
                "unused static function \"TcpIp_Tcp_checkRemoteAddr\"",
                "C:/Projects/a/b/c/d/e/f/g/TcpIp_Tcp.c",
                WARNING_TYPE, WARNING_CATEGORY, Priority.NORMAL);        
        annotation = iterator.next();
        checkWarning(annotation,
                380,
                "syntax error - token \";\" inserted before \"{\"",
                "C:/Projects/a/b/c/DmaLib.h",
                WARNING_TYPE, ERROR_CATEGORY, Priority.HIGH);
        annotation = iterator.next();
        checkWarning(annotation,
                42,
                "start of current function definition",
                "BswM_UserCallouts.c",
                WARNING_TYPE, INFO_CATEGORY, Priority.LOW);
/*
        annotation = iterator.next();
        checkWarning(annotation,
                0,
                "protection error: No license key found for product code SW160800",
                "",
                WARNING_TYPE, LICERROR_CATEGORY, Priority.HIGH);
*/
    }

    /** {@inheritDoc} */
    @Override
    protected String getWarningsFile() {
        return "tasking-vx.txt";
    }
}


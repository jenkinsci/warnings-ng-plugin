package hudson.plugins.warnings.parser;

import static junit.framework.Assert.*;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Test;

/**
 * Tests the class {@link TiCcsParser}.
 */
public class TiCcsParserTest extends ParserTester {
    /** Error message. */
    private static final String WRONG_NUMBER_OF_WARNINGS_DETECTED = "Wrong number of warnings detected.";

    /**
     * Parses a file with warnings of the TI CodeComposer tools.
     *
     * @throws IOException
     *      if the file could not be read
     */
    @Test
    public void parseWarnings() throws IOException {
        Collection<FileAnnotation> sortedWarnings = new TiCcsParser().parse(openFile());

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 10, sortedWarnings.size());

        Iterator<FileAnnotation> iterator = sortedWarnings.iterator();
        FileAnnotation annotation = iterator.next();
        checkWarning(annotation,
                341,
                "parameter \"params\" was never referenced",
                "C:/SCM/Lr/src/fxns.c",
                TiCcsParser.WARNING_TYPE, "#880-D", Priority.LOW);
        annotation = iterator.next();
        checkWarning(annotation,
                177,
                "may want to suffix float constant with an f",
                "C:/SCM/Lr/src/edge.c",
                TiCcsParser.WARNING_TYPE, "#1116-D", Priority.LOW);
        annotation = iterator.next();
        checkWarning(annotation,
                0,
                "symbol 'memset' redeclared with incompatible type",
                "unknown.file",
                TiCcsParser.WARNING_TYPE, "", Priority.NORMAL);
        annotation = iterator.next();
        checkWarning(annotation,
                12,
                "variable \"h\" was declared but never referenced",
                "i2cDisplay12x2.c",
                TiCcsParser.WARNING_TYPE, "", Priority.NORMAL);
        annotation = iterator.next();
        checkWarning(annotation,
                2578,
                "variable",
                "c:/DOCUME~1/JLINNE~1/LOCALS~1/Temp/0360811",
                TiCcsParser.WARNING_TYPE, "", Priority.NORMAL);
        annotation = iterator.next();
        checkWarning(annotation,
                11,
                "expected a \";\"",
                "i2cDisplay12x2.c",
                TiCcsParser.WARNING_TYPE, "", Priority.HIGH);
        annotation = iterator.next();
        checkWarning(annotation,
                0,
                "unresolved symbols remain",
                "unknown.file",
                TiCcsParser.WARNING_TYPE, "", Priority.HIGH);
        annotation = iterator.next();
        checkWarning(annotation,
                0,
                "errors encountered during linking; \"../../bin/Debug/lrxyz.out\" not",
                "unknown.file",
                TiCcsParser.WARNING_TYPE, "", Priority.HIGH);
        annotation = iterator.next();
        checkWarning(annotation,
                3,
                "could not open source file \"i2cDisplay12x12.h\"",
                "i2cDisplay12x2.c",
                TiCcsParser.WARNING_TYPE, "", Priority.HIGH);
        annotation = iterator.next();
        checkWarning(annotation,
                5,
                "[E0002] Illegal mnemonic specified",
                "foo.asm",
                TiCcsParser.WARNING_TYPE, "", Priority.HIGH);
    }


    /** {@inheritDoc} */
    @Override
    protected String getWarningsFile() {
        return "ticcs.txt";
    }
}


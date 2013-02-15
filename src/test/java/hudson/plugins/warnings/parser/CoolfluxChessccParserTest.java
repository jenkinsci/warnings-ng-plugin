package hudson.plugins.warnings.parser;

import static org.junit.Assert.*;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Test;

/**
 * Tests the class {@link CoolfluxChessccParserTest}.
 */
public class CoolfluxChessccParserTest extends ParserTester {
    /**
     * Parses a file with two warnings.
     *
     * @throws IOException
     *      if the file could not be read
     */
    @Test
    public void testWarningsParser() throws IOException {
        Collection<FileAnnotation> warnings = new CoolfluxChessccParser().parse(openFile());

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 2, warnings.size());

        Iterator<FileAnnotation> iterator = warnings.iterator();
        FileAnnotation annotation = iterator.next();
        checkWarning(annotation,
                150,
                "function `unsigned configureRealCh(unsigned)' was declared static, but was not defined",
                "/nfs/autofs/nett/nessie6/dailies/Monday/src/n6/heidrun/dsp/Modules/LocalChAdmin.c",
                new CoolfluxChessccParser().getGroup(), "", Priority.HIGH);

    }


    @Override
    protected String getWarningsFile() {
        return "coolfluxchesscc.txt";
    }
}


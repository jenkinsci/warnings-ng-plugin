package hudson.plugins.warnings.parser;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Test;

import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;

/**
 * Tests the class {@link CadenceIncisiveParserTest}.
 *
 * @author Andrew 'Necromant' Andrianov
 */
public class CadenceIncisiveParserTest extends ParserTester {
    private static final String WARNING_TYPE = new CadenceIncisiveParser().getGroup();

    /**
     * Test of createWarning method, of class {@link GnuMakeGccParser}.
     *
     * @throws IOException
     *             in case of an error
     */
    @Test
    public void testCreateWarning() throws IOException {
        Collection<FileAnnotation> warnings = new CadenceIncisiveParser().parse(openFile());

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 2, warnings.size());

        Iterator<FileAnnotation> iterator = warnings.iterator();
        checkWarning(iterator.next(),
                0,
                "Resolved design unit 'shittyram' at 'u_shittyrams' to 'shittysoc.shittyram:v' through a global search of all libraries.",
                "/NotFileRelated",
                WARNING_TYPE, "Warning (ncelab): CUSRCH", Priority.LOW);

        checkWarning(iterator.next(),
                313,
                "10 output ports were not connected",
                "/tmp/build-dir/../verilog/shit.v",
                WARNING_TYPE, "Warning (ncelab): CUVWSP", Priority.LOW);

    }

    @Override
    protected String getWarningsFile() {
        return "CadenceIncisive.txt";
    }
}

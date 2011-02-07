package hudson.plugins.warnings.parser;

import static junit.framework.Assert.*;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Test;

/**
 * Tests the class {@link ErlcParser}.
 */
public class ErlcParserTest extends ParserTester {
    /**
     * Parses a file with two Erlc warnings.
     *
     * @throws IOException
     *      if the file could not be read
     */
    @Test
    public void testWarningsParser() throws IOException {
        Collection<FileAnnotation> warnings = new ErlcParser().parse(openFile());

        assertEquals("Wrong number of warnings detected.", 2, warnings.size());

        Iterator<FileAnnotation> iterator = warnings.iterator();
        FileAnnotation annotation = iterator.next();
        checkWarning(annotation,
                125,
                "variable 'Name' is unused",
                "./test.erl",
                ErlcParser.WARNING_TYPE, "ERLC Warning", Priority.NORMAL);
        annotation = iterator.next();
        checkWarning(annotation,
                175,
                "record 'Extension' undefined",
                "./test2.erl",
                ErlcParser.WARNING_TYPE, "ERLC Error", Priority.HIGH);
    }

    /** {@inheritDoc} */
    @Override
    protected String getWarningsFile() {
        return "erlc.txt";
    }
}


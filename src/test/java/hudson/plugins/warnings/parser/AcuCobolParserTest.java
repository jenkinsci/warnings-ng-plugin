package hudson.plugins.warnings.parser;

import static org.junit.Assert.*;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

/**
 * Tests the class {@link AcuCobolParser}.
 */
public class AcuCobolParserTest extends ParserTester {
    private static final String TYPE = new AcuCobolParser().getGroup();

    /**
     * Parses a file with 4 COBOL warnings.
     *
     * @throws IOException
     *      if the file could not be read
     */
    @Test
    public void parseFile() throws IOException {
        Collection<FileAnnotation> warnings = new AcuCobolParser().parse(openFile());

        assertEquals("Wrong number of warnings detected.", 4, warnings.size());

        Iterator<FileAnnotation> iterator = warnings.iterator();
        FileAnnotation annotation = iterator.next();
        checkWarning(annotation,
                39,
                "Imperative statement required",
                "COPY/zzz.CPY",
                TYPE, StringUtils.EMPTY, Priority.NORMAL);
        annotation = iterator.next();
        checkWarning(annotation,
                111,
                "Don't run with knives",
                "C:/Documents and Settings/xxxx/COB/bbb.COB",
                TYPE, StringUtils.EMPTY, Priority.NORMAL);
        annotation = iterator.next();
        checkWarning(annotation,
                115,
                "Don't run with knives",
                "C:/Documents and Settings/xxxx/COB/bbb.COB",
                TYPE, StringUtils.EMPTY, Priority.NORMAL);
        annotation = iterator.next();
        checkWarning(annotation,
                123,
                "I'm a green banana",
                "C:/Documents and Settings/xxxx/COB/ccc.COB",
                TYPE, StringUtils.EMPTY, Priority.NORMAL);
    }

    @Override
    protected String getWarningsFile() {
        return "acu.txt";
    }
}


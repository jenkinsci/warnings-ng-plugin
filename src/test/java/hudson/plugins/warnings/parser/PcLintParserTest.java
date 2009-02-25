package hudson.plugins.warnings.parser;

import static junit.framework.Assert.*;
import hudson.plugins.warnings.util.model.FileAnnotation;
import hudson.plugins.warnings.util.model.Priority;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Test;

/**
 * Tests the class {@link PcLintParser}.
 */
public class PcLintParserTest extends ParserTester {
    /**
     * Creates a new instance of {@link PcLintParserTest}.
     */
    public PcLintParserTest() {
        super(PcLintParser.class);
    }

    /**
     * Parses a file with warnings of the PC lint tool.
     *
     * @throws IOException
     *      if the file could not be read
     */
    @Test
    public void parseWarnings() throws IOException {
        Collection<FileAnnotation> warnings = new PcLintParser().parse(openFile());

        assertEquals("Wrong number of warnings detected.", 21, warnings.size());

        Iterator<FileAnnotation> iterator = warnings.iterator();
        FileAnnotation annotation = iterator.next();
        checkWarning(annotation,
                24,
                "No explicit type given symbol",
                "C:/SDK/include/math.h",
                PcLintParser.WARNING_TYPE, "Info 808", Priority.LOW);
        annotation = iterator.next();
        checkWarning(annotation,
                24,
                "Expecting ';'",
                "C:/SDK/include/math.h",
                PcLintParser.WARNING_TYPE, "Error 10", Priority.HIGH);
        annotation = iterator.next();
        checkWarning(annotation,
                24,
                "Useless Declaration",
                "C:/SDK/include/math.h",
                PcLintParser.WARNING_TYPE, "Warning 42", Priority.NORMAL);
    }

    /** {@inheritDoc} */
    @Override
    protected String getWarningsFile() {
        return "pclint.txt";
    }
}


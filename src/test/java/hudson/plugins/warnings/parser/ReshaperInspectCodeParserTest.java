package hudson.plugins.warnings.parser;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Test;

import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;

/**
 * Tests the class {@link ReshaperInspectCodeParser }.
 */
public class ReshaperInspectCodeParserTest  extends ParserTester {
    /**
     * Parses a file with warnings of the Reshaper InspectCodeParser  tools.
     *
     * @throws IOException
     *      if the file could not be read
     */
    @Test
    public void parseWarnings() throws IOException {
        Collection<FileAnnotation> warnings = new ReshaperInspectCodeParser().parse(openFile());

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 1, warnings.size());

        Iterator<FileAnnotation> iterator = warnings.iterator();
        FileAnnotation annotation = iterator.next();
        checkWarning(annotation,
                4,
                "Using directive is not required by the code and can be safely removed",
                "euler61/Program.cs",
                "ReshaperInspectCode",
                "RedundantUsingDirective",
                Priority.NORMAL);
    }

    @Override
    protected String getWarningsFile() {
        return "ReshaperInspectCode.txt";
    }
}


package hudson.plugins.warnings.parser;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Test;

import static org.junit.Assert.*;

import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;

/**
 * Tests the class {@link ResharperInspectCodeParser}.
 */
public class ResharperInspectCodeAdapterTest extends ParserTester {
    /**
     * Parses a file with warnings of the Reshaper InspectCodeParser  tools.
     *
     * @throws IOException
     *      if the file could not be read
     */
    @Test
    public void parseWarnings() throws IOException {
        Collection<FileAnnotation> warnings = new ResharperInspectCodeParser().parse(openFile());

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 3, warnings.size());

        Iterator<FileAnnotation> iterator = warnings.iterator();
        
        //<Issue TypeId="CSharpErrors" File="ResharperDemo\Program.cs" Offset="408-416" Line="16" Message="" />
        FileAnnotation annotation = iterator.next();
        checkWarning(annotation,
                16,
                "Cannot resolve symbol 'GetError'",
                "ResharperDemo/Program.cs",
                "ResharperInspectCode",
                "CSharpErrors",
                Priority.HIGH);
                        
        annotation = iterator.next();
        checkWarning(annotation,
                23,
                "Expression is always true",
                "ResharperDemo/Program.cs",
                "ResharperInspectCode",
                "ConditionIsAlwaysTrueOrFalse",
                Priority.NORMAL);
        
        annotation = iterator.next();
        checkWarning(annotation,
                41,
                "Convert to auto-property",
                "ResharperDemo/Program.cs",
                "ResharperInspectCode",
                "ConvertToAutoProperty",
                Priority.LOW);
    }

    @Override
    protected String getWarningsFile() {
        return "ResharperInspectCode.xml";
    }
}


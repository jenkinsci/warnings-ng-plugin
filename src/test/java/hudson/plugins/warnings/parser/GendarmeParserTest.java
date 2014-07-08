package hudson.plugins.warnings.parser;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Test;

import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;
import hudson.plugins.warnings.parser.gendarme.GendarmeParser;

/**
 * Tests the class {@link GendarmeParser}.
 *
 * @author Ulli Hafner
 */
public class GendarmeParserTest extends ParserTester {
    /**
     * Tests the Gendarme parser with a file of 3 warnings.
     *
     * @throws IOException
     *             in case of an exception
     */
    @Test
    public void testParseViolationData() throws IOException {
        Collection<FileAnnotation> results = new GendarmeParser().parse(openFile());
        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 3, results.size());

        Iterator<FileAnnotation> iterator = results.iterator();

        checkWarning(iterator.next(), 0, "This assembly is not decorated with the [CLSCompliant] attribute.",
                "", "MarkAssemblyWithCLSCompliantRule", Priority.HIGH);
        checkWarning(iterator.next(), 10, "This method does not use any instance fields, properties or methods and can be made static.",
                "c:/Dev/src/hudson/Hudson.Domain/Dog.cs", "MethodCanBeMadeStaticRule", Priority.LOW);
        checkWarning(iterator.next(), 22, "This method does not use any instance fields, properties or methods and can be made static.",
                "c:/Dev/src/hudson/Hudson.Domain/Dog.cs", "MethodCanBeMadeStaticRule", Priority.LOW);
    }

    @Override
    protected String getWarningsFile() {
        return "gendarme/Gendarme.xml";
    }
}

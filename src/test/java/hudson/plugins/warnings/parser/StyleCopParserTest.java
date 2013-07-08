package hudson.plugins.warnings.parser;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Test;

import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;

/**
 * Tests the class {@link StyleCopParser}.
 *
 * @author Sebastian Seidl
 */
public class StyleCopParserTest extends ParserTester {
    /**
     * Verifies that the StyleCop parser works as expected with a file of 5 warnings.
     *
     * @throws IOException
     *             if the file could not be read
     */
    @Test
    public void testStyleCop() throws IOException {
        Collection<FileAnnotation> result = new StyleCopParser().parse(openFile());

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 5, result.size());

        Iterator<FileAnnotation> iterator = result.iterator();
        checkWarning(iterator.next(), 18,
                "The call to components must begin with the 'this.' prefix to indicate that the item is a member of the class.",
                "Form1.Designer.cs",
                "PrefixLocalCallsWithThis",
                "ReadabilityRules",
                Priority.NORMAL);
        checkWarning(iterator.next(), 16,
                "The call to components must begin with the 'this.' prefix to indicate that the item is a member of the class.",
                "Form1.Designer.cs",
                "PrefixLocalCallsWithThis",
                "ReadabilityRules",
                Priority.NORMAL);
        checkWarning(iterator.next(), 7,
                "The class must have a documentation header.",
                "MainClass.cs",
                "ElementsMustBeDocumented",
                "DocumentationRules",
                Priority.NORMAL);
        checkWarning(iterator.next(), 9,
                "The field must have a documentation header.",
                "MainClass.cs",
                "ElementsMustBeDocumented",
                "DocumentationRules",
                Priority.NORMAL);
        checkWarning(iterator.next(), 10,
                "The property must have a documentation header.",
                "MainClass.cs",
                "ElementsMustBeDocumented",
                "DocumentationRules",
                Priority.NORMAL);
    }

    /**
     * Verifies that the StyleCop parser works as expected with a file of 3 warnings.
     *
     * @throws IOException
     *             if the file could not be read
     */
    @Test
    public void testStyleCopOneFile() throws IOException {
        Collection<FileAnnotation> result = new StyleCopParser().parse(openFile("stylecop/onefile.xml"));

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 3, result.size());
    }

    /**
     * Verifies that the StyleCop parser works as expected with a file of 2 warnings (4.3 format).
     *
     * @throws IOException
     *             if the file could not be read
     */
    @Test
    public void testStyleCop43() throws IOException {
        Collection<FileAnnotation> result = new StyleCopParser().parse(openFile("stylecop/stylecop-v4.3.xml"));

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 2, result.size());
    }

    @Override
    protected String getWarningsFile() {
        return "stylecop.xml";
    }
}


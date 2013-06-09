package hudson.plugins.warnings.parser;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Test;

import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;
import hudson.plugins.warnings.parser.fxcop.FxCopParser;
import hudson.plugins.warnings.parser.stylecop.StyleCopParser;

/**
 * Tests the class {@link FxCopParser}.
 *
 * @author Ulli Hafner
 */
public class StyleCopParserTest extends ParserTester {
    /**
     * Verifies that the StyleCop parser works as expected.
     *
     * @throws IOException
     *             if the file could not be read
     */
    @Test
    public void testStyleCop() throws IOException {
        Collection<FileAnnotation> result = new StyleCopParser().parse(openFile());

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 2, result.size());

        Iterator<FileAnnotation> iterator = result.iterator();
        checkWarning(iterator.next(), 18,
                "The call to components must begin with the 'this.' prefix to indicate that the item is a member of the class.",
                "Form1.Designer.cs",
                "stylecop",
                Priority.NORMAL);
        checkWarning(iterator.next(), 16,
                "The call to components must begin with the 'this.' prefix to indicate that the item is a member of the class.",
                "Form1.Designer.cs",
                "stylecop",
                Priority.NORMAL);
    }

    @Override
    protected String getWarningsFile() {
        return "stylecop.xml";
    }
}


package hudson.plugins.warnings.parser;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import com.google.common.collect.Lists;

import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;
import hudson.plugins.warnings.parser.fxcop.FxCopParser;

/**
 * Tests the class {@link FxCopParser}.
 *
 * @author Ulli Hafner
 */
public class FxcopParserTest extends ParserTester {
    /**
     * Verifies that the FXCop parser works as expected.
     *
     * @throws IOException
     *             if the file could not be read
     */
    @Test
    public void testJenkins14172() throws IOException {
        InputStream file = null;
        try {
            ParserRegistry registry = new ParserRegistry(Lists.newArrayList(new FxCopParser()), "UTF-8");
            file = FxcopParserTest.class.getResourceAsStream("issue14172.xml");
            Collection<FileAnnotation> result = registry.parse(file);

            assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 44, result.size());
        }
        finally {
            IOUtils.closeQuietly(file);
        }
    }

    /**
     * Verifies that the FXCop parser works as expected.
     *
     * @throws IOException
     *             if the file could not be read
     */
    @Test
    public void testFXCop() throws IOException {
        Collection<FileAnnotation> result = new FxCopParser().parse(openFile());

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 2, result.size());

        Iterator<FileAnnotation> iterator = result.iterator();
        checkWarning(iterator.next(), 299,
                "<a href=\"http://msdn2.microsoft.com/library/ms182190(VS.90).aspx\">SpecifyIFormatProvider</a> - Because the behavior of 'decimal.ToString(string)' could vary based on the current user's locale settings, replace this call in 'FilmFacadeBase.Price.get()' with a call to 'decimal.ToString(string, IFormatProvider)'. If the result of 'decimal.ToString(string, IFormatProvider)' will be displayed to the user, specify 'CultureInfo.CurrentCulture' as the 'IFormatProvider' parameter. Otherwise, if the result will be stored and accessed by software, such as when it is persisted to disk or to a database, specify 'CultureInfo.InvariantCulture'.",
                "c:/Hudson/data/jobs/job1/workspace/test/Space/TestBase.cs", "Microsoft.Globalization", Priority.HIGH);
        checkWarning(iterator.next(), 37,
                "<a href=\"http://msdn2.microsoft.com/library/bb264474(VS.90).aspx\">CompoundWordsShouldBeCasedCorrectly</a> - In member 'MyControl.InitialParameters(bool)', the discrete term 'javascript' in parameter name 'javascript' should be expressed as a compound word, 'javaScript'.",
                "c:/Hudson/data/jobs/job1/workspace/web/UserControls/MyControl.ascx.cs", "Microsoft.Naming", Priority.HIGH);
    }

    @Override
    protected String getWarningsFile() {
        return "fxcop.xml";
    }
}


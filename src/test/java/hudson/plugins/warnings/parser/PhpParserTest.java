package hudson.plugins.warnings.parser;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;

/**
 * Tests the class {@link PhpParser}.
 *
 * @author Shimi Kiviti
 */
public class PhpParserTest extends ParserTester {
    private static final String TYPE = new PhpParser().getGroup();

    private static final String PARSE_ERROR_CATEGORY = "PHP Parse error";
    private static final String FATAL_ERROR_CATEGORY = "PHP Fatal error";
    private static final String WARNING_CATEGORY = "PHP Warning";
    private static final String NOTICE_CATEGORY = "PHP Notice";

    /**
     * Verifies that FATAL errors are reported.
     *
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-27681">Issue 27681</a>
     */
    @Test
    public void issue27681() throws IOException {
        Collection<FileAnnotation> warnings = new PhpParser().parse(openFile("issue27681.txt"));

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 1, warnings.size());
        FileAnnotation annotation = warnings.iterator().next();

        checkWarning(annotation,
                0, "SOAP-ERROR: Parsing WSDL: Couldn't load from '...' : failed to load external entity \"...\"",
                "-", TYPE, FATAL_ERROR_CATEGORY, Priority.HIGH);
    }

    /**
     * Tests the PHP parsing.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testParse() throws IOException {
        Collection<FileAnnotation> results = createParser().parse(openFile());
        assertEquals(5, results.size());

        Iterator<FileAnnotation> iterator = results.iterator();
        FileAnnotation annotation = iterator.next();
        checkWarning(annotation,
                25, "include_once(): Failed opening \'RegexpLineParser.php\' for inclusion (include_path=\'.:/usr/share/pear\') in PhpParser.php on line 25",
                "PhpParser.php", TYPE, WARNING_CATEGORY, Priority.NORMAL);

        annotation = iterator.next();
        checkWarning(annotation,
                25, "Undefined index:  SERVER_NAME in /path/to/file/Settings.php on line 25",
                "/path/to/file/Settings.php", TYPE, NOTICE_CATEGORY, Priority.NORMAL);

        annotation = iterator.next();
        checkWarning(annotation,
                35, "Undefined class constant 'MESSAGE' in /MyPhpFile.php on line 35",
                "/MyPhpFile.php", TYPE, FATAL_ERROR_CATEGORY, Priority.HIGH);

        annotation = iterator.next();
        checkWarning(annotation,
                35, "Undefined class constant 'MESSAGE' in /MyPhpFile.php on line 35",
                "/MyPhpFile.php", TYPE, PARSE_ERROR_CATEGORY, Priority.HIGH);

        annotation = iterator.next();
        checkWarning(annotation,
                34, "Missing argument 1 for Title::getText(), called in Title.php on line 22 and defined in Category.php on line 34",
                "Category.php", TYPE, WARNING_CATEGORY, Priority.NORMAL);

    }

    /**
     * Creates the parser.
     *
     * @return the warnings parser
     */
    protected AbstractWarningsParser createParser() {
        return new PhpParser();
    }

    @Override
    protected String getWarningsFile() {
        return "php.txt";
    }
}

package hudson.plugins.warnings.parser;

import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Test;

/**
 * Tests the class {@link PhpParser}.
 *
 * @author Shimi Kiviti
 */
public class PhpParserTest extends ParserTester {
    /**
     * Tests the PHP parsing.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testParse() throws IOException {
        Collection<FileAnnotation> results = new PhpParser().parse(openFile());
        Iterator<FileAnnotation> iterator = results.iterator();
        FileAnnotation annotation = iterator.next();
        checkWarning(annotation,
                25, "include_once(): Failed opening \'RegexpLineParser.php\' for inclusion (include_path=\'.:/usr/share/pear\') in PhpParser.php on line 25",
                "PhpParser.php", PhpParser.WARNING_TYPE, PhpParser.WARNING_CATEGORY, Priority.NORMAL);

        annotation = iterator.next();
        checkWarning(annotation,
                25, "Undefined index:  SERVER_NAME in /path/to/file/Settings.php on line 25",
                "/path/to/file/Settings.php", PhpParser.WARNING_TYPE, PhpParser.NOTICE_CATEGORY, Priority.NORMAL);

        annotation = iterator.next();
        checkWarning(annotation,
                35, "Undefined class constant 'MESSAGE' in /MyPhpFile.php on line 35",
                "/MyPhpFile.php", PhpParser.WARNING_TYPE, PhpParser.FATAL_ERROR_CATEGORY, Priority.HIGH);

        annotation = iterator.next();
        checkWarning(annotation,
                34, "Missing argument 1 for Title::getText(), called in Title.php on line 22 and defined in Category.php on line 34",
                "Category.php", PhpParser.WARNING_TYPE, PhpParser.WARNING_CATEGORY, Priority.NORMAL);

    }

    /** {@inheritDoc} */
    @Override
    protected String getWarningsFile() {
        return "php.txt";
    }
}

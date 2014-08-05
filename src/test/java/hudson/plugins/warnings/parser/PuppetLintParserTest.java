package hudson.plugins.warnings.parser;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Test;

import static org.junit.Assert.*;

import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;

/**
 * Tests the class {@link PuppetLintParser}.
 *
 * @author Jan Vansteenkiste <jan@vstone.eu>
 */
public class PuppetLintParserTest extends ParserTester {
    private static final String TYPE = new PuppetLintParser().getGroup();

    /**
     * Tests the Puppet-Lint parsing.
     *
     * @throws IOException
     *             in case of an error
     */
    @Test
    public void testParse() throws IOException {
        Collection<FileAnnotation> results = createParser().parse(openFile());
        assertEquals(3, results.size());

        Iterator<FileAnnotation> iterator = results.iterator();

        FileAnnotation annotation = iterator.next();
        checkLintWarning(annotation,
                1, "failtest not in autoload module layout",
                "failtest.pp", TYPE, "autoloader_layout", Priority.HIGH, "-");

        annotation = iterator.next();
        checkLintWarning(annotation,
                3, "line has more than 80 characters",
                "./modules/test/manifests/init.pp", TYPE, "80chars", Priority.NORMAL, "::test");

        annotation = iterator.next();
        checkLintWarning(annotation,
                10, "line has more than 80 characters",
                "./modules/test/manifests/sub/class/morefail.pp", TYPE, "80chars", Priority.NORMAL, "::test::sub::class");
    }

    /**
     * Checks the properties of the specified warning.
     *
     * @param annotation
     *            the warning to check
     * @param lineNumber
     *            the expected line number
     * @param message
     *            the expected message
     * @param fileName
     *            the expected filename
     * @param type
     *            the expected type
     * @param category
     *            the expected category
     * @param priority
     *            the expected priority
     * @param packageName
     *            the expected package name
     */
    // CHECKSTYLE:OFF
    private void checkLintWarning(final FileAnnotation annotation, final int lineNumber, final String message, final String fileName, final String type, final String category, final Priority priority, final String packageName) {
        checkWarning(annotation, lineNumber, message, fileName, type, category, priority);
        assertEquals("Wrong packageName detected.", packageName, annotation.getPackageName());
    }
    // CHECKSTYLE:ON

    /**
     * Creates the parser.
     *
     * @return the warnings parser
     */
    protected AbstractWarningsParser createParser() {
        return new PuppetLintParser();
    }

    @Override
    protected String getWarningsFile() {
        return "puppet-lint.txt";
    }
}

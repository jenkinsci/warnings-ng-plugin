package hudson.plugins.warnings.parser;

import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import org.junit.Test;

/**
 * Tests the class {@link PuppetLintParser}.
 *
 * @author Jan Vansteenkiste <jan@vstone.eu>
 */
public class PuppetLintParserTest extends ParserTester {
    /**
     * Tests the Puppet-Lint parsing.
     *
     */
    @Test
    public void testParse() throws IOException {
        Collection<FileAnnotation> results = createParser().parse(openFile());
        Iterator<FileAnnotation> iterator = results.iterator();

        FileAnnotation annotation = iterator.next();
        checkWarning(annotation,
                1, "failtest not in autoload module layout",
                "failtest.pp", PuppetLintParser.WARNING_TYPE, "autoloader_layout", Priority.HIGH);

        annotation = iterator.next();
        checkWarning(annotation,
                3, "line has more than 80 characters",
                "failtest.pp", PuppetLintParser.WARNING_TYPE, "80chars", Priority.NORMAL);

        annotation = iterator.next();
        checkWarning(annotation,
                10, "line has more than 80 characters",
                "morefail.pp", PuppetLintParser.WARNING_TYPE, "80chars", Priority.NORMAL);
    }

    /**
     * Creates the parser.
     *
     * @return the warnings parser
     */
    protected WarningsParser createParser() {
        return new PuppetLintParser();
    }

    /** {@inheritDoc} */
    @Override
    protected String getWarningsFile() {
        return "puppet-lint.txt";
    }
}

package hudson.plugins.warnings.parser;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Test;

import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;

/**
 * Tests the Perl::Critic Parser.
 *
 * @author Mihail Menev, menev@hm.edu
 */
public class PerlCriticParserTest extends ParserTester {
    /**
     * Parses a mixed log file with 105 perlcritic warnings and /var/log/ messages.
     *
     * @throws IOException
     *             if the file cannot be read.
     */
    @Test
    public void testPerlCriticParser() throws IOException {
        Collection<FileAnnotation> warnings = parse("perlcritic.txt");

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 105, warnings.size());
    }

    /**
     * Parses a file with three warnings.
     *
     * @throws IOException
     *             if the file cannot be read.
     */
    @Test
    public void testPerlCriticParserCreateWarning() throws IOException {
        Collection<FileAnnotation> warnings = parse("issue17792.txt");

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 3, warnings.size());

        Iterator<FileAnnotation> iterator = warnings.iterator();
        FileAnnotation annotation = iterator.next();

        checkWarning(annotation, 1, "Code is not tidy", "perl/dir_handler.pl", "33 of PBP", Priority.LOW);

        annotation = iterator.next();
        checkWarning(annotation, 10, "Code before warnings are enabled", "perl/system.pl", "431 of PBP", Priority.HIGH);

        annotation = iterator.next();
        checkWarning(annotation, 7, "Backtick operator used", "perl/ch1/hello", "Use IPC::Open3 instead",
                Priority.NORMAL);
    }

    /**
     * Parses a file with three warnings without the filename in the warning.
     *
     * @throws IOException
     *             if the file cannot be read
     */
    @Test
    public void testPerlCriticParserCreateWarningNoFileName() throws IOException {
        Collection<FileAnnotation> warnings = parse("issue17792-nofilename.txt");

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 3, warnings.size());

        Iterator<FileAnnotation> iterator = warnings.iterator();
        FileAnnotation annotation = iterator.next();

        checkWarning(annotation, 18, "Found \"\\N{SPACE}\" at the end of the line", "-", "Don't use whitespace at the end of lines", Priority.LOW);

        annotation = iterator.next();
        checkWarning(annotation, 16, "Regular expression without \"/s\" flag", "-", "240,241 of PBP", Priority.NORMAL);

        annotation = iterator.next();
        checkWarning(annotation, 15, "Bareword file handle opened", "-", "202,204 of PBP", Priority.HIGH);
    }

    private Collection<FileAnnotation> parse(final String fileName) throws IOException {
        return new PerlCriticParser().parse(openFile(fileName));
    }

    /** {@inheritDoc} */
    @Override
    protected String getWarningsFile() {
        return "perlcritic.txt";
    }
}

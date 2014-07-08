package hudson.plugins.warnings.parser;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;

import org.junit.Test;

import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;

/**
 * Tests the class {@link PyLintParser}.
 */
public class PylintParserTest extends ParserTester {
    private static final String WARNING_TYPE = Messages._Warnings_PyLint_ParserName().toString(Locale.ENGLISH);

    /**
     * Parses a txt file, containing 3 warnings.
     *
     * @throws IOException
     *      if the file could not be read
     */
    @Test
    public void pyLintTest() throws IOException {
        Collection<FileAnnotation> warnings = new PyLintParser().parse(openFile());

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 3, warnings.size());

        Iterator<FileAnnotation> iterator = warnings.iterator();
        FileAnnotation warning;

        warning = iterator.next();
        checkWarning(warning,
                3,
                "Line too long (85/80)",
                "trunk/src/python/cachedhttp.py",
                WARNING_TYPE, "C", Priority.NORMAL);

        warning = iterator.next();
        checkWarning(warning,
                28,
                "Invalid name \"seasonCount\" (should match [a-z_][a-z0-9_]{2,30}$)",
                "trunk/src/python/tv.py",
                WARNING_TYPE, "C0103", Priority.NORMAL);

        warning = iterator.next();
        checkWarning(warning,
                35,
                "Missing docstring",
                "trunk/src/python/tv.py",
                WARNING_TYPE, "C0111", Priority.NORMAL);
    }

    @Override
    protected String getWarningsFile() {
        return "pyLint.txt";
    }
}
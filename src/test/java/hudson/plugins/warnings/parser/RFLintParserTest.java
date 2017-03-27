package hudson.plugins.warnings.parser;

import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;
import org.junit.Test;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;

import static org.junit.Assert.assertEquals;

/**
 * Created by traitanit on 3/27/2017 AD.
 */
public class RFLintParserTest extends ParserTester {
    private static final String WARNING_TYPE = Messages._Warnings_RFLint_ParserName().toString(Locale.ENGLISH);

    /**
     * Parses a txt file, containing 3 warnings.
     *
     * @throws IOException
     *      if the file could not be read
     */
    @Test
    public void rfLintTest() throws IOException {
        Collection<FileAnnotation> warnings = new RFLintParser().parse(openFile());

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 3, warnings.size());
        Iterator<FileAnnotation> iterator = warnings.iterator();
        FileAnnotation warning;

        warning = iterator.next();
        checkWarning(warning,
                11,
                "test case 'Invalid Test Case Pattern'",
                "rules/InvalidTestPattern.robot",
                WARNING_TYPE, "E", Priority.HIGH);

        warning = iterator.next();
        checkWarning(warning,
                65,
                "Line is too long (exceeds 100 characters)",
                "rules/GetProfile.txt",
                WARNING_TYPE, "W", Priority.NORMAL);

        warning = iterator.next();
        checkWarning(warning,
                28,
                "Too many steps (32) in test case",
                "CancelDebit/CancelDebit.txt",
                WARNING_TYPE, "I", Priority.LOW);
    }

    @Override
    protected String getWarningsFile() {
        return "rflint.txt";
    }
}

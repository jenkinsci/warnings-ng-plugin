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
                187,
                "Found invalid variable name 'login_tmx_account_not_tmnId', " +
                        "please name your variable using snake_case: 'login_tmx_account_not_tmn_id'",
                "Login/Login.robot",
                WARNING_TYPE, "WARNING", Priority.NORMAL);

        warning = iterator.next();
        checkWarning(warning,
                188,
                "Found invalid variable name 'login_tmx_password_not_tmnId', " +
                        "please name your variable using snake_case: 'login_tmx_password_not_tmn_id'",
                "Login/Login.robot",
                WARNING_TYPE, "ERROR", Priority.HIGH);

        warning = iterator.next();
        checkWarning(warning,
                262,
                "Found invalid variable name 'last9Digit', " +
                        "please name your variable using snake_case: 'last9_digit'",
                "Login/Login.robot",
                WARNING_TYPE, "IGNORE", Priority.LOW);
    }

    @Override
    protected String getWarningsFile() {
        return "rflint.txt";
    }
}

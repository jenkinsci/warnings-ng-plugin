package hudson.plugins.warnings.parser;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;

import org.junit.Test;

import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;

/**
 * Tests the class {@link AnsibleLintParser}
 */
public class AnsibleLintTest extends ParserTester {
    private static final String WARNING_TYPE = Messages._Warnings_AnsibleLint_ParserName().toString(Locale.ENGLISH);

    /**
     * Parses a string with ansible-lint warning
     *
     * @throws IOException
     *             if the string could not be read
     */

    @Test
    public void testWarningParserError() throws IOException {
        shouldParseWarning(
                "/workspace/roles/roll_forward_target/tasks/main.yml:12: [EANSIBLE0013] Use shell only when shell functionality is required",
                12, "Use shell only when shell functionality is required","/workspace/roles/roll_forward_target/tasks/main.yml",WARNING_TYPE,"ANSIBLE0013",Priority.NORMAL);
    }

    private void shouldParseWarning(final String log, final int lineNumber, final String message, final String fileName,
            final String type, final String category, final Priority priority) {
        try {
            Collection<FileAnnotation> warnings = new AnsibleLintParser().parse(new StringReader(log));

            assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 1, warnings.size());

            Iterator<FileAnnotation> iterator = warnings.iterator();
            FileAnnotation annotation = iterator.next();
            checkWarning(annotation, lineNumber, message, fileName, type, category, priority);

        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected String getWarningsFile() {
        return null; // StringReader used in all tests
    }

}

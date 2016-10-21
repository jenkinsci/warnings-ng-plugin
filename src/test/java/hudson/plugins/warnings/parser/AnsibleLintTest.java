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
 * Tests the class {@link AnsibleLintParser}
 */
public class AnsibleLintTest extends ParserTester {
    private static final String WARNING_TYPE = Messages._Warnings_AnsibleLint_ParserName().toString(Locale.ENGLISH);

    /**
     * Parses a file with 4 ansible-lint warnings
     *
     * @throws IOException
     *             if the string could not be read
     */

    @Test
    public void testWarningParserError() throws IOException {
        Collection<FileAnnotation> warnings = new AnsibleLintParser().parse(openFile());
        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED,4,warnings.size());

        Iterator<FileAnnotation> iterator = warnings.iterator();
        FileAnnotation annotation = iterator.next();
        checkWarning(annotation,
                2,
                "Trailing whitespace",
                "/workspace/roles/backup/tasks/main.yml",
                WARNING_TYPE, "ANSIBLE0002", Priority.NORMAL);

        annotation = iterator.next();
        checkWarning(annotation,
                1,
                "Commands should not change things if nothing needs doing",
                "/workspace/roles/upgrade/tasks/main.yml",
                WARNING_TYPE,"ANSIBLE0012",Priority.NORMAL);

        annotation = iterator.next();
        checkWarning(annotation,
                12,
                "All tasks should be named",
                "/workspace/roles/upgrade/tasks/main.yml",
                WARNING_TYPE,"ANSIBLE0011",Priority.NORMAL);

        annotation = iterator.next();
        checkWarning(annotation,
                12,
                "Use shell only when shell functionality is required",
                "/workspace/roles/roll_forward_target/tasks/main.yml",
                WARNING_TYPE,"ANSIBLE0013",Priority.NORMAL);

    }

    @Override
    protected String getWarningsFile() {
        return "ansibleLint.txt";
    }

}

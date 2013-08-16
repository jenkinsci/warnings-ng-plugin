package hudson.plugins.warnings.parser;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;

/**
 * Tests the class {@link ClangParser}.
 *
 * @author Neil Davis
 */
public class ClangParserTest extends ParserTester {
    private static final String TYPE = new ClangParser().getGroup();

    /**
     * Parses a file with fatal error message.
     *
     * @throws IOException
     *      if the file could not be read
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-18084">Issue 18084</a>
     */
    @Test
    public void issue18084() throws IOException {
        Collection<FileAnnotation> warnings = new ClangParser().parse(openFile("issue18084.txt"));

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 1, warnings.size());
        FileAnnotation annotation = warnings.iterator().next();
        checkWarning(annotation, 10, 10, "'test.h' file not found",
                "./test.h", StringUtils.EMPTY, Priority.HIGH);
    }

    /**
     * Parses a file with one warning that are started by ant.
     *
     * @throws IOException
     *      if the file could not be read
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-14333">Issue 14333</a>
     */
    @Test
    public void issue14333() throws IOException {
        Collection<FileAnnotation> warnings = new ClangParser().parse(openFile("issue14333.txt"));

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 1, warnings.size());
        FileAnnotation annotation = warnings.iterator().next();
        checkWarning(annotation, 1518, 28, "Array access (via field 'yy_buffer_stack') results in a null pointer dereference",
                "scanner.cpp", StringUtils.EMPTY, Priority.NORMAL);
    }

    /**
     * Verifies that all messages are correctly parsed.
     *
     * @throws IOException
     *             in case of an error
     */
    @Test
    public void testWarningsParser() throws IOException {
        Collection<FileAnnotation> warnings = new ClangParser().parse(openFile());

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 8, warnings.size());

        Iterator<FileAnnotation> iterator = warnings.iterator();
        FileAnnotation annotation = iterator.next();
        checkWarning(annotation,
                28, 8,
                "extra tokens at end of #endif directive",
                "test.c",
                TYPE, "-Wextra-tokens", Priority.NORMAL);
        annotation = iterator.next();
        checkWarning(annotation,
                28, 8,
                "extra tokens at end of #endif directive",
                "/path/to/test.c",
                TYPE, "-Wextra-tokens", Priority.NORMAL);
        annotation = iterator.next();
        checkWarning(annotation,
                128,
                "extra tokens at end of #endif directive",
                "test.c",
                TYPE, "-Wextra-tokens", Priority.NORMAL);
        annotation = iterator.next();
        checkWarning(annotation,
                28,
                "extra tokens at end of #endif directive",
                "test.c",
                TYPE, "", Priority.NORMAL);
        annotation = iterator.next();
        checkWarning(annotation,
                3, 11,
                "conversion specifies type 'char *' but the argument has type 'int'",
                "t.c",
                TYPE, "-Wformat", Priority.NORMAL);
        annotation = iterator.next();
        checkWarning(annotation,
                3, 11,
                "conversion specifies type 'char *' but the argument has type 'int'",
                "t.c",
                TYPE, "-Wformat,1", Priority.NORMAL);
        annotation = iterator.next();
        checkWarning(annotation,
                3, 11,
                "conversion specifies type 'char *' but the argument has type 'int'",
                "t.c",
                TYPE, "-Wformat,Format String", Priority.NORMAL);

        annotation = iterator.next();
        checkWarning(annotation,
                47, 15,
                "invalid operands to binary expression ('int *' and '_Complex float')",
                "exprs.c",
                TYPE, "", Priority.NORMAL);
     }

    @Override
    protected String getWarningsFile() {
        return "apple-llvm-clang.txt";
     }
}

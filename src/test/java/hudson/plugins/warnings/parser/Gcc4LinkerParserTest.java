package hudson.plugins.warnings.parser;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Test;

import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;

/**
 * Tests the class {@link Gcc4LinkerParser}.
 */
public class Gcc4LinkerParserTest extends ParserTester {
    private static final String WARNING_CATEGORY = Gcc4LinkerParser.WARNING_CATEGORY;
    private static final String WARNING_TYPE = new Gcc4LinkerParser().getGroup();
    private static final String THERE_ARE_WARNINGS_FOUND = "There are warnings found";

    /**
     * Parses a file with GCC linker errors.
     *
     * @throws IOException
     *      if the file could not be read
     */
    @Test
    public void testWarningsParser() throws IOException {
        Collection<FileAnnotation> warnings = new Gcc4LinkerParser().parse(openFile());

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 8, warnings.size());

        Iterator<FileAnnotation> iterator = warnings.iterator();
        checkWarning(iterator.next(),
                0,
                "undefined reference to 'missing_symbol'",
                "foo.so",
                WARNING_TYPE, WARNING_CATEGORY, Priority.HIGH);
        checkWarning(iterator.next(),
                233,
                "undefined reference to `MyInterface::getValue() const'",
                "/dir1/dir3/file.cpp",
                WARNING_TYPE, WARNING_CATEGORY, Priority.HIGH);
        checkWarning(iterator.next(),
                0,
                "cannot find -lMyLib",
                "",
                WARNING_TYPE, WARNING_CATEGORY, Priority.HIGH);
        checkWarning(iterator.next(),
                0,
                "undefined reference to `clock_gettime'",
                "foo",
                WARNING_TYPE, WARNING_CATEGORY, Priority.HIGH);
        checkWarning(iterator.next(),
                109,
                "undefined reference to `main'",
                "/build/buildd/eglibc-2.10.1/csu/../sysdeps/x86_64/elf/start.S",
                WARNING_TYPE, WARNING_CATEGORY, Priority.HIGH);
        checkWarning(iterator.next(),
                109,
                "undefined reference to `main'",
                "/build/buildd/eglibc-2.10.1/csu/../sysdeps/x86_64/elf/start.S",
                WARNING_TYPE, WARNING_CATEGORY, Priority.HIGH);
        checkWarning(iterator.next(),
                7,
                "undefined reference to `clock_gettime'",
                "/home/me/foo.cpp",
                WARNING_TYPE, WARNING_CATEGORY, Priority.HIGH);
        checkWarning(iterator.next(),
                0,
                "errno: TLS definition in /lib/libc.so.6 section .tbss mismatches non-TLS reference in /tmp/ccgdbGtN.o",
                "",
                WARNING_TYPE, WARNING_CATEGORY, Priority.HIGH);
    }


    /**
     * Parses a warning log with multi line warnings.
     *
     * @throws IOException
     *      if the file could not be read
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-5445">Issue 5445</a>
     */
    @Test
    public void issue5445() throws IOException {
        Collection<FileAnnotation> warnings = new Gcc4LinkerParser().parse(openFile("issue5445.txt"));

        assertEquals(THERE_ARE_WARNINGS_FOUND, 0, warnings.size());
    }

    /**
     * Parses a warning log with autoconf messages. There should be no warning.
     *
     * @throws IOException
     *      if the file could not be read
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-5870">Issue 5870</a>
     */
    @Test
    public void issue5870() throws IOException {
        Collection<FileAnnotation> warnings = new Gcc4LinkerParser().parse(openFile("issue5870.txt"));

        assertEquals(THERE_ARE_WARNINGS_FOUND, 0, warnings.size());
    }

    /**
     * Parses a warning log with 1 warning.
     *
     * @throws IOException
     *      if the file could not be read
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-6563">Issue 6563</a>
     */
    @Test
    public void issue6563() throws IOException {
        Collection<FileAnnotation> warnings = new Gcc4LinkerParser().parse(openFile("issue6563.txt"));

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 0, warnings.size());
    }

    @Override
    protected String getWarningsFile() {
        return "gcc4ld.txt";
    }
}


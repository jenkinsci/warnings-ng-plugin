package hudson.plugins.warnings.parser;

import static junit.framework.Assert.*;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Test;

/**
 * Tests the class {@link Gcc4LinkerParser}.
 */
public class Gcc4LinkerParserTest extends ParserTester {
    /** The category. */
    private static final String WARNING_CATEGORY = Gcc4LinkerParser.WARNING_CATEGORY;
    /** The type. **/
    private static final String WARNING_TYPE = Gcc4LinkerParser.WARNING_TYPE;

    /**
     * Parses a file with GCC linker errors.
     *
     * @throws IOException
     *      if the file could not be read
     */
    @Test
    public void testWarningsParser() throws IOException {
        Collection<FileAnnotation> warnings = new Gcc4LinkerParser().parse(openFile());

        assertEquals("Wrong number of warnings detected.", 8, warnings.size());

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

    /** {@inheritDoc} */
    @Override
    protected String getWarningsFile() {
        return "gcc4ld.txt";
    }
}


package hudson.plugins.warnings.parser;

import static junit.framework.Assert.*;
import hudson.plugins.warnings.util.model.FileAnnotation;
import hudson.plugins.warnings.util.model.Priority;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests the class {@link GccParser}.
 */
public class GccParserTest extends ParserTester {
    /** Error message. */
    private static final String WRONG_NUMBER_OF_WARNINGS_DETECTED = "Wrong number of warnings detected.";
    /** An error. */
    private static final String GCC_ERROR = "GCC error";
    /** A warning. */
    private static final String GCC_WARNING = "GCC warning";

    /**
     * Creates a new instance of {@link GccParserTest}.
     */
    public GccParserTest() {
        super(GccParser.class);
    }

    /**
     * Parses a file with two GCC warnings.
     *
     * @throws IOException
     *      if the file could not be read
     */
    @Test
    public void testWarningsParser() throws IOException {
        Collection<FileAnnotation> warnings = new GccParser().parse(openFile());

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 8, warnings.size());

        Iterator<FileAnnotation> iterator = warnings.iterator();
        FileAnnotation annotation = iterator.next();
        checkWarning(annotation,
                451,
                "`void yyunput(int, char*)' defined but not used",
                "testhist.l",
                GccParser.WARNING_TYPE, GCC_WARNING, Priority.NORMAL);
        annotation = iterator.next();
        checkWarning(annotation,
                73,
                "implicit typename is deprecated, please see the documentation for details",
                "/u1/drjohn/bfdist/packages/RegrTest/V00-03-01/RgtAddressLineScan.cc",
                GccParser.WARNING_TYPE, GCC_ERROR, Priority.HIGH);
        annotation = iterator.next();
        checkWarning(annotation,
                4,
                "foo.h: No such file or directory",
                "foo.cc",
                GccParser.WARNING_TYPE, GCC_ERROR, Priority.HIGH);
        annotation = iterator.next();
        checkWarning(annotation,
                0,
                "undefined reference to 'missing_symbol'",
                "foo.so",
                GccParser.WARNING_TYPE, GCC_ERROR, Priority.HIGH);
        annotation = iterator.next();
        checkWarning(annotation,
                678,
                "missing initializer for member sigaltstack::ss_sp",
                "../../lib/linux-i686/include/boost/test/impl/execution_monitor.ipp",
                GccParser.WARNING_TYPE, GCC_WARNING, Priority.NORMAL);
        annotation = iterator.next();
        checkWarning(annotation,
                678,
                "missing initializer for member sigaltstack::ss_flags",
                "../../lib/linux-i686/include/boost/test/impl/execution_monitor.ipp",
                GccParser.WARNING_TYPE, GCC_WARNING, Priority.NORMAL);
        annotation = iterator.next();
        checkWarning(annotation,
                678,
                "missing initializer for member sigaltstack::ss_size",
                "../../lib/linux-i686/include/boost/test/impl/execution_monitor.ipp",
                GccParser.WARNING_TYPE, GCC_WARNING, Priority.NORMAL);
        annotation = iterator.next();
        checkWarning(annotation,
                52,
                "large integer implicitly truncated to unsigned type",
                "src/test_simple_sgs_message.cxx",
                GccParser.WARNING_TYPE, GCC_WARNING, Priority.NORMAL);
    }

    /**
     * Parses a warning log with 2 new GCC warnings.
     *
     * @throws IOException
     *      if the file could not be read
     * @see <a href="https://hudson.dev.java.net/issues/show_bug.cgi?id=3897">Issue 3897</a>
     * @see <a href="https://hudson.dev.java.net/issues/show_bug.cgi?id=3898">Issue 3898</a>
     */
    @Ignore("Disabled until expected behavior of issue 3897 is defined.") @Test
    public void issue3897and3898() throws IOException {
        Collection<FileAnnotation> warnings = new GccParser().parse(openFile("issue3897.txt"));

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 3, warnings.size());
        Iterator<FileAnnotation> iterator = warnings.iterator();
        checkWarning(iterator.next(),
                12,
                "file.h: No such file or directory",
                "/dir1/dir2/file.c",
                AntJavacParser.WARNING_TYPE, "", Priority.NORMAL);
        checkWarning(iterator.next(),
                233,
                "undefined reference to `MyInterface::getValue() const'",
                "/dir1/dir3/file.cpp",
                AntJavacParser.WARNING_TYPE, RegexpParser.DEPRECATION, Priority.NORMAL);
        checkWarning(iterator.next(),
                233,
                "invalid preprocessing directive #incldue",
                "/dir1/dir2/file.cpp",
                AntJavacParser.WARNING_TYPE, RegexpParser.DEPRECATION, Priority.NORMAL);
    }



    /** {@inheritDoc} */
    @Override
    protected String getWarningsFile() {
        return "gcc.txt";
    }
}


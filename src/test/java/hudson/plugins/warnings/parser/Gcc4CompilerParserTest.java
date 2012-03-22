package hudson.plugins.warnings.parser;

import static junit.framework.Assert.*;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Test;

/**
 * Tests the class {@link Gcc4CompilerParser}.
 */
public class Gcc4CompilerParserTest extends ParserTester {
    private static final String THERE_ARE_WARNINGS_FOUND = "There are warnings found";
    private static final String WARNING_CATEGORY = "Warning";
    private static final String ERROR_CATEGORY = "Error";
    private static final String WARNING_TYPE = new Gcc4CompilerParser().getGroup();

    /**
     * Parses a file with one warning that are started by ant.
     *
     * @throws IOException
     *      if the file could not be read
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-9926">Issue 9926</a>
     */
    @Test
    public void issue9926() throws IOException {
        Collection<FileAnnotation> warnings = new Gcc4CompilerParser().parse(openFile("issue9926.txt"));

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 1, warnings.size());
        FileAnnotation annotation = warnings.iterator().next();
        checkWarning(annotation, 52, "large integer implicitly truncated to unsigned type",
                "src/test_simple_sgs_message.cxx",
                WARNING_TYPE, WARNING_CATEGORY, Priority.NORMAL);
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
        Collection<FileAnnotation> warnings = new Gcc4CompilerParser().parse(openFile("issue6563.txt"));

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 10, warnings.size());
    }

    /**
     * Parses a file with GCC warnings.
     *
     * @throws IOException
     *      if the file could not be read
     */
    @Test
    public void testWarningsParser() throws IOException {
        Collection<FileAnnotation> warnings = new Gcc4CompilerParser().parse(openFile());

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 13, warnings.size());

        Iterator<FileAnnotation> iterator = warnings.iterator();
        checkWarning(iterator.next(),
                451,
                "'void yyunput(int, char*)' defined but not used",
                "testhist.l",
                WARNING_TYPE, WARNING_CATEGORY, Priority.NORMAL);
        checkWarning(iterator.next(),
                73,
                "implicit typename is deprecated, please see the documentation for details",
                "/u1/drjohn/bfdist/packages/RegrTest/V00-03-01/RgtAddressLineScan.cc",
                WARNING_TYPE, ERROR_CATEGORY, Priority.HIGH);
        checkWarning(iterator.next(),
                4,
                "foo.h: No such file or directory",
                "foo.cc",
                WARNING_TYPE, ERROR_CATEGORY, Priority.HIGH);
        checkWarning(iterator.next(),
                678,
                "missing initializer for member sigaltstack::ss_sp",
                "../../lib/linux-i686/include/boost/test/impl/execution_monitor.ipp",
                WARNING_TYPE, WARNING_CATEGORY, Priority.NORMAL);
        checkWarning(iterator.next(),
                678,
                "missing initializer for member sigaltstack::ss_flags",
                "../../lib/linux-i686/include/boost/test/impl/execution_monitor.ipp",
                WARNING_TYPE, WARNING_CATEGORY, Priority.NORMAL);
        checkWarning(iterator.next(),
                678,
                "missing initializer for member sigaltstack::ss_size",
                "../../lib/linux-i686/include/boost/test/impl/execution_monitor.ipp",
                WARNING_TYPE, WARNING_CATEGORY, Priority.NORMAL);
        checkWarning(iterator.next(),
                52,
                "large integer implicitly truncated to unsigned type",
                "src/test_simple_sgs_message.cxx",
                WARNING_TYPE, WARNING_CATEGORY, Priority.NORMAL);
        checkWarning(iterator.next(),
                352,
                "'s2.mepSector2::lubrications' may be used uninitialized in this function",
                "main/mep.cpp",
                WARNING_TYPE, WARNING_CATEGORY, Priority.NORMAL);
        checkWarning(iterator.next(),
                6,
                "passing 'Test' chooses 'int' over 'unsigned int'",
                "warnings.cc",
                WARNING_TYPE, WARNING_CATEGORY, Priority.NORMAL);
        checkWarning(iterator.next(),
                6,
                "in call to 'std::basic_ostream<_CharT, _Traits>& std::basic_ostream<_CharT, _Traits>::operator<<(int) [with _CharT = char, _Traits = std::char_traits<char>]'",
                "warnings.cc",
                WARNING_TYPE, WARNING_CATEGORY, Priority.NORMAL);
        checkWarning(iterator.next(),
                33,
                "#warning This file includes at least one deprecated or antiquated header which may be removed without further notice at a future date. Please use a non-deprecated interface with equivalent functionality instead. For a listing of replacement headers and interfaces, consult the file backward_warning.h. To disable this warning use -Wno-deprecated.",
                "/usr/include/c++/4.3/backward/backward_warning.h",
                WARNING_TYPE, WARNING_CATEGORY, Priority.NORMAL);
        checkWarning(iterator.next(),
                8,
                "'bar' was not declared in this scope",
                "fo:oo.cpp",
                WARNING_TYPE, ERROR_CATEGORY, Priority.HIGH);
        checkWarning(iterator.next(),
                12,
                "expected ';' before 'return'",
                "fo:oo.cpp",
                WARNING_TYPE, ERROR_CATEGORY, Priority.HIGH);
    }

    /**
     * Parses a warning log with 10 template warnings.
     *
     * @throws IOException
     *      if the file could not be read
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-5606">Issue 5606</a>
     */
    @Test
    public void issue5606() throws IOException {
        Collection<FileAnnotation> warnings = new Gcc4CompilerParser().parse(openFile("issue5606.txt"));

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 10, warnings.size());
    }

    /**
     * Parses a warning log with multi line warnings.
     *
     * @throws IOException
     *      if the file could not be read
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-5605">Issue 5605</a>
     */
    @Test
    public void issue5605() throws IOException {
        Collection<FileAnnotation> warnings = new Gcc4CompilerParser().parse(openFile("issue5605.txt"));

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 6, warnings.size());
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
        Collection<FileAnnotation> warnings = new Gcc4CompilerParser().parse(openFile("issue5445.txt"));

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
        Collection<FileAnnotation> warnings = new Gcc4CompilerParser().parse(openFile("issue5870.txt"));

        assertEquals(THERE_ARE_WARNINGS_FOUND, 0, warnings.size());
    }

    /** {@inheritDoc} */
    @Override
    protected String getWarningsFile() {
        return "gcc4.txt";
    }

}


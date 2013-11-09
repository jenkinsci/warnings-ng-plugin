package hudson.plugins.warnings.parser;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Test;

import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;

/**
 * Tests the class {@link GnuMakeGccParser}.
 *
 * @author vichak
 */
public class GnuMakeGccParserTest extends ParserTester {
    private static final String WARNING_CATEGORY = "Warning";
    private static final String ERROR_CATEGORY = "Error";
    private static final String WARNING_TYPE = new GnuMakeGccParser().getGroup();

    /**
     * Test of createWarning method, of class {@link GnuMakeGccParser}.
     *
     * @throws IOException
     *             in case of an error
     */
    @Test
    public void testCreateWarning() throws IOException {
        Collection<FileAnnotation> warnings = new GnuMakeGccParser().parse(openFile());

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 13, warnings.size());

        Iterator<FileAnnotation> iterator = warnings.iterator();
        checkWarning(iterator.next(),
                451,
                "'void yyunput(int, char*)' defined but not used",
                "/dir1/testhist.l",
                WARNING_TYPE, WARNING_CATEGORY, Priority.NORMAL);
        checkWarning(iterator.next(),
                73,
                "implicit typename is deprecated, please see the documentation for details",
                "/u1/drjohn/bfdist/packages/RegrTest/V00-03-01/RgtAddressLineScan.cc",
                WARNING_TYPE, ERROR_CATEGORY, Priority.HIGH);
        checkWarning(iterator.next(),
                4,
                "foo.h: No such file or directory",
                "/dir1/foo.cc",
                WARNING_TYPE, ERROR_CATEGORY, Priority.HIGH);
        checkWarning(iterator.next(),
                678,
                "missing initializer for member sigaltstack::ss_sp",
                "/dir1/../../lib/linux-i686/include/boost/test/impl/execution_monitor.ipp",
                WARNING_TYPE, WARNING_CATEGORY, Priority.NORMAL);
        checkWarning(iterator.next(),
                678,
                "missing initializer for member sigaltstack::ss_flags",
                "/dir1/../../lib/linux-i686/include/boost/test/impl/execution_monitor.ipp",
                WARNING_TYPE, WARNING_CATEGORY, Priority.NORMAL);
        checkWarning(iterator.next(),
                678,
                "missing initializer for member sigaltstack::ss_size",
                "/dir1/../../lib/linux-i686/include/boost/test/impl/execution_monitor.ipp",
                WARNING_TYPE, WARNING_CATEGORY, Priority.NORMAL);
        checkWarning(iterator.next(),
                52,
                "large integer implicitly truncated to unsigned type",
                "/dir1/src/test_simple_sgs_message.cxx",
                WARNING_TYPE, WARNING_CATEGORY, Priority.NORMAL);
        checkWarning(iterator.next(),
                352,
                "'s2.mepSector2::lubrications' may be used uninitialized in this function",
                "/dir1/dir2/main/mep.cpp",
                WARNING_TYPE, WARNING_CATEGORY, Priority.NORMAL);
        checkWarning(iterator.next(),
                6,
                "passing 'Test' chooses 'int' over 'unsigned int'",
                "/dir1/dir2/warnings.cc",
                WARNING_TYPE, WARNING_CATEGORY, Priority.NORMAL);
        checkWarning(iterator.next(),
                6,
                "in call to 'std::basic_ostream<_CharT, _Traits>& std::basic_ostream<_CharT, _Traits>::operator<<(int) [with _CharT = char, _Traits = std::char_traits<char>]'",
                "/dir1/dir2/warnings.cc",
                WARNING_TYPE, WARNING_CATEGORY, Priority.NORMAL);
        checkWarning(iterator.next(),
                33,
                "#warning This file includes at least one deprecated or antiquated header which may be removed without further notice at a future date. Please use a non-deprecated interface with equivalent functionality instead. For a listing of replacement headers and interfaces, consult the file backward_warning.h. To disable this warning use -Wno-deprecated.",
                "/usr/include/c++/4.3/backward/backward_warning.h",
                WARNING_TYPE, WARNING_CATEGORY, Priority.NORMAL);
        checkWarning(iterator.next(),
                8,
                "'bar' was not declared in this scope",
                "/dir1/dir2/dir3/fo:oo.cpp",
                WARNING_TYPE, ERROR_CATEGORY, Priority.HIGH);
        checkWarning(iterator.next(),
                12,
                "expected ';' before 'return'",
                "/dir1/dir2/dir3/fo:oo.cpp",
                WARNING_TYPE, ERROR_CATEGORY, Priority.HIGH);
    }

    @Override
    protected String getWarningsFile() {
        return "gnuMakeGcc.txt";
    }
}

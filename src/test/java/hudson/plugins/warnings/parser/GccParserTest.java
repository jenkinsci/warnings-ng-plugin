package hudson.plugins.warnings.parser;

import static junit.framework.Assert.*;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Test;

/**
 * Tests the class {@link GccParser}.
 */
public class GccParserTest extends ParserTester {
    /** Error message. */
    private static final String WRONG_NUMBER_OF_WARNINGS_DETECTED = "Wrong number of warnings detected.";
    /** An error. */
    private static final String GCC_ERROR = GccParser.GCC_ERROR;
    /** A warning. */
    private static final String GCC_WARNING = "GCC warning";

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
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-3897">Issue 3897</a>
     */
    @Test
    public void issue3897and3898() throws IOException {
        Collection<FileAnnotation> warnings = new GccParser().parse(openFile("issue3897.txt"));

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 3, warnings.size());
        Iterator<FileAnnotation> iterator = warnings.iterator();
        checkWarning(iterator.next(),
                12,
                "file.h: No such file or directory",
                "/dir1/dir2/file.c",
                GccParser.WARNING_TYPE, GccParser.GCC_ERROR, Priority.HIGH);
        checkWarning(iterator.next(),
                233,
                "undefined reference to `MyInterface::getValue() const'",
                "/dir1/dir3/file.cpp",
                GccParser.WARNING_TYPE, GccParser.GCC_ERROR, Priority.HIGH);
        checkWarning(iterator.next(),
                20,
                "invalid preprocessing directive #incldue",
                "/dir1/dir2/file.cpp",
                GccParser.WARNING_TYPE, GccParser.GCC_ERROR, Priority.HIGH);
    }

    /**
     * Parses a warning log with 2 GCC warnings, one of them a note.
     *
     * @throws IOException
     *      if the file could not be read
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-4712">Issue 4712</a>
     */
    @Test
    public void issue4712() throws IOException {
        Collection<FileAnnotation> warnings = new GccParser().parse(openFile("issue4712.txt"));

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 2, warnings.size());
        Iterator<FileAnnotation> iterator = warnings.iterator();
        checkWarning(iterator.next(),
                352,
                "'s2.mepSector2::lubrications' may be used",
                "main/mep.cpp",
                GccParser.WARNING_TYPE, GCC_WARNING, Priority.NORMAL);
        checkWarning(iterator.next(),
                1477,
                "'s2.mepSector2::lubrications' was declared here",
                "main/mep.cpp",
                GccParser.WARNING_TYPE, "GCC note", Priority.LOW);
    }

    /**
     * Parses a warning log with a ClearCase command line that should not be parsed as a warning.
     *
     * @throws IOException
     *      if the file could not be read
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-4712">Issue 4712</a>
     */
    @Test
    public void issue4700() throws IOException {
        Collection<FileAnnotation> warnings = new GccParser().parse(openFile("issue4700.txt"));

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 0, warnings.size());
    }

    /**
     * Parses a warning log with [exec] prefix.
     *
     * @throws IOException
     *      if the file could not be read
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-4712">Issue 4707</a>
     */
    @Test
    public void issue4707() throws IOException {
        Collection<FileAnnotation> warnings = new GccParser().parse(openFile("issue4707.txt"));

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 22, warnings.size());
        Iterator<FileAnnotation> iterator = warnings.iterator();
        checkWarning(iterator.next(),
                1128,
                "NULL used in arithmetic",
                "/Users/rthomson/hudson/jobs/Bryce7-MacWarnings/workspace/bryce7/src/Bryce/Plugins/3DSExport/3DSExport.cpp",
                GccParser.WARNING_TYPE, GCC_WARNING, Priority.NORMAL);
    }

    /**
     * Parses a linker error.
     *
     * @throws IOException
     *      if the file could not be read
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-4010">Issue 4010</a>
     */
    @Test
    public void issue4010() throws IOException {
        Collection<FileAnnotation> warnings = new GccParser().parse(openFile("issue4010.txt"));

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 1, warnings.size());
        Iterator<FileAnnotation> iterator = warnings.iterator();
        checkWarning(iterator.next(),
                0,
                "cannot find -lMyLib",
                "MyLib",
                GccParser.WARNING_TYPE, GccParser.LINKER_ERROR, Priority.HIGH);
    }

    /**
     * Parses a warning log with 6 new objective C warnings.
     *
     * @throws IOException
     *      if the file could not be read
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-4274">Issue 4274</a>
     */
    @Test
    public void issue4274() throws IOException {
        Collection<FileAnnotation> warnings = new GccParser().parse(openFile("issue4274.txt"));

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 4, warnings.size());
        Iterator<FileAnnotation> iterator = warnings.iterator();
        checkWarning(iterator.next(),
                638,
                "local declaration of \"command\" hides instance variable",
                "folder1/file1.m",
                GccParser.WARNING_TYPE, GCC_WARNING, Priority.NORMAL);
        checkWarning(iterator.next(),
                640,
                "instance variable \"command\" accessed in class method",
                "folder1/file1.m",
                GccParser.WARNING_TYPE, GCC_WARNING, Priority.NORMAL);
        checkWarning(iterator.next(),
                47,
                "\"oldGeb\" might be used uninitialized in this function",
                "file1.m",
                GccParser.WARNING_TYPE, GCC_WARNING, Priority.NORMAL);
        checkWarning(iterator.next(),
                640,
                "local declaration of \"command\" hides instance variable",
                "file1.m",
                GccParser.WARNING_TYPE, GCC_WARNING, Priority.NORMAL);
    }

    /**
     * Parses a file with one warning and matching warning that will be excluded afterwards.
     *
     * @throws IOException
     *      if the file could not be read
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-4260">Issue 4260</a>
     */
    @Test
    public void issue4260() throws IOException {
        Collection<FileAnnotation> warnings = new GccParser().parse(openFile("issue4260.txt"));

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 1, warnings.size());
    }



    /** {@inheritDoc} */
    @Override
    protected String getWarningsFile() {
        return "gcc.txt";
    }
}


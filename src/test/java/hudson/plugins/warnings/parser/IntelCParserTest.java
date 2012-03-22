package hudson.plugins.warnings.parser;

import static junit.framework.Assert.*;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Test;

/**
 * Tests the class {@link IntelCParserTest}.
 */
public class IntelCParserTest extends ParserTester {
    private static final String TYPE = new IntelCParser().getGroup();

    /**
     * Parses a file with two Intel warnings.
     *
     * @throws IOException
     *      if the file could not be read
     */
    @Test
    public void testWarningsParser() throws IOException {
        Collection<FileAnnotation> warnings = new IntelCParser().parse(openFile());

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 4, warnings.size());

        Iterator<FileAnnotation> iterator = warnings.iterator();
        FileAnnotation annotation = iterator.next();
        checkWarning(annotation,
                1460,
                "LOOP WAS VECTORIZED.",
                "D:/Hudson/workspace/foo/busdates.cpp",
                TYPE, "Remark", Priority.LOW);
        annotation = iterator.next();
        // remark
        checkWarning(annotation,
                2630,
                "FUSED LOOP WAS VECTORIZED.",
                "D:/Hudson/workspace/foo/hols.cpp",
                TYPE, "Remark", Priority.LOW);
        annotation = iterator.next();
        checkWarning(annotation,
                721,
                "last line of file ends without a newline",
                "D:/Hudson/workspace/zoo/oppdend2d_slv_strip_utils.cpp",
                TYPE, "Remark #1", Priority.LOW);
        annotation = iterator.next();
        checkWarning(annotation,
                17,
                "external function definition with no prior declaration",
                "D:/Hudson/workspace/boo/serviceif.cpp",
                TYPE, "Remark #1418", Priority.LOW);
    }

    /**
     * Parses a warning log with 3 warnings.
     *
     * @throws IOException
     *      if the file could not be read
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-5402">Issue 5402</a>
     */
    @Test
    public void issue5402() throws IOException {
        Collection<FileAnnotation> warnings = new IntelCParser().parse(openFile("issue5402.txt"));

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 4, warnings.size());
        Iterator<FileAnnotation> iterator = warnings.iterator();
        checkWarning(iterator.next(),
                980,
                "label \"find_rule\" was declared but never referenced",
                "<stdout>",
                TYPE, "Warning #177", Priority.NORMAL);
        checkWarning(iterator.next(),
                2454,
                "function \"yy_flex_strlen\" was declared but never referenced",
                "<stdout>",
                TYPE, "Warning #177", Priority.NORMAL);
        checkWarning(iterator.next(),
                120,
                "function \"fopen\" (declared at line 237 of \"C:\\Program Files\\Microsoft Visual Studio 9.0\\VC\\INCLUDE\\stdio.h\") was declared \"deprecated (\"This function or variable may be unsafe. Consider using fopen_s instead. To disable deprecation, use _CRT_SECURE_NO_WARNINGS. See online help for details.\") \"",
                "D:/hudson/workspace/continuous-snext-main-Win32/trunk/src/engine/AllocationProfiler.cpp",
                TYPE, "Warning #1786", Priority.NORMAL);
        checkWarning(iterator.next(),
                120,
                "function \"fopen\" (declared at line 237 of \"C:\\Program Files\\Microsoft Visual Studio 9.0\\VC\\INCLUDE\\stdio.h\") was declared \"deprecated (\"This function or variable may be unsafe. Consider using fopen_s instead. To disable deprecation, use _CRT_SECURE_NO_WARNINGS. See online help for details.\") \"",
                "D:/hudson/workspace/continuous-snext-main-Win32/trunk/src/engine/AllocationProfiler.cpp",
                TYPE, "Error #1786", Priority.HIGH);
    }

    /** {@inheritDoc} */
    @Override
    protected String getWarningsFile() {
        return "intelc.txt";
    }
}


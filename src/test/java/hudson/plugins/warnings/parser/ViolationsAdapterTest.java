package hudson.plugins.warnings.parser;

import static junit.framework.Assert.*;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;
import hudson.plugins.violations.ViolationsParser;
import hudson.plugins.violations.types.codenarc.CodenarcParser;
import hudson.plugins.violations.types.fxcop.FxCopParser;
import hudson.plugins.violations.types.gendarme.GendarmeParser;
import hudson.plugins.violations.types.jcreport.JcReportParser;
import hudson.plugins.violations.types.jslint.JsLintParser;
import hudson.plugins.violations.types.stylecop.StyleCopParser;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import org.junit.Test;

import com.google.common.collect.Sets;

/**
 * Tests the class {@link ViolationsAdapter}.
 *
 * @author Ulli Hafner
 */
public class ViolationsAdapterTest extends ParserTester {
    private static final String EXPECTED_FILE_NAME = "duckworth/hudson-jslint-freestyle/src/prototype.js";

    /**
     * Verifies that the JSLint parser works as expected.
     *
     * @throws IOException
     *             if the file could not be read
     */
    @Test
    public void testJSLintSingle() throws IOException {
        Collection<FileAnnotation> result = parse("jslint/single.xml", createJSLintParser());

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 51, result.size());
        for (FileAnnotation warning : result) {
            assertEquals("Wrong filename", EXPECTED_FILE_NAME, warning.getFileName());
            assertEquals("Wrong priority", Priority.NORMAL, warning.getPriority());
        }
    }

    /**
     * Verifies that the JSLint parser works as expected.
     *
     * @throws IOException
     *             if the file could not be read
     */
    @Test
    public void testJSLintMulti() throws IOException {
        Collection<FileAnnotation> result = parse("jslint/multi.xml", createJSLintParser());

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 102, result.size());
        Set<String> files = Sets.newHashSet();

        for (FileAnnotation warning : result) {
            files.add(warning.getFileName());
        }
        assertEquals("Wrong number of files", 2, files.size());
        assertTrue("File not found", files.contains(EXPECTED_FILE_NAME));
        assertTrue("File not found",
                files.contains("duckworth/hudson-jslint-freestyle/src/scriptaculous.js"));
    }

    private ViolationsAdapter createJSLintParser() {
        return new ViolationsAdapter(new JsLintParser(),
                Messages._Warnings_JSLint_ParserName(),
                Messages._Warnings_JSLint_LinkName(),
                Messages._Warnings_JSLint_TrendName());
    }

    /**
     * Verifies that the FXCop parser works as expected.
     *
     * @throws IOException
     *             if the file could not be read
     */
    @Test
    public void testFXCop() throws IOException {
        verify(new FxCopParser(), "fxcop.xml", 2, 2);
    }

    /**
     * Verifies that the StyleCop parser works as expected.
     *
     * @throws IOException
     *             if the file could not be read
     */
    @Test
    public void testStyleCop() throws IOException {
        verify(new StyleCopParser(), "stylecop/onefile.xml", 3, 1);
    }

    /**
     * Verifies that the JcReport parser works as expected.
     *
     * @throws IOException
     *             if the file could not be read
     */
    @Test
    public void testJCReport() throws IOException {
        verify(new JcReportParser(), "jcreport/jcoderz-report.xml", 42, 3);
    }

    /**
     * Verifies that the Codenarc parser works as expected.
     *
     * @throws IOException
     *             if the file could not be read
     */
    @Test
    public void testCodenarcReport() throws IOException {
        verify(new CodenarcParser(), "codenarc/CodeNarcXmlReport.xml", 10, 7);
    }

    /**
     * Verifies that the Gendarme parser works as expected.
     *
     * @throws IOException
     *             if the file could not be read
     */
    @Test
    public void testGendarmeReport() throws IOException {
        verify(new GendarmeParser(), "gendarme/Gendarme_unix.xml", 3, 2);
    }

    private Collection<FileAnnotation> parse(final String fileName, final ViolationsAdapter violationsAdapter) throws IOException {
        return violationsAdapter.parse(openFile(fileName));
    }

    @Override
    protected String getWarningsFile() {
        return "jslint/single.xml";
    }

    private void verify(final ViolationsParser parser, final String fileName, final int expectedWarnings, final int expectedFiles) throws IOException {
        Collection<FileAnnotation> result = parse(fileName,
                new ViolationsAdapter(parser,
                        Messages._Warnings_JSLint_ParserName(),
                        Messages._Warnings_JSLint_LinkName(),
                        Messages._Warnings_JSLint_TrendName()));

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, expectedWarnings, result.size());
        Set<String> files = Sets.newHashSet();

        for (FileAnnotation warning : result) {
            files.add(warning.getFileName());
        }
        assertEquals("Wrong number of files", expectedFiles, files.size());
    }
}


package hudson.plugins.warnings.parser;

import static junit.framework.Assert.*;
import hudson.plugins.warnings.util.model.FileAnnotation;
import hudson.plugins.warnings.util.model.Priority;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

/**
 * Tests the class {@link HpiCompileParser}.
 */
public class MsBuildParserTest extends ParserTester {
    /**
     * Parses a file with warnings of the MS Build tools.
     *
     * @throws IOException
     *      if the file could not be read
     */
    @Test
    public void parseWarnings() throws IOException {
        Collection<FileAnnotation> warnings = new MsBuildParser().parse(MsBuildParserTest.class.getResourceAsStream("msbuild.txt"));

        assertEquals("Wrong number of warnings detected.", 5, warnings.size());

        Iterator<FileAnnotation> iterator = warnings.iterator();
        FileAnnotation annotation = iterator.next();
        checkWarning(annotation,
                2242,
                "The variable 'type' is declared but never used",
                "Src/Parser/CSharp/cs.ATG",
                MsBuildParser.WARNING_TYPE, "CS0168", Priority.NORMAL);
        annotation = iterator.next();
        checkWarning(annotation,
                10,
                "An error occurred",
                "C:/Src/Parser/CSharp/file.cs",
                MsBuildParser.WARNING_TYPE, "XXX", Priority.HIGH);
        annotation = iterator.next();
        checkWarning(annotation,
                1338,
                "System.ComponentModel.Design.ComponentDesigner.OnSetComponentDefaults() : This method has been deprecated. Use InitializeNewComponent instead. http://go.microsoft.com/fwlink/?linkid=14202",
                "Controls/MozItem.cs",
                MsBuildParser.WARNING_TYPE, "CS0618", Priority.NORMAL);
        annotation = iterator.next();
        checkWarning(annotation,
                3001,
                "Kod som inte kan n†s uppt„cktes",
                "MediaPortal.cs",
                MsBuildParser.WARNING_TYPE, "CS0162", Priority.NORMAL);
        annotation = iterator.next();
        checkWarning(annotation,
                18,
                "Cannot open include file: xyz.h:...",
                "x/a/b/include/abc.h",
                MsBuildParser.WARNING_TYPE, "C1083", Priority.HIGH);
    }

    /**
     * MSBuildParser should also detect keywords 'Warning' and 'Error', as they are
     * produced by the .NET-2.0 compiler of VS2005.
     *
     * @throws IOException
     *
     * @see <a href="https://hudson.dev.java.net/issues/show_bug.cgi?id=2383">Issue 2383</a>
     */
    @Test
    public void shouldDetectKeywordsInRegexCaseInsensitive() throws IOException {
        StringBuilder testData = new StringBuilder();
        testData.append("Src\\Parser\\CSharp\\cs.ATG (2242,17):  Warning CS0168: The variable 'type' is declared but never used");
        testData.append("\r\n");
        testData.append("C:\\Src\\Parser\\CSharp\\file.cs (10): Error XXX: An error occurred");

        Collection<FileAnnotation> warnings = new MsBuildParser().parse(IOUtils.toInputStream(testData.toString()));

        assertEquals("Wrong number of warnings detected.", 2, warnings.size());

        Iterator<FileAnnotation> iterator = warnings.iterator();
        FileAnnotation annotation = iterator.next();
        checkWarning(annotation,
                2242,
                "The variable 'type' is declared but never used",
                "Src/Parser/CSharp/cs.ATG",
                MsBuildParser.WARNING_TYPE, "CS0168", Priority.NORMAL);
        annotation = iterator.next();
        checkWarning(annotation,
                10,
                "An error occurred",
                "C:/Src/Parser/CSharp/file.cs",
                MsBuildParser.WARNING_TYPE, "XXX", Priority.HIGH);

    }
}


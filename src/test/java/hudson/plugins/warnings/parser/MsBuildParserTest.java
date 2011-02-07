package hudson.plugins.warnings.parser;

import static junit.framework.Assert.*;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

/**
 * Tests the class {@link MsBuildParser}.
 */
public class MsBuildParserTest extends ParserTester {
    /** Error message. */
    private static final String WRONG_NUMBER_OF_WARNINGS_DETECTED = "Wrong number of warnings detected.";

    /**
     * Parses a file with warnings of the MS Build tools.
     *
     * @throws IOException
     *      if the file could not be read
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-3582">Issue 3582</a>
     */
    @Test
    public void issue3582() throws IOException {
        Collection<FileAnnotation> warnings = new MsBuildParser().parse(openFile("issue3582.txt"));

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 1, warnings.size());
        FileAnnotation annotation = warnings.iterator().next();
        assertEquals("Wrong file name.", "TestLib.lib", annotation.getFileName());
    }

    /**
     * Parses a file with warnings of Stylecop.
     *
     * @throws IOException
     *      if the file could not be read
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-8347">Issue 8347</a>
     */
    @Test
    public void issue8347() throws IOException {
        Collection<FileAnnotation> warnings = new MsBuildParser().parse(openFile("issue8347.txt"));

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 5, warnings.size());
        FileAnnotation annotation = warnings.iterator().next();
        checkWarning(annotation, 2, "Using directives must be sorted alphabetically by the namespaces. [C:\\hudsonSlave\\workspace\\MyProject\\Source\\Common.Tests.Stubs.csproj]",
                "MoqExtensions.cs", MsBuildParser.WARNING_TYPE, "SA1210", Priority.NORMAL);
    }

    /**
     * Parses a file with one warning of the MS Build tools (parallel build).
     *
     * @throws IOException
     *      if the file could not be read
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-3582">Issue 3582</a>
     */
    @Test
    public void issue6709() throws IOException {
        Collection<FileAnnotation> warnings = new MsBuildParser().parse(openFile("issue6709.txt"));

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 1, warnings.size());
        FileAnnotation annotation = warnings.iterator().next();
        checkWarning(annotation, 1145, "The variable 'ex' is declared but never used", "Rules/TaskRules.cs",
                MsBuildParser.WARNING_TYPE, "CS0168", Priority.NORMAL);
    }

    /**
     * Parses a file with warnings of the MS Build linker.
     *
     * @throws IOException
     *      if the file could not be read
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-4932">Issue 4932</a>
     */
    @Test
    public void issue4932() throws IOException {
        Collection<FileAnnotation> warnings = new MsBuildParser().parse(openFile("issue4932.txt"));

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 2, warnings.size());
        Iterator<FileAnnotation> iterator = warnings.iterator();
        FileAnnotation annotation = iterator.next();
        checkWarning(annotation,
                0,
                "unresolved external symbol \"public:",
                "SynchronisationHeure.obj",
                MsBuildParser.WARNING_TYPE, "LNK2001", Priority.HIGH);
        annotation = iterator.next();
        checkWarning(annotation,
                0,
                "1 unresolved externals",
                "Release/Navineo.exe",
                MsBuildParser.WARNING_TYPE, "LNK1120", Priority.HIGH);
    }

    /**
     * Parses a file with warnings of MS sharepoint.
     *
     * @throws IOException
     *      if the file could not be read
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-4731">Issue 4731</a>
     */
    @Test
    public void issue4731() throws IOException {
        Collection<FileAnnotation> warnings = new MsBuildParser().parse(openFile("issue4731.txt"));

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 12, warnings.size());
        Iterator<FileAnnotation> iterator = warnings.iterator();
        FileAnnotation annotation = iterator.next();
        checkWarning(annotation,
                0,
                "The Project Item \"StructureLibrary\" is included in the following Features: TypesAndLists, StructureBrowser [c:\\playpens\\Catalyst\\Platform\\src\\Ptc.Platform.Web\\Ptc.Platform.Web.csproj]",
                "c:/playpens/Catalyst/Platform/src/Ptc.Platform.Web/Package/Package.package",
                MsBuildParser.WARNING_TYPE, "SPT6", Priority.NORMAL);
        annotation = iterator.next();
        annotation = iterator.next();
        annotation = iterator.next();
        annotation = iterator.next();
        annotation = iterator.next();
        annotation = iterator.next();
        annotation = iterator.next();
        checkWarning(annotation,
                29,
                "'Ptc.Ppm.PpmInstaller.PpmInstaller.InitializeComponent()' hides inherited member 'Ptc.Platform.Forms.Wizard.WizardControl.InitializeComponent()'. To make the current member override that implementation, add the override keyword. Otherwise add the new keyword. [c:\\playpens\\Catalyst\\PPM\\tools\\Ptc.Ppm.Configurator\\src\\Ptc.Ppm.PpmInstaller\\Ptc.Ppm.PpmInstaller.csproj]",
                "PpmInstaller.Designer.cs",
                MsBuildParser.WARNING_TYPE, "CS0114", Priority.NORMAL);
    }

    /**
     * Parses a file with warnings of the MS Build tools.
     *
     * @throws IOException
     *      if the file could not be read
     */
    @Test
    public void parseWarnings() throws IOException {
        Collection<FileAnnotation> warnings = new MsBuildParser().parse(openFile());

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 6, warnings.size());

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
                "Hier kommt der Warnings Text",
                "MediaPortal.cs",
                MsBuildParser.WARNING_TYPE, "CS0162", Priority.NORMAL);
        annotation = iterator.next();
        checkWarning(annotation,
                18,
                "Cannot open include file: xyz.h:...",
                "x/a/b/include/abc.h",
                MsBuildParser.WARNING_TYPE, "C1083", Priority.HIGH);
        annotation = iterator.next();
        checkWarning(annotation,
                5,
                "This is an info message from PcLint",
                "foo.h",
                MsBuildParser.WARNING_TYPE, "701", Priority.LOW);
    }

    /**
     * MSBuildParser should also detect keywords 'Warning' and 'Error', as they
     * are produced by the .NET-2.0 compiler of VS2005.
     *
     * @throws IOException
     *             if the file could not be read
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-2383">Issue
     *      2383</a>
     */
    @Test
    public void shouldDetectKeywordsInRegexCaseInsensitive() throws IOException {
        StringBuilder testData = new StringBuilder();
        testData.append("Src\\Parser\\CSharp\\cs.ATG (2242,17):  Warning CS0168: The variable 'type' is declared but never used");
        testData.append("\r\n");
        testData.append("C:\\Src\\Parser\\CSharp\\file.cs (10): Error XXX: An error occurred");

        Collection<FileAnnotation> warnings = new MsBuildParser().parse(new InputStreamReader(IOUtils.toInputStream(testData.toString())));

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 2, warnings.size());

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

    /** {@inheritDoc} */
    @Override
    protected String getWarningsFile() {
        return "msbuild.txt";
    }
}


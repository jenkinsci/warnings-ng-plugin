package hudson.plugins.warnings.parser;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import static org.junit.Assert.*;

import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;

/**
 * Tests the class {@link MsBuildParser}.
 */
public class MsBuildParserTest extends ParserTester {
    /**
     * MSBuildParser should make relative paths absolute, based on the project name listed in the message.
     * @throws IOException
     *          if the stream could not be read
     *
     * @see <a href="https://issues.jenkins-ci.org/browse/JENKINS-38215">Issue 38215</a>
     */
    @Test
    public void issue38215() throws IOException {
        Collection<FileAnnotation> warnings = new MsBuildParser().parse(openFile("issue38215.txt"));

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 1, warnings.size());
        Iterator<FileAnnotation> iterator = warnings.iterator();
        FileAnnotation annotation = iterator.next();
        checkWarning(annotation,
                0,
                "ignoring unknown option '-std=c++11'",
                "C:/J/workspace/ci_windows/ws/build/rmw/test/test_error_handling.vcxproj",
                MsBuildParser.WARNING_TYPE, "D9002", Priority.NORMAL);
    }

    /**
     * MSBuildParser should make relative paths absolute, based on the project name listed in the message.
     * @throws IOException
     *          if the stream could not be read
     *
     * @see <a href="https://issues.jenkins-ci.org/browse/JENKINS-22386">Issue 22386</a>
     */
    @Test
    public void issue22386() throws IOException {
        Collection<FileAnnotation> warnings = new MsBuildParser().parse(openFile("issue22386.txt"));

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 2, warnings.size());

        Iterator<FileAnnotation> iterator = warnings.iterator();
        FileAnnotation annotation = iterator.next();
        checkWarning(annotation,
                97,
                "'_splitpath': This function or variable may be unsafe. Consider using _splitpath_s instead. To disable deprecation, use _CRT_SECURE_NO_WARNINGS. See online help for details.",
                "c:/solutiondir/projectdir/src/main.cpp",
                MsBuildParser.WARNING_TYPE, "C4996", Priority.NORMAL);
        annotation = iterator.next();
        checkWarning(annotation,
                99,
                "'strdup': The POSIX name for this item is deprecated. Instead, use the ISO C++ conformant name: _strdup. See online help for details.",
                "c:/solutiondir/projectdir/src/main.cpp",
                MsBuildParser.WARNING_TYPE, "C4996", Priority.NORMAL);
     }

    /**
     * MSBuildParser should also detect subcategories as described at
     * <a href="http://blogs.msdn.com/b/msbuild/archive/2006/11/03/msbuild-visual-studio-aware-error-messages-and-message-formats.aspx">
     * MSBuild / Visual Studio aware error messages and message formats</a>.
     * @throws IOException
     *          if the stream could not be read
     *
     * @see <a href="https://issues.jenkins-ci.org/browse/JENKINS-27914">Issue 27914</a>
     */
    @Test
    public void issue27914() throws IOException {
        Collection<FileAnnotation> warnings = new MsBuildParser().parse(openFile("issue27914.txt"));

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 3, warnings.size());

        Iterator<FileAnnotation> iterator = warnings.iterator();
        FileAnnotation annotation = iterator.next();
        checkWarning(annotation,
                4522,
                "Variable 'lTaskDialog' is declared but never used in 'TDialog.ShowTaskDialog'",
                "E:/workspace/TreeSize release nightly/DelphiLib/Jam.UI.Dialogs.pas",
                MsBuildParser.WARNING_TYPE, "H2164", Priority.NORMAL);
        annotation = iterator.next();
        checkWarning(annotation,
                4523,
                "Variable 'lButton' is declared but never used in 'TDialog.ShowTaskDialog'",
                "E:/workspace/TreeSize release nightly/DelphiLib/Jam.UI.Dialogs.pas",
                MsBuildParser.WARNING_TYPE, "H2164", Priority.NORMAL);
        annotation = iterator.next();
        checkWarning(annotation,
                4524,
                "Variable 'lIndex' is declared but never used in 'TDialog.ShowTaskDialog'",
                "E:/workspace/TreeSize release nightly/DelphiLib/Jam.UI.Dialogs.pas",
                MsBuildParser.WARNING_TYPE, "H2164", Priority.NORMAL);
     }

    /**
     * Parses a file with a google-test failure that should not be shown as a warning.
     *
     * @throws IOException
     *      if the file could not be read
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-26441">Issue 26441</a>
     */
    @Test
    public void issue26441() throws IOException {
        Collection<FileAnnotation> warnings = new MsBuildParser().parse(openFile("issue26441.txt"));

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 0, warnings.size());
     }

    /**
     * Parses a file with gcc warnings that should be skipped.
     *
     * @throws IOException
     *      if the file could not be read
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-20544">Issue 20544</a>
     */
    @Test
    public void issue20544() throws IOException {
        Collection<FileAnnotation> warnings = new MsBuildParser().parse(openFile("issue20544.txt"));

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 0, warnings.size());
     }

    /**
     * Parses a file with  warnings of a Visual Studio analysis.
     *
     * @throws IOException
     *      if the file could not be read
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-20154">Issue 20154</a>
     */
    @Test
    public void issue20154() throws IOException {
        Collection<FileAnnotation> warnings = new MsBuildParser().parse(openFile("issue20154.txt"));

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 8, warnings.size());
        Iterator<FileAnnotation> iterator = warnings.iterator();
        FileAnnotation annotation;
        annotation = iterator.next();
        checkWarning(annotation,
                0, "Sign 'SampleCodeAnalysis.exe' with a strong name key.",
                "MSBUILD", "Microsoft.Design", "CA2210", Priority.NORMAL);
        annotation = iterator.next();
        checkWarning(annotation,
                12, "Parameter 'args' of 'Program.Main(string[])' is never used. Remove the parameter or use it in the method body.",
                "i:/devel/projects/SampleCodeAnalysis/SampleCodeAnalysis/Program.cs", "Microsoft.Usage", "CA1801", Priority.NORMAL);
     }

    /**
     * Parses a file with 4 warnings of PCLint tools.
     *
     * @throws IOException
     *      if the file could not be read
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-14888">Issue 14888</a>
     */
    @Test
    public void issue14888() throws IOException {
        Collection<FileAnnotation> warnings = new MsBuildParser().parse(openFile("issue14888.txt"));

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 4, warnings.size());
    }

    /**
     * Parses a file with warnings of the MS Build tools.
     *
     * @throws IOException
     *      if the file could not be read
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-10566">Issue 10566</a>
     */
    @Test
    public void issue10566() throws IOException {
        Collection<FileAnnotation> warnings = new MsBuildParser().parse(openFile("issue10566.txt"));

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 1, warnings.size());
        FileAnnotation annotation = warnings.iterator().next();
        checkWarning(annotation,
                54, "cannot open include file: 'Header.h': No such file or directory",
                "..//..//..//xx_Source//file.c", MsBuildParser.WARNING_TYPE, "c1083", Priority.HIGH);
    }

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
        checkWarning(annotation, 2, "Using directives must be sorted alphabetically by the namespaces.",
                "C:/hudsonSlave/workspace/MyProject/Source/MoqExtensions.cs",
                MsBuildParser.WARNING_TYPE, "SA1210", Priority.NORMAL);
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
     * Parses a file with one warning of the MS Build tools that are started by ant.
     *
     * @throws IOException
     *      if the file could not be read
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-9926">Issue 9926</a>
     */
    @Test
    public void issue9926() throws IOException {
        Collection<FileAnnotation> warnings = new MsBuildParser().parse(openFile("issue9926.txt"));

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 1, warnings.size());
        FileAnnotation annotation = warnings.iterator().next();
        checkWarning(annotation, 125, "assignment within conditional expression",
                "c:/jci/jobs/external_nvtristrip/workspace/compiler/cl/config/debug/platform/win32/tfields/live/external/nvtristrip/nvtristrip.cpp",
                MsBuildParser.WARNING_TYPE, "C4706", Priority.NORMAL);
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
                "The Project Item \"StructureLibrary\" is included in the following Features: TypesAndLists, StructureBrowser",
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
                "'Ptc.Ppm.PpmInstaller.PpmInstaller.InitializeComponent()' hides inherited member 'Ptc.Platform.Forms.Wizard.WizardControl.InitializeComponent()'. To make the current member override that implementation, add the override keyword. Otherwise add the new keyword.",
                "c:/playpens/Catalyst/PPM/tools/Ptc.Ppm.Configurator/src/Ptc.Ppm.PpmInstaller/PpmInstaller.Designer.cs",
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
                2242, 17,
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
                3001, 5,
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
        StringBuilder testData = new StringBuilder(256);
        testData.append("Src\\Parser\\CSharp\\cs.ATG (2242,17):  Warning CS0168: The variable 'type' is declared but never used");
        testData.append("\r\n");
        testData.append("C:\\Src\\Parser\\CSharp\\file.cs (10): Error XXX: An error occurred");

        Collection<FileAnnotation> warnings = new MsBuildParser().parse(new InputStreamReader(
                IOUtils.toInputStream(testData.toString())));

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

    @Override
    protected String getWarningsFile() {
        return "msbuild.txt";
    }
}


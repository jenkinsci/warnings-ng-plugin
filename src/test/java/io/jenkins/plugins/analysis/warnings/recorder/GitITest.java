package io.jenkins.plugins.analysis.warnings.recorder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.ClassRule;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.plugins.git.GitSCM;
import jenkins.plugins.git.GitSampleRepoRule;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.warnings.Java;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.SourceControlTable;

import static io.jenkins.plugins.analysis.core.util.AnalysisBuildResultAssert.*;

public class GitITest extends IntegrationTestWithJenkinsPerSuite {

    @ClassRule
    public static GitSampleRepoRule repository = new GitSampleRepoRule();

    @Test
    public void shouldCreateRepositoryWithTwoContributors() throws Exception {
        final String fileName = "helloWorld.java";
        final File repositoryRoot = createFileInRepositoryWithTwoContributors(fileName);
        final FreeStyleProject project = createFreeStyleProject();

        Java java = new Java();
        java.setPattern("**/*.txt");
        enableWarnings(project, java);

        createFileWithJavaWarningsInRepository(project, fileName, 3, 6);

        project.setScm(new GitSCM("file://" + repositoryRoot));

        AnalysisResult analysisResult = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(analysisResult).hasTotalSize(2);

        HtmlPage DetailsPage = getDetailsWebPage(project, analysisResult);
        SourceControlTable sourceControlTable = new SourceControlTable(DetailsPage);

        System.out.println(sourceControlTable.getInfo());
    }

    /**
     * Creates a Repository with one file. The file will have contributions by two different people.
     * @param fileName The name of the file that will be created in the Repository.
     * @return The path to the root of the created repository.
     * @throws Exception
     */
    private File createFileInRepositoryWithTwoContributors(final String fileName) throws Exception {
        repository.init();
        repository.git("config", "user.name", "Hans Hamburg");
        repository.git("config", "user.email", "hans@hamburg.com");
        repository.git("checkout", "master");

        appendTextToFile(repository.getRoot(), fileName,
                "public class HelloWorld \n"
                        + "{\n"
                        + "       public static void main (String[] args)\n"
                        + "       {\n");

        repository.git("add", fileName);
        repository.git("commit", "-m", "Initial " + fileName, fileName);

        repository.git("config", "user.name", "Peter Petersburg");
        repository.git("config", "user.email", "peter@petersburg.com");

        appendTextToFile(repository.getRoot(), fileName,
                "             // Ausgabe Hello World!\n"
                        + "             System.out.println(\"Hello World!\");\n"
                        + "       }\n"
                        + "}"

        );

        repository.git("add", fileName);
        repository.git("commit", "-m", "Adding to " + fileName, fileName);

        return repository.getRoot();
    }

    /**
     * Create a file with some java warnings in the workspace of the project.
     *
     * @param project
     *         in which the file will be placed
     * @param sourceFile
     *         the source file that the warning corresponds to.
     * @param linesWithWarning
     *         all lines in which a mocked warning should be placed
     */
    private void createFileWithJavaWarningsInRepository(final FreeStyleProject project,
            final String sourceFile,
            final int... linesWithWarning) throws Exception {
        String warningsFile = "javac_warnings.txt";

        StringBuilder warningText = new StringBuilder();
        for (int lineNumber : linesWithWarning) {
            warningText.append(createJavaWarning(sourceFile, lineNumber)).append("\n");
        }

        repository.write(warningsFile, warningText.toString());
        repository.git("add", warningsFile);
        repository.git("commit", "-m", "Adding to " + warningsFile, warningsFile);
    }

    /**
     * Builds a string representing a java deprecation warning.
     *
     * @param lineNumber
     *         line number in which the mock warning occurred
     *
     * @return a mocked warning string
     */
    private String createJavaWarning(final String file, final int lineNumber) {
        return String.format("[WARNING] %s:[%d,42] [deprecation] path.AClass in path has been deprecated\n",
                file, lineNumber);
    }

    /**
     * Get the details web page of a recent build.
     *
     * @param project
     *         of the build used for web request
     * @param result
     *         of the most recent build to show the charts
     *
     * @return loaded web page which contains the charts
     */
    private HtmlPage getDetailsWebPage(final FreeStyleProject project, final AnalysisResult result) {
        int buildNumber = result.getBuild().getNumber();
        String pluginId = result.getId();
        return getWebPage(JavaScriptSupport.JS_ENABLED, project, buildNumber + "/" + pluginId);
    }

    /**
     * Append text to a file in a given Path.
     *
     * @param path
     *         The path where the file can be found.
     * @param fileName
     *         The name of the file that should be written to.
     * @param text
     *         The text that should be appended.
     * @throws IOException
     */
    private void appendTextToFile(File path, String fileName, String text) throws IOException {
        File file = new File(path, fileName);
        FileWriter fileWriter = new FileWriter(file, true);
        fileWriter.write(text);
        fileWriter.close();
    }
}

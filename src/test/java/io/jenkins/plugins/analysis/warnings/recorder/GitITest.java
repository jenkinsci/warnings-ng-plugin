package io.jenkins.plugins.analysis.warnings.recorder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.extensions.impl.RelativeTargetDirectory;
import jenkins.plugins.git.GitSCMBuilder;
import jenkins.plugins.git.GitSampleRepoRule;
import jenkins.scm.api.SCMHead;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.warnings.Java;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.SourceControlRow;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.SourceControlTable;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

/**
 * Tests if the details in the git history can be properly retrieved and shown in the source control table.
 */
public class GitITest extends IntegrationTestWithJenkinsPerSuite {

    /**
     * The git repo rule used to create and use the git repository.
     */
    @Rule
    public GitSampleRepoRule repository = new GitSampleRepoRule();



    /**
     * Creates a java file with commits by two different users and checks if the details in the source control table
     * table reflects the information of the commits.
     *
     * @throws Exception
     *         on exception in the git plugin.
     */
    @Test
    public void shouldGetCommitDetailsForWarnings() throws Exception {
        final String fileName = "helloWorld.java";
        final FreeStyleProject project = createJavaWarningsFreestyleProject();

        createRepositoryInProject(project);
        appendTextToFileInRepository(fileName, "public class HelloWorld {\n", "Hans Hamburg",
                "hans@hamburg.com");
        createJavaWarningInRepository(fileName, 1, "HelloWorld method opened");
        appendTextToFileInRepository(fileName, "}", "Peter Petersburg", "peter@petersburg.com");
        createJavaWarningInRepository(fileName, 2, "HelloWorld method closed");

        AnalysisResult analysisResult = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        assertThat(analysisResult).hasTotalSize(2);

        HtmlPage detailsPage = getDetailsWebPage(project, analysisResult);
        SourceControlTable sourceControlTable = new SourceControlTable(detailsPage);

        List<SourceControlRow> sourceControlRows = sourceControlTable.getRows();
        assertThat(sourceControlRows.size()).isEqualTo(2);
        assertThat(sourceControlRows.get(0).getValue(SourceControlRow.AUTHOR)).isEqualTo("Hans Hamburg");
        assertThat(sourceControlRows.get(0).getValue(SourceControlRow.EMAIL)).isEqualTo("hans@hamburg.com");
        assertThat(sourceControlRows.get(0).getValue(SourceControlRow.FILE)).isEqualTo(fileName + ":1");
        assertThat(sourceControlRows.get(0).getValue(SourceControlRow.DETAILS_CONTENT)).isEqualTo(
                "HelloWorld method opened");
        assertThat(sourceControlRows.get(1).getValue(SourceControlRow.AUTHOR)).isEqualTo("Peter Petersburg");
        assertThat(sourceControlRows.get(1).getValue(SourceControlRow.EMAIL)).isEqualTo("peter@petersburg.com");
        assertThat(sourceControlRows.get(1).getValue(SourceControlRow.FILE)).isEqualTo(fileName + ":2");
        assertThat(sourceControlRows.get(1).getValue(SourceControlRow.DETAILS_CONTENT)).isEqualTo(
                "HelloWorld method closed");
    }

    /**
     * Creates a java file with commits by two different users and checks if the details in the source control table
     * table reflects the information of the commits. The commits override the same lines multiple times, the details
     * should only display the most recent author.
     *
     * @throws Exception
     *         on exception in the git plugin.
     */
    @Test
    public void shouldGetCommitDetailsWithOverwritingCommits() throws Exception {
        final String fileName = "helloWorld.java";
        final FreeStyleProject project = createJavaWarningsFreestyleProject();

        createRepositoryInProject(project);
        appendTextToFileInRepository(fileName, "public class HelloWorld {\nprintln(':)');\n}",
                "Hans Hamburg", "hans@hamburg.com");
        createJavaWarningInRepository(fileName, 1, "HelloWorld method opened");

        // Pretend that the initial commit triggered the pipeline
        scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        // Now change a line in the file which creates a new warning
        replaceLineInRepository(fileName, 2, "error(':(')",
                "Peter Petersburg", "peter@petersburg.com");
        createJavaWarningInRepository(fileName, 2, "Error method called");
        scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        // Change the line again, updating once again the "owner" of the error.
        replaceLineInRepository(fileName, 1, "public class HelloWorld extends World {",
                "Hans Hamburg", "hans@hamburg.com");
        replaceLineInRepository(fileName, 2, "error('other msg')",
                "August Augsburg", "august@augsburg.com");

        AnalysisResult analysisResult = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        assertThat(analysisResult).hasTotalSize(2);

        HtmlPage detailsPage = getDetailsWebPage(project, analysisResult);
        SourceControlTable sourceControlTable = new SourceControlTable(detailsPage);

        List<SourceControlRow> sourceControlRows = sourceControlTable.getRows();
        assertThat(sourceControlRows.size()).isEqualTo(2);
        assertThat(sourceControlRows.get(0).getValue(SourceControlRow.AUTHOR)).isEqualTo("Hans Hamburg");
        assertThat(sourceControlRows.get(0).getValue(SourceControlRow.EMAIL)).isEqualTo("hans@hamburg.com");
        assertThat(sourceControlRows.get(0).getValue(SourceControlRow.FILE)).isEqualTo(fileName + ":1");
        assertThat(sourceControlRows.get(0).getValue(SourceControlRow.DETAILS_CONTENT)).isEqualTo(
                "HelloWorld method opened");
        assertThat(sourceControlRows.get(1).getValue(SourceControlRow.AUTHOR)).isEqualTo("August Augsburg");
        assertThat(sourceControlRows.get(1).getValue(SourceControlRow.EMAIL)).isEqualTo("august@augsburg.com");
        assertThat(sourceControlRows.get(1).getValue(SourceControlRow.FILE)).isEqualTo(fileName + ":2");
        assertThat(sourceControlRows.get(1).getValue(SourceControlRow.DETAILS_CONTENT)).isEqualTo(
                "Error method called");
    }

    /**
     * This tests the behaviour of [JENKINS-57260].
     * @throws Exception if git commands fail to execute.
     */
    @Test
    public void shouldGitBlameForOutOfTreeSources() throws Exception {
        final String fileName = "helloWorld.java";
        final FreeStyleProject project = createJavaWarningsFreestyleProject();

        // Copied code to init repo
        repository.init();
        repository.git("checkout", "master");
        GitSCMBuilder builder = new GitSCMBuilder(new SCMHead("master"), null, repository.fileUrl(), null)
                .withExtension(new RelativeTargetDirectory("src"));
        project.setScm(builder.build());

        appendTextToFileInRepository(fileName, "public class HelloWorld {\n", "Hans Hamburg",
                "hans@hamburg.com");
        createJavaWarningInRepository(fileName, 1, "HelloWorld method opened");
        appendTextToFileInRepository(fileName, "}", "Peter Petersburg", "peter@petersburg.com");
        createJavaWarningInRepository(fileName, 2, "HelloWorld method closed");

        AnalysisResult analysisResult = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        assertThat(analysisResult).hasTotalSize(2);

        HtmlPage detailsPage = getDetailsWebPage(project, analysisResult);
        SourceControlTable sourceControlTable = new SourceControlTable(detailsPage);

        List<SourceControlRow> sourceControlRows = sourceControlTable.getRows();
        assertThat(sourceControlRows.size()).isEqualTo(2);
        assertThat(sourceControlRows.get(0).getValue(SourceControlRow.AUTHOR)).isEqualTo("Hans Hamburg");
        assertThat(sourceControlRows.get(0).getValue(SourceControlRow.EMAIL)).isEqualTo("hans@hamburg.com");
        assertThat(sourceControlRows.get(0).getValue(SourceControlRow.FILE)).isEqualTo(fileName + ":1");
        assertThat(sourceControlRows.get(0).getValue(SourceControlRow.DETAILS_CONTENT)).isEqualTo(
                "HelloWorld method opened");
        assertThat(sourceControlRows.get(1).getValue(SourceControlRow.AUTHOR)).isEqualTo("Peter Petersburg");
        assertThat(sourceControlRows.get(1).getValue(SourceControlRow.EMAIL)).isEqualTo("peter@petersburg.com");
        assertThat(sourceControlRows.get(1).getValue(SourceControlRow.FILE)).isEqualTo(fileName + ":2");
        assertThat(sourceControlRows.get(1).getValue(SourceControlRow.DETAILS_CONTENT)).isEqualTo(
                "HelloWorld method closed");
    }


    /**
     * Create a Freestyle Project with enabled Java warnings.
     * @return The created Freestyle Project.
     */
    private FreeStyleProject createJavaWarningsFreestyleProject() {
        FreeStyleProject project = createFreeStyleProject();
        Java java = new Java();
        java.setPattern("**/*.txt");
        enableWarnings(project, java);
        return project;
    }

    /**
     * Create a repository with the git plugin and add it to the project.
     *
     * @param project
     *         The project to add the git repository to.
     *
     * @throws Exception
     *         on exception in the git plugin.
     */
    private void createRepositoryInProject(final FreeStyleProject project)
            throws Exception {
        repository.init();
        repository.git("checkout", "master");
        project.setScm(new GitSCM(repository.fileUrl()));
    }

    /**
     * Builds a string representing a java warning and adds it to the warnings file in the repository.
     *
     * @param file
     *         The the warning should point to.
     * @param lineNumber
     *         The line number in where the warning occurred.
     * @param warningText
     *         The text the warning should show.
     */
    private void createJavaWarningInRepository(final String file, final int lineNumber, final String warningText)
            throws Exception {
        String warningsFile = "javac_warnings.txt";
        String warning = String.format("[WARNING] %s:[%d,42] [deprecation] %s\n", file, lineNumber, warningText);
        appendTextToFileInRepository(warningsFile, warning, "dummy user", "dummy@user.de");
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
     * Append text to a file in the git repository and commit it with the given user details.
     *
     * @param fileName
     *         The file name where the text should be appended.
     * @param text
     *         The text that should be appended.
     * @param user
     *         The user in the commit message.
     * @param email
     *         The email in the commit message.
     *
     * @throws Exception
     *         on exception in the git plugin.
     */
    private void appendTextToFileInRepository(final String fileName,
            final String text, final String user, final String email)
            throws Exception {
        repository.git("config", "user.name", user);
        repository.git("config", "user.email", email);

        appendTextToFile(repository.getRoot(), fileName, text);

        repository.git("add", fileName);
        repository.git("commit", "-m", "Adding to " + fileName, fileName);
    }

    /**
     * Update one line of a file stored in the local repository.
     *
     * @param fileName
     *         The file to be updated.
     * @param lineToReplace
     *         Line Number in which the update should be done.
     * @param text
     *         New Line content.
     * @param user
     *         The user in the commit message.
     * @param email
     *         The email in the commit message.
     *
     * @throws Exception
     *         if one or more git command fails.
     */
    private void replaceLineInRepository(final String fileName,
            final int lineToReplace, final String text, final String user, final String email)
            throws Exception {
        repository.git("config", "user.name", user);
        repository.git("config", "user.email", email);

        File targetFile = new File(repository.getRoot(), fileName);
        BufferedReader fileReader = new BufferedReader(new FileReader(targetFile));
        StringBuilder outputBuilder = new StringBuilder();
        String currentLine;
        int currentLineNum = 1; // Lines start at index 1

        while ((currentLine = fileReader.readLine()) != null) {
            if (lineToReplace == currentLineNum++) {
                outputBuilder.append(text);
            }
            else {
                outputBuilder.append(currentLine);
            }
            outputBuilder.append('\n');
        }
        fileReader.close();

        FileOutputStream fileOut = new FileOutputStream(targetFile);
        fileOut.write(outputBuilder.toString().getBytes());
        fileOut.close();

        repository.git("add", fileName);
        repository.git("commit", "-m", "File update");
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
     *
     * @throws IOException
     *         if the file can not be opened or writing fails.
     */
    private void appendTextToFile(final File path, final String fileName, final String text) throws IOException {
        File file = new File(path, fileName);
        FileWriter fileWriter = new FileWriter(file, true);
        fileWriter.write(text);
        fileWriter.close();
    }
}

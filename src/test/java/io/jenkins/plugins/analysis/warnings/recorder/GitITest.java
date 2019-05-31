package io.jenkins.plugins.analysis.warnings.recorder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
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

    private static final String SRC_FILE_NAME = "helloWorld.java";

    /**
     * The git repo rule used to create and use the git repository.
     */
    @Rule
    public GitSampleRepoRule repository = new GitSampleRepoRule();

    /**
     * Creates a java file with commits by two different users and checks if the details in the source control table
     * table reflects the information of the commits.
     *
     * @throws IOException
     *         if file writes fail.
     */
    @Test
    public void shouldGetCommitDetailsForWarnings()  throws IOException {
        final FreeStyleProject project = createJavaWarningsFreestyleProject();

        createRepositoryInProject(project);
        appendTextToFileInRepository(SRC_FILE_NAME, "public class HelloWorld {\n", "Hans Hamburg",
                "hans@hamburg.com");
        createJavaWarningInRepository(1, "HelloWorld method opened");
        appendTextToFileInRepository(SRC_FILE_NAME, "}", "Peter Petersburg", "peter@petersburg.com");
        createJavaWarningInRepository(2, "HelloWorld method closed");

        AnalysisResult analysisResult = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        assertThat(analysisResult).hasTotalSize(2);

        HtmlPage detailsPage = getDetailsWebPage(project, analysisResult);
        SourceControlTable sourceControlTable = new SourceControlTable(detailsPage);

        List<SourceControlRow> sourceControlRows = sourceControlTable.getRows();
        assertThat(sourceControlRows.size()).isEqualTo(2);
        assertThat(sourceControlRows.get(0).getValue(SourceControlRow.AUTHOR)).isEqualTo("Hans Hamburg");
        assertThat(sourceControlRows.get(0).getValue(SourceControlRow.EMAIL)).isEqualTo("hans@hamburg.com");
        assertThat(sourceControlRows.get(0).getValue(SourceControlRow.FILE)).isEqualTo(SRC_FILE_NAME + ":1");
        assertThat(sourceControlRows.get(0).getValue(SourceControlRow.DETAILS_CONTENT)).isEqualTo(
                "HelloWorld method opened");
        assertThat(sourceControlRows.get(1).getValue(SourceControlRow.AUTHOR)).isEqualTo("Peter Petersburg");
        assertThat(sourceControlRows.get(1).getValue(SourceControlRow.EMAIL)).isEqualTo("peter@petersburg.com");
        assertThat(sourceControlRows.get(1).getValue(SourceControlRow.FILE)).isEqualTo(SRC_FILE_NAME + ":2");
        assertThat(sourceControlRows.get(1).getValue(SourceControlRow.DETAILS_CONTENT)).isEqualTo(
                "HelloWorld method closed");
    }

    /**
     * Creates a java file with commits by two different users and checks if the details in the source control table
     * table reflects the information of the commits. The commits override the same lines multiple times, the details
     * should only display the most recent author.
     *
     * @throws IOException
     *         if file writes fail.
     */
    @Test
    public void shouldGetCommitDetailsWithOverwritingCommits() throws IOException {
        final FreeStyleProject project = createJavaWarningsFreestyleProject();

        createRepositoryInProject(project);
        appendTextToFileInRepository(SRC_FILE_NAME, "public class HelloWorld {\nprintln(':)');\n}",
                "Hans Hamburg", "hans@hamburg.com");
        createJavaWarningInRepository(1, "HelloWorld method opened");

        // Pretend that the initial commit triggered the pipeline
        scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        // Now change a line in the file which creates a new warning
        replaceLineInRepository(2, "error(':(')",
                "Peter Petersburg", "peter@petersburg.com");
        createJavaWarningInRepository(2, "Error method called");
        scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        // Change the line again, updating once again the "owner" of the error.
        replaceLineInRepository(1, "public class HelloWorld extends World {",
                "Hans Hamburg", "hans@hamburg.com");
        replaceLineInRepository(2, "error('other msg')",
                "August Augsburg", "august@augsburg.com");

        AnalysisResult analysisResult = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        assertThat(analysisResult).hasTotalSize(2);

        HtmlPage detailsPage = getDetailsWebPage(project, analysisResult);
        SourceControlTable sourceControlTable = new SourceControlTable(detailsPage);

        List<SourceControlRow> sourceControlRows = sourceControlTable.getRows();
        assertThat(sourceControlRows.size()).isEqualTo(2);
        assertThat(sourceControlRows.get(0).getValue(SourceControlRow.AUTHOR)).isEqualTo("Hans Hamburg");
        assertThat(sourceControlRows.get(0).getValue(SourceControlRow.EMAIL)).isEqualTo("hans@hamburg.com");
        assertThat(sourceControlRows.get(0).getValue(SourceControlRow.FILE)).isEqualTo(SRC_FILE_NAME + ":1");
        assertThat(sourceControlRows.get(0).getValue(SourceControlRow.DETAILS_CONTENT)).isEqualTo(
                "HelloWorld method opened");
        assertThat(sourceControlRows.get(1).getValue(SourceControlRow.AUTHOR)).isEqualTo("August Augsburg");
        assertThat(sourceControlRows.get(1).getValue(SourceControlRow.EMAIL)).isEqualTo("august@augsburg.com");
        assertThat(sourceControlRows.get(1).getValue(SourceControlRow.FILE)).isEqualTo(SRC_FILE_NAME + ":2");
        assertThat(sourceControlRows.get(1).getValue(SourceControlRow.DETAILS_CONTENT)).isEqualTo(
                "Error method called");
    }

    /**
     * This tests the behaviour of [JENKINS-57260].
     *
     * @throws IOException
     *         if file writes fail.
     */
    @Test
    public void shouldGitBlameForOutOfTreeSources() throws IOException {
        String fileName = "helloWorld.java";
        final FreeStyleProject project = createJavaWarningsFreestyleProject();

        // Copied code to init repo
        initGitRepository();
        doGitCommand("checkout", "master");
        GitSCMBuilder builder = new GitSCMBuilder(new SCMHead("master"), null, repository.fileUrl(), null)
                .withExtension(new RelativeTargetDirectory("src"));
        project.setScm(builder.build());

        appendTextToFileInRepository(fileName, "public class HelloWorld {\n", "Hans Hamburg",
                "hans@hamburg.com");
        createJavaWarningInRepository(1, "HelloWorld method opened");
        appendTextToFileInRepository(fileName, "}", "Peter Petersburg", "peter@petersburg.com");
        createJavaWarningInRepository(2, "HelloWorld method closed");

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
     *
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
     */
    @SuppressWarnings("IllegalCatch") // Exception is thrown by git tool
    private void createRepositoryInProject(final FreeStyleProject project) {
        try {
            repository.init();
            doGitCommand("checkout", "master");
            project.setScm(new GitSCM(repository.fileUrl()));
        }
        catch (Exception initException) {
            throw new AssertionError("Unable to initialize a repository in project", initException);
        }
    }

    /**
     * Builds a string representing a java warning and adds it to the warnings file in the repository.
     *
     * @param lineNumber
     *         The line number in where the warning occurred.
     * @param warningText
     *         The message the created warning will contain.
     */
    private void createJavaWarningInRepository(final int lineNumber, final String warningText) throws IOException {
        String warningsFile = "javac_warnings.txt";
        String warning = String.format("[WARNING] %s:[%d,42] [deprecation] %s\n", SRC_FILE_NAME, lineNumber,
                warningText);
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
     * @throws IOException
     *         writing to file failed.
     */
    private void appendTextToFileInRepository(final String fileName,
            final String text, final String user, final String email) throws IOException {
        doGitCommand("config", "user.name", user);
        doGitCommand("config", "user.email", email);

        appendTextToFile(repository.getRoot(), fileName, text);

        doGitCommand("add", fileName);
        doGitCommand("commit", "-m", "Adding to " + fileName, fileName);
    }

    /**
     * Update one line of a file stored in the local repository.
     *
     * @param lineToReplace
     *         Line Number in which the update should be done.
     * @param text
     *         New Line content.
     * @param user
     *         The user in the commit message.
     * @param email
     *         The email in the commit message.
     *
     * @throws IOException
     *         if the file is not able to be written to.
     */
    private void replaceLineInRepository(final int lineToReplace, final String text, final String user,
            final String email) throws IOException {
        doGitCommand("config", "user.name", user);
        doGitCommand("config", "user.email", email);

        Path targetFile = new File(repository.getRoot(), SRC_FILE_NAME).toPath();
        List<String> lines = Files.readAllLines(targetFile);
        StringBuilder outputBuilder = new StringBuilder();

        int currentLineNum = 1; // Lines start at index 1

        for (String currentLine: lines) {
            if (lineToReplace == currentLineNum) {
                outputBuilder.append(text);
            }
            else {
                outputBuilder.append(currentLine);
            }
            outputBuilder.append('\n');
            currentLineNum++;
        }

        Files.write(targetFile, outputBuilder.toString().getBytes());

        doGitCommand("add", "helloWorld.java");
        doGitCommand("commit", "-m", "File update");
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
        Path file = new File(path, fileName).toPath();
        Files.write(file, text.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    /**
     * Initializes the Git Repository.
     * Handles all possible Exceptions with Assertion Errors.
     */
    @SuppressWarnings("IllegalCatch") // Exception is thrown by git tool
    private void initGitRepository() {
        try {
            repository.init();
        }
        catch (Exception gitException) {
            throw new AssertionError("Git initialization of repository failed", gitException);
        }
    }

    /**
     * Executes a git command on the test repository.
     * Handles all possible Exceptions with Assertion Errors.
     * @param cmds to be executed on the repository.
     */
    @SuppressWarnings("IllegalCatch") // Exception is thrown by git tool
    private void doGitCommand(final String... cmds) {
        try {
            repository.git(cmds);
        }
        catch (Exception gitException) {
            throw new AssertionError("Git command on repository failed", gitException);
        }
    }
}

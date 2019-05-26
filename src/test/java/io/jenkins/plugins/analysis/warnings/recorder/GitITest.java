package io.jenkins.plugins.analysis.warnings.recorder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

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
    @ClassRule
    public static GitSampleRepoRule repository = new GitSampleRepoRule();

    /**
     * Creates a java file with commits by two different users and checks if the details in the source control table
     * table reflects the information of the commits.
     * @throws Exception on exception in the git plugin.
     */
    @Test
    public void shouldGetCommitDetailsForWarnings() throws Exception {
        final String fileName = "helloWorld.java";
        final FreeStyleProject project = createFreeStyleProject();

        Java java = new Java();
        java.setPattern("**/*.txt");
        enableWarnings(project, java);

        createRepositoryInProject(project);
        appendTextToFileInRepository(fileName, "public class HelloWorld {\n", "Hans Hamburg", "hans@hamburg.com");
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
        assertThat(sourceControlRows.get(0).getValue(SourceControlRow.DETAILS_CONTENT)).isEqualTo("HelloWorld method opened");
        assertThat(sourceControlRows.get(1).getValue(SourceControlRow.AUTHOR)).isEqualTo("Peter Petersburg");
        assertThat(sourceControlRows.get(1).getValue(SourceControlRow.EMAIL)).isEqualTo("peter@petersburg.com");
        assertThat(sourceControlRows.get(1).getValue(SourceControlRow.FILE)).isEqualTo(fileName + ":2");
        assertThat(sourceControlRows.get(1).getValue(SourceControlRow.DETAILS_CONTENT)).isEqualTo("HelloWorld method closed");
    }

    /**
     * Create a repository with the git plugin and add it to the project.
     * @param project The project to add the git repository to.
     * @throws Exception on exception in the git plugin.
     */
    private void createRepositoryInProject(final FreeStyleProject project) throws Exception {
        repository.init();
        repository.git("checkout", "master");
        project.setScm(new GitSCM("file://" + repository.getRoot()));
    }

    /**
     * Builds a string representing a java warning and adds it to the warnings file in the repository.
     * @param file
     *         The the warning should point to.
     * @param lineNumber
     *         The line number in where the warning occurred.
     * @param warningText
     *         The text the warning should show.
     */
    private void createJavaWarningInRepository(final String file, final int lineNumber, final String warningText) throws Exception {
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
     * @param fileName
     *         The file name where the text should be appended.
     * @param text
     *         The text that should be appended.
     * @param user
     *         The user in the commit message.
     * @param email
     *         The email in the commit message.
     * @throws Exception on exception in the git plugin.
     */
    private void appendTextToFileInRepository(final String fileName, final String text, final String user, final String email)
            throws Exception {
        repository.git("config", "user.name", user);
        repository.git("config", "user.email", email);

        appendTextToFile(repository.getRoot(), fileName, text);

        repository.git("add", fileName);
        repository.git("commit", "-m", "Adding to " + fileName, fileName);
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
     * @throws IOException if the file can not be opened or writing fails.
     */
    private void appendTextToFile(final File path, final String fileName, final String text) throws IOException {
        File file = new File(path, fileName);
        FileWriter fileWriter = new FileWriter(file, true);
        fileWriter.write(text);
        fileWriter.close();
    }
}

package io.jenkins.plugins.analysis.core.scm;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
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

public class GitBlameITest extends IntegrationTestWithJenkinsPerSuite {

    @Rule
    public GitSampleRepoRule gitRepo = new GitSampleRepoRule();

    @Test
    public void shouldShowOneGitBlameWarning() throws Exception {
        final String fileName = "Hello.java";

        FreeStyleProject project = createScmJavaFreestyleProject();
        appendTextToFileInScm(gitRepo, fileName, "public class HelloWorld {\n"
                + "       public static void main (String[] args) {\n"
                + "             System.out.println(\"Hello World!\");\n"
                + "       }}", "John Doe", "John@localhost");
        appendTextToFileInScm(gitRepo, fileName, "{", "Jane Doe", "Jane@localhost");
        saveJavaWarningToScm(gitRepo, fileName, 4, "Unexpected end of File");

        AnalysisResult analysisResult = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        assertThat(analysisResult).hasTotalSize(1);

        HtmlPage detailsPage = getDetailsWebPage(project, analysisResult);
        SourceControlTable sourceControlTable = new SourceControlTable(detailsPage);

        List<SourceControlRow> sourceControlRows = sourceControlTable.getRows();
        assertThat(sourceControlRows.get(0).getValue(SourceControlRow.AUTHOR)).isEqualTo("Jane Doe");
        assertThat(sourceControlRows.get(0).getValue(SourceControlRow.EMAIL)).isEqualTo("Jane@localhost");
        assertThat(sourceControlRows.get(0).getValue(SourceControlRow.FILE)).isEqualTo(fileName + ":4");
        assertThat(sourceControlRows.get(0).getValue(SourceControlRow.DETAILS_CONTENT)).isEqualTo(
                "Unexpected end of File");
    }

    /**
     * Creates a Java FreestyleProject which checks out a newly created git repository and is capable of parsing Java warnings.
     * @return A Java Freestyle project.
     * @throws Exception If git commands fail to execute.
     */
    private FreeStyleProject createScmJavaFreestyleProject()
            throws Exception {
        FreeStyleProject project = createFreeStyleProject();
        Java java = new Java();
        java.setPattern("**/*.txt");
        enableWarnings(project, java);

        gitRepo.init();
        gitRepo.git("checkout", "master");
        project.setScm(new GitSCM(gitRepo.fileUrl()));
        return project;
    }

    /**
     * Writes an Java warning into a given repository.
     * @param repository The repository containing the error.
     * @param javaFile The file containing an error.
     * @param lineNumber The line containing the error.
     * @param warningText The javac error message.
     * @throws Exception If git commands used to commit the warning are failing.
     */
    private void saveJavaWarningToScm(final GitSampleRepoRule repository, final String javaFile,
            final int lineNumber, final String warningText) throws Exception {
        String warningsFile = "warnings.txt";
        String warning = String.format("[WARNING] %s:[%d,0] %s\n", javaFile, lineNumber, warningText);
        appendTextToFileInScm(repository, warningsFile, warning, "John Doe", "John@localhost");
    }

    /**
     * Returns the details page of a job.
     * @param project The project containing the job.
     * @param result The result to use.
     * @return The web page containing build details.
     */
    private HtmlPage getDetailsWebPage(final FreeStyleProject project, final AnalysisResult result) {
        int buildNumber = result.getBuild().getNumber();
        String pluginId = result.getId();
        return getWebPage(JavaScriptSupport.JS_ENABLED, project, String.format("%d/%s", buildNumber, pluginId));
    }

    /**
     * Appends text to an file and adds the changes to the repository given.
     * @param repository The repository to commit to.
     * @param fileName The file to append to.
     * @param text The text to append.
     * @param user The user who commits the change.
     * @param email The email of the user committing.
     * @throws Exception If the git commands are failing.
     */
    private void appendTextToFileInScm(final GitSampleRepoRule repository, final String fileName, final String text,
            final String user, final String email) throws Exception {
        repository.git("config", "user.name", user);
        repository.git("config", "user.email", email);

        File file = new File(repository.getRoot(), fileName);
        try (FileWriter fileWriter = new FileWriter(file, true)) {
            fileWriter.write(text);
        }

        repository.git("add", fileName);
        repository.git("commit", "-m", String.format("\"Appended code to %s\"", fileName));
    }
}

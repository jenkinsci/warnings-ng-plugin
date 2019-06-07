package io.jenkins.plugins.analysis.core.scm;

import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import hudson.model.FreeStyleProject;
import hudson.model.Item;
import hudson.model.Result;
import hudson.plugins.git.GitSCM;
import jenkins.plugins.git.GitSampleRepoRule;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.warnings.Java;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.SourceControlRow;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.SourceControlTable;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;
import static java.nio.file.StandardOpenOption.*;

/**
 * Integration test for GitBlamer.
 *
 * @author Andreas Neumeier
 * @author Tobias Redl
 */
public class GitBlameITest extends IntegrationTestWithJenkinsPerSuite {
    private static final String JAVA_FILE = "Hello.java";
    private static final String CPP_FILE = "Hello.cpp";
    private static final String USER_1_EMAIL = "John@localhost";
    private static final String USER_1_AUTHOR = "John Doe";
    private static final String USER_2_EMAIL = "Jane@localhost";
    private static final String USER_2_AUTHOR = "Jane Doe";
    private static final String JAVA_WARNING = "Unexpected end of File";

    /**
     * Git repository used for Testing.
     */
    @Rule
    public GitSampleRepoRule gitRepo = new GitSampleRepoRule();

    /**
     * Test if Source Control Table contains a git blame issue when a simple Java syntax error occurs.
     */
    @Test
    public void shouldShowOneGitBlameWarning() {
        FreeStyleProject project = createScmJavaFreestyleProject();
        appendTextToFileInScm(gitRepo, JAVA_FILE, "public class HelloWorld {\n"
                + "       public static void main (String[] args) {\n"
                + "             System.out.println(\"Hello World!\");\n"
                + "       }}", USER_1_AUTHOR, USER_1_EMAIL);
        appendTextToFileInScm(gitRepo, JAVA_FILE, "{", USER_2_AUTHOR, USER_2_EMAIL);
        saveJavaWarningToScm(gitRepo, JAVA_FILE, 4, JAVA_WARNING);

        AnalysisResult analysisResult = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        assertThat(analysisResult).hasTotalSize(1);

        HtmlPage detailsPage = getDetailsWebPage(project, analysisResult);
        SourceControlTable sourceControlTable = new SourceControlTable(detailsPage);

        List<SourceControlRow> sourceControlRows = sourceControlTable.getRows();
        assertThat(sourceControlRows.get(0).getValue(SourceControlRow.AUTHOR)).isEqualTo(USER_2_AUTHOR);
        assertThat(sourceControlRows.get(0).getValue(SourceControlRow.EMAIL)).isEqualTo(USER_2_EMAIL);
        assertThat(sourceControlRows.get(0).getValue(SourceControlRow.FILE)).isEqualTo(JAVA_FILE + ":4");
        assertThat(sourceControlRows.get(0).getValue(SourceControlRow.DETAILS_CONTENT)).isEqualTo(JAVA_WARNING);
    }

    /**
     * Checks if git blame issues are generated properly if default checkout is disabled.
     */
    @Issue("JENKINS-57260")
    @Test
    //gitRepo.init() throws exception of type Exception, therefore it is necessary to catch it
    @SuppressWarnings("checkstyle:IllegalCatch")
    public void shouldShowGitBlameWarningWithDisabledDefaultCheckout() {
        try {
            gitRepo.init();
        }
        catch (Exception e) {
            throw new AssertionError("Error while initialize git repository.", e);
        }
        appendTextToFileInScm(gitRepo, JAVA_FILE, "class Hello {}", USER_1_AUTHOR, USER_1_EMAIL);
        appendTextToFileInScm(gitRepo, CPP_FILE, "class Hello {}", USER_2_AUTHOR, USER_2_EMAIL);

        final String pipeline = "pipeline {\n"
                + "agent any\n"
                + "options{\n"
                + "skipDefaultCheckout()\n"
                + "}\n"
                + "stages{\n"
                + "stage('Prepare') {\n"
                + "  steps {\n"
                + "    dir('src') {\n"
                + "      checkout scm\n"
                + "    }\n"
                + "  }\n"
                + "}\n"
                + "stage('Doxygen') {\n"
                + "  steps {\n"
                + "    dir('build/doxygen') {\n"
                + "      writeFile file: 'doxygen/doxygen.log', text: '\"Notice: Output directory doc/doxygen/framework does not exist. I have created it for you.\\nsrc/Hello.java:1: Warning: reached end of file while inside a dot block!\\nThe command that should end the block seems to be missing!\\nsrc/Hello.cpp:1: Warning: the name lcp_lexicolemke.c supplied as the second argument in the file statement is not an input file\"'\n"
                + "    }\n"
                + "    recordIssues(aggregatingResults: true, enabledForFailure: true, tools: [ doxygen(name: 'Doxygen', pattern: 'build/doxygen/doxygen/doxygen.log') ] )\n"
                + "  }\n"
                + "}\n"
                + "}}";

        WorkflowJob project = createPipeline();
        appendTextToFileInScm(gitRepo, "Pipeline.txt", pipeline, "Pipeline", "root@localhost");
        project.setDefinition(new CpsScmFlowDefinition(new GitSCM(gitRepo.toString()), "Pipeline.txt"));
        scheduleSuccessfulBuild(project);
        AnalysisResult analysisResult = scheduleSuccessfulBuild(project);

        assertThat(analysisResult.getErrorMessages()).doesNotContain(
                "Can't determine head commit using 'git rev-parse'. Skipping blame.");
        assertThat(analysisResult).hasTotalSize(2);

        HtmlPage detailsPage = getDetailsWebPage(project, analysisResult);
        SourceControlTable sourceControlTable = new SourceControlTable(detailsPage);

        List<SourceControlRow> sourceControlRows = sourceControlTable.getRows();
        assertThat(sourceControlRows.get(1).getValue(SourceControlRow.AUTHOR)).isEqualTo(USER_1_AUTHOR);
        assertThat(sourceControlRows.get(1).getValue(SourceControlRow.EMAIL)).isEqualTo(USER_1_EMAIL);
        assertThat(sourceControlRows.get(1).getValue(SourceControlRow.FILE)).isEqualTo(JAVA_FILE + ":1");
        assertThat(sourceControlRows.get(1).getValue(SourceControlRow.DETAILS_CONTENT)).isEqualTo(
                "reached end of file while inside a dot block! The command that should end the block seems to be missing!");

        assertThat(sourceControlRows.get(0).getValue(SourceControlRow.AUTHOR)).isEqualTo(USER_2_AUTHOR);
        assertThat(sourceControlRows.get(0).getValue(SourceControlRow.EMAIL)).isEqualTo(USER_2_EMAIL);
        assertThat(sourceControlRows.get(0).getValue(SourceControlRow.FILE)).isEqualTo(CPP_FILE + ":1");
        assertThat(sourceControlRows.get(0).getValue(SourceControlRow.DETAILS_CONTENT)).isEqualTo(
                "the name lcp_lexicolemke.c supplied as the second argument in the file statement is not an input file\"");
    }

    //gitRepo methods throw exceptions of type Exception, therefore it is necessary to catch them
    @SuppressWarnings("checkstyle:IllegalCatch")
    private FreeStyleProject createScmJavaFreestyleProject() {
        FreeStyleProject project = createFreeStyleProject();
        Java java = new Java();
        java.setPattern("**/*.txt");
        enableWarnings(project, java);

        try {
            gitRepo.init();
            gitRepo.git("checkout", "master");
            project.setScm(new GitSCM(gitRepo.fileUrl()));
        }
        catch (Exception e) {
            throw new AssertionError("Error while setting up new git repository.", e);
        }
        return project;
    }

    private void saveJavaWarningToScm(final GitSampleRepoRule repository, final String javaFile,
            final int lineNumber, final String warningText) {
        String warningsFile = "warnings.txt";
        String warning = String.format("[WARNING] %s:[%d,0] %s\n", javaFile, lineNumber, warningText);
        appendTextToFileInScm(repository, warningsFile, warning, USER_1_AUTHOR, USER_1_EMAIL);
    }

    private HtmlPage getDetailsWebPage(final Item project, final AnalysisResult result) {
        int buildNumber = result.getBuild().getNumber();
        String pluginId = result.getId();
        return getWebPage(JavaScriptSupport.JS_ENABLED, project, String.format("%d/%s", buildNumber, pluginId));
    }

    //repository.git throws exception of type Exception, therefore it is necessary to catch it
    @SuppressWarnings("checkstyle:IllegalCatch")
    private void appendTextToFileInScm(final GitSampleRepoRule repository, final String fileName, final String text,
            final String user, final String email) {
        try {
            repository.git("config", "user.name", user);
            repository.git("config", "user.email", email);

            File file = new File(repository.getRoot(), fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            try (OutputStream outputStream = Files.newOutputStream(file.toPath(), APPEND)) {
                outputStream.write(text.getBytes());
            }

            repository.git("add", fileName);
            repository.git("commit", "-m", String.format("\"Appended code to %s\"", fileName));
        }
        catch (Exception e) {
            throw new AssertionError("Error while committing to repository.", e);
        }
    }
}

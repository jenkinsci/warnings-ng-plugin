package io.jenkins.plugins.analysis.warnings;

import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.extensions.impl.RelativeTargetDirectory;
import jenkins.plugins.git.GitSCMBuilder;
import jenkins.plugins.git.GitSampleRepoRule;
import jenkins.scm.api.SCMHead;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.SourceControlRow;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.SourceControlTable;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for git.
 *
 * @author Colin Kaschel
 * @author Nils Engelbrecht
 */
public class GitITest extends IntegrationTestWithJenkinsPerSuite {

    /**
     * Git rule for itest.
     */
    @Rule
    public GitSampleRepoRule repository = new GitSampleRepoRule();

    private static final String TEST_CLASS_FILE_NAME = "Test.java";
    private static final String TEST_CLASS_FILE_CONTENT_1 = "public class Test {\n"
            + "\n"
            + "     private String test1;\n"
            + "\n"
            + "     public Test() {\n"
            + "         this.test1 = (String) \"Test1\";\n"
            + "     }\n"
            + "\n"
            + " }";

    private static final String TEST_CLASS_FILE_CONTENT_2 = "public class Test {\n"
            + "\n"
            + "     private String test1;\n"
            + "     private String test2;\n"
            + "\n"
            + "     public Test() {\n"
            + "         this.test1 = (String) \"Test1\";\n"
            + "         this.test2 = (String) \"Test2\";\n"
            + "     }\n"
            + "\n"
            + " }";

    private static final String WARNINGS_FILE_NAME = "Warnings.txt";
    private static final String WARNINGS_FILE_CONTENT_1 = "Test.java:6: warning: [cast] redundant cast to String";
    private static final String WARNINGS_FILE_CONTENT_2 =
            "Test.java:7: warning: [cast] redundant cast to String\n"
                    + "Test.java:8: warning: [cast] redundant cast to String\"";

    private static final String GIT_USER_NAME_1 = "GitUser 1";
    private static final String GIT_USER_EMAIL_1 = "gituser1@email.com";

    private static final String GIT_USER_NAME_2 = "GitUser 2";
    private static final String GIT_USER_EMAIL_2 = "gituser2@email.com";

    @Before
    public void initGit() throws Exception {
        repository.init();
    }

    /**
     * Should blame the committer who wrote the line that causes the Issue.
     */
    @Test
    public void shouldBlameOneCommitter() throws Exception {
        setGitUser(GIT_USER_NAME_1, GIT_USER_EMAIL_1);

        writeAndCommitFile(TEST_CLASS_FILE_NAME, TEST_CLASS_FILE_CONTENT_1, "Add Test.java");
        writeAndCommitFile(WARNINGS_FILE_NAME, WARNINGS_FILE_CONTENT_1, "Add Warnings");

        FreeStyleProject project = initFreeStyleProject();
        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        List<SourceControlRow> controlRows = getSourceControlRows(project, result);
        assertThat(controlRows).hasSize(1);
        validateRow(controlRows.get(0), GIT_USER_NAME_1, GIT_USER_EMAIL_1, TEST_CLASS_FILE_NAME);
    }

    /**
     * Should blame the correct committer who wrote the line that causes the Issue.
     *
     * @throws Exception
     *         throws exception.
     */
    @Test
    public void shouldBlameTwoCommitter() throws Exception {
        setGitUser(GIT_USER_NAME_1, GIT_USER_EMAIL_1);
        writeAndCommitFile(TEST_CLASS_FILE_NAME, TEST_CLASS_FILE_CONTENT_1, "Add Test.java");

        setGitUser(GIT_USER_NAME_2, GIT_USER_EMAIL_2);
        writeAndCommitFile(TEST_CLASS_FILE_NAME, TEST_CLASS_FILE_CONTENT_2, "Edit Test.java");

        writeAndCommitFile(WARNINGS_FILE_NAME, WARNINGS_FILE_CONTENT_2, "Add Warnings");

        FreeStyleProject project = initFreeStyleProject();

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        List<SourceControlRow> controlRows = getSourceControlRows(project, result);
        assertThat(controlRows).hasSize(2);
        validateRow(controlRows.get(0), GIT_USER_NAME_1, GIT_USER_EMAIL_1, TEST_CLASS_FILE_NAME);
        validateRow(controlRows.get(1), GIT_USER_NAME_2, GIT_USER_EMAIL_2, TEST_CLASS_FILE_NAME);
    }

    /**
     * Should blame the committer who changed the line at last which caused an Issue.
     *
     * @throws Exception
     *         throws exception.
     */
    @Test
    public void shouldBlameCorrectCommitterAfterChangingLine() throws Exception {
        setGitUser(GIT_USER_NAME_1, GIT_USER_EMAIL_1);
        writeAndCommitFile(TEST_CLASS_FILE_NAME,
                "public class Test {\n"
                        + "\n"
                        + "     private String test1;\n"
                        + "\n"
                        + "     public Test() {\n"
                        + "         this.test1 = \"Test1\";\n"
                        + "     }\n"
                        + "\n"
                        + " }",
                "Add Test.java");

        setGitUser(GIT_USER_NAME_2, GIT_USER_EMAIL_2);
        writeAndCommitFile(TEST_CLASS_FILE_NAME, TEST_CLASS_FILE_CONTENT_1, "Add Test.java");
        writeAndCommitFile(WARNINGS_FILE_NAME, WARNINGS_FILE_CONTENT_1, "Add Warnings");

        FreeStyleProject project = initFreeStyleProject();

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        List<SourceControlRow> controlRows = getSourceControlRows(project, result);
        assertThat(controlRows).hasSize(1);
        validateRow(controlRows.get(0), GIT_USER_NAME_2, GIT_USER_EMAIL_2, TEST_CLASS_FILE_NAME);
    }

    /**
     * Should test the ability of blaming when build is out of git tree.
     *
     * @throws Exception
     *         throws exception.
     */
    @Test
    @Issue("JENKINS-57260")
    //FIXME not able to reproduce bug JENKINS-57260
    public void shouldBlameOutOfTreeBuildsWithFreeStyleProject() throws Exception {
        setGitUser(GIT_USER_NAME_1, GIT_USER_EMAIL_1);
        writeAndCommitFile(TEST_CLASS_FILE_NAME, TEST_CLASS_FILE_CONTENT_1, "Add Test.java");

        FreeStyleProject project = createFreeStyleProject();
        copySingleFileToWorkspace(project, "JavaWarnings.txt", "JavaWarnings.txt");
        project.setScm(new GitSCMBuilder(
                new SCMHead("master"),
                null,
                repository.toString(),
                null)
                .withExtension(new RelativeTargetDirectory("src"))
                .build());

        Java java = new Java();
        java.setPattern("JavaWarnings.txt");
        IssuesRecorder recorder = enableWarnings(project, java);
        recorder.setBlameDisabled(false);

        AnalysisResult result = scheduleSuccessfulBuild(project);

        List<SourceControlRow> controlRows = getSourceControlRows(project, result);
        assertThat(controlRows).hasSize(1);
        validateRow(controlRows.get(0), GIT_USER_NAME_1, GIT_USER_EMAIL_1, TEST_CLASS_FILE_NAME);

    }

    private FreeStyleProject initFreeStyleProject() throws Exception {
        FreeStyleProject project = createFreeStyleProject();
        project.setScm(new GitSCM(repository.toString()));

        Java java = new Java();
        java.setPattern(WARNINGS_FILE_NAME);
        enableWarnings(project, java);

        return project;
    }

    private List<SourceControlRow> getSourceControlRows(final FreeStyleProject project, final AnalysisResult result) {
        HtmlPage page = getWebPage(JavaScriptSupport.JS_ENABLED,
                project,
                result.getBuild().getNumber() + "/" + result.getId());
        return new SourceControlTable(page).getRows();
    }

    private void validateRow(final SourceControlRow row, final String name, final String email, final String fileName) {
        assertThat(row.getValue(SourceControlRow.AUTHOR)).isEqualTo(name);
        assertThat(row.getValue(SourceControlRow.EMAIL)).isEqualTo(email);
        assertThat(row.getValue(SourceControlRow.FILE)).contains(fileName);
    }

    private void setGitUser(final String userName, final String userEmail) throws Exception {
        repository.git("config", "user.name", userName);
        repository.git("config", "user.email", userEmail);
    }

    private void writeAndCommitFile(final String fileName, final String content, final String commitMsg)
            throws Exception {
        repository.write(fileName, content);
        repository.git("add", fileName);
        repository.git("commit", "-m", commitMsg);
    }

}

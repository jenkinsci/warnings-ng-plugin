package io.jenkins.plugins.analysis.core.scm;

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
import io.jenkins.plugins.analysis.core.model.ReportScanningTool;
import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.warnings.Java;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.SourceControlRow;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.SourceControlTable;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for the Git-Plugin.
 *
 * @author Matthias Herpers
 * @author Tanja Roithmeiergi
 */
public class GitITest extends IntegrationTestWithJenkinsPerSuite {

    private static final String FIRST_JAVA = "First.java";
    private static final String FIRST_COMMIT = "public class First{\n"
            + "public static void main(String... args){\n"
            + "System.out.println((String)\"Sowas\");\n"
            + "}\n\r"
            + "}\n\r";
    private static final String FIRST_WARNING = "[WARNING] First.java:[2,10] [deprecation] something is wrong. \n";
    private static final String FIRST_USER = "user1";
    private static final String FIRST_EMAIL = "user1@email";

    private static final String SECOND_COMMIT = "public class First{\n"
            + "public static void main(String... args){\n"
            + "System.out.println((String)\"Sowas\");\n"
            + "int i = -1;\n"
            + "}\n\r"
            + "}\n\r";
    private static final String SECOND_WARNING = "[WARNING] First.java:[4,10] [deprecation] something is wrong. \n";
    private static final String SECOND_USER = "user2";
    private static final String SECOND_EMAIL = "user2@email";

    /**
     * Repository is created for every test case.
     */
    @Rule
    public GitSampleRepoRule repo = new GitSampleRepoRule();

    /**
     * Covers [JENKINS-57260].
     *
     * @throws Exception
     *         comes from Git-Plugin.
     */
    @Test
    public void outOfTreeTestWithJava() throws Exception {
        repo.init();
        repo.write(FIRST_JAVA, FIRST_COMMIT);
        repo.write("issues.txt", FIRST_WARNING);
        repo.git("add", "issues.txt");
        repo.git("add", FIRST_JAVA);
        repo.git("config", "user.name", FIRST_USER);
        repo.git("config", "user.email", FIRST_EMAIL);
        repo.git("commit", "--message=user1_1");

        GitSCMBuilder builder = new GitSCMBuilder(new SCMHead("master"), null, repo.fileUrl(), null)
                .withExtension(new RelativeTargetDirectory("src"));

        FreeStyleProject project = createFreeStyleProject();
        project.setScm(builder.build());
        IssuesRecorder recorder = enableGenericWarnings(project, new Java());
        recorder.setBlameDisabled(false);
        buildWithResult(project, Result.SUCCESS);

        AnalysisResult result = scheduleSuccessfulBuild(project);

        assertThat(
                result.getInfoMessages().contains("Created no blame requests - Git blame will be skipped")).isFalse();

    }

    /**
     * Tests Blames without Html-page.
     *
     * @throws Exception
     *         comes from Git-Plugin.
     */
    @Test
    public void defaultWithoutHtmlPageTest() throws Exception {
        repo.init();
        repo.write(FIRST_JAVA, FIRST_COMMIT);
        repo.write("issues.txt", FIRST_WARNING);
        repo.git("add", "issues.txt");
        repo.git("add", FIRST_JAVA);
        repo.git("config", "user.name", FIRST_USER);
        repo.git("config", "user.email", FIRST_EMAIL);
        repo.git("commit", "--message=user1_1");

        FreeStyleProject project = buildWithGit(repo, new Java());
        AnalysisResult result = scheduleSuccessfulBuild(project);
        Blames blames = result.getBlames();
        String blamedFile = (String) blames.getFiles().toArray()[0];
        BlameRequest blameRequest = blames.get(blamedFile);
        assertThat(blameRequest).isNotNull();
        assertThat(blameRequest.getEmail(2)).isEqualTo(FIRST_EMAIL);
        assertThat(blameRequest.getFileName()).isEqualTo(FIRST_JAVA);
        assertThat(blameRequest.getName(2)).isEqualTo(FIRST_USER);
    }

    /**
     * Check if Blames work with one commit.
     *
     * @throws Exception
     *         comes from Git-Plugin.
     */
    @Test
    public void oneCommitterTest() throws Exception {
        repo.init();
        repo.write(FIRST_JAVA, FIRST_COMMIT);
        repo.write("issues.txt", FIRST_WARNING);
        repo.git("add", "issues.txt");
        repo.git("add", FIRST_JAVA);
        repo.git("config", "user.name", FIRST_USER);
        repo.git("config", "user.email", FIRST_EMAIL);
        repo.git("commit", "--message=user1_1");

        FreeStyleProject project = buildWithGit(repo, new Java());
        AnalysisResult result = scheduleSuccessfulBuild(project);
        int buildNr = result.getBuild().getNumber();
        String pluginId = result.getId();

        HtmlPage detailsPage = getWebPage(JavaScriptSupport.JS_ENABLED, project, buildNr + "/" + pluginId);
        SourceControlTable sourceControlTable = new SourceControlTable(detailsPage);

        List<SourceControlRow> sourceControlRows = sourceControlTable.getRows();
        assertThat(sourceControlRows.size()).isEqualTo(1);

        checkRow(sourceControlRows.get(0), FIRST_USER, FIRST_EMAIL, FIRST_JAVA);
    }

    /**
     * Check if Blames work with two commits from different Users.
     *
     * @throws Exception
     *         comes from Git-Plugin.
     */
    @Test
    public void twoCommitterTest() throws Exception {
        repo.init();
        repo.write(FIRST_JAVA, FIRST_COMMIT);
        repo.write("issues.txt", FIRST_WARNING);
        repo.git("add", "issues.txt");
        repo.git("add", FIRST_JAVA);
        repo.git("config", "user.name", FIRST_USER);
        repo.git("config", "user.email", FIRST_EMAIL);
        repo.git("commit", "--message=user1_1");

        repo.write(FIRST_JAVA, SECOND_COMMIT);
        repo.write("issues.txt", FIRST_WARNING + SECOND_WARNING);
        repo.git("add", "issues.txt");
        repo.git("add", FIRST_JAVA);
        repo.git("config", "user.name", SECOND_USER);
        repo.git("config", "user.email", SECOND_EMAIL);
        repo.git("commit", "--message=user2_1");

        FreeStyleProject project = buildWithGit(repo, new Java());
        AnalysisResult result = scheduleSuccessfulBuild(project);
        int buildNr = result.getBuild().getNumber();
        String pluginId = result.getId();

        HtmlPage detailsPage = getWebPage(JavaScriptSupport.JS_ENABLED, project, buildNr + "/" + pluginId);
        SourceControlTable sourceControlTable = new SourceControlTable(detailsPage);

        List<SourceControlRow> sourceControlRows = sourceControlTable.getRows();
        assertThat(sourceControlRows.size()).isEqualTo(2);

        checkRow(sourceControlRows.get(0), FIRST_USER, FIRST_EMAIL, FIRST_JAVA);
        checkRow(sourceControlRows.get(1), SECOND_USER, SECOND_EMAIL, FIRST_JAVA);
    }

    /**
     * Creates FreeStyleProject for git Repo.
     *
     * @param repository
     *         repository to use.
     * @param tool
     *         tool to use
     *
     * @return project.
     * @throws IOException
     *         comes from Git-Plugin.
     */
    private FreeStyleProject buildWithGit(final GitSampleRepoRule repository, final ReportScanningTool tool)
            throws IOException {
        GitSCMBuilder builder = new GitSCMBuilder(new SCMHead("master"),
                null, repository.fileUrl(), null);
        GitSCM git = builder.build();
        FreeStyleProject project = createFreeStyleProject();
        project.setScm(git);
        IssuesRecorder recorder = enableGenericWarnings(project, tool);
        recorder.setBlameDisabled(false);
        buildWithResult(project, Result.SUCCESS);
        return project;
    }

    /**
     * Checks a Blame (Row of the SourceControl).
     *
     * @param sourceControlRow
     *         Row to check.
     * @param checkAuthor
     *         expected author.
     * @param checkEmail
     *         expected email.
     * @param checkFile
     *         expected file.
     */
    private void checkRow(final SourceControlRow sourceControlRow, final String checkAuthor, final String checkEmail,
            final String checkFile) {
        assertThat(sourceControlRow.getValue(SourceControlRow.AUTHOR)).isEqualTo(checkAuthor);
        assertThat(sourceControlRow.getValue(SourceControlRow.EMAIL)).isEqualTo(checkEmail);
        assertThat(sourceControlRow.getValue(SourceControlRow.FILE)).contains(checkFile);
    }

}

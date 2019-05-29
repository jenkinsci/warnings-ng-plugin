package io.jenkins.plugins.analysis.core.scm;

import java.io.IOException;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.model.Run;
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

public class GitITest extends IntegrationTestWithJenkinsPerSuite {

    private static final String fileName = "First.java";
    private static final String firstCommit = "public class First{\n"
            + "public static void main(String... args){\n"
            + "System.out.println((String)\"Sowas\");\n"
            + "}\n\r"
            + "}\n\r";
    private static final String firstWarning = "[WARNING] First.java:[2,10] [deprecation] something is wrong. \n";
    private static final String firstUser = "user1";
    private static final String firstEmail = "user1@email";

    @Rule
    public GitSampleRepoRule repo = new GitSampleRepoRule();

    /**
     * Covers [JENKINS-57260].
     */
    @Test
    public void outOfTreeTestWithJava() throws Exception {
        repo.init();
        repo.write(fileName, firstCommit);
        repo.write("issues.txt", firstWarning);
        repo.git("add", "issues.txt");
        repo.git("add", fileName);
        repo.git("config", "user.name", firstUser);
        repo.git("config", "user.email", firstEmail);
        repo.git("commit", "--message=user1_1");

        GitSCMBuilder builder = new GitSCMBuilder(new SCMHead("master"), null, repo.fileUrl(), null)
                .withExtension(new RelativeTargetDirectory("src"));

        FreeStyleProject project = createFreeStyleProject();
        project.setScm(builder.build());
        IssuesRecorder recorder = enableGenericWarnings(project, new Java());
        recorder.setBlameDisabled(false);
        Run<?, ?> run = buildWithResult(project, Result.SUCCESS);

        AnalysisResult result = scheduleSuccessfulBuild(project);
        int buildNr = result.getBuild().getNumber();
        String pluginId = result.getId();

        HtmlPage detailsPage = getWebPage(JavaScriptSupport.JS_ENABLED, project, buildNr + "/" + pluginId);
        result.getInfoMessages().forEach(message -> System.out.println(message));
        assertThat(
                result.getInfoMessages().contains("Created no blame requests - Git blame will be skipped")).isFalse();

    }

    @Test
    public void defaultWithoutHtmlPageTest() throws Exception {
        repo.init();
        repo.write(fileName, firstCommit);
        repo.write("issues.txt", firstWarning);
        repo.git("add", "issues.txt");
        repo.git("add", fileName);
        repo.git("config", "user.name", firstUser);
        repo.git("config", "user.email", firstEmail);
        repo.git("commit", "--message=user1_1");

        FreeStyleProject project = buildWithGit(repo, new Java());
        AnalysisResult result = scheduleSuccessfulBuild(project);
        Blames blames = result.getBlames();
        String blamedFile = (String) blames.getFiles().toArray()[0];
        System.out.println(blamedFile);
        BlameRequest blameRequest = blames.get(blamedFile);
        assertThat(blameRequest).isNotNull();
        assertThat(blameRequest.getEmail(2)).isEqualTo(firstEmail);
        assertThat(blameRequest.getFileName()).isEqualTo(fileName);
        assertThat(blameRequest.getName(2)).isEqualTo(firstUser);
    }

    @Test
    public void oneCommitterTest() throws Exception {
        repo.init();
        repo.write(fileName, firstCommit);
        repo.write("issues.txt", firstWarning);
        repo.git("add", "issues.txt");
        repo.git("add", fileName);
        repo.git("config", "user.name", firstUser);
        repo.git("config", "user.email", firstEmail);
        repo.git("commit", "--message=user1_1");

        FreeStyleProject project = buildWithGit(repo, new Java());
        AnalysisResult result = scheduleSuccessfulBuild(project);
        int buildNr = result.getBuild().getNumber();
        String pluginId = result.getId();

        HtmlPage detailsPage = getWebPage(JavaScriptSupport.JS_ENABLED, project, buildNr + "/" + pluginId);
        SourceControlTable sourceControlTable = new SourceControlTable(detailsPage);

        List<SourceControlRow> sourceControlRows = sourceControlTable.getRows();
        assertThat(sourceControlRows.size()).isEqualTo(1);

        checkRow(sourceControlRows.get(0), firstUser, firstEmail, fileName);
    }

    private FreeStyleProject buildWithGit(GitSampleRepoRule repository, ReportScanningTool tool) throws IOException {
        GitSCMBuilder builder = new GitSCMBuilder(new SCMHead("master"),
                null, repository.fileUrl(), null);
        GitSCM git = builder.build();
        FreeStyleProject project = createFreeStyleProject();
        project.setScm(git);
        IssuesRecorder recorder = enableGenericWarnings(project, tool);
        recorder.setBlameDisabled(false);
        Run<?, ?> run = buildWithResult(project, Result.SUCCESS);
        return project;
    }

    private void checkRow(SourceControlRow sourceControlRow, String checkAuthor, String checkEmail, String checkFile) {
        assertThat(sourceControlRow.getValue(SourceControlRow.AUTHOR)).isEqualTo(checkAuthor);
        assertThat(sourceControlRow.getValue(SourceControlRow.EMAIL)).isEqualTo(checkEmail);
        assertThat(sourceControlRow.getValue(SourceControlRow.FILE)).contains(checkFile);
    }

}

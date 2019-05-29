package io.jenkins.plugins.analysis.core.scm;

import java.io.IOException;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.CreateFileBuilder;
import org.jvnet.hudson.test.Issue;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import hudson.model.FreeStyleProject;
import hudson.model.Project;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.extensions.impl.RelativeTargetDirectory;
import jenkins.plugins.git.GitSCMBuilder;
import jenkins.plugins.git.GitSampleRepoRule;
import jenkins.scm.api.SCMHead;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.warnings.Doxygen;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.SourceControlRow;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.SourceControlTable;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests the jenkins with git in its natural habitat.
 *
 * @author Michael Schmid, Raphael Furch
 */
public class RealGitITest extends IntegrationTestWithJenkinsPerSuite {

    /**
     * Rule to init a git repo.
     */
    @Rule
    public GitSampleRepoRule sampleRepo = new GitSampleRepoRule();

    private static final String FILE_NAME_1 = "CentralDifferenceSolver.cpp";
    private static final String FILE_NAME_2 = "LCPcalc.cpp";

    private static final String USER_NAME_1 = "User1";
    private static final String USER_NAME_2 = "User2";

    private static final String USER_EMAIL_1 = "user1@git.git";
    private static final String USER_EMAIL_2 = "user2@git.git";
    /**
     * Validates issue JENKINS-57260 with freestyle projects.
     */
    @Test
    @Issue("JENKINS-57260")
    public void gitBlameOnDifferentDirectoryIssue57260FreeStyleProject() {
        gitInitIssue57260();

        FreeStyleProject project = createFreeStyleProject();

        GitSCMBuilder builder = new GitSCMBuilder(new SCMHead("master"), null, sampleRepo.fileUrl(), null);
        RelativeTargetDirectory extension = new RelativeTargetDirectory("src"); // JENKINS-57260
        builder.withExtension(extension);
        setGitAtProject(project, builder.build());

        project.getBuildersList().add(new CreateFileBuilder("build/doxygen/doxygen/doxygen.log",
                "Notice: Output directory `build/doxygen/doxygen' does not exist. I have created it for you.\n"
                        + "src/CentralDifferenceSolver.cpp:11: Warning: reached end of file while inside a dot block!\n"
                        + "The command that should end the block seems to be missing!\n"
                        + " \n"
                        + "src/LCPcalc.cpp:12: Warning: the name `lcp_lexicolemke.c' supplied as the second argument in the \\file statement is not an input file"));

        IssuesRecorder recorder = enableWarnings(project,
                createTool(new Doxygen(), "build/doxygen/doxygen/doxygen.log"));
        recorder.setAggregatingResults(true);
        recorder.setEnabledForFailure(true);

        scheduleSuccessfulBuild(project);
        AnalysisResult result = scheduleSuccessfulBuild(project);

        assertThat(result.getErrorMessages()).doesNotContain(
                "Can't determine head commit using 'git rev-parse'. Skipping blame.");
    }

    /**
     * Validates issue JENKINS-57260 with pipeline.
     */
    @Test
    @Issue("JENKINS-57260")
    public void gitBlameOnDifferentDirectoryIssue57260Pipeline() {
        gitInitIssue57260();
        WorkflowJob project = createPipeline();

        project.setDefinition(new CpsFlowDefinition("pipeline {\n"
                + "agent any\n"
                + "options{\n"
                + "skipDefaultCheckout()\n"
                + "}\n"
                + "stages{\n"
                + "stage('Prepare') {\n"
                + "  steps {\n"
                + "    dir('src') {\n"
                + "      checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[url: '"
                + sampleRepo.fileUrl() + "']]])\n"
                + "    }\n"
                + "  }\n"
                + "}\n"
                + "stage('Doxygen') {\n"
                + "  steps {\n"
                + "    dir('build/doxygen') {\n"
                + "      writeFile file: 'doxygen/doxygen.log', text: '\"Notice: Output directory doc/doxygen/framework does not exist. I have created it for you.\\nsrc/CentralDifferenceSolver.cpp:11: Warning: reached end of file while inside a dot block!\\nThe command that should end the block seems to be missing!\\nsrc/LCPcalc.cpp:12: Warning: the name lcp_lexicolemke.c supplied as the second argument in the file statement is not an input file\"'\n"
                + "    }\n"
                + "    recordIssues(aggregatingResults: true, enabledForFailure: true, tools: [ doxygen(name: 'Doxygen', pattern: 'build/doxygen/doxygen/doxygen.log') ] )\n"
                + "  }\n"
                + "}\n"
                + "}}", false));

        scheduleSuccessfulBuild(project);
        AnalysisResult result = scheduleSuccessfulBuild(project);
        assertThat(result.getErrorMessages()).doesNotContain(
                "Can't determine head commit using 'git rev-parse'. Skipping blame.");

    }

    /**
     * Verify git blames of warnings from different users.
     */
    @Test
    public void gitBlameWithDifferentUsers() {

        gitInitTwoUser();

        FreeStyleProject project = createFreeStyleProject();
        GitSCMBuilder builder = new GitSCMBuilder(new SCMHead("master"), null, sampleRepo.fileUrl(), null);
        setGitAtProject(project, builder.build());

        project.getBuildersList().add(new CreateFileBuilder("doxygen.log",
                "Notice: Output directory `build/doxygen/doxygen' does not exist. I have created it for you.\n"
                        + "CentralDifferenceSolver.cpp:4: Warning: reached end of file while inside a dot block!\n"
                        + "CentralDifferenceSolver.cpp:11: Warning: reached end of file while inside a dot block!\n"
                        + "The command that should end the block seems to be missing!\n"
                        + " \n"
                        + "LCPcalc.cpp:5: Warning: the name `lcp_lexicolemke.c' supplied as the second argument in the \\file statement is not an input file"));

        enableWarnings(project, createTool(new Doxygen(), "doxygen.log"));

        scheduleSuccessfulBuild(project);
        AnalysisResult result = scheduleSuccessfulBuild(project);

        Blames blames = result.getBlames();
        String firstFile = result.getBlames().getFiles()
                .stream().filter(name -> name.endsWith(FILE_NAME_1))
                .findFirst().orElseThrow(() -> new RuntimeException("CentralDifferenceSolver.cpp not found in blames"));
        String secondFile = result.getBlames().getFiles()
                .stream().filter(name -> name.endsWith(FILE_NAME_2))
                .findFirst().orElseThrow(() -> new RuntimeException("LCPcalc.cpp not found in blames"));

        assertThat(blames.get(firstFile).getName(4)).isEqualTo(USER_NAME_1);
        assertThat(blames.get(firstFile).getEmail(4)).isEqualTo(USER_EMAIL_1);
        assertThat(blames.get(firstFile).getName(11)).isEqualTo(USER_NAME_2);
        assertThat(blames.get(firstFile).getEmail(11)).isEqualTo(USER_EMAIL_2);
        assertThat(blames.get(secondFile).getName(5)).isEqualTo(USER_NAME_1);
        assertThat(blames.get(secondFile).getEmail(5)).isEqualTo(USER_EMAIL_1);
    }

    /**
     * Verify git blames of warnings from different users via gui.
     */
    @Test
    public void gitBlameWithDifferentUsersViaGui() {
        gitInitTwoUser();

        FreeStyleProject project = createFreeStyleProject();
        AnalysisResult result = scheduleSuccessfulBuild(project);
        int buildNr = result.getBuild().getNumber();
        String pluginId = result.getId();

        HtmlPage detailsPage = getWebPage(JavaScriptSupport.JS_ENABLED, project, buildNr + "/" + pluginId);
        SourceControlTable sourceControlTable = new SourceControlTable(detailsPage);

        List<SourceControlRow> sourceControlRows = sourceControlTable.getRows();
        assertThat(sourceControlRows.size()).isEqualTo(1);

        assertThat(sourceControlRows.get(0).getValue(SourceControlRow.AUTHOR)).isEqualTo(USER_NAME_1);
        assertThat(sourceControlRows.get(0).getValue(SourceControlRow.EMAIL)).isEqualTo(USER_EMAIL_1);
        assertThat(sourceControlRows.get(0).getValue(SourceControlRow.FILE)).contains(FILE_NAME_1);

        assertThat(sourceControlRows.get(1).getValue(SourceControlRow.AUTHOR)).isEqualTo(USER_NAME_2);
        assertThat(sourceControlRows.get(1).getValue(SourceControlRow.EMAIL)).isEqualTo(USER_EMAIL_2);
        assertThat(sourceControlRows.get(1).getValue(SourceControlRow.FILE)).contains(FILE_NAME_2);
    }
    private void gitInitIssue57260() {
        try {
            sampleRepo.init();
            sampleRepo.write(FILE_NAME_1, "1\n2\n3\n4\n5\n6\n7\n8\n9\n10\n11\n12");
            sampleRepo.write(FILE_NAME_2, "1\n2\n3\n4\n5\n6\n7\n8\n9\n10\n11\n12\n13");
            sampleRepo.git("add", FILE_NAME_1, FILE_NAME_2);
            sampleRepo.git("commit", "--all", "--message=init");
        }
        catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }


    private void gitInitTwoUser() {
        try {
            sampleRepo.init();
            sampleRepo.write(FILE_NAME_1, "1\n2\n3\n4\n5\n6");
            sampleRepo.write(FILE_NAME_2, "1\n2\n3\n4\n5\n6\n7");
            sampleRepo.git("add", FILE_NAME_1, FILE_NAME_2);
            sampleRepo.git("commit", "--author=\"" + USER_NAME_1 + " <" + USER_EMAIL_1 + ">\"", "--all", "--message=\"commit from user1\"");
            sampleRepo.write(FILE_NAME_1, "1\n2\n3\n4\n5\n6\n7\n8\n9\n10\n11\n12");
            sampleRepo.write(FILE_NAME_2, "1\n2\n3\n4\n5\n6\n7\n8\n9\n10\n11\n12\n13");
            sampleRepo.git("add", FILE_NAME_1, FILE_NAME_2);
            sampleRepo.git("commit", "--author=\"" + USER_NAME_2 + " <" + USER_EMAIL_2 + ">\"", "--all", "--message=\"commit from user2\"");
        }
        catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private void setGitAtProject(final Project project, final GitSCM git) {
        try {
            project.setScm(git);
        }
        catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }
}

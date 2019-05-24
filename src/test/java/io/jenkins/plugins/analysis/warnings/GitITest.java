package io.jenkins.plugins.analysis.warnings;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.plugins.git.GitSCM;
import jenkins.plugins.git.GitSampleRepoRule;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerTest;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.SourceControlRow;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.SourceControlTable;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

/**
 * @author Colin Kaschel
 * @author Nils Engelbrecht
 */
public class GitITest extends IntegrationTestWithJenkinsPerTest {

    @ClassRule
    public static GitSampleRepoRule repository = new GitSampleRepoRule();

    private static final String TEST_CLASS_FILE_NAME = "Test.java";
    private static final String WARNINGS_FILE_NAME = "Warnings.txt";

    @BeforeClass
    public static void initGit() throws Exception {
        repository.init();
        repository.git("checkout", "master");
    }

    @Test
    public void shouldBlameUser() throws Exception {
        setGitUser("GitUser 1", "gituser1@email.com");
        writeAndCommitFile(TEST_CLASS_FILE_NAME,
                "public class Test {\n"
                        + "\n"
                        + "     private String test1;\n"
                        + "\n"
                        + "     public Test() {\n"
                        + "         this.test1 = (String) \"Test1\";\n"
                        + "     }\n"
                        + "\n"
                        + " }",
                "Add Test.java");

        writeAndCommitFile(WARNINGS_FILE_NAME,
                "Test.java:6: warning: [cast] redundant cast to String",
                "Add Warnings");

        FreeStyleProject project = initFreeStyleProject();
        final String workspacePath = project.getParent().getRootDir().getAbsolutePath() + "/workspace/test0/";

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(result.getBlames().get(workspacePath + TEST_CLASS_FILE_NAME).getEmail(6)).isEqualTo(
                "gituser1@email.com");

        SourceControlTable sourceControlTable = new SourceControlTable(
                getWebPage(JavaScriptSupport.JS_ENABLED, result));
        String email = sourceControlTable.getRows().get(0).getValue(SourceControlRow.EMAIL);
        assertThat(email).isEqualTo("gituser1@email.com");
    }

    @Test
    public void shouldBlameDifferentUser() throws Exception {
        setGitUser("GitUser 1", "gituser1@email.com");
        writeAndCommitFile(TEST_CLASS_FILE_NAME,
                "public class Test {\n"
                        + "\n"
                        + "     private String test1;\n"
                        + "\n"
                        + "     public Test() {\n"
                        + "         this.test1 = (String) \"Test1\";\n"
                        + "     }\n"
                        + "\n"
                        + " }",
                "Add Test.java");

        setGitUser("GitUser 2", "gituser2@email.com");
        writeAndCommitFile(TEST_CLASS_FILE_NAME,
                "public class Test {\n"
                        + "\n"
                        + "     private String test1;\n"
                        + "     private String test2;\n"
                        + "\n"
                        + "     public Test() {\n"
                        + "         this.test1 = (String) \"Test1\";\n"
                        + "         this.test2 = (String) \"Test2\";\n"
                        + "     }\n"
                        + "\n"
                        + " }",
                "Add Test.java");

        writeAndCommitFile(WARNINGS_FILE_NAME,
                "Test.java:7: warning: [cast] redundant cast to String\n"
                        + "Test.java:8: warning: [cast] redundant cast to String",
                "Add Warnings");

        FreeStyleProject project = initFreeStyleProject();
        final String workspacePath = project.getParent().getRootDir().getAbsolutePath() + "/workspace/test0/";

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(result.getBlames().get(workspacePath + TEST_CLASS_FILE_NAME).getEmail(7)).isEqualTo(
                "gituser1@email.com");
        assertThat(result.getBlames().get(workspacePath + TEST_CLASS_FILE_NAME).getEmail(8)).isEqualTo(
                "gituser2@email.com");
    }

    @Test
    public void shouldBlameCorrectUser() throws Exception {
        setGitUser("GitUser 1", "gituser1@email.com");
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

        setGitUser("GitUser 2", "gituser2@email.com");
        writeAndCommitFile(TEST_CLASS_FILE_NAME,
                "public class Test {\n"
                        + "\n"
                        + "     private String test1;\n"
                        + "\n"
                        + "     public Test() {\n"
                        + "         this.test1 = (String) \"Test1\";\n"
                        + "     }\n"
                        + "\n"
                        + " }",
                "Add Test.java");

        writeAndCommitFile(WARNINGS_FILE_NAME,
                "Test.java:6: warning: [cast] redundant cast to String\n",
                "Add Warnings");

        FreeStyleProject project = initFreeStyleProject();
        final String workspacePath = project.getParent().getRootDir().getAbsolutePath() + "/workspace/test0/";

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(result.getBlames().get(workspacePath + TEST_CLASS_FILE_NAME).getEmail(6)).isEqualTo(
                "gituser2@email.com");
    }

    //TODO finish test
    @Test
    public void shouldBlameOutOfTreeBuilds() throws Exception {
        setGitUser("GitUser 1", "gituser1@email.com");
        writeAndCommitFile(TEST_CLASS_FILE_NAME,
                "public class Test {\n"
                        + "\n"
                        + "     private String test1;\n"
                        + "\n"
                        + "     public Test() {\n"
                        + "         this.test1 = (String) \"Test1\";\n"
                        + "     }\n"
                        + "\n"
                        + " }",
                "Add Test.java");

        writeAndCommitFile(WARNINGS_FILE_NAME,
                "Test.java:6: warning: [cast] redundant cast to String",
                "Add Warnings");
        WorkflowJob job = createPipeline();
        final String workspacePath = job.getParent().getRootDir().getAbsolutePath() + "/workspace/test0/";
        // FIXME checkout scm does not work
        job.setDefinition(new CpsFlowDefinition(
                "node {\n"
                        + "     stage ('Git') {\n"
                        + "         git branch:'master', url: '" + repository.toString() + "'\n"
                        + "     }\n"
                        + "     stage ('Checkout'){\n"
                        + "         dir('src'){\n"
                        + "             checkout scm\n"
                        + "             recordIssues tool: java(pattern: 'Warnings.txt')\n"
                        + "         }\n"
                        + "     }\n"
                        + "}",
                true));


//        AnalysisResult result = scheduleSuccessfulBuild(job);
    }

    private FreeStyleProject initFreeStyleProject() throws Exception {
        FreeStyleProject project = createFreeStyleProject();
        project.setScm(new GitSCM(repository.toString()));

        Java java = new Java();
        java.setPattern(WARNINGS_FILE_NAME);
        enableWarnings(project, java);

        return project;
    }

    private void setGitUser(String userName, String userEmail) throws Exception {
        repository.git("config", "user.name", userEmail);
        repository.git("config", "user.email", userEmail);
    }

    private void writeAndCommitFile(String fileName, String content, String commitMsg) throws Exception {
        repository.write(fileName, content);
        repository.git("add", fileName);
        repository.git("commit", "-m", commitMsg);
    }

}

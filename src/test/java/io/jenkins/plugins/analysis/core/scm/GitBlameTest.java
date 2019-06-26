package io.jenkins.plugins.analysis.core.scm;

import java.io.IOException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.CreateFileBuilder;
import org.jvnet.hudson.test.Issue;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.plugins.git.GitSCM;
import jenkins.plugins.git.GitSampleRepoRule;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerTest;
import io.jenkins.plugins.analysis.warnings.Java;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for the Git Blame Functionality.
 *
 * @author Artem Polovyi
 */
public class GitBlameTest extends IntegrationTestWithJenkinsPerTest {
    private static final String FILE = "Test.java";
    private static final String JAVA_WARNING_FILE = "issues.txt";
    private static final String WARNING_01 = "[WARNING] Test.java:[1,0] [deprecation] 1 something has been deprecated\n";
    private static final String WARNING_02 = "[WARNING] Test.java:[2,0] [deprecation] 2 something has been deprecated\n";

    private static final String CONTENT_01 = "public class First{\n int i; \n}";
    private static final String CONTENT_02 = "public class First{\n int j; \n}";

    private static final String COMMIT_MESSAGE_01 = "first commit";
    private static final String COMMIT_MESSAGE_02 = "second commit";

    private static final String USER_01 = "user01";
    private static final String USER_02 = "user02";

    private static final String EMAIL_01 = "user01@mail.com";
    private static final String EMAIL_02 = "user02@mail.com";
    /**
     * Git repository class for test cases.
     */
    @Rule
    public GitSampleRepoRule repository = new GitSampleRepoRule();

    /**
     * Initializes the git repository.
     *
     */
    @Before
    public void initRepository() {
        try {
            repository.init();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Makes sure blame does not shown if there is no warning to blame user for.
     *
     * @throws IOException
     *         when adding a SCM failed.
     */
    @Test
    public void shouldNotBlameTest() throws IOException {
        try {
            createAndCommitFileByUser(FILE, CONTENT_01, COMMIT_MESSAGE_01, USER_01, EMAIL_01);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        FreeStyleProject project = createFreeStyleProject();
        project.setScm(new GitSCM(repository.fileUrl()));

        enableGenericWarnings(project, new Java());

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        assertThat(result.getBlames().isEmpty());
    }

    /**
     * Tests blame functionality for single commiter with a warning.
     *
     * @throws IOException
     *         when adding a SCM failed.
     */
    @Test
    public void shouldBlameSingleUserTest() throws IOException {
        try {
            createAndCommitFileByUser(FILE, CONTENT_01, COMMIT_MESSAGE_01, USER_01, EMAIL_01);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        FreeStyleProject project = createFreeStyleProject();
        project.setScm(new GitSCM(repository.fileUrl()));
        project.getBuildersList().add(new CreateFileBuilder(FILE, WARNING_01));

        enableGenericWarnings(project, new Java());

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        assertThat(result.getBlames().getFiles().size()).isEqualTo(1);
        assertThat(result.getBlames().getFiles().iterator().next()).contains(FILE);
        assertThat(result.getBlames().getRequests()).hasSize(1);
        verifyBlamer(result.getBlames().getRequests().iterator().next(), 1, USER_01, EMAIL_01);
    }

    /**
     * Tests blame functionality for two commiters with several warning.
     *
     * @throws IOException
     *         when adding a SCM failed.
     */
    @Test
    public void shouldBlameMultipleUsersTest() throws IOException {
        try {
            createAndCommitFileByUser(FILE, CONTENT_01, COMMIT_MESSAGE_01, USER_01, EMAIL_01);
            createAndCommitFileByUser(FILE, CONTENT_02, COMMIT_MESSAGE_02, USER_02, EMAIL_02);
        }
        catch (Exception e) {
            e.printStackTrace();
        }


        FreeStyleProject project = createFreeStyleProject();
        project.setScm(new GitSCM(repository.fileUrl()));
        project.getBuildersList().add(new CreateFileBuilder(JAVA_WARNING_FILE, WARNING_01 + WARNING_02));

        enableGenericWarnings(project, new Java());

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        assertThat(result.getBlames().getFiles().size()).isEqualTo(1);
        assertThat(result.getBlames().getFiles().iterator().next()).contains(FILE);

        assertThat(result.getBlames().getRequests()).hasSize(1);
        BlameRequest blameRequest = result.getBlames().getRequests().iterator().next();
        verifyBlamer(blameRequest, 1, USER_01, EMAIL_01);
        verifyBlamer(blameRequest, 2, USER_02, EMAIL_02);
    }

    private void verifyBlamer(final BlameRequest request, final int line, final String user,
            final String email) {
        assertThat(request.getName(line)).isEqualTo(user);
        assertThat(request.getEmail(line)).isEqualTo(email);
    }

    /**
     * Blame should work in builds out of three. Verifies the issue JENKINS-57260.
     * (Fails since the issue haven't been fixed yet)
     *
     */
    @Issue("JENKINS-57260")
    @Test
    public void shouldBlameInOutOfTreeBuilds()  {
        try {
            createAndCommitFileByUser(FILE, WARNING_01, COMMIT_MESSAGE_01, USER_01, EMAIL_01);
            createAndCommitFileByUser(FILE, WARNING_02, COMMIT_MESSAGE_02, USER_02, EMAIL_02);
        }
        catch (Exception e) {
            e.printStackTrace();
        }


        WorkflowJob job = createPipeline();
        job.setDefinition(new CpsFlowDefinition("pipeline {\n"
                + "agent any\n"
                + "options{\n"
                + "skipDefaultCheckout()\n"
                + "}\n"
                + "stages{\n"
                + "stage('Prepare') {\n"
                + "  steps {\n"
                + "    dir('src') {\n"
                + "      checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[url: '"
                + repository.fileUrl() + "']]])\n"
                + "    }\n"
                + "  }\n"
                + "}\n"
                + "stage('Doxygen') {\n"
                + "  steps {\n"
                + "    dir('build/doxygen/doxygen') {\n"
                + "      writeFile file: 'doxygen.log', text:'''src/Test.java:4: Warning: some warning.'''\n"
                + "    }\n"
                + "    recordIssues(aggregatingResults: true, enabledForFailure: true, tools: [ doxygen(name: 'Doxygen', pattern: 'build/doxygen/doxygen/doxygen.log') ] )\n"
                + "  }\n"
                + "}\n"
                + "}}", false));

        scheduleBuildAndAssertStatus(job, Result.SUCCESS);
        AnalysisResult result = scheduleBuildAndAssertStatus(job, Result.SUCCESS);

        assertThat(result.getErrorMessages()).doesNotContain(
                "Can't determine head commit using 'git rev-parse'. Skipping blame.");
    }

    private void createAndCommitFileByUser(
            final String file,
            final String content,
            final String commitMessage,
            final String name,
            final String email) throws Exception {
        repository.git("config", "user.name", name);
        repository.git("config", "user.email", email);
        repository.write(file, content);
        repository.git("add", file);
        repository.git("commit", "-m", commitMessage);
    }
}

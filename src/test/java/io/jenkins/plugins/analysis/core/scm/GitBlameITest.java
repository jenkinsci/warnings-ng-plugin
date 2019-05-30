package io.jenkins.plugins.analysis.core.scm;

import java.util.List;

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
import hudson.plugins.git.extensions.impl.RelativeTargetDirectory;
import jenkins.plugins.git.GitSCMBuilder;
import jenkins.plugins.git.GitSampleRepoRule;
import jenkins.scm.api.SCMHead;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.warnings.Doxygen;
import io.jenkins.plugins.analysis.warnings.Java;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.DetailsTab;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.SourceControlRow;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.SourceControlTable;

import static io.jenkins.plugins.analysis.core.testutil.IntegrationTest.JavaScriptSupport.*;
import static io.jenkins.plugins.analysis.warnings.recorder.pageobj.DetailsTab.TabType.*;
import static io.jenkins.plugins.analysis.warnings.recorder.pageobj.SourceControlRow.*;
import static org.assertj.core.api.Assertions.*;

/**
 * Tests the git blame functionality within the git-plugin.
 *
 * @author Veronika Zwickenpflug
 * @author Florian Hageneder
 */
public class GitBlameITest extends IntegrationTestWithJenkinsPerSuite {

    private static final String BRANCH = "master";

    private static final String USER_1 = "Alice";
    private static final String USER_2 = "Wolfgang";

    private static final String EMAIL_1 = "alice@mail.com";
    private static final String EMAIL_2 = "wolfgang@mail.com";

    /**
     * Local git integration for testing purposes.
     */
    @Rule
    public GitSampleRepoRule repository = new GitSampleRepoRule();

    /**
     * Initializes the git repository.
     *
     * @throws Exception
     *         When git interaction fails.
     */
    @Before
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    public void init() throws Exception {
        repository.init();
        repository.git("checkout", BRANCH);
    }

    /**
     * Checks if nowone is blames if there is only a single file changed by one committer without an issues.
     *
     * @throws Exception
     *         When initializing git fails.
     */
    @Test
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    public void testNothingToBlame() throws Exception {
        addFileToGit("NoOne", "noOne@example.com", "This never happens",
                "impossible.txt", "Init impossible file");

        FreeStyleProject job = createFreeStyleProject();
        job.setScm(new GitSCM(repository.fileUrl()));
        enableGenericWarnings(job, new Java());

        AnalysisResult result = scheduleBuildAndAssertStatus(job, Result.SUCCESS);
        assertThat(result.getBlames().isEmpty());
    }

    /**
     * Creates a repository with a single file that is changed by one committer. Afterwards the plugin has to record
     * correct blame information for arbitrary issues.
     *
     * @throws Exception
     *         When initializing git fails.
     */
    @Test
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    public void testBlameSingleUser() throws Exception {
        String file = "deprecated.txt";
        addFileToGit(USER_1, EMAIL_1, "@Deprecated \n public void test () { }\n", file, "init deprecated");

        FreeStyleProject job = createFreeStyleProject();
        job.setScm(new GitSCM(repository.fileUrl()));

        job.getBuildersList().add(new CreateFileBuilder("issues.txt",
                "[WARNING] deprecated.txt:[1,0] [deprecation] something has been deprecated"));

        enableGenericWarnings(job, new Java());

        AnalysisResult result = scheduleBuildAndAssertStatus(job, Result.SUCCESS);
        assertThat(result.getBlames().getFiles().size()).isEqualTo(1);
        assertThat(result.getBlames().getFiles().iterator().next()).contains(file);
        assertThat(result.getBlames().getRequests()).hasSize(1);
        verifyBlameRequest(result.getBlames().getRequests().iterator().next(), 1, USER_1, EMAIL_1);

    }

    /**
     * Creates a repository with a single file that is changed by two committer. Afterwards the plugin has to record
     * correct blame information for arbitrary issues.
     *
     * @throws Exception
     *         When any git command fails
     */
    @Test
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    public void testBlame2Users() throws Exception {
        String file = "opentasks.txt";
        String commit1 = "init opentasks";
        String commit2 = "update opentasks";
        addFileToGit(USER_1, EMAIL_1, "Line 1\nLine 2\n", file, commit1);
        addFileToGit(USER_2, EMAIL_2, "Line 1\nLine 2 but better\n", file, commit2);

        FreeStyleProject job = createFreeStyleProject();
        job.setScm(new GitSCM(repository.fileUrl()));

        job.getBuildersList().add(new CreateFileBuilder("issues.txt",
                "[WARNING] opentasks.txt:[1,0] [deprecation] something has been deprecated\n"
                        + "[WARNING] opentasks.txt:[2,0] [deprecation] something else has been deprecated too\n"));

        enableGenericWarnings(job, new Java());

        AnalysisResult result = scheduleBuildAndAssertStatus(job, Result.SUCCESS);
        assertThat(result.getBlames().getFiles().size()).isEqualTo(1);
        assertThat(result.getBlames().getFiles().iterator().next()).contains(file);

        assertThat(result.getBlames().getRequests()).hasSize(1);
        BlameRequest blameRequest = result.getBlames().getRequests().iterator().next();
        verifyBlameRequest(blameRequest, 1, USER_1, EMAIL_1);
        verifyBlameRequest(blameRequest, 2, USER_2, EMAIL_2);
    }

    /**
     * Creates a repository with a single file that is changed by two committer. If the blame is correctly is checked
     * with the gui.
     *
     * @throws Exception
     *         When any git command fails
     */
    @Test
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    public void testBlame2UsersGUI() throws Exception {
        String file = "opentasks.txt";
        addFileToGit(USER_1, EMAIL_1, "Line 1\nLine 2\n", file, "init opentasks");
        addFileToGit(USER_2, EMAIL_2, "Line 1\nLine 2 but better\n", file, "update opentasks");

        FreeStyleProject job = createFreeStyleProject();
        job.setScm(new GitSCM(repository.fileUrl()));

        job.getBuildersList().add(new CreateFileBuilder("issues.txt",
                "[WARNING] opentasks.txt:[1,0] [deprecation] something has been deprecated\n"
                        + "[WARNING] opentasks.txt:[2,0] [deprecation] something else has been deprecated too\n"));

        enableGenericWarnings(job, new Java());

        AnalysisResult result = scheduleBuildAndAssertStatus(job, Result.SUCCESS);

        SourceControlTable blames = new DetailsTab(getWebPage(JS_ENABLED, result)).select(BLAMES);

        assertThat(result.getErrorMessages()).isEmpty();
        assertThat(result.getInfoMessages()).contains("-> found 2 issues (skipped 0 duplicates)",
                "-> blamed authors of issues in 1 files");
        List<SourceControlRow> rows = blames.getRows();
        verifySourceControlRow(rows.get(0), USER_1, EMAIL_1, "something has been deprecated");
        verifySourceControlRow(rows.get(1), USER_2, EMAIL_2, "something else has been deprecated too");
    }

    /**
     * Creates an repository with an out of tree build and checks if git blame works correctly, using a freestylejob.
     * Source files are contained in "src"-folder, while build files are located in the "build"-folder.
     *
     * @throws Exception
     *         When any git command fails
     * @see <a href="https://issues.jenkins-ci.org/browse/JENKINS-57260">Issue 57260</a>
     */
    @Test
    @Issue("JENKINS-57260")
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    public void testOutOfTreeBuildFreeStyle() throws Exception {
        initGitWithIssue57260();

        GitSCMBuilder builder = new GitSCMBuilder(new SCMHead("master"), null, repository.fileUrl(), null);
        RelativeTargetDirectory sourceDirectory = new RelativeTargetDirectory("src");
        builder.withExtension(sourceDirectory);
        GitSCM gitSCM = builder.build();

        FreeStyleProject job = createFreeStyleProject();
        job.setScm(gitSCM);
        enableGenericWarnings(job, createTool(new Doxygen(), "build/doxygen/doxygen/doxygen.log"));

        job.getBuildersList().add(new CreateFileBuilder(
                "build/doxygen/doxygen/doxygen.log",
                "src/SomeFile.cpp:11: Warning: something was wrong here.\n"
                        + "src/SomeOtherFile.cpp: Error: Something is broke here!"));

        scheduleBuildAndAssertStatus(job, Result.SUCCESS);
        AnalysisResult result = scheduleBuildAndAssertStatus(job, Result.SUCCESS);
        assertThat(result.getErrorMessages()).doesNotContain(
                "Can't determine head commit using 'git rev-parse'. Skipping blame.");
    }

    /**
     * Creates an repository with an out of tree build and checks if git blame works correctly, using a pipeline. Source
     * files are contained in "src"-folder, while build files are located in the "build"-folder.
     *
     * @throws Exception
     *         When any git command fails
     * @see <a href="https://issues.jenkins-ci.org/browse/JENKINS-57260">Issue 57260</a>
     */
    @Test
    @Issue("JENKINS-57260")
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    public void testOutOfTreeBuildPipeline() throws Exception {
        initGitWithIssue57260();
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
                + "      writeFile file: 'doxygen.log', text:'''src/SomeFile.cpp:11: Warning: something was wrong here.\nsrc/SomeOtherFile.cpp: Error: Something is broke here!\n'''\n"
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

    /**
     * Initializes the git repository with files to recreate the Issue 57260.
     *
     * @throws Exception
     *         When any git command fails
     */
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    private void initGitWithIssue57260() throws Exception {
        addFileToGit("Alice", "alice@example.com",
                "Line 1\nLine 2\n", "SomeFile.cpp", "Created some file");
        addFileToGit("Bob", "bob@example.com",
                "Line 1\nLine 2 but better\n", "SomeOtherFile.cpp", "Created some other file");
    }

    /**
     * Check whether the given SourceControlRow has the given author, email and detailsContent.
     *
     * @param row
     *         SourceControlRow to be checked
     * @param author
     *         Author that is asserted
     * @param email
     *         Email that is asserted
     * @param detailsContent
     *         Details content that is asserted
     */
    private void verifySourceControlRow(final SourceControlRow row, final String author, final String email,
            final String detailsContent) {
        assertThat(row.getValue(AUTHOR)).isEqualTo(author);
        assertThat(row.getValue(EMAIL)).isEqualTo(email);
        assertThat(row.getValue(DETAILS_CONTENT)).isEqualTo(detailsContent);
    }

    /**
     * Verifies if the given blame request has the expected values.
     *
     * @param request
     *         Blame request that should be verified
     * @param line
     *         Line that should be blamed
     * @param user
     *         User that should have written this line
     * @param email
     *         Mail of the user
     */
    private void verifyBlameRequest(final BlameRequest request, final int line, final String user,
            final String email) {
        assertThat(request.getName(line)).isEqualTo(user);
        assertThat(request.getEmail(line)).isEqualTo(email);
    }

    /**
     * Adds file with specified text to git using the given user information and commit message.
     *
     * @param name
     *         Name of the user for the commit
     * @param email
     *         Email of the user for the commit
     * @param text
     *         Text that should be written to file
     * @param file
     *         Local file path
     * @param msg
     *         Commit message
     *
     * @throws Exception
     *         When any git command fails
     */
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    private void addFileToGit(final String name, final String email, final String text, final String file,
            final String msg) throws Exception {
        repository.git("config", "user.name", name);
        repository.git("config", "user.email", email);
        repository.write(file, text);
        repository.git("add", file);
        repository.git("commit", "-m", msg);
    }
}

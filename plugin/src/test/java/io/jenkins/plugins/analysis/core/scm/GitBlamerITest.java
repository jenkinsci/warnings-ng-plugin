package io.jenkins.plugins.analysis.core.scm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.CreateFileBuilder;
import org.jvnet.hudson.test.Issue;

import org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import hudson.model.FreeStyleProject;
import hudson.model.Run;
import hudson.plugins.git.GitSCM;
import jenkins.plugins.git.GitSampleRepoRule;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.BlamesModel.BlamesRow;
import io.jenkins.plugins.analysis.core.model.IssuesDetail;
import io.jenkins.plugins.analysis.core.model.ResultAction;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerTest;
import io.jenkins.plugins.analysis.warnings.Java;
import io.jenkins.plugins.datatables.TableColumn;
import io.jenkins.plugins.datatables.TableModel;
import io.jenkins.plugins.forensics.blame.Blamer;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

/**
 * Tests the {@link Blamer GitBlamer} in several jobs that uses a real Git repository.
 *
 * @author Fabian Janker
 * @author Andreas Pabst
 */
@SuppressWarnings("PMD.SignatureDeclareThrowsException")
public class GitBlamerITest extends IntegrationTestWithJenkinsPerTest {
    private static final String DETAILS = "Details";
    private static final String FILE = "File";
    private static final String AGE = "Age";
    private static final String AUTHOR = "Author";
    private static final String EMAIL = "Email";
    private static final String COMMIT = "Commit";
    private static final String ADDED = "Added";

    /** The Git repository for the test. */
    @Rule
    public GitSampleRepoRule gitRepo = new GitSampleRepoRule();

    /**
     * Verifies that a pipeline with one issue will be correctly blamed.
     *
     * @throws Exception
     *         if there is a problem with the git repository
     */
    @Test
    public void shouldBlameOneIssueWithPipeline() throws Exception {
        WorkflowJob job = createJob("");

        Run<?, ?> build = buildSuccessfully(job);

        AnalysisResult result = scheduleSuccessfulBuild(job);
        assertSuccessfulBlame(result, 1, 1);

        TableModel table = getBlamesTableModel(build);
        assertOneIssue(gitRepo.head(), table);
    }

    /**
     * Verifies that the repository blamer can be disabled.
     *
     * @throws Exception
     *         if there is a problem with the git repository
     */
    @Test
    public void shouldDisableBlames() throws Exception {
        WorkflowJob job = createJob("blameDisabled: 'true', ");

        AnalysisResult result = scheduleSuccessfulBuild(job);

        assertThat(result.getBlames().getFiles()).isEmpty();
        assertThat(result.getInfoMessages()).contains("Skipping SCM blames as requested");
    }

    private WorkflowJob createJob(final String disableBlamesParameter) throws Exception {
        gitRepo.init();

        createAndCommitFile("Jenkinsfile", "node {\n"
                + "  stage ('Checkout') {\n"
                + "    checkout scm\n"
                + "  }\n"
                + "  stage ('Build and Analysis') {"
                + "    echo '[javac] Test.java:1: warning: Test Warning for Jenkins'\n"
                + "    recordIssues " + disableBlamesParameter + "tools: [java()]\n"
                + "  }\n"
                + "}");
        createAndCommitFile("Test.java", "public class Test {}");

        WorkflowJob project = createPipeline();
        project.setDefinition(new CpsScmFlowDefinition(new GitSCM(gitRepo.toString()), "Jenkinsfile"));
        return project;
    }

    /**
     * Verifies that a freestyle job with one issue will be correctly blamed.
     *
     * @throws Exception
     *         if there is a problem with the git repository
     */
    @Test
    public void shouldBlameOneIssueWithFreestyle() throws Exception {
        gitRepo.init();
        createAndCommitFile("Test.java", "public class Test {}");

        String testCommit = gitRepo.head();

        FreeStyleProject project = createFreeStyleProject();
        project.setScm(new GitSCM(gitRepo.toString()));
        project.getBuildersList()
                .add(new CreateFileBuilder("warnings.txt", "[javac] Test.java:1: warning: Test Warning for Jenkins"));

        Java javaJob = new Java();
        javaJob.setPattern("warnings.txt");
        enableWarnings(project, javaJob);

        AnalysisResult result = scheduleSuccessfulBuild(project);
        assertSuccessfulBlame(result, 1, 1);

        Run<?, ?> build = buildSuccessfully(project);

        TableModel table = getBlamesTableModel(build);
        assertOneIssue(testCommit, table);
    }

    /**
     * Tests blaming eleven issues with a fake git repository. Test run from a pipeline script.
     *
     * @throws Exception
     *         if there is a problem with the git repository
     */
    @Test
    public void shouldBlameElevenIssuesWithPipeline() throws Exception {
        Map<String, String> commits = createGitRepository();

        createAndCommitFile("Jenkinsfile", "node {\n"
                + "  stage ('Checkout') {\n"
                + "    checkout scm\n"
                + "  }\n"
                + "  stage ('Build and Analysis') {"
                + "    echo '[javac] Test.java:1: warning: Test Warning for Jenkins'\n"
                + "    echo '[javac] Test.java:2: warning: Test Warning for Jenkins'\n"
                + "    echo '[javac] Test.java:3: warning: Test Warning for Jenkins'\n"
                + "    echo '[javac] Test.java:4: warning: Test Warning for Jenkins'\n"
                + "    echo '[javac] LoremIpsum.java:1: warning: Another Warning for Jenkins'\n"
                + "    echo '[javac] LoremIpsum.java:2: warning: Another Warning for Jenkins'\n"
                + "    echo '[javac] LoremIpsum.java:3: warning: Another Warning for Jenkins'\n"
                + "    echo '[javac] LoremIpsum.java:4: warning: Another Warning for Jenkins'\n"
                + "    echo '[javac] Bob.java:1: warning: Bobs Warning for Jenkins'\n"
                + "    echo '[javac] Bob.java:2: warning: Bobs Warning for Jenkins'\n"
                + "    echo '[javac] Bob.java:3: warning: Bobs Warning for Jenkins'\n"
                + "    recordIssues tools: [java()]\n"
                + "  }\n"
                + "}");

        WorkflowJob project = createPipeline();
        project.setDefinition(new CpsScmFlowDefinition(new GitSCM(gitRepo.toString()), "Jenkinsfile"));

        AnalysisResult result = scheduleSuccessfulBuild(project);
        assertSuccessfulBlame(result, 11, 3);

        Run<?, ?> build = buildSuccessfully(project);

        TableModel table = getBlamesTableModel(build);
        assertElevenIssues(commits, table);
    }

    /**
     * Tests blaming eleven issues with a fake git repository. Test run with a freestyle job.
     *
     * @throws Exception
     *         if there is a problem with the git repository
     */
    @Test
    public void shouldBlameElevenIssuesWithFreestyle() throws Exception {
        Map<String, String> commits = createGitRepository();

        FreeStyleProject project = createFreeStyleProject();
        project.setScm(new GitSCM(gitRepo.toString()));
        project.getBuildersList()
                .add(new CreateFileBuilder("warnings.txt",
                        "[javac] Test.java:1: warning: Test Warning for Jenkins\n"
                                + "[javac] Test.java:2: warning: Test Warning for Jenkins\n"
                                + "[javac] Test.java:3: warning: Test Warning for Jenkins\n"
                                + "[javac] Test.java:4: warning: Test Warning for Jenkins\n"
                                + "[javac] LoremIpsum.java:1: warning: Another Warning for Jenkins\n"
                                + "[javac] LoremIpsum.java:2: warning: Another Warning for Jenkins\n"
                                + "[javac] LoremIpsum.java:3: warning: Another Warning for Jenkins\n"
                                + "[javac] LoremIpsum.java:4: warning: Another Warning for Jenkins\n"
                                + "[javac] Bob.java:1: warning: Bobs Warning for Jenkins\n"
                                + "[javac] Bob.java:2: warning: Bobs Warning for Jenkins\n"
                                + "[javac] Bob.java:3: warning: Bobs Warning for Jenkins"));

        Java javaJob = new Java();
        javaJob.setPattern("warnings.txt");
        enableWarnings(project, javaJob);

        AnalysisResult result = scheduleSuccessfulBuild(project);
        assertSuccessfulBlame(result, 11, 3);

        Run<?, ?> build = buildSuccessfully(project);

        TableModel table = getBlamesTableModel(build);

        assertElevenIssues(commits, table);
    }

    /**
     * Test if blaming works on a build out of tree. See JENKINS-57260.
     *
     * @throws Exception
     *         if there is a problem with the git repository
     */
    @Test
    @Issue("JENKINS-57260")
    public void shouldBlameWithBuildOutOfTree() throws Exception {
        gitRepo.init();
        createAndCommitFile("Test.h", "#ifdef \"");

        String firstCommit = gitRepo.head();

        createAndCommitFile("Jenkinsfile", "pipeline {\n"
                + "  agent any\n"
                + "  options {\n"
                + "    skipDefaultCheckout()\n"
                + "  }\n"
                + "  stages {\n"
                + "    stage('Prepare') {\n"
                + "      steps {\n"
                + "        dir('source') {\n"
                + "          checkout scm\n"
                + "        }\n"
                + "      }\n"
                + "    }\n"
                + "    stage('Doxygen') {\n"
                + "      steps {\n"
                + "        dir('build/doxygen') {\n"
                + "          echo 'Test.h:1: Error: Unexpected character'\n"
                + "        }\n"
                + "        recordIssues(aggregatingResults: true, "
                + "             enabledForFailure: true, "
                + "             tool: doxygen(name: 'Doxygen'), "
                + "             sourceDirectory: 'source'"
                + "        )\n"
                + "      }\n"
                + "    }\n"
                + "  }\n"
                + "}");

        WorkflowJob project = createPipeline();
        project.setDefinition(new CpsScmFlowDefinition(new GitSCM(gitRepo.toString()), "Jenkinsfile"));

        AnalysisResult result = scheduleSuccessfulBuild(project);
        assertSuccessfulBlame(result, 1, 1);

        Run<?, ?> build = buildSuccessfully(project);
        TableModel table = getBlamesTableModel(build);
        List<Object> rows = table.getRows();

        assertThat(rows).hasSize(1);
        BlamesRow row = (BlamesRow) rows.get(0);

        assertThat(row.getDescription()).contains("Unexpected character");
        assertThat(row.getFileName().getSort()).isEqualTo("Test.h:0000001");
        assertThat(row.getAge()).contains("1");

        assertThat(row).hasAuthor("Git SampleRepoRule")
                .hasEmail("gits@mplereporule")
                .hasCommit(firstCommit);
    }

    private TableModel getBlamesTableModel(final Run<?, ?> build) {
        IssuesDetail issuesDetail = (IssuesDetail) build.getAction(ResultAction.class).getTarget();
        return issuesDetail.getTableModel("blames");
    }

    private void assertSuccessfulBlame(final AnalysisResult result, final int numberOfIssues, final int numberOfFiles) {
        assertThat(result).hasNoErrorMessages();
        assertThat(result).hasTotalSize(numberOfIssues);
        assertThat(result).hasInfoMessages(
                "Invoking Git blamer to create author and commit information for " + numberOfFiles + " affected files",
                "-> blamed authors of issues in " + numberOfFiles + " files");
    }

    private void assertColumnHeaders(final TableModel table) {
        List<String> columnHeaders = table.getColumns().stream().map(TableColumn::getHeaderLabel).collect(
                Collectors.toList());
        assertThat(columnHeaders)
                .containsExactly(DETAILS, FILE, AGE, AUTHOR, EMAIL, COMMIT, ADDED);
    }

    private void assertOneIssue(final String commit, final TableModel table) {
        assertColumnHeaders(table);

        List<Object> rows = table.getRows();
        assertThat(rows).hasSize(1);
        assertColumnsOfTest((BlamesRow) rows.get(0), commit, 1);
    }

    private void assertColumnsOfTest(final BlamesRow row, final String commit, final int lineNumber) {
        assertThat(row.getDescription()).contains("Test Warning for Jenkins");
        assertThat(row.getFileName().getSort()).isEqualTo("Test.java:000000" + lineNumber);
        assertThat(row.getAge()).contains("1");
        assertThat(row).hasAuthor("Git SampleRepoRule").hasEmail("gits@mplereporule").hasCommit(commit);
    }

    private void assertColumnsOfRowLoremIpsum(final BlamesRow row, final String commitId, final int lineNumber) {
        assertThat(row.getDescription()).contains("Another Warning for Jenkins");
        assertThat(row.getFileName().getSort()).isEqualTo("LoremIpsum.java:000000" + lineNumber);
        assertThat(row.getAge()).contains("1");
        assertThat(row).hasAuthor("John Doe").hasEmail("john@doe").hasCommit(commitId);
    }

    private void assertColumnsOfRowBob(final BlamesRow row, final String commitId, final int lineNumber) {
        assertThat(row.getDescription()).contains("Bobs Warning for Jenkins");
        assertThat(row.getFileName().getSort()).isEqualTo("Bob.java:000000" + lineNumber);
        assertThat(row.getAge()).contains("1");
        assertThat(row).hasAuthor("Alice Miller").hasEmail("alice@miller").hasCommit(commitId);
    }

    private void assertElevenIssues(final Map<String, String> commits, final TableModel table) {
        assertColumnHeaders(table);

        List<Object> rows = table.getRows();
        assertThat(rows).hasSize(11);

        assertColumnsOfTest((BlamesRow) rows.get(0), commits.get("Test"), 1);
        assertColumnsOfTest((BlamesRow) rows.get(1), commits.get("Test"), 2);
        assertColumnsOfTest((BlamesRow) rows.get(2), commits.get("Test"), 3);
        assertColumnsOfTest((BlamesRow) rows.get(3), commits.get("Test"), 4);

        assertColumnsOfRowLoremIpsum((BlamesRow) rows.get(4), commits.get("LoremIpsum"), 1);
        assertColumnsOfRowLoremIpsum((BlamesRow) rows.get(5), commits.get("LoremIpsum"), 2);
        assertColumnsOfRowLoremIpsum((BlamesRow) rows.get(6), commits.get("LoremIpsum"), 3);
        assertColumnsOfRowLoremIpsum((BlamesRow) rows.get(7), commits.get("LoremIpsum"), 4);

        assertColumnsOfRowBob((BlamesRow) rows.get(8), commits.get("Bob"), 1);
        assertColumnsOfRowBob((BlamesRow) rows.get(9), commits.get("Bob"), 2);
        assertColumnsOfRowBob((BlamesRow) rows.get(10), commits.get("Bob"), 3);
    }

    private Map<String, String> createGitRepository() throws Exception {
        Map<String, String> commits = new HashMap<>();

        gitRepo.init();
        createAndCommitFile("Test.java", "public class Test {\n"
                + "    public Test() {\n"
                + "        System.out.println(\"Test\");"
                + "    }\n"
                + "}");

        commits.put("Test", gitRepo.head());

        gitRepo.git("config", "user.name", "John Doe");
        gitRepo.git("config", "user.email", "john@doe");
        createAndCommitFile("2Lorem^1Ipsum.java", "public class wQ  a BVGTC^!$&/(LoremIpsum {\n"
                + "    public LoremIpsum() {\n"
                + "        Log.log(\"Lorem ipsum dolor sit amet\");"
                + "    }\n"
                + "}");

        commits.put("LoremIpsum", gitRepo.head());

        gitRepo.git("config", "user.name", "Alice Miller");
        gitRepo.git("config", "user.email", "alice@miller");
        createAndCommitFile("Bob.java", "public class Bob {\n"
                + "    public Bob() {\n"
                + "        Log.log(\"Bob: 'Where are you?'\");"
                + "    }\n"
                + "}");

        commits.put("Bob", gitRepo.head());

        return commits;
    }

    private void createAndCommitFile(final String fileName, final String content) throws Exception {
        gitRepo.write(fileName, content);
        gitRepo.git("add", fileName);
        gitRepo.git("commit", "--message=" + fileName + " created");
    }
}

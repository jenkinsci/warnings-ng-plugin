package io.jenkins.plugins.analysis.core.scm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.assertj.core.data.MapEntry;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.CreateFileBuilder;
import org.jvnet.hudson.test.Issue;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import hudson.model.FreeStyleProject;
import hudson.plugins.git.GitSCM;
import jenkins.plugins.git.GitSampleRepoRule;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.AnalysisResultAssert;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerTest;
import io.jenkins.plugins.analysis.warnings.Java;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.BlamesRow;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.BlamesRow.BlamesColumn;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.BlamesTable;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.DetailsTab;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.DetailsTab.TabType;
import io.jenkins.plugins.forensics.blame.Blamer;

import static io.jenkins.plugins.forensics.assertions.Assertions.*;

/**
 * Tests the {@link Blamer GitBlamer} in several jobs that uses a real Git repository.
 *
 * @author Fabian Janker
 * @author Andreas Pabst
 */
@SuppressWarnings("PMD.SignatureDeclareThrowsException")
public class GitBlamerITest extends IntegrationTestWithJenkinsPerTest {
    private static final String DETAILS = "Another Warning for Jenkins";
    private static final String AUTHOR = "John Doe";
    private static final String EMAIL = "john@doe";
    private static final String FILE_NAME = "LoremIpsum";

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

        AnalysisResult result = scheduleSuccessfulBuild(job);
        assertSuccessfulBlame(result, 1, 1);

        BlamesTable table = getSourceControlTable(result);
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

        assertThat(result.getBlames()).isEmpty();
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

        BlamesTable table = getSourceControlTable(result);
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

        BlamesTable table = getSourceControlTable(result);
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

        BlamesTable table = getSourceControlTable(result);
        assertElevenIssues(commits, table);
    }

    /**
     * Test filtering in the {@link BlamesTable}.
     *
     * @throws Exception
     *         if there is a problem with the git repository
     */
    @Test
    public void shouldFilterTable() throws Exception {
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

        BlamesTable table = getSourceControlTable(result);
        assertElevenIssues(commits, table);

        table.filter("LoremIpsum.java");
        assertThat(table.getInfo()).isEqualTo("Showing 1 to 4 of 4 entries (filtered from 11 total entries)");
        List<BlamesRow> rows = table.getRows();
        assertThat(rows).hasSize(4);
        assertThat(rows.get(0).getValuesByColumn()).contains(
                details(DETAILS),
                file("LoremIpsum.java:1"),
                author(AUTHOR),
                email(EMAIL),
                commit(commits.get(FILE_NAME)),
                age("1"));
        assertThat(rows.get(1).getValuesByColumn()).contains(
                details(DETAILS),
                file("LoremIpsum.java:2"),
                author(AUTHOR),
                email(EMAIL),
                commit(commits.get(FILE_NAME)),
                age("1"));
        assertThat(rows.get(2).getValuesByColumn()).contains(
                details(DETAILS),
                file("LoremIpsum.java:3"),
                author(AUTHOR),
                email(EMAIL),
                commit(commits.get(FILE_NAME)),
                age("1"));
        assertThat(rows.get(3).getValuesByColumn()).contains(
                details(DETAILS),
                file("LoremIpsum.java:4"),
                author(AUTHOR),
                email(EMAIL),
                commit(commits.get(FILE_NAME)),
                age("1"));
    }

    /**
     * Test if blaming works on a build out of tree. See JENKINS-57260.
     *
     * @throws Exception
     *         if there is a problem with the git repository
     */
    @Issue("JENKINS-57260") @Ignore("Until JENKINS-57260 has been fixed")
    @Test
    public void shouldBlameWithBuildOutOfTree() throws Exception {
        gitRepo.init();
        createAndCommitFile("Test.h", "#ifdef \"");

        final String commit = gitRepo.head();

        createAndCommitFile("Jenkinsfile", "pipeline {\n"
                + "  agent any\n"
                + "  options {\n"
                + "    skipDefaultCheckout()\n"
                + "  }\n"
                + "  stages {\n"
                + "    stage('Prepare') {\n"
                + "      steps {\n"
                + "        dir('src') {\n"
                + "          checkout scm\n"
                + "        }\n"
                + "      }\n"
                + "    }\n"
                + "    stage('Doxygen') {\n"
                + "      steps {\n"
                + "        dir('build/doxygen') {\n"
                + "          sh 'mkdir doxygen'\n"
                + "          sh 'echo Test.h:1: Error: Unexpected character `\"`> doxygen/doxygen.log'\n"
                + "        }\n"
                + "        recordIssues(aggregatingResults: true, enabledForFailure: true, tools: [ doxygen(name: 'Doxygen', pattern: 'build/doxygen/doxygen/doxygen.log') ] )\n"
                + "      }\n"
                + "    }\n"
                + "  }\n"
                + "}");

        WorkflowJob project = createPipeline();
        project.setDefinition(new CpsScmFlowDefinition(new GitSCM(gitRepo.toString()), "Jenkinsfile"));

        AnalysisResult result = scheduleSuccessfulBuild(project);
        assertSuccessfulBlame(result, 1, 1);

        BlamesTable table = getSourceControlTable(result);
        assertThat(table.getInfo()).isEqualTo("Showing 1 to 1 of 1 entries");
        List<BlamesRow> rows = table.getRows();
        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).getValuesByColumn()).contains(
                details("Unexpected character"),
                file("Test.h:1"),
                author("Git SampleRepoRule"),
                email("gits@mplereporule"),
                commit(commit),
                age("1"));
    }

    private BlamesTable getSourceControlTable(final AnalysisResult result) {
        HtmlPage page = getWebPage(JavaScriptSupport.JS_ENABLED, result);

        DetailsTab detailsTab = new DetailsTab(page);
        assertThat(detailsTab.getTabTypes()).contains(TabType.BLAMES);

        return detailsTab.select(TabType.BLAMES);
    }

    private void assertSuccessfulBlame(final AnalysisResult result, final int numberOfIssues, final int numberOfFiles) {
        AnalysisResultAssert.assertThat(result).hasNoErrorMessages();
        AnalysisResultAssert.assertThat(result).hasTotalSize(numberOfIssues);
        AnalysisResultAssert.assertThat(result)
                .hasInfoMessages(
                        "Invoking Git blamer to create author and commit information for " + numberOfFiles
                                + " affected files",
                        "-> blamed authors of issues in " + numberOfFiles + " files");
    }

    private void assertOneIssue(final String commit, final BlamesTable table) {
        assertThat(table.getColumns())
                .containsExactly(BlamesColumn.DETAILS, BlamesColumn.FILE, BlamesColumn.AGE, BlamesColumn.AUTHOR,
                        BlamesColumn.EMAIL, BlamesColumn.COMMIT);
        assertThat(table.getInfo()).isEqualTo("Showing 1 to 1 of 1 entries");
        List<BlamesRow> rows = table.getRows();
        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).getValuesByColumn()).contains(
                details("Test Warning for Jenkins"),
                file("Test.java:1"),
                author("Git SampleRepoRule"),
                email("gits@mplereporule"),
                commit(commit),
                age("1"));
    }

    private void assertElevenIssues(final Map<String, String> commits, final BlamesTable table) {
        assertThat(table.getColumns())
                .containsExactly(BlamesColumn.DETAILS, BlamesColumn.FILE, BlamesColumn.AGE, BlamesColumn.AUTHOR,
                        BlamesColumn.EMAIL, BlamesColumn.COMMIT);
        assertThat(table.getInfo()).isEqualTo("Showing 1 to 10 of 11 entries");
        List<BlamesRow> rows = table.getRows();
        assertThat(rows).hasSize(10);
        assertThat(rows.get(0).getValuesByColumn()).contains(
                details("Bobs Warning for Jenkins"),
                file("Bob.java:1"),
                author("Alice Miller"),
                email("alice@miller"),
                commit(commits.get("Bob")),
                age("1"));
        assertThat(rows.get(1).getValuesByColumn()).contains(
                details("Bobs Warning for Jenkins"),
                file("Bob.java:2"),
                author("Alice Miller"),
                email("alice@miller"),
                commit(commits.get("Bob")),
                age("1"));
        assertThat(rows.get(2).getValuesByColumn()).contains(
                details("Bobs Warning for Jenkins"),
                file("Bob.java:3"),
                author("Alice Miller"),
                email("alice@miller"),
                commit(commits.get("Bob")),
                age("1"));
        assertThat(rows.get(3).getValuesByColumn()).contains(
                details(DETAILS),
                file("LoremIpsum.java:1"),
                author(AUTHOR),
                email(EMAIL),
                commit(commits.get(FILE_NAME)),
                age("1"));
        assertThat(rows.get(4).getValuesByColumn()).contains(
                details(DETAILS),
                file("LoremIpsum.java:2"),
                author(AUTHOR),
                email(EMAIL),
                commit(commits.get(FILE_NAME)),
                age("1"));
        assertThat(rows.get(5).getValuesByColumn()).contains(
                details(DETAILS),
                file("LoremIpsum.java:3"),
                author(AUTHOR),
                email(EMAIL),
                commit(commits.get(FILE_NAME)),
                age("1"));
        assertThat(rows.get(6).getValuesByColumn()).contains(
                details(DETAILS),
                file("LoremIpsum.java:4"),
                author(AUTHOR),
                email(EMAIL),
                commit(commits.get(FILE_NAME)),
                age("1"));
        assertThat(rows.get(7).getValuesByColumn()).contains(
                details("Test Warning for Jenkins"),
                file("Test.java:1"),
                author("Git SampleRepoRule"),
                email("gits@mplereporule"),
                commit(commits.get("Test")),
                age("1"));
        assertThat(rows.get(8).getValuesByColumn()).contains(
                details("Test Warning for Jenkins"),
                file("Test.java:2"),
                author("Git SampleRepoRule"),
                email("gits@mplereporule"),
                commit(commits.get("Test")),
                age("1"));
        assertThat(rows.get(9).getValuesByColumn()).contains(
                details("Test Warning for Jenkins"),
                file("Test.java:3"),
                author("Git SampleRepoRule"),
                email("gits@mplereporule"),
                commit(commits.get("Test")),
                age("1"));

        table.goToPage(2);
        assertThat(table.getInfo()).isEqualTo("Showing 11 to 11 of 11 entries");

        List<BlamesRow> secondPageRows = table.getRows();
        assertThat(secondPageRows).hasSize(1);
        assertThat(secondPageRows.get(0).getValuesByColumn()).contains(
                details("Test Warning for Jenkins"),
                file("Test.java:4"),
                author("Git SampleRepoRule"),
                email("gits@mplereporule"),
                commit(commits.get("Test")),
                age("1"));
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
        createAndCommitFile("LoremIpsum.java", "public class LoremIpsum {\n"
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

    private MapEntry<BlamesColumn, String> details(final String details) {
        return entry(BlamesColumn.DETAILS, details);
    }

    private MapEntry<BlamesColumn, String> age(final String age) {
        return entry(BlamesColumn.AGE, age);
    }

    private MapEntry<BlamesColumn, String> author(final String author) {
        return entry(BlamesColumn.AUTHOR, author);
    }

    private MapEntry<BlamesColumn, String> email(final String email) {
        return entry(BlamesColumn.EMAIL, email);
    }

    private MapEntry<BlamesColumn, String> commit(final String commit) {
        return entry(BlamesColumn.COMMIT, commit);
    }

    private MapEntry<BlamesColumn, String> file(final String file) {
        return entry(BlamesColumn.FILE, file);
    }

}

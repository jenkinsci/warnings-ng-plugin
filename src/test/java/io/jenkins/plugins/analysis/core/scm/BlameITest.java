package io.jenkins.plugins.analysis.core.scm;

import java.util.ArrayList;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import hudson.plugins.git.GitSCM;
import jenkins.plugins.git.GitSampleRepoRule;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.AnalysisResultAssert;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerTest;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.DetailsTab;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.DetailsTab.TabType;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.SourceControlRow;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.SourceControlTable;

import static io.jenkins.plugins.analysis.core.testutil.Assertions.*;

/**
 * Integration Test for the git blame functionality.
 *
 * @author Andreas Reiser
 * @author Andreas Moser
 */
public class BlameITest extends IntegrationTestWithJenkinsPerTest {

    private static final String ERROR_AUTHOR = "Error author";
    private static final String ERROR_MAIL = "error@gmail.com";
    private static final String OTHER_AUTHOR = "Rock";
    private static final String OTHER_MAIL = "roshar@gmail.com";
    private static final String ALERT_AUTHOR = "Kioyaki Matsugae";
    private static final String ALERT_MAIL = "mishima@gmail.com";

    /**
     * The git repository.
     */
    @Rule
    public GitSampleRepoRule gitRepo = new GitSampleRepoRule();

    /**
     * Tests if git blame work correctly in a simple project with one issue.
     */
    @Test
    public void shouldBlameOneIssueInSimpleProject() {

        initRepository();

        createAndCommitFile("File.java", "public class Test {}");
        final String testCommit = getHead();

        createAndCommitFile("Jenkinsfile", "node {\n"
                + "  stage ('Checkout') {\n"
                + "    checkout scm\n"
                + "  }\n"
                + "  stage ('Build and Analysis') {"
                + "    echo '[javac] File.java:1: warning: Test Warning'\n"
                + "    recordIssues tools: [java()]\n"
                + "  }\n"
                + "}");

        WorkflowJob project = createPipeline();
        project.setDefinition(new CpsScmFlowDefinition(new GitSCM(gitRepo.toString()), "Jenkinsfile"));

        AnalysisResult result = scheduleSuccessfulBuild(project);
        assertBlameCount(result, 1, 1);

        SourceControlTable table = getSourceControlTable(result);

        assertThat(table.getRows()).hasSize(1);
        assertThat(table.getRows()).containsExactly(
                new SourceControlRow("Test Warning", "File.java:1", ERROR_AUTHOR,
                        ERROR_MAIL, testCommit, 1));
    }

    /**
     * Tests if git blame work correctly if a user changes the content of a file.
     */
    @Test
    public void shouldBlameCorrectUserAfterChangingFile() {
        initRepository();
        createAndCommitFile("File.java", "public class Test {}");
        changeUser(ALERT_AUTHOR, ALERT_MAIL);
        createAndCommitFile("File.java", "public class Test2 {}");
        final String testCommit = getHead();

        createAndCommitFile("Jenkinsfile", "node {\n"
                + "  stage ('Checkout') {\n"
                + "    checkout scm\n"
                + "  }\n"
                + "  stage ('Build and Analysis') {"
                + "    echo '[javac] File.java:1: warning: Test Warning'\n"
                + "    recordIssues tools: [java()]\n"
                + "  }\n"
                + "}");

        WorkflowJob project = createPipeline();
        project.setDefinition(new CpsScmFlowDefinition(new GitSCM(gitRepo.toString()), "Jenkinsfile"));

        AnalysisResult result = scheduleSuccessfulBuild(project);
        assertBlameCount(result, 1, 1);

        SourceControlTable table = getSourceControlTable(result);

        assertThat(table.getRows()).hasSize(1);
        assertThat(table.getRows()).containsExactly(
                new SourceControlRow("Test Warning", "File.java:1", ALERT_AUTHOR,
                        ALERT_MAIL, testCommit, 1));
    }

    /**
     * Tests if git blame works correctly with multiple users and commits.
     */
    @Test
    public void shouldBlameThreeIssuesInMultiCommitProject() {

        initRepository();

        List<String> commits = createCommits();

        createAndCommitFile("Jenkinsfile", "node {\n"
                + "  stage ('Checkout') {\n"
                + "    checkout scm\n"
                + "  }\n"
                + "  stage ('Build and Analysis') {"
                + "    echo '[javac] File.java:1: warning: Test Warning'\n"
                + "    echo '[javac] Random.java:1: warning: Test Warning'\n"
                + "    echo '[javac] Random.java:2: warning: Test Warning'\n"
                + "    echo '[javac] Alert.java:1: warning: Test Warning'\n"
                + "    echo '[javac] Alert.java:3: warning: Test Warning'\n"
                + "    recordIssues tools: [java()]\n"
                + "  }\n"
                + "}");

        WorkflowJob project = createPipeline();
        project.setDefinition(new CpsScmFlowDefinition(new GitSCM(gitRepo.toString()), "Jenkinsfile"));

        AnalysisResult result = scheduleSuccessfulBuild(project);
        assertBlameCount(result, 5, 3);

        SourceControlTable table = getSourceControlTable(result);

        assertThat(table.getRows()).hasSize(5);
        assertThat(table.getRows()).containsExactlyInAnyOrder(
                new SourceControlRow("Test Warning", "File.java:1", ERROR_AUTHOR, ERROR_MAIL, commits.get(0), 1),
                new SourceControlRow("Test Warning", "Random.java:1", OTHER_AUTHOR, OTHER_MAIL, commits.get(1), 1),
                new SourceControlRow("Test Warning", "Random.java:2", OTHER_AUTHOR, OTHER_MAIL, commits.get(1), 1),
                new SourceControlRow("Test Warning", "Alert.java:1", ALERT_AUTHOR, ALERT_MAIL, commits.get(2), 1),
                new SourceControlRow("Test Warning", "Alert.java:3", ALERT_AUTHOR, ALERT_MAIL, commits.get(2), 1));
    }

    /**
     * Test if blaming works on a build out of tree. See JENKINS-57260.
     */
    @Issue("JENKINS-57260")
    @Test
    public void shouldBlameWithBuildOutOfTree() {
        initRepository();
        createAndCommitFile("File.java", "public class Test {}");

        final String commit = getHead();

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
                + "          sh 'echo \"[javac] File.java:1: warning: Test Warning\" > doxygen/doxygen.log'\n"
                + "        }\n"
                + "        recordIssues(aggregatingResults: true, enabledForFailure: true, tools: [ doxygen(name: 'Doxygen', pattern: 'build/doxygen/doxygen/doxygen.log') ] )\n"
                + "      }\n"
                + "    }\n"
                + "  }\n"
                + "}");

        WorkflowJob project = createPipeline();
        project.setDefinition(new CpsScmFlowDefinition(new GitSCM(gitRepo.toString()), "Jenkinsfile"));

        AnalysisResult result = scheduleSuccessfulBuild(project);
        assertBlameCount(result, 1, 1);

        SourceControlTable table = getSourceControlTable(result);

        assertThat(table.getRows()).hasSize(1);
        assertThat(table.getRows()).containsExactly(
                new SourceControlRow("Test Warning", "File.java:1", ERROR_AUTHOR,
                        ERROR_MAIL, commit, 1));
    }

    @SuppressWarnings("IllegalCatch")
    private String getHead() {
        try {
            return gitRepo.head();
        }
        catch (Exception e) {
            throw new IllegalStateException("Unexpected Exception", e);
        }
    }

    private SourceControlTable getSourceControlTable(final AnalysisResult result) {
        HtmlPage page = getWebPage(JavaScriptSupport.JS_ENABLED, result);

        DetailsTab detailsTab = new DetailsTab(page);
        assertThat(detailsTab.getTabTypes()).contains(TabType.BLAMES);

        return detailsTab.select(TabType.BLAMES);
    }

    private void assertBlameCount(final AnalysisResult result, final int numberOfIssuesExpected,
            final int numberOfFiles) {

        AnalysisResultAssert.assertThat(result).hasNoErrorMessages();
        AnalysisResultAssert.assertThat(result).hasTotalSize(numberOfIssuesExpected);
        AnalysisResultAssert.assertThat(result)
                .hasInfoMessages("Created blame requests for " + numberOfFiles
                                + " files - invoking Git blame on agent for each of the requests",
                        "-> blamed authors of issues in " + numberOfFiles + " files");
    }

    @SuppressWarnings("IllegalCatch")
    private void initRepository() throws IllegalStateException {
        try {
            gitRepo.init();
            changeUser(ERROR_AUTHOR, ERROR_MAIL);
        }
        catch (Exception e) {
            throw new IllegalStateException("Unexpected IOException", e);
        }
    }

    @SuppressWarnings("IllegalCatch")
    private void changeUser(final String name, final String mail) throws IllegalStateException {
        try {
            gitRepo.git("config", "user.name", name);
            gitRepo.git("config", "user.email", mail);
        }
        catch (Exception e) {
            throw new IllegalStateException("Unexpected IOException", e);
        }
    }

    private List<String> createCommits() {
        List<String> commits = new ArrayList<>();
        createAndCommitFile("File.java", "public class Test {\n"
                + "multiline\n"
                + "file\n"
                + "}");
        commits.add(getHead());

        changeUser(OTHER_AUTHOR, OTHER_MAIL);
        createAndCommitFile("Random.java", "public class Random {\n"
                + "    public Random() {\n"
                + "   multiline again }\n"
                + "}");
        commits.add(getHead());

        changeUser(ALERT_AUTHOR, ALERT_MAIL);
        createAndCommitFile("Alert.java", "public class Alert {\n"
                + "    public Alert() {\n"
                + "    }\n"
                + "}");
        commits.add(getHead());
        return commits;
    }

    @SuppressWarnings("IllegalCatch")
    private void createAndCommitFile(final String fileName, final String content) throws IllegalStateException {
        try {
            gitRepo.write(fileName, content);
            gitRepo.git("add", fileName);
            gitRepo.git("commit", "--message=" + fileName + " created");
        }
        catch (Exception e) {
            throw new IllegalStateException("Unexpected IOException", e);
        }
    }
}

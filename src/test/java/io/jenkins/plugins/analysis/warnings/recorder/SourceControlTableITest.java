package io.jenkins.plugins.analysis.warnings.recorder;

import java.io.IOException;

import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import hudson.model.FreeStyleProject;
import hudson.plugins.git.GitSCM;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.warnings.Java;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.SourceControlRow;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.SourceControlTable;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration test of the SourceControlTable.
 *
 * @author Fabian Janker
 * @author Andreas Pabst
 */
// TODO: use 'fake' git instead of real repository + then also use a pipeline
public class SourceControlTableITest extends IntegrationTestWithJenkinsPerSuite {

    /**
     * Test for one file being listed and attributed correctly.
     *
     * @throws IOException
     *         if the scm can't be set
     */
    // TODO: don't use a remote git repository in the future, since e.g. the author or commitId might change
    @Test
    public void shouldListAFile() throws IOException {
        FreeStyleProject project = createFreeStyleProject();
        project.setScm(new GitSCM("https://github.com/jenkinsci/warnings-ng-plugin.git"));

        addScriptStep(project,
                "echo \"[javac] src/main/java/io/jenkins/plugins/analysis/warnings/AndroidLint.java:10: warning: Test Warning for Jenkins\" >> test-warnings.custom.txt");

        Java javaJob = new Java();
        javaJob.setPattern("**/*.custom.txt");
        enableWarnings(project, javaJob);

        AnalysisResult result = scheduleSuccessfulBuild(project);

        HtmlPage page = getWebPage(JavaScriptSupport.JS_ENABLED, result);

        SourceControlTable table = new SourceControlTable(page);

        assertThat(result.getTotalSize()).isEqualTo(1);
        assertThat(table.getInfo()).isEqualTo("Showing 1 to 1 of 1 entries");
        assertThat(table.getColumnNames()).containsExactly(SourceControlRow.DETAILS, SourceControlRow.FILE,
                SourceControlRow.AGE, SourceControlRow.AUTHOR, SourceControlRow.EMAIL, SourceControlRow.COMMIT);
        assertThat(table.getRows()).containsExactly(
                new SourceControlRow("Test Warning for Jenkins", "AndroidLint.java:10", "Ulli Hafner",
                        "ullrich.hafner@gmail.com", "762e9bf397cb7c4f9ec954d97e464884ed33ded5", 1));
    }

    /**
     * Test for three files being listed and attributed correctly.
     *
     * @throws IOException
     *         if the scm can't be set
     */
    // TODO: don't use a remote git repository in the future, since e.g. the author or commitId might change
    @Test
    public void shouldListThreeFiles() throws IOException {
        FreeStyleProject project = createFreeStyleProject();
        project.setScm(new GitSCM("https://github.com/jenkinsci/warnings-ng-plugin.git"));

        addScriptStep(project,
                "echo \"[javac] src/main/java/io/jenkins/plugins/analysis/warnings/AndroidLint.java:10: warning: Test Warning for Jenkins\" >> test-warnings.custom.txt\n"
                        + "echo \"[javac] src/main/java/io/jenkins/plugins/analysis/warnings/Gendarme.java:39: warning: Test Warning for Jenkins\" >> test-warnings.custom.txt\n"
                        + "echo \"[javac] src/main/java/io/jenkins/plugins/analysis/warnings/IarCstat.java:24: warning: Another Test Warning for Jenkins\" >> test-warnings.custom.txt");

        Java javaJob = new Java();
        javaJob.setPattern("**/*.custom.txt");
        enableWarnings(project, javaJob);

        AnalysisResult result = scheduleSuccessfulBuild(project);

        HtmlPage page = getWebPage(JavaScriptSupport.JS_ENABLED, result);

        SourceControlTable table = new SourceControlTable(page);

        assertThat(result.getTotalSize()).isEqualTo(3);
        assertThat(table.getInfo()).isEqualTo("Showing 1 to 3 of 3 entries");
        assertThat(table.getColumnNames()).containsExactly(SourceControlRow.DETAILS, SourceControlRow.FILE,
                SourceControlRow.AGE, SourceControlRow.AUTHOR, SourceControlRow.EMAIL, SourceControlRow.COMMIT);
        assertThat(table.getRows()).containsExactly(
                new SourceControlRow("Test Warning for Jenkins", "AndroidLint.java:10", "Ulli Hafner",
                        "ullrich.hafner@gmail.com", "762e9bf397cb7c4f9ec954d97e464884ed33ded5", 1),
                new SourceControlRow("Test Warning for Jenkins", "Gendarme.java:39", "Ulli Hafner",
                        "ullrich.hafner@gmail.com", "f9fd0154b84b7956047b7aad87c87847dac4cf20", 1),
                new SourceControlRow("Another Test Warning for Jenkins", "IarCstat.java:24", "Lorenz Aebi",
                        "git@xca.ch", "a8f5fc0e6c1ba43878502e0e983dba5cc966b5b0", 1));
    }

    /**
     * Test filtering in the table.
     *
     * @throws IOException
     *         if the scm can't be set
     */
    // TODO: don't use a remote git repository in the future, since e.g. the author or commitId might change
    @Test
    public void shouldFilter() throws IOException {
        FreeStyleProject project = createFreeStyleProject();
        project.setScm(new GitSCM("https://github.com/jenkinsci/warnings-ng-plugin.git"));

        addScriptStep(project,
                "echo \"[javac] src/main/java/io/jenkins/plugins/analysis/warnings/AndroidLint.java:10: warning: Test Warning for Jenkins\" >> test-warnings.custom.txt\n"
                        + "echo \"[javac] src/main/java/io/jenkins/plugins/analysis/warnings/Gendarme.java:39: warning: Test Warning for Jenkins\" >> test-warnings.custom.txt\n"
                        + "echo \"[javac] src/main/java/io/jenkins/plugins/analysis/warnings/IarCstat.java:24: warning: Another Test Warning for Jenkins\" >> test-warnings.custom.txt");

        Java javaJob = new Java();
        javaJob.setPattern("**/*.custom.txt");
        enableWarnings(project, javaJob);

        AnalysisResult result = scheduleSuccessfulBuild(project);

        HtmlPage page = getWebPage(JavaScriptSupport.JS_ENABLED, result);

        SourceControlTable table = new SourceControlTable(page);

        assertThat(result.getTotalSize()).isEqualTo(3);
        assertThat(table.getInfo()).isEqualTo("Showing 1 to 3 of 3 entries");
        assertThat(table.getColumnNames()).containsExactly(SourceControlRow.DETAILS, SourceControlRow.FILE,
                SourceControlRow.AGE, SourceControlRow.AUTHOR, SourceControlRow.EMAIL, SourceControlRow.COMMIT);
        assertThat(table.getRows()).containsExactly(
                new SourceControlRow("Test Warning for Jenkins", "AndroidLint.java:10", "Ulli Hafner",
                        "ullrich.hafner@gmail.com", "762e9bf397cb7c4f9ec954d97e464884ed33ded5", 1),
                new SourceControlRow("Test Warning for Jenkins", "Gendarme.java:39", "Ulli Hafner",
                        "ullrich.hafner@gmail.com", "f9fd0154b84b7956047b7aad87c87847dac4cf20", 1),
                new SourceControlRow("Another Test Warning for Jenkins", "IarCstat.java:24", "Lorenz Aebi",
                        "git@xca.ch", "a8f5fc0e6c1ba43878502e0e983dba5cc966b5b0", 1));

        table.filter("AndroidLint.java");
        assertThat(table.getInfo()).isEqualTo("Showing 1 to 1 of 1 entries (filtered from 3 total entries)");
        assertThat(table.getRows()).containsExactly(
                new SourceControlRow("Test Warning for Jenkins", "AndroidLint.java:10", "Ulli Hafner",
                        "ullrich.hafner@gmail.com", "762e9bf397cb7c4f9ec954d97e464884ed33ded5", 1));
    }

    /**
     * Test for eleven files being listed and attributed correctly. Tests pagination.
     *
     * @throws IOException
     *         if the scm can't be set
     */
    // TODO: don't use a remote git repository in the future, since e.g. the author or commitId might change
    @Test
    public void shouldListElevenFiles() throws IOException {
        FreeStyleProject project = createFreeStyleProject();
        project.setScm(new GitSCM("https://github.com/jenkinsci/warnings-ng-plugin.git"));

        addScriptStep(project,
                "echo \"[javac] src/main/java/io/jenkins/plugins/analysis/warnings/AndroidLint.java:10: warning: Test Warning for Jenkins\" >> test-warnings.custom.txt\n"
                        + "echo \"[javac] src/main/java/io/jenkins/plugins/analysis/warnings/Gendarme.java:39: warning: Test Warning for Jenkins\" >> test-warnings.custom.txt\n"
                        + "echo \"[javac] src/main/java/io/jenkins/plugins/analysis/warnings/Gendarme.java:40: warning: Test Warning for Jenkins\" >> test-warnings.custom.txt\n"
                        + "echo \"[javac] src/main/java/io/jenkins/plugins/analysis/warnings/Gendarme.java:41: warning: Test Warning for Jenkins\" >> test-warnings.custom.txt\n"
                        + "echo \"[javac] src/main/java/io/jenkins/plugins/analysis/warnings/Gendarme.java:42: warning: Test Warning for Jenkins\" >> test-warnings.custom.txt\n"
                        + "echo \"[javac] src/main/java/io/jenkins/plugins/analysis/warnings/Gendarme.java:43: warning: Test Warning for Jenkins\" >> test-warnings.custom.txt\n"
                        + "echo \"[javac] src/main/java/io/jenkins/plugins/analysis/warnings/Gendarme.java:44: warning: Test Warning for Jenkins\" >> test-warnings.custom.txt\n"
                        + "echo \"[javac] src/main/java/io/jenkins/plugins/analysis/warnings/Gendarme.java:45: warning: Test Warning for Jenkins\" >> test-warnings.custom.txt\n"
                        + "echo \"[javac] src/main/java/io/jenkins/plugins/analysis/warnings/Gendarme.java:46: warning: Test Warning for Jenkins\" >> test-warnings.custom.txt\n"
                        + "echo \"[javac] src/main/java/io/jenkins/plugins/analysis/warnings/Gendarme.java:47: warning: Test Warning for Jenkins\" >> test-warnings.custom.txt\n"
                        + "echo \"[javac] src/main/java/io/jenkins/plugins/analysis/warnings/IarCstat.java:24: warning: Another Test Warning for Jenkins\" >> test-warnings.custom.txt");

        Java javaJob = new Java();
        javaJob.setPattern("**/*.custom.txt");
        enableWarnings(project, javaJob);

        AnalysisResult result = scheduleSuccessfulBuild(project);

        HtmlPage page = getWebPage(JavaScriptSupport.JS_ENABLED, result);

        SourceControlTable table = new SourceControlTable(page);

        assertThat(result.getTotalSize()).isEqualTo(11);
        assertThat(table.getInfo()).isEqualTo("Showing 1 to 10 of 11 entries");

        assertThat(table.getColumnNames()).containsExactly(SourceControlRow.DETAILS, SourceControlRow.FILE,
                SourceControlRow.AGE, SourceControlRow.AUTHOR, SourceControlRow.EMAIL, SourceControlRow.COMMIT);
        assertThat(table.getRows()).containsExactly(
                new SourceControlRow("Test Warning for Jenkins", "AndroidLint.java:10", "Ulli Hafner",
                        "ullrich.hafner@gmail.com", "762e9bf397cb7c4f9ec954d97e464884ed33ded5", 1),
                new SourceControlRow("Test Warning for Jenkins", "Gendarme.java:39", "Ulli Hafner",
                        "ullrich.hafner@gmail.com", "f9fd0154b84b7956047b7aad87c87847dac4cf20", 1),
                new SourceControlRow("Test Warning for Jenkins", "Gendarme.java:40", "Ulli Hafner",
                        "ullrich.hafner@gmail.com", "f9fd0154b84b7956047b7aad87c87847dac4cf20", 1),
                new SourceControlRow("Test Warning for Jenkins", "Gendarme.java:41", "Ulli Hafner",
                        "ullrich.hafner@gmail.com", "f9fd0154b84b7956047b7aad87c87847dac4cf20", 1),
                new SourceControlRow("Test Warning for Jenkins", "Gendarme.java:42", "Ulli Hafner",
                        "ullrich.hafner@gmail.com", "1567466fc616d5d367937c928c0ddedf729eeafb", 1),
                new SourceControlRow("Test Warning for Jenkins", "Gendarme.java:43", "Ulli Hafner",
                        "ullrich.hafner@gmail.com", "f9fd0154b84b7956047b7aad87c87847dac4cf20", 1),
                new SourceControlRow("Test Warning for Jenkins", "Gendarme.java:44", "Ulli Hafner",
                        "ullrich.hafner@gmail.com", "f9fd0154b84b7956047b7aad87c87847dac4cf20", 1),
                new SourceControlRow("Test Warning for Jenkins", "Gendarme.java:45", "Ulli Hafner",
                        "ullrich.hafner@gmail.com", "f9fd0154b84b7956047b7aad87c87847dac4cf20", 1),
                new SourceControlRow("Test Warning for Jenkins", "Gendarme.java:46", "Ulli Hafner",
                        "ullrich.hafner@gmail.com", "f9fd0154b84b7956047b7aad87c87847dac4cf20", 1),
                new SourceControlRow("Test Warning for Jenkins", "Gendarme.java:47", "Ulli Hafner",
                        "ullrich.hafner@gmail.com", "997f4f938318310744366575e1a32d352c21a25c", 1));

        table.goToPage(2);
        assertThat(table.getInfo()).isEqualTo("Showing 11 to 11 of 11 entries");
        assertThat(table.getRows()).containsExactly(
                new SourceControlRow("Another Test Warning for Jenkins", "IarCstat.java:24", "Lorenz Aebi",
                        "git@xca.ch", "a8f5fc0e6c1ba43878502e0e983dba5cc966b5b0", 1));

    }
}

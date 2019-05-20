package io.jenkins.plugins.analysis.warnings.recorder;

import java.io.IOException;
import java.util.Collections;

import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import hudson.model.FreeStyleProject;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.SubmoduleConfig;
import hudson.plugins.git.extensions.GitSCMExtension;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.warnings.Java;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.DetailsTab;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.DetailsTab.TabType;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.SourceControlRow;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.SourceControlTable;

import static io.jenkins.plugins.analysis.warnings.recorder.pageobj.SourceControlRow.*;
import static org.assertj.core.api.Assertions.*;

/**
 * Integration test of the {@link SourceControlTable}.
 *
 * @author Fabian Janker
 * @author Andreas Pabst
 */
public class SourceControlTableITest extends IntegrationTestWithJenkinsPerSuite {
    /**
     * Test for one file being listed and attributed correctly.
     */
    @Test
    public void shouldListAFile() {
        FreeStyleProject project = createJob();

        addScriptStep(project,
                escape("echo [javac] src/main/java/io/jenkins/plugins/analysis/warnings/AndroidLint.java:10: warning: Test Warning for Jenkins >> test-warnings.custom.txt"));

        Java javaJob = new Java();
        javaJob.setPattern("**/*.custom.txt");
        enableWarnings(project, javaJob);

        AnalysisResult result = scheduleSuccessfulBuild(project);

        SourceControlTable table = getSourceControlTable(result);

        assertThat(result.getTotalSize()).isEqualTo(1);
        assertThat(table.getInfo()).isEqualTo("Showing 1 to 1 of 1 entries");
        assertThat(table.getColumnNames())
                .containsExactly(DETAILS, FILE, AGE, AUTHOR, EMAIL, COMMIT);
        assertThat(table.getRows()).containsExactly(
                new SourceControlRow("Test Warning for Jenkins", "AndroidLint.java:10", "Ulli Hafner",
                        "ullrich.hafner@gmail.com", "8fdcabb3a74491e32d049db3de4223c43c98d81f", 1));
    }

    /**
     * Test for three files being listed and attributed correctly.
     */
    @Test
    public void shouldListThreeFiles() {
        FreeStyleProject project = createJob();

        addScriptStep(project, escape("echo [javac] src/main/java/io/jenkins/plugins/analysis/warnings/AndroidLint.java:10: warning: Test Warning for Jenkins >> test-warnings.custom.txt\n")
                        + escape("echo [javac] src/main/java/io/jenkins/plugins/analysis/warnings/Gendarme.java:39: warning: Test Warning for Jenkins >> test-warnings.custom.txt\n")
                        + escape("echo [javac] src/main/java/io/jenkins/plugins/analysis/warnings/IarCstat.java:24: warning: Another Test Warning for Jenkins >> test-warnings.custom.txt"));

        Java javaJob = new Java();
        javaJob.setPattern("**/*.custom.txt");
        enableWarnings(project, javaJob);

        AnalysisResult result = scheduleSuccessfulBuild(project);

        SourceControlTable table = getSourceControlTable(result);

        assertThat(result.getTotalSize()).isEqualTo(3);
        assertThat(table.getInfo()).isEqualTo("Showing 1 to 3 of 3 entries");
        assertThat(table.getColumnNames())
                .containsExactly(DETAILS, FILE, AGE, AUTHOR, EMAIL, COMMIT);
        assertThat(table.getRows()).containsExactly(
                new SourceControlRow("Test Warning for Jenkins", "AndroidLint.java:10", "Ulli Hafner",
                        "ullrich.hafner@gmail.com", "8fdcabb3a74491e32d049db3de4223c43c98d81f", 1),
                new SourceControlRow("Test Warning for Jenkins", "Gendarme.java:39", "Ulli Hafner",
                        "ullrich.hafner@gmail.com", "f9fd0154b84b7956047b7aad87c87847dac4cf20", 1),
                new SourceControlRow("Another Test Warning for Jenkins", "IarCstat.java:24", "Lorenz Aebi",
                        "git@xca.ch", "a8f5fc0e6c1ba43878502e0e983dba5cc966b5b0", 1));
    }

    /**
     * Test filtering in the table.
     */
    @Test
    public void shouldFilter() {
        FreeStyleProject project = createJob();

        addScriptStep(project, escape("echo [javac] src/main/java/io/jenkins/plugins/analysis/warnings/AndroidLint.java:10: warning: Test Warning for Jenkins >> test-warnings.custom.txt\n")
                        + escape("echo [javac] src/main/java/io/jenkins/plugins/analysis/warnings/Gendarme.java:39: warning: Test Warning for Jenkins >> test-warnings.custom.txt\n")
                        + escape("echo [javac] src/main/java/io/jenkins/plugins/analysis/warnings/IarCstat.java:24: warning: Another Test Warning for Jenkins >> test-warnings.custom.txt"));

        Java javaJob = new Java();
        javaJob.setPattern("**/*.custom.txt");
        enableWarnings(project, javaJob);

        AnalysisResult result = scheduleSuccessfulBuild(project);

        SourceControlTable table = getSourceControlTable(result);

        assertThat(result.getTotalSize()).isEqualTo(3);
        assertThat(table.getInfo()).isEqualTo("Showing 1 to 3 of 3 entries");
        assertThat(table.getColumnNames())
                .containsExactly(DETAILS, FILE, AGE, AUTHOR, EMAIL, COMMIT);
        assertThat(table.getRows()).containsExactly(
                new SourceControlRow("Test Warning for Jenkins", "AndroidLint.java:10", "Ulli Hafner",
                        "ullrich.hafner@gmail.com", "8fdcabb3a74491e32d049db3de4223c43c98d81f", 1),
                new SourceControlRow("Test Warning for Jenkins", "Gendarme.java:39", "Ulli Hafner",
                        "ullrich.hafner@gmail.com", "f9fd0154b84b7956047b7aad87c87847dac4cf20", 1),
                new SourceControlRow("Another Test Warning for Jenkins", "IarCstat.java:24", "Lorenz Aebi",
                        "git@xca.ch", "a8f5fc0e6c1ba43878502e0e983dba5cc966b5b0", 1));

        table.filter("AndroidLint.java");
        assertThat(table.getInfo()).isEqualTo("Showing 1 to 1 of 1 entries (filtered from 3 total entries)");
        assertThat(table.getRows()).containsExactly(
                new SourceControlRow("Test Warning for Jenkins", "AndroidLint.java:10", "Ulli Hafner",
                        "ullrich.hafner@gmail.com", "8fdcabb3a74491e32d049db3de4223c43c98d81f", 1));
    }

    /**
     * Test for eleven files being listed and attributed correctly. Tests pagination.
     */
    @Test
    public void shouldListElevenFiles() {
        FreeStyleProject project = createJob();

        addScriptStep(project,
                escape("echo [javac] src/main/java/io/jenkins/plugins/analysis/warnings/AndroidLint.java:10: warning: Test Warning for Jenkins >> test-warnings.custom.txt\n")
                        + escape("echo [javac] src/main/java/io/jenkins/plugins/analysis/warnings/Gendarme.java:39: warning: Test Warning for Jenkins >> test-warnings.custom.txt\n")
                        + escape("echo [javac] src/main/java/io/jenkins/plugins/analysis/warnings/Gendarme.java:40: warning: Test Warning for Jenkins >> test-warnings.custom.txt\n")
                        + escape("echo [javac] src/main/java/io/jenkins/plugins/analysis/warnings/Gendarme.java:41: warning: Test Warning for Jenkins >> test-warnings.custom.txt\n")
                        + escape("echo [javac] src/main/java/io/jenkins/plugins/analysis/warnings/Gendarme.java:42: warning: Test Warning for Jenkins >> test-warnings.custom.txt\n")
                        + escape("echo [javac] src/main/java/io/jenkins/plugins/analysis/warnings/Gendarme.java:43: warning: Test Warning for Jenkins >> test-warnings.custom.txt\n")
                        + escape("echo [javac] src/main/java/io/jenkins/plugins/analysis/warnings/Gendarme.java:44: warning: Test Warning for Jenkins >> test-warnings.custom.txt\n")
                        + escape("echo [javac] src/main/java/io/jenkins/plugins/analysis/warnings/Gendarme.java:45: warning: Test Warning for Jenkins >> test-warnings.custom.txt\n")
                        + escape("echo [javac] src/main/java/io/jenkins/plugins/analysis/warnings/Gendarme.java:46: warning: Test Warning for Jenkins >> test-warnings.custom.txt\n")
                        + escape("echo [javac] src/main/java/io/jenkins/plugins/analysis/warnings/Gendarme.java:47: warning: Test Warning for Jenkins >> test-warnings.custom.txt\n")
                        + escape("echo [javac] src/main/java/io/jenkins/plugins/analysis/warnings/IarCstat.java:24: warning: Another Test Warning for Jenkins >> test-warnings.custom.txt"));

        Java javaJob = new Java();
        javaJob.setPattern("**/*.custom.txt");
        enableWarnings(project, javaJob);

        AnalysisResult result = scheduleSuccessfulBuild(project);

        SourceControlTable table = getSourceControlTable(result);

        assertThat(result.getTotalSize()).isEqualTo(11);
        assertThat(table.getInfo()).isEqualTo("Showing 1 to 10 of 11 entries");

        assertThat(table.getColumnNames())
                .containsExactly(DETAILS, FILE, AGE, AUTHOR, EMAIL, COMMIT);
        assertThat(table.getRows()).containsExactly(
                new SourceControlRow("Test Warning for Jenkins", "AndroidLint.java:10", "Ulli Hafner",
                        "ullrich.hafner@gmail.com", "8fdcabb3a74491e32d049db3de4223c43c98d81f", 1),
                new SourceControlRow("Test Warning for Jenkins", "Gendarme.java:39", "Ulli Hafner",
                        "ullrich.hafner@gmail.com", "f9fd0154b84b7956047b7aad87c87847dac4cf20", 1),
                new SourceControlRow("Test Warning for Jenkins", "Gendarme.java:40", "Ulli Hafner",
                        "ullrich.hafner@gmail.com", "f9fd0154b84b7956047b7aad87c87847dac4cf20", 1),
                new SourceControlRow("Test Warning for Jenkins", "Gendarme.java:41", "Ulli Hafner",
                        "ullrich.hafner@gmail.com", "f9fd0154b84b7956047b7aad87c87847dac4cf20", 1),
                new SourceControlRow("Test Warning for Jenkins", "Gendarme.java:42", "Ulli Hafner",
                        "ullrich.hafner@gmail.com", "f9fd0154b84b7956047b7aad87c87847dac4cf20", 1),
                new SourceControlRow("Test Warning for Jenkins", "Gendarme.java:43", "Ulli Hafner",
                        "ullrich.hafner@gmail.com", "1567466fc616d5d367937c928c0ddedf729eeafb", 1),
                new SourceControlRow("Test Warning for Jenkins", "Gendarme.java:44", "Ulli Hafner",
                        "ullrich.hafner@gmail.com", "f9fd0154b84b7956047b7aad87c87847dac4cf20", 1),
                new SourceControlRow("Test Warning for Jenkins", "Gendarme.java:45", "Ulli Hafner",
                        "ullrich.hafner@gmail.com", "f9fd0154b84b7956047b7aad87c87847dac4cf20", 1),
                new SourceControlRow("Test Warning for Jenkins", "Gendarme.java:46", "Ulli Hafner",
                        "ullrich.hafner@gmail.com", "f9fd0154b84b7956047b7aad87c87847dac4cf20", 1),
                new SourceControlRow("Test Warning for Jenkins", "Gendarme.java:47", "Ulli Hafner",
                        "ullrich.hafner@gmail.com", "f9fd0154b84b7956047b7aad87c87847dac4cf20", 1));

        table.goToPage(2);
        assertThat(table.getInfo()).isEqualTo("Showing 11 to 11 of 11 entries");
        assertThat(table.getRows()).containsExactly(
                new SourceControlRow("Another Test Warning for Jenkins", "IarCstat.java:24", "Lorenz Aebi",
                        "git@xca.ch", "a8f5fc0e6c1ba43878502e0e983dba5cc966b5b0", 1));

    }

    private FreeStyleProject createJob() {
        try {
            FreeStyleProject project = createFreeStyleProject();
            // FIXME: use 'fake' git instead of real repository + then also use a pipeline
            GitSCM scm = new GitSCM(
                    GitSCM.createRepoList("https://github.com/jenkinsci/warnings-ng-plugin.git", null),
                    Collections.emptyList(), false, Collections.<SubmoduleConfig>emptyList(),
                    null, null, Collections.<GitSCMExtension>emptyList());
            project.setScm(scm);
            return project;
        }
        catch (IOException exception) {
            throw new AssertionError(exception);
        }
    }

    private SourceControlTable getSourceControlTable(final AnalysisResult result) {
        HtmlPage page = getWebPage(JavaScriptSupport.JS_ENABLED, result);

        DetailsTab detailsTab = new DetailsTab(page);
        assertThat(detailsTab.getTabTypes()).contains(TabType.BLAMES);

        return detailsTab.select(TabType.BLAMES);
    }

    private String escape(final String message) {
        if (!isWindows()) {
            return message
                    .replace("[", "\\[")
                    .replace("]", "\\]");
        }
        return message;
    }
}

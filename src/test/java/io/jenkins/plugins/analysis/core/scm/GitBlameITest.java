package io.jenkins.plugins.analysis.core.scm;

import java.util.List;

import org.junit.ClassRule;
import org.junit.Test;

import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.plugins.git.GitSCM;
import jenkins.plugins.git.GitSampleRepoRule;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
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

    /**
     * Local git integration for testing purposes.
     */
    @ClassRule
    public static GitSampleRepoRule repository = new GitSampleRepoRule();

    /**
     * Creates a repository with a single file that is changed by two committer.
     * Afterwards the plugin has to record correct blame information for arbitrary issues.
     * @throws Exception When initializing git fails.
     */
    @Test
    public void shouldReadCorrectBlameInformation() throws Exception {
        String file = "opentasks.txt";
        String issuesFile = "issues.txt";
        repository.init();
        repository.git("checkout", "master");
        // make first change as ALICE
        repository.git("config", "user.name", "Alice");
        repository.git("config", "user.email", "alice@example.com");
        repository.write(file, "Line 1\nLine 2\n");
        repository.git("add", file);
        repository.git("commit", "-m", "init opentasks", file);
        // second change as BOB
        repository.git("config", "user.name", "Bob");
        repository.git("config", "user.email", "bob@example.com");
        repository.write(file, "Line 1\nLine 2 but better\n");
        repository.git("add", file);
        repository.git("commit", "-m", "update opentasks", file);
        // add issues.txt to avoid copying it by hand
        repository.write(issuesFile, "[WARNING] opentasks.txt:[1,0] [deprecation] something has been deprecated\n"
                + "[WARNING] opentasks.txt:[2,0] [deprecation] something else has been deprecated too\n");
        repository.git("add", issuesFile);
        repository.git("commit", "-m", "add issue file", issuesFile);

        FreeStyleProject job = createFreeStyleProject();
        job.setScm(new GitSCM(repository.fileUrl()));
        enableGenericWarnings(job, new Java());


        AnalysisResult result = scheduleBuildAndAssertStatus(job, Result.SUCCESS);
        SourceControlTable blames = new DetailsTab(getWebPage(JS_ENABLED, result)).select(BLAMES);


        assertThat(result.getErrorMessages()).isEmpty();
        assertThat(result.getInfoMessages()).contains("-> found 2 issues (skipped 0 duplicates)",
                "-> blamed authors of issues in 1 files");

        List<SourceControlRow> rows = blames.getRows();
        assertThat(rows.get(0).getValue(AUTHOR)).isEqualTo("Alice");
        assertThat(rows.get(0).getValue(EMAIL)).isEqualTo("alice@example.com");
        assertThat(rows.get(0).getValue(DETAILS_CONTENT)).isEqualTo("something has been deprecated");

        assertThat(rows.get(1).getValue(AUTHOR)).isEqualTo("Bob");
        assertThat(rows.get(1).getValue(EMAIL)).isEqualTo("bob@example.com");
        assertThat(rows.get(1).getValue(DETAILS_CONTENT)).isEqualTo("something else has been deprecated too");
    }

}

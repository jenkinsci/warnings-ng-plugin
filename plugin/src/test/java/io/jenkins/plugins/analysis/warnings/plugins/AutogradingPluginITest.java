package io.jenkins.plugins.analysis.warnings.plugins;

import java.util.List;

import org.junit.Test;

import hudson.model.FreeStyleProject;
import hudson.model.Run;

import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.warnings.Cpd;
import io.jenkins.plugins.analysis.warnings.Java;
import io.jenkins.plugins.analysis.warnings.Pmd;
import io.jenkins.plugins.analysis.warnings.SpotBugs;
import io.jenkins.plugins.analysis.warnings.checkstyle.CheckStyle;
import io.jenkins.plugins.grading.AggregatedScore;
import io.jenkins.plugins.grading.AutoGrader;
import io.jenkins.plugins.grading.AutoGradingBuildAction;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

/**
 * This class tests the compatibility between the warnings-ng and the autograding plugin.
 *
 * @author Lion Kosiuk
 */
public class AutogradingPluginITest extends IntegrationTestWithJenkinsPerSuite {

    private static final String AUTOGRADER_RESULT = "{\"analysis\":{\"maxScore\":100,\"errorImpact\":-10,\"highImpact\":-5,\"normalImpact\":-2,\"lowImpact\":-1}}";

    /**
     * Ensures that the autographing plugin outputs the expected result after passing the checks.
     */
    @Test
    public void shouldCheckCompatibility() {
        FreeStyleProject project = createJavaWarningsFreestyleProject("checkstyle.xml", "spotbugs.xml", "cpd.xml", "pmd.xml");

        IssuesRecorder recorder = new IssuesRecorder();

        CheckStyle checkStyle = new CheckStyle();
        checkStyle.setPattern("**/*checkstyle*");

        SpotBugs spotBugs = new SpotBugs();
        spotBugs.setPattern("**/spotbugs*");

        Cpd cpd = new Cpd();
        cpd.setPattern("**/cpd*");

        Pmd pmd = new Pmd();
        pmd.setPattern("**/pmd");

        recorder.setTools(checkStyle);
        recorder.setTools(spotBugs);
        recorder.setTools(cpd);

        project.getPublishersList().add(recorder);
        project.getPublishersList().add(new AutoGrader(AUTOGRADER_RESULT));

        Run<?, ?> baseline = buildSuccessfully(project);

        List<AutoGradingBuildAction> actions = baseline.getActions(AutoGradingBuildAction.class);
        assertThat(actions).hasSize(1);
        AggregatedScore score = actions.get(0).getResult();
        assertThat(score.getAchieved()).isEqualTo(98);
    }

    /**
     * Create a Freestyle Project with enabled Java warnings.
     *
     * @param files
     *         The files to be imported into the Freestyle project.
     *
     * @return The created Freestyle Project.
     */
    private FreeStyleProject createJavaWarningsFreestyleProject(final String... files) {
        FreeStyleProject project = createFreeStyleProjectWithWorkspaceFiles(files);
        Java java = new Java();
        for(String file: files) {
            java.setPattern("**/*" + file + "*");
        }
        enableWarnings(project, java);
        return project;
    }
}

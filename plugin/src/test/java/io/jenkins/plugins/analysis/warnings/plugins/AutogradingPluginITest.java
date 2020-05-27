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
import io.jenkins.plugins.grading.AnalysisScore;
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
     * Ensures that the autographing plugin outputs the expected score after passing the checks.
     * Used tools: checkstyle, spotbugs, cpd, pmd
     */
    @Test
    public void checksCorrectGradingWithSeveralTools() {
        FreeStyleProject project = createFreeStyleProjectWithWorkspaceFiles("checkstyle.xml", "spotbugs.xml", "cpd.xml", "pmd.xml");

        IssuesRecorder recorder = new IssuesRecorder();

        CheckStyle checkStyle = new CheckStyle();
        checkStyle.setPattern("**/*checkstyle*");

        SpotBugs spotBugs = new SpotBugs();
        spotBugs.setPattern("**/spotbugs*");

        Cpd cpd = new Cpd();
        cpd.setPattern("**/cpd*");

        Pmd pmd = new Pmd();
        pmd.setPattern("**/pmd*");

        recorder.setTools(checkStyle, spotBugs, cpd, pmd);

        project.getPublishersList().add(recorder);
        project.getPublishersList().add(new AutoGrader(AUTOGRADER_RESULT));

        Run<?, ?> baseline = buildSuccessfully(project);

        List<AutoGradingBuildAction> actions = baseline.getActions(AutoGradingBuildAction.class);
        assertThat(actions).hasSize(1);
        AggregatedScore score = actions.get(0).getResult();
        List<AnalysisScore> analysisScore = score.getAnalysisScores();
        assertThat(score.getAchieved()).isEqualTo(22);
        assertThat(analysisScore).hasSize(4);
        assertThat(analysisScore.get(0).getTotalImpact()).isEqualTo(-60);
        assertThat(analysisScore.get(1).getTotalImpact()).isEqualTo(-4);
        assertThat(analysisScore.get(2).getTotalImpact()).isEqualTo(-2);
        assertThat(analysisScore.get(3).getTotalImpact()).isEqualTo(-12);
    }
}

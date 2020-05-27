package io.jenkins.plugins.analysis.warnings.plugins;

import java.util.List;

import org.junit.Test;

import edu.hm.hafner.grading.AggregatedScore;
import edu.hm.hafner.grading.AnalysisScore;

import hudson.model.FreeStyleProject;
import hudson.model.Run;

import io.jenkins.plugins.analysis.core.model.ResultAction;
import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.warnings.Cpd;
import io.jenkins.plugins.analysis.warnings.Pmd;
import io.jenkins.plugins.analysis.warnings.SpotBugs;
import io.jenkins.plugins.analysis.warnings.checkstyle.CheckStyle;
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
    private static final String PMD = "pmd";
    private static final String CPD = "cpd";
    private static final String SPOTBUGS = "spotbugs";
    private static final String CHECKSTYLE = "checkstyle";

    /**
     * Ensures that the autographing plugin outputs the expected score after passing the checks.
     * Used tools: CheckStyle, SpotBugs, CPD, and PMD.
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
        List<ResultAction> analysisActions = baseline.getActions(ResultAction.class);

        AggregatedScore score = actions.get(0).getResult();
        List<AnalysisScore> analysisScore = score.getAnalysisScores();
        assertThat(score.getAchieved()).isEqualTo(22);

        assertThat(analysisScore).hasSize(4);

        ResultAction checkStyleAction = analysisActions.get(0);
        assertThat(checkStyleAction.getId()).isEqualTo(CHECKSTYLE);
        assertThat(checkStyleAction.getResult()).hasTotalErrorsSize(6);
        assertThat(analysisScore.get(0).getId()).isEqualTo(CHECKSTYLE);
        assertThat(analysisScore.get(0).getTotalImpact()).isEqualTo(-60);

        ResultAction spotBugsAction = analysisActions.get(1);
        assertThat(spotBugsAction.getId()).isEqualTo(SPOTBUGS);
        assertThat(spotBugsAction.getResult()).hasTotalNormalPrioritySize(2);
        assertThat(analysisScore.get(1).getId()).isEqualTo(SPOTBUGS);
        assertThat(analysisScore.get(1).getTotalImpact()).isEqualTo(-4);

        ResultAction cpdAction = analysisActions.get(2);
        assertThat(cpdAction.getId()).isEqualTo(CPD);
        assertThat(cpdAction.getResult()).hasTotalLowPrioritySize(2);
        assertThat(analysisScore.get(2).getId()).isEqualTo(CPD);
        assertThat(analysisScore.get(2).getTotalImpact()).isEqualTo(-2);

        ResultAction pmdAction = analysisActions.get(3);
        assertThat(pmdAction.getId()).isEqualTo(PMD);
        assertThat(pmdAction.getResult()).hasTotalErrorsSize(1);
        assertThat(pmdAction.getResult()).hasTotalNormalPrioritySize(1);
        assertThat(analysisScore.get(3).getId()).isEqualTo(PMD);
        assertThat(analysisScore.get(3).getTotalImpact()).isEqualTo(-12);
    }
}

package io.jenkins.plugins.analysis.warnings.integrations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jenkins_ci.plugins.flexible_publish.ConditionalPublisher;
import org.jenkins_ci.plugins.flexible_publish.FlexiblePublisher;
import org.jenkins_ci.plugins.run_condition.BuildStepRunner.Run;
import org.jenkins_ci.plugins.run_condition.core.AlwaysRun;
import org.junit.jupiter.api.Test;

import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.tasks.BuildStep;

import io.jenkins.plugins.analysis.core.filter.ExcludeFile;
import io.jenkins.plugins.analysis.core.filter.RegexpFilter;
import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.core.util.WarningsQualityGate;
import io.jenkins.plugins.analysis.core.util.WarningsQualityGate.QualityGateType;
import io.jenkins.plugins.analysis.warnings.CheckStyle;
import io.jenkins.plugins.analysis.warnings.Java;
import io.jenkins.plugins.util.QualityGate.QualityGateCriticality;
import io.jenkins.plugins.util.QualityGateStatus;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

/**
 * Test the flexible publish plugin in combination with the warnings-ng-plugin.
 *
 * @author Tobias Redl
 * @author Andreas Neumeier
 */
class FlexiblePublishITest extends IntegrationTestWithJenkinsPerSuite {
    private static final String JAVA_WARNINGS = "java-start.txt";
    private static final String CHECKSTYLE_WARNINGS = "checkstyle.xml";

    /** Test that different tools can be configured with different settings. */
    @Test
    void shouldAnalyseTwoToolsWithDifferentSettings() {
        FreeStyleProject project = createFreeStyleProjectWithWorkspaceFilesWithSuffix(CHECKSTYLE_WARNINGS,
                JAVA_WARNINGS);

        CheckStyle checkStyle = new CheckStyle();
        checkStyle.setPattern("**/checkstyle*");
        IssuesRecorder checkStyleRecorder = new IssuesRecorder();
        checkStyleRecorder.setTools(checkStyle);
        checkStyleRecorder.setQualityGates(List.of(
                new WarningsQualityGate(6, QualityGateType.TOTAL, QualityGateCriticality.FAILURE)));

        Java java = new Java();
        java.setPattern("**/java*");
        IssuesRecorder javaRecorder = new IssuesRecorder();
        javaRecorder.setTools(java);
        javaRecorder.setEnabledForFailure(true);
        javaRecorder.setQualityGates(List.of(
                new WarningsQualityGate(2, QualityGateType.TOTAL, QualityGateCriticality.UNSTABLE)));

        project.getPublishersList().add(new FlexiblePublisher(Arrays.asList(
                constructConditionalPublisher(checkStyleRecorder),
                constructConditionalPublisher(javaRecorder)
        )));

        List<AnalysisResult> results = getAnalysisResults(buildWithResult(project, Result.FAILURE));
        assertThat(results).hasSize(2);

        AnalysisResult checkStyleResult = results.get(0);
        assertThat(checkStyleResult).hasId(checkStyle.getActualId());
        assertThat(checkStyleResult).hasQualityGateStatus(QualityGateStatus.FAILED);
        assertThat(checkStyleResult).hasTotalSize(6);

        AnalysisResult javaResult = results.get(1);
        assertThat(javaResult).hasId(java.getActualId());
        assertThat(javaResult).hasQualityGateStatus(QualityGateStatus.WARNING);
        assertThat(javaResult).hasTotalSize(2);

        checkStyleRecorder.setFilters(createFileExcludeFilter("\\.java$"));

        results = getAnalysisResults(buildWithResult(project, Result.UNSTABLE));
        assertThat(results).hasSize(2);

        checkStyleResult = results.get(0);
        assertThat(checkStyleResult).hasId(checkStyle.getActualId());
        assertThat(checkStyleResult).hasQualityGateStatus(QualityGateStatus.PASSED);
        assertThat(checkStyleResult).hasTotalSize(0);

        javaResult = results.get(1);
        assertThat(javaResult).hasId(java.getActualId());
        assertThat(javaResult).hasQualityGateStatus(QualityGateStatus.WARNING);
        assertThat(javaResult).hasTotalSize(2);
    }

    private List<RegexpFilter> createFileExcludeFilter(final String pattern) {
        RegexpFilter filter = new ExcludeFile(pattern);
        List<RegexpFilter> filterList = new ArrayList<>();
        filterList.add(filter);
        return filterList;
    }

    private ConditionalPublisher constructConditionalPublisher(final BuildStep publisher) {
        return new ConditionalPublisher(
                new AlwaysRun(),
                Collections.singletonList(publisher),
                new Run(),
                false,
                null,
                null,
                null
        );
    }
}

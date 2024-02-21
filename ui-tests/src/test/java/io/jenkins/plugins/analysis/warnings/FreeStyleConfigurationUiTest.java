package io.jenkins.plugins.analysis.warnings;

import org.junit.Test;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;

import io.jenkins.plugins.analysis.warnings.IssuesRecorder.QualityGateCriticality;
import io.jenkins.plugins.analysis.warnings.IssuesRecorder.QualityGateType;
import io.jenkins.plugins.analysis.warnings.IssuesRecorder.SourceCodeRetention;
import io.jenkins.plugins.analysis.warnings.IssuesRecorder.TrendChartType;

import static io.jenkins.plugins.analysis.warnings.Assertions.*;

/**
 * Verifies the freestyle UI configuration of the {@link IssuesRecorder}.
 *
 * @author Ullrich Hafner
 */
@WithPlugins("warnings-ng")
public class FreeStyleConfigurationUiTest extends AbstractJUnitTest {
    private static final String PATTERN = "**/*.txt";
    private static final String ENCODING = "UTF-8";
    private static final String SOURCE_DIRECTORY = "relative";
    private static final String SEVERITY = "NORMAL";
    private static final String REGEX = "testRegex";
    private static final String SCM = "SCM";

    /**
     * Verifies that job configuration screen correctly modifies the properties of an {@link IssuesRecorder} instance.
     */
    @Test @SuppressWarnings({"checkstyle:JavaNCSS", "PMD.NcssCount"})
    public void shouldSetPropertiesInJobConfiguration() {
        FreeStyleJob job = jenkins.getJobs().create(FreeStyleJob.class);

        IssuesRecorder issuesRecorder = job.addPublisher(IssuesRecorder.class, recorder -> recorder.setTool("Eclipse ECJ"));

        issuesRecorder.setSourceCodeEncoding(ENCODING);
        issuesRecorder.addSourceDirectory(SOURCE_DIRECTORY);
        issuesRecorder.setScm(SCM);
        issuesRecorder.setAggregatingResults(true);
        issuesRecorder.setTrendChartType(TrendChartType.TOOLS_ONLY);
        issuesRecorder.setSkipBlames(true);
        issuesRecorder.setEnabledForFailure(true);
        issuesRecorder.setIgnoreQualityGate(true);
        issuesRecorder.setSkipPublishingChecks(true);
        issuesRecorder.setSkipPostProcessing(true);
        issuesRecorder.setPublishAllIssues(true);
        issuesRecorder.setFailOnError(true);
        issuesRecorder.setQuiet(true);
        issuesRecorder.setHealthReport(1, 9, SEVERITY);
        issuesRecorder.setReportFilePattern(PATTERN);
        issuesRecorder.addIssueFilter("Exclude categories", REGEX);
        issuesRecorder.addQualityGateConfiguration(1, QualityGateType.TOTAL_ERROR, QualityGateCriticality.UNSTABLE);
        issuesRecorder.setSourceCodeRetention(SourceCodeRetention.MODIFIED);

        job.save();
        job.configure();
        issuesRecorder.openAdvancedOptions();

        assertThat(issuesRecorder).hasSourceCodeEncoding(ENCODING);
        assertThat(issuesRecorder).hasSourceDirectories(SOURCE_DIRECTORY);
        assertThat(issuesRecorder).hasScm(SCM);
        assertThat(issuesRecorder).isAggregatingResults();
        assertThat(issuesRecorder).hasTrendChartType(TrendChartType.TOOLS_ONLY.toString());
        assertThat(issuesRecorder).isSkipBlames();
        assertThat(issuesRecorder).isEnabledForFailure();
        assertThat(issuesRecorder).isIgnoringQualityGate();
        assertThat(issuesRecorder).isSkipPublishingChecks();
        assertThat(issuesRecorder).isSkipPostProcessing();
        assertThat(issuesRecorder).isPublishAllIssues();
        assertThat(issuesRecorder).isFailingOnError();
        assertThat(issuesRecorder).isQuiet();
        assertThat(issuesRecorder).hasHealthThreshold("1");
        assertThat(issuesRecorder).hasUnhealthyThreshold("9");
        assertThat(issuesRecorder).hasHealthSeverity(SEVERITY);
        assertThat(issuesRecorder).hasReportFilePattern(PATTERN);
        assertThat(issuesRecorder).hasFilterRegex(REGEX);
        assertThat(issuesRecorder).hasQualityGateThreshold("1");
        assertThat(issuesRecorder).hasQualityGateType(QualityGateType.TOTAL_ERROR.toString());
        assertThat(issuesRecorder).hasQualityGateCriticality(QualityGateCriticality.UNSTABLE.toString());
        assertThat(issuesRecorder).hasSourceCodeRetention(SourceCodeRetention.MODIFIED.toString());

        // Now invert all booleans:
        issuesRecorder.setAggregatingResults(false);
        issuesRecorder.setSkipBlames(false);
        issuesRecorder.setPublishAllIssues(false);
        issuesRecorder.setEnabledForFailure(false);
        issuesRecorder.setIgnoreQualityGate(false);
        issuesRecorder.setFailOnError(false);
        issuesRecorder.setQuiet(false);

        issuesRecorder.addSourceDirectory("second");

        job.save();
        job.configure();
        issuesRecorder.openAdvancedOptions();

        assertThat(issuesRecorder).isNotAggregatingResults();
        assertThat(issuesRecorder).isNotPublishAllIssues();
        assertThat(issuesRecorder).isNotEnabledForFailure();
        assertThat(issuesRecorder).isNotIgnoringQualityGate();
        assertThat(issuesRecorder).isNotFailingOnError();
        assertThat(issuesRecorder).isNotQuiet();

        assertThat(issuesRecorder).hasSourceDirectories(SOURCE_DIRECTORY, "second");
    }
}

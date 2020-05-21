package io.jenkins.plugins.analysis.warnings;

import org.junit.Test;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import io.jenkins.plugins.analysis.warnings.IssuesRecorder.TrendChartType;
import static io.jenkins.plugins.analysis.warnings.Assertions.*;

/**
 * Acceptance tests for the Warnings Next Generation Plugin.

 */
@WithPlugins("warnings-ng")
@SuppressWarnings({"checkstyle:ClassFanOutComplexity", "PMD.SystemPrintln", "PMD.ExcessiveImports"})
public class FreeStyleConfigurationUITest extends AbstractJUnitTest {

    private static final String PATTERN = "**/*.txt";
    private static final String ENCODING = "UTF-8";
    private static final String REFERENCE = "reference";
    private static final String SOURCE_DIRECTORY = "relative";
    private static final String SERVERITY = "NORMAL";

    /**
     * Verifies that job configuration screen correctly modifies the properties of an {@link IssuesRecorder} instance.
     */
    @Test
    public void shouldSetPropertiesInJobConfiguration() {
        FreeStyleJob job = jenkins.getJobs().create(FreeStyleJob.class);


        IssuesRecorder issuesRecorder = job.addPublisher(IssuesRecorder.class, recorder -> {
            recorder.setTool("Eclipse ECJ");
        });

        issuesRecorder.setSourceCodeEncoding(ENCODING);
        issuesRecorder.setSourceDirectory(SOURCE_DIRECTORY);
        issuesRecorder.setAggregatingResults(true);
        issuesRecorder.setTrendChartType(TrendChartType.TOOLS_ONLY);
        issuesRecorder.setBlameDisabled(true);
        issuesRecorder.setForensicsDisabled(true);
        issuesRecorder.setEnabledForFailure(true);
        issuesRecorder.setIgnoreQualityGate(true);
        issuesRecorder.setIgnoreFailedBuilds(true);
        issuesRecorder.setFailOnError(true);
        issuesRecorder.setReferenceJobField(REFERENCE);
        issuesRecorder.setHealthReport(1, 9, SERVERITY);
        issuesRecorder.setReportFilePattern(PATTERN);


        job.save();
        job.configure();
        issuesRecorder.openAdvancedOptions();
        assertThat(issuesRecorder.getSourceCodeEncoding()).isEqualTo(ENCODING);
        assertThat(issuesRecorder.getSourceDirectory()).isEqualTo(SOURCE_DIRECTORY);
        assertThat(issuesRecorder.getAggregatingResults()).isTrue();
        assertThat(issuesRecorder.getTrendChartType()).isEqualTo(TrendChartType.TOOLS_ONLY.toString());
        assertThat(issuesRecorder.getBlameDisabled()).isTrue();
        assertThat(issuesRecorder.getForensicsDisabled()).isTrue();
        assertThat(issuesRecorder.getEnabledForFailure()).isTrue();
        assertThat(issuesRecorder.getIgnoreQualityGate()).isTrue();
        assertThat(issuesRecorder.getIgnoreFailedBuilds()).isTrue();
        assertThat(issuesRecorder.getFailOnError()).isTrue();
        assertThat(issuesRecorder.getReferenceJobField()).isEqualTo(REFERENCE);
        assertThat(issuesRecorder.getHealthThreshold()).isEqualTo("1");
        assertThat(issuesRecorder.getUnhealthyThreshold()).isEqualTo("9");
        assertThat(issuesRecorder.getHealthSeverity()).isEqualTo(SERVERITY);
        assertThat(issuesRecorder.getReportFilePattern()).isEqualTo(PATTERN);

        // Now invert all booleans:
        issuesRecorder.setAggregatingResults(false);
        issuesRecorder.setBlameDisabled(false);
        issuesRecorder.setForensicsDisabled(false);
        issuesRecorder.setEnabledForFailure(false);
        issuesRecorder.setIgnoreQualityGate(false);
        issuesRecorder.setIgnoreFailedBuilds(false);
        issuesRecorder.setFailOnError(false);
        job.save();
        job.configure();
        issuesRecorder.openAdvancedOptions();

        assertThat(issuesRecorder.getAggregatingResults()).isFalse();
        assertThat(issuesRecorder.getBlameDisabled()).isFalse();
        assertThat(issuesRecorder.getForensicsDisabled()).isFalse();
        assertThat(issuesRecorder.getEnabledForFailure()).isFalse();
        assertThat(issuesRecorder.getIgnoreQualityGate()).isFalse();
        assertThat(issuesRecorder.getIgnoreFailedBuilds()).isFalse();
        assertThat(issuesRecorder.getFailOnError()).isFalse();
    }
}



package io.jenkins.plugins.analysis.warnings;


import org.junit.Test;


import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.Job;


import static io.jenkins.plugins.analysis.warnings.Assertions.*;


/**
 * Ui test for the Trend Chart Page.
 *
 * @author Mitja Oldenbourg
 */
@WithPlugins("warnings-ng")
public class TrendChartsUiTest extends AbstractJUnitTest {
    private static final String WARNINGS_PLUGIN_PREFIX = "/";
    private static final String SOURCE_VIEW_FOLDER = WARNINGS_PLUGIN_PREFIX + "trend_charts_tests/";


    /**
     *  Shows new versus fixed TrendCharts.
     */
    @Test
    public void shouldShowNewVersusFixedTrendChartWithBuildDomain() {
        FreeStyleJob job = createFreeStyleJob("javac_warnings.txt");
        job.addPublisher(IssuesRecorder.class, recorder -> recorder.setTool("javac"));
        job.save();
        String jobName = job.name;
        Build build = shouldBuildJobSuccessfully(job);

        AnalysisResult analysisResult = new AnalysisResult(build, jobName);
        TrendChart trendChart = new TrendChart(build, analysisResult.url);
    }


    private FreeStyleJob createFreeStyleJob(final String... resourcesToCopy) {
        FreeStyleJob job = jenkins.jobs.create(FreeStyleJob.class);
        ScrollerUtil.hideScrollerTabBar(driver);
        for (String resource : resourcesToCopy) {
            job.copyResource(SOURCE_VIEW_FOLDER + resource);
        }
        return job;
    }

    private Build shouldBuildJobSuccessfully(final Job job) {
        Build build = job.startBuild().waitUntilFinished();
        assertThat(build.isSuccess()).isTrue();
        return build;
    }

    private void createFileWithJavaWarnings(final FreeStyleJob job, final int... linesWithWarning) {
        StringBuilder warningText = new StringBuilder();
        for (int lineNumber : linesWithWarning) {
            warningText.append(createJavaWarning(lineNumber)).append("\n");
        }
    }

    private String createJavaWarning(final int lineNumber) {
        return String.format(
                "[WARNING] C:\\Path\\SourceFile.java:[%d,42] [deprecation] path.A Class in path has been deprecated%n",
                lineNumber);
    }
}


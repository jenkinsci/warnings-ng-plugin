package io.jenkins.plugins.analysis.warnings;

import org.junit.jupiter.api.Test;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.Job;

import static io.jenkins.plugins.analysis.warnings.Assertions.*;

public class TrendChartsUiTest extends AbstractJUnitTest {
    private static final String WARNINGS_PLUGIN_PREFIX = "/trend_charts_tests/";

    @Test
    public void shouldShowNewVersusFixedTrendChartWithBuildDomain() {
        FreeStyleJob job = createFreeStyleJob("checkstyle.xml", "pmd.xml");
        job.save();
        String jobName = job.name;
        Build build = shouldBuildJobSuccessfully(job);

        TrendChartsTable trendChartsTable = new TrendChartsTable(build, jobName);
    }

    private FreeStyleJob createFreeStyleJob(final String... resourcesToCopy) {
        FreeStyleJob job = jenkins.getJobs().create(FreeStyleJob.class);
        ScrollerUtil.hideScrollerTabBar(driver);
        for (String resource : resourcesToCopy) {
            job.copyResource(WARNINGS_PLUGIN_PREFIX + resource);
        }
        return job;
    }

    private Build shouldBuildJobSuccessfully(final Job job) {
        Build build = job.startBuild().waitUntilFinished();
        assertThat(build.isSuccess()).isTrue();
        return build;
    }
}


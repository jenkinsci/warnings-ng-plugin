package io.jenkins.plugins.analysis.warnings;

import org.junit.Test;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.plugins.dashboard_view.AbstractDashboardViewPortlet;
import org.jenkinsci.test.acceptance.plugins.dashboard_view.DashboardView;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;

import io.jenkins.plugins.analysis.warnings.AnalysisResult.Tab;
import io.jenkins.plugins.analysis.warnings.AnalysisSummary.InfoType;

import static io.jenkins.plugins.analysis.warnings.Assertions.*;

/**
 * Integration tests for the dashboard portlet.
 *
 * @author Lukas Kirner
 */
public class DashboardViewPortletUITest extends AbstractJUnitTest {

    @Test
    public void test() {
        DashboardView v = createDashboardWithStaticAnalysisPortlet(true, true);


        assertThat(true).isTrue();
    }

    private DashboardView createDashboardWithStaticAnalysisPortlet(final Boolean hideCleanJobs, final Boolean showIcons) {
        DashboardView v = createDashboardView();
        StaticAnalysisIssuesPerToolAndJobPortlet portlet = v.addTopPortlet(StaticAnalysisIssuesPerToolAndJobPortlet.class);
        if (hideCleanJobs) { portlet.toggleHideCleanJobs(); }
        if (showIcons) { portlet.toggleShowIcons(); }
        v.save();
        return v;
    }

    private DashboardView createDashboardView() {
        DashboardView v = jenkins.views.create(DashboardView.class);
        v.configure();
        v.matchAllJobs();
        return v;
    }
}

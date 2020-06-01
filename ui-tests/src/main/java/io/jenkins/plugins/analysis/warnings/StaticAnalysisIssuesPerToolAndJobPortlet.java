package io.jenkins.plugins.analysis.warnings;

import org.jenkinsci.test.acceptance.plugins.dashboard_view.AbstractDashboardViewPortlet;
import org.jenkinsci.test.acceptance.plugins.dashboard_view.DashboardView;
import org.jenkinsci.test.acceptance.po.Describable;

/**
 * Portlet for "Static analysis issues per tool and job" in Dashboard.
 *
 * @author Lukas Kirner
 */
@Describable("Static analysis issues per tool and job")
public class StaticAnalysisIssuesPerToolAndJobPortlet extends AbstractDashboardViewPortlet {
    StaticAnalysisIssuesPerToolAndJobPortlet(final DashboardView parent, final String path) {
        super(parent, path);
    }

    void toggleHideCleanJobs() {
        this.find(by.name("_.hideCleanJobs")).click();
    }

    void toggleShowIcons() {
        this.find(by.name("_.showIcons")).click();
    }
}

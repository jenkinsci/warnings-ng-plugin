package io.jenkins.plugins.analysis.warnings;

import org.jenkinsci.test.acceptance.plugins.dashboard_view.AbstractDashboardViewPortlet;
import org.jenkinsci.test.acceptance.plugins.dashboard_view.DashboardView;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;

/**
 * Portlet for "Static analysis issues per tool and job" in Dashboard.
 *
 * @author Lukas Kirner
 */
// TODO: add tool selection
@Describable("Static analysis issues per tool and job")
public class StaticAnalysisIssuesPerToolAndJobPortlet extends AbstractDashboardViewPortlet {
    private final Control hideCleanJobs = control("hideCleanJobs");
    private final Control showIcons = control("showIcons");

    /**
     * Creates a new portlet page object.
     *
     * @param parent
     *         parent page object that contains this portlet
     * @param path
     *         absolute path to the page area
     */
    public StaticAnalysisIssuesPerToolAndJobPortlet(final DashboardView parent, final String path) {
        super(parent, path);
    }

    void setHideCleanJobs(final boolean checked) {
        hideCleanJobs.check(checked);
    }

    void setShowIcons(final boolean checked) {
        showIcons.check(checked);
    }
}

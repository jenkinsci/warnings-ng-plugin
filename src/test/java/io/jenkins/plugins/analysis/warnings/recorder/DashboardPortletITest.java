package io.jenkins.plugins.analysis.warnings.recorder;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import javax.servlet.ServletException;

import org.junit.Test;
import org.xml.sax.SAXException;

import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebRequest;

import hudson.model.FreeStyleProject;
import hudson.model.Project;
import hudson.model.Result;
import hudson.plugins.view.dashboard.Dashboard;

import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.warnings.MavenConsole;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.DashboardTable;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for the dashboard portlet table.
 *
 * @author Michael Schmid, Raphael Furch
 */
public class DashboardPortletITest extends IntegrationTestWithJenkinsPerSuite {
    /**
     * Dashboard Portlet should show the recorded warning count of two tools.
     */
    @Test
    public void showJobResultsAccordingTheirPluginConfiguration() {
        FreeStyleProject project1 = createFreeStyleProjectWithWorkspaceFiles("checkstyle-filtering.xml",
                "eclipse.txt");
        enableEclipseWarnings(project1);
        enableCheckStyleWarnings(project1);
        enableGenericWarnings(project1, createTool(new MavenConsole(), ""));

        FreeStyleProject project2 = createFreeStyleProjectWithWorkspaceFiles("checkstyle-healthReport.xml");
        enableCheckStyleWarnings(project2);

        Dashboard dashboard = configureDashboard("showJobResultsAccordingTheirPluginConfiguration", project1, project2);

        buildWithResult(project1, Result.SUCCESS);
        buildWithResult(project2, Result.SUCCESS);

        DashboardTable pageObject = loadDashboardTable(dashboard);
        assertThat(pageObject.getWarningCounts(project1)).hasSize(3)
                .containsEntry("CheckStyle", 7)
                .containsEntry("Eclipse ECJ", 8)
                .containsEntry("Maven", 0);
        assertThat(pageObject.getWarningCounts(project2)).hasSize(1)
                .containsEntry("CheckStyle", 6);

        assertThat(pageObject.getDashboardTableEntry(project1).get("CheckStyle").getUrl())
                .endsWith("job/" + project1.getName() + "/1/checkstyle");
        assertThat(pageObject.getDashboardTableEntry(project1).get("Eclipse ECJ").getUrl())
                .endsWith("job/" + project1.getName() + "/1/eclipse");
        assertThat(pageObject.getDashboardTableEntry(project1).get("Maven").getUrl())
                .endsWith("job/" + project1.getName() + "/1/maven-warnings");
        assertThat(pageObject.getDashboardTableEntry(project2).get("CheckStyle").getUrl())
                .endsWith("job/" + project2.getName() + "/1/checkstyle");
    }

    /**
     * Dashboard Portlet should show the recorded warning count of the latest build.
     */
    @Test
    public void showJobResultsOfTheLastBuild() {
        FreeStyleProject project = createFreeStyleProjectWithWorkspaceFiles("checkstyle-filtering.xml", "eclipse.txt");
        enableEclipseWarnings(project);
        enableCheckStyleWarnings(project);

        Dashboard dashboard = configureDashboard("showJobResultsOfTheLastBuild", project);
        buildWithResult(project, Result.SUCCESS);

        DashboardTable pageObject = loadDashboardTable(dashboard);
        assertThat(pageObject.getWarningCounts(project)).hasSize(2)
                .containsEntry("CheckStyle", 7)
                .containsEntry("Eclipse ECJ", 8);
        assertThat(pageObject.getDashboardTableEntry(project).get("CheckStyle").getUrl())
                .endsWith("job/" + project.getName() + "/1/checkstyle");
        assertThat(pageObject.getDashboardTableEntry(project).get("Eclipse ECJ").getUrl())
                .endsWith("job/" + project.getName() + "/1/eclipse");

        copySingleFileToWorkspace(project, "checkstyle-healthReport.xml", "checkstyle-filtering-issues.txt");
        buildWithResult(project, Result.SUCCESS);

        pageObject = loadDashboardTable(dashboard);
        assertThat(pageObject.getWarningCounts(project)).hasSize(2)
                .containsEntry("CheckStyle", 6)
                .containsEntry("Eclipse ECJ", 8);
        assertThat(pageObject.getDashboardTableEntry(project).get("CheckStyle").getUrl())
                .endsWith("job/" + project.getName() + "/2/checkstyle");
        assertThat(pageObject.getDashboardTableEntry(project).get("Eclipse ECJ").getUrl())
                .endsWith("job/" + project.getName() + "/2/eclipse");
    }

    /**
     * Dashboard Portlet should show job results when a tool is configured after first build.
     */
    @Test
    public void showJobResultsWhenAToolIsConfiguredAfterFirstBuild() {
        FreeStyleProject project = createFreeStyleProjectWithWorkspaceFiles("checkstyle-filtering.xml", "eclipse.txt");
        enableEclipseWarnings(project);

        Dashboard dashboard = configureDashboard("showJobResultsWhenAToolIsConfiguredAfterFirstBuild", project);
        buildWithResult(project, Result.SUCCESS);

        DashboardTable pageObject = loadDashboardTable(dashboard);
        assertThat(pageObject.getWarningCounts(project)).hasSize(1)
                .containsEntry("Eclipse ECJ", 8);
        assertThat(pageObject.getDashboardTableEntry(project).get("Eclipse ECJ").getUrl())
                .endsWith("job/" + project.getName() + "/1/eclipse");

        enableCheckStyleWarnings(project);
        buildWithResult(project, Result.SUCCESS);

        pageObject = loadDashboardTable(dashboard);
        assertThat(pageObject.getWarningCounts(project)).hasSize(2)
                .containsEntry("CheckStyle", 7)
                .containsEntry("Eclipse ECJ", 8);

        assertThat(pageObject.getDashboardTableEntry(project).get("CheckStyle").getUrl())
                .endsWith("job/" + project.getName() + "/2/checkstyle");
        assertThat(pageObject.getDashboardTableEntry(project).get("Eclipse ECJ").getUrl())
                .endsWith("job/" + project.getName() + "/2/eclipse");
    }

    /**
     * Dashboard Portlet should hide clean jobs, when hideCleanJobs is true.
     */
    @Test
    public void hideJobResultsWhenAJobIsClean() {
        FreeStyleProject project1 = createFreeStyleProjectWithWorkspaceFiles("checkstyle-filtering.xml",
                "eclipse.txt");
        enableEclipseWarnings(project1);
        enableCheckStyleWarnings(project1);

        FreeStyleProject project2 = createFreeStyleProject();
        enableCheckStyleWarnings(project2);
        enableGenericWarnings(project2, createTool(new MavenConsole(), ""));

        Dashboard dashboard = configureDashboard("hideJobResultsWhenAJobIsClean", false, project1, project2);
        Dashboard dashboardHideCleanJobs = configureDashboard("hideJobResultsWhenAJobIsCleanHideCleanJobs",
                true, project1, project2);

        buildWithResult(project1, Result.SUCCESS);
        buildWithResult(project2, Result.SUCCESS);

        DashboardTable pageObject = loadDashboardTable(dashboard);
        assertThat(pageObject.getWarningCounts(project1)).hasSize(2)
                .containsEntry("CheckStyle", 7)
                .containsEntry("Eclipse ECJ", 8);
        assertThat(pageObject.getWarningCounts(project2)).hasSize(2)
                .containsEntry("CheckStyle", 0)
                .containsEntry("Maven", 0);

        assertThat(pageObject.getDashboardTableEntry(project1).get("CheckStyle").getUrl())
                .endsWith("job/" + project1.getName() + "/1/checkstyle");
        assertThat(pageObject.getDashboardTableEntry(project1).get("Eclipse ECJ").getUrl())
                .endsWith("job/" + project1.getName() + "/1/eclipse");
        assertThat(pageObject.getDashboardTableEntry(project2).get("CheckStyle").getUrl())
                .endsWith("job/" + project2.getName() + "/1/checkstyle");
        assertThat(pageObject.getDashboardTableEntry(project2).get("Maven").getUrl())
                .endsWith("job/" + project2.getName() + "/1/maven-warnings");

        DashboardTable pageObjectHideCleanJobs = loadDashboardTable(dashboardHideCleanJobs);
        assertThat(pageObjectHideCleanJobs.getWarningCounts(project1)).hasSize(2)
                .containsEntry("CheckStyle", 7)
                .containsEntry("Eclipse ECJ", 8);
        assertThat(pageObjectHideCleanJobs.containsJob(project2)).isFalse();

        assertThat(pageObject.getDashboardTableEntry(project1).get("CheckStyle").getUrl())
                .endsWith("job/" + project1.getName() + "/1/checkstyle");
        assertThat(pageObject.getDashboardTableEntry(project1).get("Eclipse ECJ").getUrl())
                .endsWith("job/" + project1.getName() + "/1/eclipse");
    }



    private DashboardTable loadDashboardTable(final Dashboard dashboard) {
        DashboardTable result;
        try {
            result = new DashboardTable(getWebClient(JavaScriptSupport.JS_DISABLED).getPage(dashboard));
        }
        catch (SAXException | IOException exception) {
            throw new AssertionError(exception);
        }
        return result;
    }

    private Dashboard configureDashboard(final String name, final Project... projects) {
        return configureDashboard(name, false, projects);
    }

    private Dashboard configureDashboard(final String name, final boolean hideCleanJobs, final Project... projects) {
        Dashboard dashboardView = new Dashboard(name);

        try {
            getJenkins().getInstance().addView(dashboardView);
        }
        catch (IOException exception) {
            throw new AssertionError("Dashboard configuration failed", exception);
        }

        WebRequest request;
        try {
            request = new WebRequest(
                    new URL(getJenkins().getInstance().getRootUrl() + "view/" + name + "/configSubmit"),
                    HttpMethod.POST);
        }
        catch (MalformedURLException exception) {
            throw new AssertionError(exception);
        }

        String hideCleanJobsString = hideCleanJobs ? "true" : "false";
        String postCommand = "name=" + name
                + "&description="
                + "&statusFilter="
                + "&includeRegex="
                + "&stapler-class=hudson.views.StatusColumn&%24class=hudson.views.StatusColumn"
                + "&stapler-class=hudson.views.WeatherColumn&%24class=hudson.views.WeatherColumn"
                + "&stapler-class=hudson.views.JobColumn&%24class=hudson.views.JobColumn"
                + "&stapler-class=hudson.views.LastSuccessColumn&%24class=hudson.views.LastSuccessColumn"
                + "&stapler-class=hudson.views.LastFailureColumn&%24class=hudson.views.LastFailureColumn"
                + "&stapler-class=hudson.views.LastDurationColumn&%24class=hudson.views.LastDurationColumn"
                + "&stapler-class=hudson.views.BuildButtonColumn&%24class=hudson.views.BuildButtonColumn"
                + "&_.name=%23+Issues"
                + "&_.id=&stapler-class=io.jenkins.plugins.analysis.core.columns.IssuesTotalColumn"
                + "&%24class=io.jenkins.plugins.analysis.core.columns.IssuesTotalColumn"
                + "&leftPortletWidth=50%25&rightPortletWidth=50%25"
                + "&_.name=Static+analysis+issues+per+tool+and+job"
                + "&_.id="
                + "&stapler-class=io.jenkins.plugins.analysis.core.portlets.IssuesTablePortlet"
                + "&%24class=io.jenkins.plugins.analysis.core.portlets.IssuesTablePortlet&core%3Aapply="
                + "&Jenkins-Crumb=test"
                + "&json=%7B%22name%22%3A+%22Test%22%2C+%22description%22%3A+%22%22%2C+%22filterQueue%22%3A+false%2C+"
                + "%22filterExecutors%22%3A+false%2C+%22statusFilter%22%3A+%22%22%2C+%22recurse%22%3A+false%2C+%22test0"
                + "%22%3A+%22true%22%2C+%22columns%22%3A+%5B%7B%22stapler-class%22%3A+%22hudson.views.StatusColumn"
                + "%22%2C+%22%24class%22%3A+%22hudson.views.StatusColumn%22%7D%2C+%7B%22stapler-class%22%3A+"
                + "%22hudson.views.WeatherColumn%22%2C+%22%24class%22%3A+%22hudson.views.WeatherColumn%22%7D%2C+%7B"
                + "%22stapler-class%22%3A+%22hudson.views.JobColumn%22%2C+%22%24class%22%3A+%22hudson.views.JobColumn"
                + "%22%7D%2C+%7B%22stapler-class%22%3A+%22hudson.views.LastSuccessColumn%22%2C+%22%24class%22%3A+"
                + "%22hudson.views.LastSuccessColumn%22%7D%2C+%7B%22stapler-class%22%3A+%22hudson.views.LastFailureColumn"
                + "%22%2C+%22%24class%22%3A+%22hudson.views.LastFailureColumn%22%7D%2C+%7B%22stapler-class"
                + "%22%3A+%22hudson.views.LastDurationColumn%22%2C+%22%24class%22%3A+%22hudson.views.LastDurationColumn"
                + "%22%7D%2C+%7B%22stapler-class%22%3A+%22hudson.views.BuildButtonColumn%22%2C+%22%24class%22%3A+"
                + "%22hudson.views.BuildButtonColumn%22%7D%2C+%7B%22name%22%3A+%22%23+Issues%22%2C+%22selectTools"
                + "%22%3A+false%2C+%22tools%22%3A+%7B%22id%22%3A+%22%22%7D%2C+%22stapler-class%22%3A+"
                + "%22io.jenkins.plugins.analysis.core.columns.IssuesTotalColumn%22%2C+%22%24class%22%3A+"
                + "%22io.jenkins.plugins.analysis.core.columns.IssuesTotalColumn%22%7D%5D%2C+%22includeStdJobList"
                + "%22%3A+false%2C+%22topPortlet%22%3A+%7B%22name%22%3A+%22Static+analysis+issues+per+tool+and+job"
                + "%22%2C+%22hideCleanJobs%22%3A+" + hideCleanJobsString + "%2C+%22showIcons%22%3A+false%2C+%22selectTools%22%3A+false%2C+"
                + "%22tools%22%3A+%7B%22id%22%3A+%22%22%7D%2C+%22stapler-class%22%3A+"
                + "%22io.jenkins.plugins.analysis.core.portlets.IssuesTablePortlet%22%2C+%22%24class%22%3A+"
                + "%22io.jenkins.plugins.analysis.core.portlets.IssuesTablePortlet%22%7D%2C+%22core%3Aapply%22%3A+"
                + "%22%22%2C+%22Jenkins-Crumb%22%3A+%22test%22%7D"
                + "&Submit=OK";
        request.setRequestBody(postCommand);

        try {
            getWebClient(JavaScriptSupport.JS_DISABLED).getPage(request);
        }
        catch (IOException exception) {
            throw new AssertionError("The setup of the IssuesTablePortlet failed", exception);
        }

        Arrays.stream(projects).forEach(project -> {
            try {
                dashboardView.doAddJobToView(project.getName());
            }
            catch (ServletException | IOException exception) {
                throw new AssertionError("The project " + project.getName() + " couldn't be added to the dashboard",
                        exception);
            }
        });

        return dashboardView;
    }
}

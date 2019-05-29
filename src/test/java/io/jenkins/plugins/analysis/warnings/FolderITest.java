package io.jenkins.plugins.analysis.warnings;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.jvnet.hudson.test.recipes.WithPlugin;

import com.cloudbees.hudson.plugins.folder.Folder;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import hudson.model.FreeStyleProject;
import hudson.model.Result;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.DetailsViewCharts;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.SummaryBox;

import static io.jenkins.plugins.analysis.core.testutil.IntegrationTest.JavaScriptSupport.*;
import static org.assertj.core.api.Assertions.*;

/**
 * Checks whether the folder plugin functionally is fully supported.
 *
 * @author Florian Hageneder
 * @author Veronika Zwickenpflug
 */
public class FolderITest extends IntegrationTestWithJenkinsPerSuite {
    private static final String RELATIVE_URL_JAVA = "/java";

    /**
     * Checks the summary box is still correct when in a single folder.
     *
     * @throws IOException
     *         when creating jobs fails.
     */
    @Test
    @WithPlugin("cloudbees-folder")
    public void testSingleFolderSummaryBox() throws IOException {
        Folder folder = getJenkins().createProject(Folder.class, "singleSummary");
        FreeStyleProject project = folder.createProject(FreeStyleProject.class, "project");
        buildJobAndVerifySummaryBoxes(project);
    }

    /**
     * Checks the summary box is still correct when in a multiple folders.
     *
     * @throws IOException
     *         when creating jobs fails.
     */
    @Test
    @WithPlugin("cloudbees-folder")
    public void testDoubleFolderSummaryBox() throws IOException {
        Folder folder = getJenkins().createProject(Folder.class, "doubleSummary");
        Folder folder2 = folder.createProject(Folder.class, "doubleSummary");
        FreeStyleProject project = folder2.createProject(FreeStyleProject.class, "project");

        buildJobAndVerifySummaryBoxes(project);
    }

    /**
     * Schedules two builds with different outcome and verifies summary box for each of them.
     *
     * @param project
     *         Project to be used for the build
     */
    private void buildJobAndVerifySummaryBoxes(final FreeStyleProject project) {
        copyMultipleFilesToWorkspace(project, "javac.txt", "javac-second-build.txt");

        // First build
        Java java = new Java();
        java.setPattern("**/javac.txt");
        IssuesRecorder recorder = enableWarnings(project, java);
        AnalysisResult firstResult = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        verifyJavaSummaryBox(firstResult, "Java: 2 warnings", Arrays.asList("java", "java/info"),
                Collections.emptyList(), Collections.emptyList());

        // Second build
        Java java2 = new Java();
        java2.setPattern("**/javac-second-build.txt");
        recorder.setTool(java2);
        AnalysisResult secondResult = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        verifyJavaSummaryBox(secondResult,
                "Java: 2 warnings", Arrays.asList("java", "java/info"),
                Arrays.asList("One new warning", "One fixed warning"),
                Arrays.asList("java/new", "java/fixed"));
    }

    /**
     * Verifies the summary box of a given AnalysisResult.
     *
     * @param analysisResult
     *         AnalysisResult which summary box should be verified.
     * @param title
     *         Title that should be set in the summary box
     * @param titleHrefs
     *         Hrefs that should be located in the title
     * @param items
     *         Items that should be located in the summary
     * @param itemHrefs
     *         Item hrefs that should be located in the summary
     */
    private void verifyJavaSummaryBox(final AnalysisResult analysisResult,
            final String title, final List<String> titleHrefs, final List<String> items, final List<String> itemHrefs) {

        SummaryBox summaryBox = new SummaryBox(
                getWebPage(JavaScriptSupport.JS_DISABLED, analysisResult.getOwner()), "java");

        assertThat(summaryBox.getTitle()).isEqualTo(title);
        assertThat(summaryBox.getTitleHrefs()).containsAll(titleHrefs);

        assertThat(summaryBox.getItems()).containsAll(items);
        assertThat(summaryBox.getItemHrefs()).containsAll(itemHrefs);
    }

    /**
     * Checks that the warning overview of the severity distribution is still correct when in a single folder.
     *
     * @throws IOException
     *         when creating jobs fails.
     */
    @Test
    @WithPlugin("cloudbees-folder")
    public void testSingleFolderSeverityDistribution() throws IOException {
        FreeStyleProject project = singleFolderJob("singleseverity", "javac.txt");

        buildJobAndVerifySeverityDistribution(project);
    }

    /**
     * Checks that the warning overview of the severity distribution is still correct when in multiple folders.
     *
     * @throws IOException
     *         when creating jobs fails.
     */
    @Test
    @WithPlugin("cloudbees-folder")
    public void testDoubleFolderSeverityDistribution() throws IOException {
        FreeStyleProject project = doubleFolderJob("doubleseverity", "javac.txt");

        buildJobAndVerifySeverityDistribution(project);
    }

    /**
     * Schedules the build and verifies the severity distribution in the chart details view.
     *
     * @param project
     *         Project to be used for the build
     */
    private void buildJobAndVerifySeverityDistribution(final FreeStyleProject project) {
        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        DetailsViewCharts charts = new DetailsViewCharts(getWebPage(JS_ENABLED, result, RELATIVE_URL_JAVA));
        JSONArray severity = charts.getSeveritiesDistributionPieChart()
                .getJSONArray("series")
                .getJSONObject(0)
                .getJSONArray("data");

        // check dynamically to be resistant against changes of the order
        for (int i = 0; i < severity.size(); i++) {
            JSONObject o = severity.getJSONObject(i);

            if ("Low".equals(o.getString("name"))) {
                assertThat(o.getInt("value")).isEqualTo(0);
            }
            else if ("Normal".equals(o.getString("name"))) {
                assertThat(o.getInt("value")).isEqualTo(2);
            }
            else if ("High".equals(o.getString("name"))) {
                assertThat(o.getInt("value")).isEqualTo(0);
            }
        }
    }

    /**
     * Checks that the warning overview of the reference comparison is still correct when in a single folder.
     *
     * @throws IOException
     *         when creating jobs fails.
     */
    @Test
    @WithPlugin("cloudbees-folder")
    public void testSingleFolderReferenceComparison() throws IOException {
        FreeStyleProject project = singleFolderJob("singlereference", "javac.txt");

        buildJobAndVerifyReferenceComparison(project);
    }

    /**
     * Checks that the warning overview of the reference comparison is still correct when in a single folder.
     *
     * @throws IOException
     *         when creating jobs fails.
     */
    @Test
    @WithPlugin("cloudbees-folder")
    public void testDoubleFolderReferenceComparison() throws IOException {
        FreeStyleProject project = doubleFolderJob("doublereference", "javac.txt");

        buildJobAndVerifyReferenceComparison(project);
    }

    /**
     * Schedules the build and verifies the reference comparision in the chart details view.
     *
     * @param project
     *         Project to be used for the build
     */
    private void buildJobAndVerifyReferenceComparison(final FreeStyleProject project) {
        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        DetailsViewCharts charts = new DetailsViewCharts(getWebPage(JS_ENABLED, result, RELATIVE_URL_JAVA));
        JSONArray references = charts.getReferenceComparisonPieChart()
                .getJSONArray("series")
                .getJSONObject(0)
                .getJSONArray("data");

        // check dynamically to be resistant against changes of the order
        for (int i = 0; i < references.size(); i++) {
            JSONObject o = references.getJSONObject(i);

            if ("New".equals(o.getString("name"))) {
                assertThat(o.getInt("value")).isEqualTo(0);
            }
            else if ("Outstanding".equals(o.getString("name"))) {
                assertThat(o.getInt("value")).isEqualTo(2);
            }
            else if ("Fixed".equals(o.getString("name"))) {
                assertThat(o.getInt("value")).isEqualTo(0);
            }
        }
    }

    /**
     * Checks that the warning overview of the history is still correct when in a single folder.
     *
     * @throws IOException
     *         when creating jobs fails.
     */
    @Test
    @WithPlugin("cloudbees-folder")
    public void testSingleFolderToolTrend() throws IOException {
        FreeStyleProject project = singleFolderJob("singletooltrend", "javac.txt");

        buildAndVerifyToolTrend(project);
    }

    /**
     * Checks that the warning overview of the history is still correct when in a single folder.
     *
     * @throws IOException
     *         when creating jobs fails.
     */
    @Test
    @WithPlugin("cloudbees-folder")
    public void testDoubleFolderToolTrend() throws IOException {
        FreeStyleProject project = doubleFolderJob("doubletooltrend", "javac.txt");

        buildAndVerifyToolTrend(project);
    }

    /**
     * Schedules the build and verifies the tool trend in the chart details view.
     *
     * @param project
     *         Project to be used for the build
     */
    private void buildAndVerifyToolTrend(final FreeStyleProject project) {
        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        DetailsViewCharts charts = new DetailsViewCharts(getWebPage(JS_ENABLED, result, RELATIVE_URL_JAVA));
        JSONObject chart = charts.getToolsTrendChart()
                .getJSONArray("series")
                .getJSONObject(0);
        JSONArray data = chart.getJSONArray("data");

        assertThat(chart.getString("name")).isEqualTo("java");
        assertThat(data.getInt(0)).isEqualTo(2);
    }

    /**
     * Checks project path when in single folder.
     *
     * @throws IOException
     *         when folder creation fails.
     */
    @Test
    @WithPlugin("cloudbees-folder")
    public void testSingleFolderProjectPath() throws IOException {
        FreeStyleProject project = singleFolderJob("singlePath", "javac.txt");
        AnalysisResult analysisResult = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        HtmlPage page = getWebPage(JS_ENABLED, analysisResult, "/java");

        assertThat(page.getUrl().toString()).contains("job/singlePath/job/project/");
    }

    /**
     * Checks project path when in multiple folders.
     *
     * @throws IOException
     *         when folder creation fails.
     */
    @Test
    @WithPlugin("cloudbees-folder")
    public void testMultipleFolderProjectPath() throws IOException {
        FreeStyleProject project = doubleFolderJob("doublePath", "javac.txt");
        AnalysisResult analysisResult = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        HtmlPage page = getWebPage(JS_ENABLED, analysisResult, "/java");

        assertThat(page.getUrl().toString()).contains("job/doublePath/job/doublePath/job/project/");
    }

    /**
     * Setup a freestyle job within a single folder, and copies named files.
     *
     * @param folderName
     *         Name for the folder
     * @param files
     *         Files to copy into the workspace
     *
     * @return configured job.
     * @throws IOException
     *         When creation jobs/folders fails.
     */
    private FreeStyleProject singleFolderJob(final String folderName, final String... files)
            throws IOException {
        Folder folder = getJenkins().createProject(Folder.class, folderName);
        return initJob(folder, files);
    }

    /**
     * Setup a freestyle job within a double folder, and copies named files.
     *
     * @param foldername
     *         Name for the folder
     * @param files
     *         Files to copy into the workspace
     *
     * @return configured job.
     * @throws IOException
     *         When creation jobs/folders fails.
     */
    private FreeStyleProject doubleFolderJob(final String foldername, final String... files)
            throws IOException {
        Folder folder = getJenkins().createProject(Folder.class, foldername);
        Folder folder2 = folder.createProject(Folder.class, foldername);
        return initJob(folder2, files);
    }

    /**
     * Creates a freestyle project located in the given folder, copies the given warning files into the working
     * directory of the job and enables a java scanner for all *.txt files.
     *
     * @param folder
     *         In which the project should be set up.
     * @param files
     *         Files to scan.
     *
     * @return Created and initialized job
     * @throws IOException
     *         When creation jobs/folders fails.
     */
    private FreeStyleProject initJob(final Folder folder, final String... files) throws IOException {
        FreeStyleProject project = folder.createProject(FreeStyleProject.class, "project");

        copyMultipleFilesToWorkspace(project, files);

        Java java = new Java();
        java.setPattern("**/*.txt");
        enableWarnings(project, java);

        return project;
    }
}

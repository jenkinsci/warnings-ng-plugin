package io.jenkins.plugins.analysis.warnings;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.fileupload.util.Streams;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule.WebClient;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlHeading5;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTableBody;
import com.gargoylesoftware.htmlunit.html.HtmlTableCell;
import com.gargoylesoftware.htmlunit.html.HtmlTableHeaderCell;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;
import com.gargoylesoftware.htmlunit.html.HtmlUnorderedList;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import static io.jenkins.plugins.analysis.core.model.Assertions.*;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;
import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;
import io.jenkins.plugins.analysis.core.steps.ToolConfiguration;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTest;
import io.jenkins.plugins.analysis.core.views.ResultAction;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;

/**
 * Integration tests of the warnings plug-in in freestyle jobs. Tests the new recorder {@link IssuesRecorder}.
 *
 * @author Ullrich Hafner
 * @author Stephan Plöderl
 */
public class IssuesRecorderITest extends IntegrationTest {
    /**
     * Runs the Eclipse parser on an empty workspace: the build should report 0 issues and an error message.
     */
    @Test
    public void shouldCreateEmptyResult() {
        FreeStyleProject project = createJob();
        enableWarnings(project);

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(result).hasTotalSize(0);
        assertThat(result).hasErrorMessages("No files found for pattern '**/*issues.txt'. Configuration error?");
    }

    /**
     * Runs the Eclipse parser on an output file that contains several issues: the build should report 8 issues.
     */
    @Test
    public void shouldCreateResultWithWarnings() {
        FreeStyleProject project = createJobWithWorkspaceFile("eclipse.txt");
        enableWarnings(project);

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(result).hasTotalSize(8);
        assertThat(result).hasInfoMessages("Resolved module names for 8 issues",
                "Resolved package names of 4 affected files");
    }

    /**
     * Sets the UNSTABLE threshold to 8 and parse a file that contains exactly 8 warnings: the build should be
     * unstable.
     */
    @Test
    public void shouldCreateUnstableResult() {
        FreeStyleProject project = createJobWithWorkspaceFile("eclipse.txt");
        enableWarnings(project, publisher -> publisher.setUnstableTotalAll(7));

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.UNSTABLE);

        assertThat(result).hasTotalSize(8);
        assertThat(result).hasOverallResult(Result.UNSTABLE);

        HtmlPage page = getWebPage(result, "eclipseResult");
        assertThat(page.getElementsByIdAndOrName("statistics")).hasSize(1);
    }

    /**
     * returns a WebPage-instance of an analysis result.
     * @param result the corresponding AnalysisResult of a finished build.
     * @param page the relative path to the result page.
     * @return a WebPage as {@link HtmlPage}.
     */
    private HtmlPage getWebPage(final AnalysisResult result, final String page) {
        try {
            WebClient webClient = j.createWebClient();
            //webClient.setJavaScriptEnabled(false);
            return webClient.getPage(result.getOwner(), page);
        }
        catch (SAXException | IOException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Verifies that the right amount of duplicate code warnings are detected.
     */
    @Test
    public void shouldHaveDuplicateCodeWarnings(){
        FreeStyleProject project = createJobWithWorkspaceFile("duplicateCode/cpd.xml");
        Cpd cpd = new Cpd();
        enableWarnings(project, cpd);

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(result).hasTotalSize(20);
        assertThat(result).hasOverallResult(Result.SUCCESS);

    }

    /**
     * Verifies that the priority of the duplicate code warnings are changed corresponding
     * to the defined thresholds for cpd warnings.
     */
    @Test
    public void priorityShouldChangeIfThresholdsChange() {
        FreeStyleProject project = createJobWithWorkspaceFile("duplicateCode/cpd.xml");
        Cpd cpd = new Cpd();
        cpd.setNormalThreshold(1);
        cpd.setHighThreshold(2);
        enableWarnings(project, cpd);

        List<HtmlTableRow> tableRows = getIssueTableRows(project);
        HtmlTableRow row = tableRows.get(0);
        List<HtmlTableCell> tableCells = row.getCells();

        assertThat(tableCells.get(2).getTextContent()).isEqualTo("High");


        cpd.setHighThreshold(5);

        tableRows = getIssueTableRows(project);
        row = tableRows.get(0);
        tableCells = row.getCells();

        assertThat(tableCells.get(2).getTextContent()).isEqualTo("Normal");


        cpd.setNormalThreshold(4);

        tableRows = getIssueTableRows(project);
        row = tableRows.get(0);
        tableCells = row.getCells();

        assertThat(tableCells.get(2).getTextContent()).isEqualTo("Low");
    }

    /**
     * Verifies that the priority of the duplicate code warnings are changed corresponding
     * to the defined thresholds for simian warnings.
     */
    @Test
    public void priorityShouldChangeIfThresholdsChangeSimian() {
        FreeStyleProject project = createJobWithWorkspaceFile("duplicateCode/simian.xml");
        Simian simian = new Simian();
        simian.setNormalThreshold(1);
        simian.setHighThreshold(2);
        enableWarnings(project, simian);

        HtmlTable table = getIssuesTable(project, "simian");
        List<HtmlTableRow> tableRows = table.getBodies().get(0).getRows();
        HtmlTableRow row = tableRows.get(0);
        List<HtmlTableCell> tableCells = row.getCells();

        assertThat(tableCells.get(2).getTextContent()).isEqualTo("High");


        simian.setHighThreshold(6);

        table = getIssuesTable(project, "simian");
        tableRows = table.getBodies().get(0).getRows();
        row = tableRows.get(0);
        tableCells = row.getCells();

        assertThat(tableCells.get(2).getTextContent()).isEqualTo("Normal");

        simian.setNormalThreshold(5);

        table = getIssuesTable(project, "simian");
        tableRows = table.getBodies().get(0).getRows();
        row = tableRows.get(0);
        tableCells = row.getCells();

        assertThat(tableCells.get(2).getTextContent()).isEqualTo("Low");
    }

    /**
     * Verifies that the priority links are redirecting to a filtered side,
     * showing only the warnings of this priority.
     */
    @Test
    public void priorityLinksShouldOpenFilteredSite() {
        FreeStyleProject project = createJobWithWorkspaceFile("duplicateCode/cpd.xml");
        Cpd cpd = new Cpd();
        cpd.setNormalThreshold(2);
        cpd.setHighThreshold(4);
        enableWarnings(project, cpd);

        List<HtmlTableRow> tableRows = getIssueTableRows(project);

        HtmlTableRow rowWithLowPriorityWarning = tableRows.get(5);
        HtmlTableRow rowWithNormalPriorityWarning = tableRows.get(0);
        HtmlTableRow rowWithHighPriorityWarning = tableRows.get(2);

        HtmlPage lowPriorityPage = clickOnLink(rowWithLowPriorityWarning.getCell(2).getFirstElementChild());
        HtmlPage normalPriorityPage = clickOnLink(rowWithNormalPriorityWarning.getCell(2).getFirstElementChild());
        HtmlPage highPriorityPage = clickOnLink(rowWithHighPriorityWarning.getCell(2).getFirstElementChild());


        HtmlHeading5 heading = (HtmlHeading5) lowPriorityPage.getElementById("number-priorities");
        checkAmountOfPriorityWarnings(heading,6,0,0);

        heading = (HtmlHeading5) normalPriorityPage.getElementById("number-priorities");
        checkAmountOfPriorityWarnings(heading,0,9,0);

        heading = (HtmlHeading5) highPriorityPage.getElementById("number-priorities");
        checkAmountOfPriorityWarnings(heading,0,0,5);
    }

    /**
     * Verifies that the source code links are redirecting to a side displaying the source code.
     */
    @Test
    public void sourceCodeLinksShouldWork(){
        FreeStyleProject project = createJobWithWorkspaceFile("duplicateCode/cpd.xml");
        Cpd cpd = new Cpd();
        cpd.setNormalThreshold(2);
        cpd.setHighThreshold(4);
        enableWarnings(project, cpd);

        List<HtmlTableRow> tableRows = getIssueTableRows(project);

        HtmlPage sourceCodePage = clickOnLink(tableRows.get(0).getCell(1).getFirstElementChild());

        DomElement tableElement = sourceCodePage.getElementById("main-panel");

        String htmlFile = null;
        try {
            htmlFile = Streams.asString(getTestResourceClass()
                    .getResourceAsStream("duplicateCode/expected_html_code_block.html")).trim();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        assertThat(tableElement.asText()).isEqualTo(htmlFile);
    }

    /**
     * Helper-method for clicking on a link.
     * @param element a {@link DomElement} which will trigger the redirection to a new page.
     * @return the wanted {@link HtmlPage}.
     */
    private HtmlPage clickOnLink(DomElement element){
        HtmlPage page = null;
        try {
            page = element.click();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return page;
    }

    /**
     * Verifies the structure of the issues table.
     * i.e. the displayed table headers and the defined classes of the table.
     */
    @Test
    public void tableShouldHaveExpectedStructure() {
        FreeStyleProject project = createJobWithWorkspaceFile("duplicateCode/cpd.xml");
        String classString = "class";
        String[] headers = {"Details","File","Priority", "#Lines", "Duplicated In", "Age"};
        Cpd cpd = new Cpd();
        enableWarnings(project, cpd);

        HtmlTable table = getIssuesTable(project, "cpd");

        assertThat(table).hasFieldOrProperty(classString);
        assertThat(table.getTagName()).isEqualTo(HtmlTable.TAG_NAME);
        assertThat(table.getAttribute(classString)).isEqualTo("table table-responsive table-responsive-block "
                + "table-hover table-striped dataTable no-footer");

        List<HtmlTableRow> tableHeaderRows = table.getHeader().getRows();

        assertThat(tableHeaderRows).hasSize(1);

        HtmlTableRow headerRow = tableHeaderRows.get(0);

        List<HtmlTableCell> headerRowCells = headerRow.getCells();
        assertThat(headerRowCells).hasSize(headers.length);
        for (int i = 0; i < headers.length; i++){
            HtmlTableCell cell = headerRowCells.get(i);
            assertThat(cell.getTagName()).isEqualTo(HtmlTableHeaderCell.TAG_NAME);
            assertThat(cell.getTextContent()).isEqualTo(headers[i]);
        }

    }

    /**
     * Helper-method which builds the result and returns the issues table of a specified result type.
     * @param project the project, which shall be build.
     * @param resultType the type of the warning (i.e. 'cpd' or 'simian').
     * @return the issues table as {@link HtmlTable} object.
     */
    private HtmlTable getIssuesTable(final FreeStyleProject project, final String resultType){
        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        HtmlPage page = getWebPage(result, resultType + "Result");
        return (HtmlTable) page.getElementById("issues");
    }


    private List<HtmlTableRow> getIssueTableRows(final FreeStyleProject project){
        HtmlTable table = getIssuesTable(project, "cpd");

        List<HtmlTableBody> bodies = table.getBodies();
        assertThat(bodies).hasSize(1);
        HtmlTableBody tableBody = bodies.get(0);
        return tableBody.getRows();
    }

    /**
     * Verifies that the content of the issues table is as expected.
     */
    @Test
    public void firstTableRowShouldHaveRightContent() {
        FreeStyleProject project = createJobWithWorkspaceFile("duplicateCode/cpd.xml");
        Cpd cpd = new Cpd();
        cpd.setNormalThreshold(2);
        cpd.setHighThreshold(4);
        enableWarnings(project, cpd);

        List<HtmlTableRow> tableRows = getIssueTableRows(project);

        HtmlTableRow firstTableRow = tableRows.get(0);
        List<HtmlTableCell> firstTableRowCells = firstTableRow.getCells();
        HtmlDivision divElement = (HtmlDivision) firstTableRowCells.get(0).getFirstElementChild();

        assertThat(divElement.getAttribute("class")).isEqualTo("details-control");
        assertThat(divElement.getAttribute("data-description")).isEqualTo(
                "<p><strong></strong></p> <pre><code>public static void functionOne()\n  "
                        + "{\n    System.out.println(\"testfile for redundancy\");</code></pre>");
        HtmlAnchor anchor = (HtmlAnchor) firstTableRowCells.get(1).getFirstElementChild();
        assertThat(anchor.getAttribute("href")).startsWith("source.");
        assertThat(anchor.getAttribute("href")).endsWith("/#11");
        assertThat(anchor.getTextContent()).isEqualTo("Main.java:11");

        int[] duplications = {8, 17, 20, 26, 29};
        HtmlUnorderedList duplicationList = (HtmlUnorderedList) firstTableRowCells.get(4).getFirstElementChild();
        @SuppressWarnings("unchecked")
        NodeList duplicationListItems = duplicationList.getChildNodes();

        assertThat(duplicationListItems.getLength()).isEqualTo(duplications.length);
        for (int i = 0; i < duplications.length; i++) {
            HtmlAnchor anchorElement = (HtmlAnchor) duplicationListItems.item(i).getFirstChild();
            assertThat(anchorElement.getTextContent()).isEqualTo("Main.java:" + duplications[i]);
            assertThat(anchorElement.getAttribute("href")).startsWith("source");
            assertThat(anchorElement.getAttribute("href")).endsWith("/#" + duplications[i]);
        }
    }

    /**
     * Verifies that the duplicate code lines of cpd warnings are correct.
     */
    @Test
    public void duplicateCodeLinesShouldBeOfRightAmount() {
        FreeStyleProject project = createJobWithWorkspaceFile("duplicateCode/cpd.xml");
        Cpd cpd = new Cpd();
        cpd.setNormalThreshold(2);
        cpd.setHighThreshold(4);
        enableWarnings(project, cpd);

        List<HtmlTableRow> tableRows = getIssueTableRows(project);
        //only 10 are displayed because of the paging

        assertThat(tableRows).hasSize(10);
        for (HtmlTableRow tableRow : tableRows) {
            List<HtmlTableCell> tableCells = tableRow.getCells();
            assertThat(tableCells).hasSize(6);
            HtmlDivision divElement = (HtmlDivision) tableCells.get(0).getFirstElementChild();
            int lineCount = Integer.parseInt(tableCells.get(3).getTextContent());
            assertThat(divElement.getAttribute("data-description").split("\n")).hasSize(lineCount);
        }
    }

    /**
     * Verifies that the total amount of low, normal, and high warnings should change according to the thresholds.
     */
    @Test
    public void shouldDifferInAmountOfDuplicateWarningForPriorities() {
        FreeStyleProject project = createJobWithWorkspaceFile("duplicateCode/cpd.xml");
        Cpd cpd = new Cpd();
        enableWarnings(project, cpd);

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        HtmlPage page = getWebPage(result, "cpdResult");
        HtmlHeading5 heading = (HtmlHeading5) page.getElementById("number-priorities");
        checkAmountOfPriorityWarnings(heading,20 ,0,0);


        cpd.setNormalThreshold(2);
        cpd.setHighThreshold(4);

        result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        page = getWebPage(result, "cpdResult");
        heading = (HtmlHeading5) page.getElementById("number-priorities") ;
        checkAmountOfPriorityWarnings(heading,6,9,5);


        cpd.setNormalThreshold(1);
        cpd.setHighThreshold(3);

        result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        page = getWebPage(result, "cpdResult");
        heading = (HtmlHeading5) page.getElementById("number-priorities") ;
        checkAmountOfPriorityWarnings(heading,0,6,14);

    }

    /**
     * Helper-Method that checks for the expected amount of warnings displayed in the wheel diagram.
     * @param heading The heading that defines the amounts of the warnings.
     * @param low Amount of low warnings.
     * @param normal Amount of normal warnings.
     * @param high Amount of high warnings.
     */
    private void checkAmountOfPriorityWarnings(final HtmlHeading5 heading, int low, int normal, int high){
        String high_data = "data-high";
        String normal_data = "data-normal";
        String low_data = "data-low";

        assertThat(heading.getAttribute(low_data)).isEqualTo(String.valueOf(low));
        assertThat(heading.getAttribute(normal_data)).isEqualTo(String.valueOf(normal));
        assertThat(heading.getAttribute(high_data)).isEqualTo(String.valueOf(high));
    }

    /**
     * Creates a new {@link FreeStyleProject freestyle job}. The job will get a generated name.
     *
     * @return the created job
     */
    private FreeStyleProject createJob() {
        try {
            return j.createFreeStyleProject();
        }
        catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Creates a new {@link FreeStyleProject freestyle job} and copies the specified resources
     * to the workspace folder.
     * The job will get a generated name.
     *
     * @param fileNames
     *         the files to copy to the workspace
     *
     * @return the created job
     */
    private FreeStyleProject createJobWithWorkspaceFile(final String... fileNames) {
        FreeStyleProject job = createJob();
        copyFilesToWorkspace(job, fileNames);
        return job;
    }

    /**
     * Enables the warnings plugin for the specified job. I.e., it registers a new {@link IssuesRecorder}
     * recorder for sthe job.
     *
     * @param job the job to register the recorder for
     * @param tool the used {@link StaticAnalysisTool} that shall be used.
     * @return the created recorder
     */
    private IssuesRecorder enableWarnings(final FreeStyleProject job, final StaticAnalysisTool tool){
        IssuesRecorder publisher = new IssuesRecorder();
        publisher.setTools(Collections.singletonList(new ToolConfiguration("**/*issues.txt", tool)));
        job.getPublishersList().add(publisher);
        return publisher;
    }

    /**
     * Enables the warnings plugin for the specified job. I.e., it registers a new {@link IssuesRecorder }
     * recorder for the job.
     *
     * @param job
     *         the job to register the recorder for
     *
     * @return the created recorder
     */
    @CanIgnoreReturnValue
    private IssuesRecorder enableWarnings(final FreeStyleProject job) {
        return enableWarnings(job, new Eclipse());
    }

    /**
     * Enables the warnings plugin for the specified job. I.e., it registers a new {@link IssuesRecorder } recorder
     * for the job.
     *
     * @param job
     *         the job to register the recorder for
     * @param configuration
     *         configuration of the recorder
     *
     * @return the created recorder
     */
    @SuppressWarnings("UnusedReturnValue")
    @CanIgnoreReturnValue
    private IssuesRecorder enableWarnings(final FreeStyleProject job, final Consumer<IssuesRecorder> configuration) {
        IssuesRecorder publisher = enableWarnings(job);
        configuration.accept(publisher);
        return publisher;
    }

    /**
     * Schedules a new build for the specified job and returns the created {@link AnalysisResult} after the build has
     * been finished.
     *
     * @param job
     *         the job to schedule
     * @param status
     *         the expected result for the build
     *
     * @return the created {@link ResultAction}
     */
    @SuppressWarnings({"illegalcatch", "OverlyBroadCatchBlock"})
    private AnalysisResult scheduleBuildAndAssertStatus(final FreeStyleProject job, final Result status) {
        try {
            FreeStyleBuild build = j.assertBuildStatus(status, job.scheduleBuild2(0));

            ResultAction action = build.getAction(ResultAction.class);

            assertThat(action).isNotNull();

            return action.getResult();
        }
        catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}

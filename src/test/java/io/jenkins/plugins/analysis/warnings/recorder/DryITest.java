package io.jenkins.plugins.analysis.warnings.recorder;

import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.w3c.dom.NodeList;

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

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import static io.jenkins.plugins.analysis.core.model.Assertions.*;
import io.jenkins.plugins.analysis.core.quality.Status;
import io.jenkins.plugins.analysis.warnings.Cpd;
import io.jenkins.plugins.analysis.warnings.DuplicateCodeScanner;
import io.jenkins.plugins.analysis.warnings.Simian;

import hudson.model.FreeStyleProject;
import hudson.model.Result;

/**
 * Integration tests for the DRY parsers of the warnings plug-in in freestyle jobs.
 *
 * @author Stephan Ploederl
 */
public class DryITest extends IssuesRecorderITest {
    /**
     * Verifies that the right amount of duplicate code warnings are detected.
     */
    @Test
    public void shouldHaveDuplicateCodeWarnings() {
        FreeStyleProject project = createJobWithWorkspaceFiles("dry/cpd.xml");
        enableWarnings(project, new Cpd());

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(result).hasTotalSize(20);
        assertThat(result).hasStatus(Status.INACTIVE);
    }

    /**
     * Verifies that the priority of the duplicate code warnings are changed corresponding to the defined thresholds for
     * cpd warnings.
     */
    @Test
    public void priorityShouldChangeIfThresholdsChange() {
        FreeStyleProject project = createJobWithWorkspaceFiles("dry/cpd.xml");
        Cpd cpd = new Cpd();
        cpd.setNormalThreshold(1);
        enableWarnings(project, cpd);

        setHighThresholdAndCheckPriority(2, "High", cpd, project);
        setHighThresholdAndCheckPriority(5, "Normal", cpd, project);
        setNormalThresholdAndCheckPriority(4, "Low", cpd, project);
    }

    /**
     * Verifies that the priority of the duplicate code warnings are changed corresponding to the defined thresholds for
     * simian warnings.
     */

    @Test
    public void priorityShouldChangeIfThresholdsChangeSimian() {
        FreeStyleProject project = createJobWithWorkspaceFiles("dry/simian.xml");
        Simian simian = new Simian();
        simian.setNormalThreshold(1);
        enableWarnings(project, simian);

        setHighThresholdAndCheckPriority(2, "High", simian, project);
        setHighThresholdAndCheckPriority(6, "Normal", simian, project);
        setNormalThresholdAndCheckPriority(5, "Low", simian, project);
    }

    /**
     * Verifies that the priority links are redirecting to a filtered side, showing only the warnings of this priority.
     */
    @Test
    public void priorityLinksShouldOpenFilteredSite() {
        FreeStyleProject project = createJobWithWorkspaceFiles("dry/cpd.xml");
        Cpd cpd = new Cpd();
        cpd.setNormalThreshold(2);
        cpd.setHighThreshold(4);
        enableWarnings(project, cpd);

        List<HtmlTableRow> tableRows = getIssueTableRows(project, getResultPath(cpd));

        HtmlTableRow rowWithLowPriorityWarning = tableRows.get(5);
        HtmlTableRow rowWithNormalPriorityWarning = tableRows.get(0);
        HtmlTableRow rowWithHighPriorityWarning = tableRows.get(2);

        HtmlPage lowPriorityPage = clickOnLink(getPriorityCell(rowWithLowPriorityWarning));
        HtmlPage normalPriorityPage = clickOnLink(getPriorityCell(rowWithNormalPriorityWarning));
        HtmlPage highPriorityPage = clickOnLink(getPriorityCell(rowWithHighPriorityWarning));

        String priorityHeaderId = "number-priorities";
        HtmlHeading5 heading = (HtmlHeading5) lowPriorityPage.getElementById(priorityHeaderId);
        checkAmountOfPriorityWarnings(heading, 6, 0, 0);

        heading = (HtmlHeading5) normalPriorityPage.getElementById(priorityHeaderId);
        checkAmountOfPriorityWarnings(heading, 0, 9, 0);

        heading = (HtmlHeading5) highPriorityPage.getElementById(priorityHeaderId);
        checkAmountOfPriorityWarnings(heading, 0, 0, 5);
    }

    private DomElement getPriorityCell(final HtmlTableRow rowWithLowPriorityWarning) {
        return rowWithLowPriorityWarning.getCell(2).getFirstElementChild();
    }

    /**
     * Verifies that the source code links are redirecting to a side displaying the source code.
     */
    @Test
    public void sourceCodeLinksShouldWork() {
        FreeStyleProject project = createJobWithWorkspaceFiles("dry/cpd.xml");
        copySingleFileToWorkspace(project, "dry/Main.source", "Main.java");
        Cpd cpd = new Cpd();
        cpd.setNormalThreshold(2);
        cpd.setHighThreshold(4);
        enableWarnings(project, cpd);

        List<HtmlTableRow> tableRows = getIssueTableRows(project, getResultPath(cpd));

        HtmlPage sourceCodePage = clickOnLink(tableRows.get(0).getCell(1).getFirstElementChild());

        DomElement tableElement = sourceCodePage.getElementById("main-panel");

        String htmlFile = toString("dry/expected_html_code_block.html").trim();

        assertThat(tableElement.asText()).isEqualTo(htmlFile);
    }

    /**
     * Verifies the structure of the issues table. i.e. the displayed table headers and the defined classes of the
     * table.
     */
    @Test
    public void tableShouldHaveExpectedStructure() {
        FreeStyleProject project = createJobWithWorkspaceFiles("dry/cpd.xml");
        String classString = "class";
        String[] headers = {"Details", "File", "Priority", "#Lines", "Duplicated In", "Age"};
        Cpd cpd = new Cpd();
        enableWarnings(project, cpd);

        HtmlTable table = getIssuesTable(project, getResultPath(cpd));

        assertThat(table).hasFieldOrProperty(classString);
        assertThat(table.getTagName()).isEqualTo(HtmlTable.TAG_NAME);
        assertThat(table.getAttribute(classString)).isEqualTo("table table-responsive table-responsive-block "
                + "table-hover table-striped dataTable no-footer");

        List<HtmlTableRow> tableHeaderRows = table.getHeader().getRows();

        assertThat(tableHeaderRows).hasSize(1);

        HtmlTableRow headerRow = tableHeaderRows.get(0);

        List<HtmlTableCell> headerRowCells = headerRow.getCells();
        assertThat(headerRowCells).hasSize(headers.length);
        for (int i = 0; i < headers.length; i++) {
            HtmlTableCell cell = headerRowCells.get(i);
            assertThat(cell.getTagName()).isEqualTo(HtmlTableHeaderCell.TAG_NAME);
            assertThat(cell.getTextContent()).isEqualTo(headers[i]);
        }

    }

    /**
     * Verifies that the content of the issues table is as expected.
     */
    @Test
    public void firstTableRowShouldHaveRightContent() {
        FreeStyleProject project = createJobWithWorkspaceFiles("dry/cpd.xml");
        Cpd cpd = new Cpd();
        cpd.setNormalThreshold(2);
        cpd.setHighThreshold(4);
        enableWarnings(project, cpd);

        List<HtmlTableRow> tableRows = getIssueTableRows(project, getResultPath(cpd));

        HtmlTableRow firstTableRow = tableRows.get(0);
        List<HtmlTableCell> firstTableRowCells = firstTableRow.getCells();
        HtmlDivision divElement = (HtmlDivision) firstTableRowCells.get(0).getFirstElementChild();

        assertThat(divElement.getAttribute("class")).isEqualTo("details-control");
        assertThat(divElement.getAttribute("data-description")).isEqualTo(
                "<p><strong></strong></p> <pre><code>public static void functionOne()\n  "
                        + "{\n    System.out.println(&quot;testfile for redundancy&quot;);</code></pre>");
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
        FreeStyleProject project = createJobWithWorkspaceFiles("dry/cpd.xml");
        Cpd cpd = new Cpd();
        cpd.setNormalThreshold(2);
        cpd.setHighThreshold(4);
        enableWarnings(project, cpd);

        List<HtmlTableRow> tableRows = getIssueTableRows(project, getResultPath(cpd));
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
        FreeStyleProject project = createJobWithWorkspaceFiles("dry/cpd.xml");
        Cpd cpd = new Cpd();
        enableWarnings(project, cpd);

        changeThresholdsAndCheckAmountOfPriorityWarnings(25, 50, 20, 0, 0, cpd, project);

        changeThresholdsAndCheckAmountOfPriorityWarnings(2, 4, 6, 9, 5, cpd, project);

        changeThresholdsAndCheckAmountOfPriorityWarnings(1, 3, 0, 6, 14, cpd, project);
    }

    /**
     * Changes the thresholds, builds the project and checks for the expected amount of warnings displayed in the wheel
     * diagram.
     *
     * @param normalThreshold
     *         normalThreshold that shall be set.
     * @param highThreshold
     *         highThreshold that shall be set.
     * @param low
     *         Expected amount of low warnings.
     * @param normal
     *         Expected amount of normal warnings.
     * @param high
     *         Expected amount of high warnings.
     * @param scanner
     *         the {@link DuplicateCodeScanner} used in this test.
     * @param project
     *         the {@link FreeStyleProject} that shall be build.
     */
    private void changeThresholdsAndCheckAmountOfPriorityWarnings(final int normalThreshold, final int highThreshold,
            final int low, final int normal, final int high,
            final DuplicateCodeScanner scanner, final FreeStyleProject project) {
        String priorityHeaderId = "number-priorities";
        scanner.setNormalThreshold(normalThreshold);
        scanner.setHighThreshold(highThreshold);

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        HtmlPage page = getWebPage(result, getResultPath(scanner));
        HtmlHeading5 heading = (HtmlHeading5) page.getElementById(priorityHeaderId);
        checkAmountOfPriorityWarnings(heading, low, normal, high);
    }

    /**
     * Helper-Method that checks for the expected amount of warnings displayed in the wheel diagram.
     *
     * @param heading
     *         The heading that defines the amounts of the warnings.
     * @param low
     *         Expected amount of low warnings.
     * @param normal
     *         Expected amount of normal warnings.
     * @param high
     *         Expected amount of high warnings.
     */
    private void checkAmountOfPriorityWarnings(final HtmlHeading5 heading, int low, int normal, int high) {
        String high_data = "data-high";
        String normal_data = "data-normal";
        String low_data = "data-low";

        assertThat(heading.getAttribute(low_data)).isEqualTo(String.valueOf(low));
        assertThat(heading.getAttribute(normal_data)).isEqualTo(String.valueOf(normal));
        assertThat(heading.getAttribute(high_data)).isEqualTo(String.valueOf(high));
    }

    /**
     * Helper-method which builds the result and returns the issues table of a specified result type.
     *
     * @param project
     *         the project, which shall be build.
     * @param resultPath
     *         path to the result page (e.g. cpdResult or simianResult).
     *
     * @return the issues table as {@link HtmlTable} object.
     */
    private HtmlTable getIssuesTable(final FreeStyleProject project, final String resultPath) {

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        HtmlPage page = getWebPage(result, resultPath);
        DomElement table = page.getElementById("issues");

        assertThat(table).isInstanceOf(HtmlTable.class);

        return (HtmlTable) table;
    }

    /**
     * Helper-method which builds the result and returns a list of all {@link HtmlTableRow}-elements of the
     * issues-table.
     *
     * @param project
     *         the current {@link FreeStyleProject}.
     * @param resultPath
     *         the relative path to the page containing the issues-table.
     *
     * @return a list of #{@link HtmlTableRow}-elements displayed in the issues-table.
     */
    private List<HtmlTableRow> getIssueTableRows(final FreeStyleProject project, String resultPath) {
        HtmlTable table = getIssuesTable(project, resultPath);

        List<HtmlTableBody> bodies = table.getBodies();
        assertThat(bodies).hasSize(1);
        HtmlTableBody tableBody = bodies.get(0);
        return tableBody.getRows();
    }

    /**
     * Helper-method for clicking on a link.
     *
     * @param element
     *         a {@link DomElement} which will trigger the redirection to a new page.
     *
     * @return the wanted {@link HtmlPage}.
     */
    private HtmlPage clickOnLink(final DomElement element) {
        HtmlPage page;
        try {
            page = element.click();
        }
        catch (IOException e) {
            throw new AssertionError(e);
        }
        return page;
    }

    /**
     * Helper-method which returns the relative path to the result page of a {@link DuplicateCodeScanner}.
     *
     * @param scanner
     *         the {@link DuplicateCodeScanner} for which the results shall be tested.
     *
     * @return the relative path as {@link String} to the tools result page.
     */
    private String getResultPath(final DuplicateCodeScanner scanner) {
        return scanner.getClass().getSimpleName().toLowerCase() + "Result";
    }

    /**
     * Helper-method which builds the project and checks the priority of the warning in the first row of the
     * issues-table.
     *
     * @param expectedPriority
     *         the expected priority of the first warning.
     * @param project
     *         the current {@link FreeStyleProject}.
     * @param resultPath
     *         the relative path to the tools result page.
     */
    private void checkPriorityOfFirstWarningInTable(final String expectedPriority, final FreeStyleProject project,
            final String resultPath) {
        List<HtmlTableRow> tableRows = getIssueTableRows(project, resultPath);
        HtmlTableRow firstRow = tableRows.get(0);
        List<HtmlTableCell> tableCells = firstRow.getCells();

        assertThat(tableCells.get(2).getTextContent()).isEqualTo(expectedPriority);
    }

    /**
     * Helper-method which sets the highThreshold, builds the project and checks the priority of the warning in the
     * first row of the issues-table.
     *
     * @param highThreshold
     *         the value for the normal threshold.
     * @param expectedPriority
     *         the expected priority of the first warning.
     * @param scanner
     *         the {@link DuplicateCodeScanner} for which the threshold shall be changed.
     * @param project
     *         the current {@link FreeStyleProject}.
     */
    private void setHighThresholdAndCheckPriority(final int highThreshold, final String expectedPriority,
            final DuplicateCodeScanner scanner, final FreeStyleProject project) {
        scanner.setHighThreshold(highThreshold);
        checkPriorityOfFirstWarningInTable(expectedPriority, project, getResultPath(scanner));
    }

    /**
     * Helper-method which sets the normalThreshold, builds the project and checks the priority of the warning in the
     * first row of the issues-table.
     *
     * @param normalThreshold
     *         the value for the normal threshold.
     * @param expectedPriority
     *         the expected priority of the first warning.
     * @param scanner
     *         the {@link DuplicateCodeScanner} for which the threshold shall be changed.
     * @param project
     *         the current {@link FreeStyleProject}.
     */
    private void setNormalThresholdAndCheckPriority(final int normalThreshold,
            @SuppressWarnings("SameParameterValue") final String expectedPriority,
            final DuplicateCodeScanner scanner, final FreeStyleProject project) {
        scanner.setNormalThreshold(normalThreshold);
        checkPriorityOfFirstWarningInTable(expectedPriority, project, getResultPath(scanner));
    }
}

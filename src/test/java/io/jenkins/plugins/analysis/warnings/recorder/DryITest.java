package io.jenkins.plugins.analysis.warnings.recorder;

import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTableBody;
import com.gargoylesoftware.htmlunit.html.HtmlTableCell;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;
import com.gargoylesoftware.htmlunit.html.HtmlUnorderedList;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import static io.jenkins.plugins.analysis.core.model.Assertions.*;
import io.jenkins.plugins.analysis.core.quality.QualityGateStatus;
import io.jenkins.plugins.analysis.warnings.Cpd;
import io.jenkins.plugins.analysis.warnings.DuplicateCodeScanner;
import io.jenkins.plugins.analysis.warnings.Simian;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.DuplicationTable;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.DuplicationTable.DuplicationRow;

import hudson.model.FreeStyleProject;
import hudson.model.Result;

/**
 * Integration tests for the DRY parsers of the warnings plug-in in freestyle jobs.
 *
 * @author Stephan Plöderl
 */
public class DryITest extends AbstractIssuesRecorderITest {
    private static final String PRIORITY_HEADER_ID = "number-priorities";
    private static final String FOLDER = "dry/";
    private static final String SIMIAN_REPORT = FOLDER + "simian.xml";
    private static final String CPD_REPORT = FOLDER + "cpd.xml";

    /**
     * Verifies that the right amount of duplicate code warnings are detected.
     */
    @Test
    public void shouldHaveDuplicateCodeWarnings() {
        FreeStyleProject project = createJobWithWorkspaceFiles(CPD_REPORT);
        enableWarnings(project, new Cpd());

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(result).hasTotalSize(20);
        assertThat(result).hasQualityGateStatus(QualityGateStatus.INACTIVE);
    }

    /**
     * Verifies that the priority of the duplicate code warnings are changed corresponding to the defined thresholds for
     * cpd warnings.
     */
    @Test
    public void priorityShouldChangeIfThresholdsChange() {
        FreeStyleProject project = createJobWithWorkspaceFiles(CPD_REPORT);
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
        FreeStyleProject project = createJobWithWorkspaceFiles(SIMIAN_REPORT);
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
        FreeStyleProject project = createJobWithWorkspaceFiles(CPD_REPORT);
        Cpd cpd = new Cpd();
        cpd.setNormalThreshold(2);
        cpd.setHighThreshold(4);
        enableWarnings(project, cpd);

        List<HtmlTableRow> tableRows = getIssueTableRows(project);
        assertSizeOfSeverity(tableRows, 2, 5, 0, 0);
        assertSizeOfSeverity(tableRows, 0, 0, 9, 0);
        assertSizeOfSeverity(tableRows, 5, 0, 0, 6);
    }

    private void assertSizeOfSeverity(final List<HtmlTableRow> tableRows, final int row, 
            final int high, final int normal, final int low) {
        HtmlTableRow rowWithSeverityToSelect = tableRows.get(row);
        HtmlPage detailsOfSeverity = clickOnLink(getPriorityCell(rowWithSeverityToSelect));
        checkAmountOfPriorityWarnings(detailsOfSeverity.getElementById(PRIORITY_HEADER_ID), low, normal, high);
    }

    private DomElement getPriorityCell(final HtmlTableRow rowWithLowPriorityWarning) {
        return rowWithLowPriorityWarning.getCell(2).getFirstElementChild();
    }

    /**
     * Verifies that the source code links are redirecting to a side displaying the source code.
     */
    @Test
    public void sourceCodeLinksShouldWork() {
        FreeStyleProject project = createJobWithWorkspaceFiles(CPD_REPORT);
        copySingleFileToWorkspace(project, FOLDER + "Main.source", "Main.java");
        Cpd cpd = new Cpd();
        cpd.setNormalThreshold(2);
        cpd.setHighThreshold(4);
        enableWarnings(project, cpd);

        List<HtmlTableRow> tableRows = getIssueTableRows(project);

        HtmlPage sourceCodePage = clickOnLink(selectSourceCodeLink(tableRows));
        DomElement tableElement = sourceCodePage.getElementById("main-panel");

        String htmlFile = toString(FOLDER + "expected_html_code_block.html").trim();
        assertThat(tableElement.asText()).isEqualTo(htmlFile);
    }

    private DomElement selectSourceCodeLink(final List<HtmlTableRow> tableRows) {
        return tableRows.get(0).getCell(1).getFirstElementChild();
    }

   /**
     * Verifies the structure of the issues table. i.e. the displayed table headers and the defined classes of the
     * table.
     */
    @Test
    public void tableShouldHaveExpectedStructureWithPo() {
        FreeStyleProject project = createJobWithWorkspaceFiles(CPD_REPORT);
        Cpd cpd = new Cpd();
        enableWarnings(project, cpd);

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        HtmlPage details = getWebPage(result);
        DuplicationTable issues = new DuplicationTable(details, false);

        assertThat(issues.getTitle()).isEqualTo("Issues");
        assertThat(issues.getRows()).containsExactly(
                new DuplicationRow("Main.java:11", "-", "Low", 3, 1),
                new DuplicationRow("Main.java:15", "-", "Low", 3, 1),
                new DuplicationRow("Main.java:17", "-", "Low", 10, 1),
                new DuplicationRow("Main.java:17", "-", "Low", 8, 1),
                new DuplicationRow("Main.java:17", "-", "Low", 3, 1),
                new DuplicationRow("Main.java:17", "-", "Low", 1, 1),
                new DuplicationRow("Main.java:20", "-", "Low", 3, 1),
                new DuplicationRow("Main.java:24", "-", "Low", 3, 1),
                new DuplicationRow("Main.java:26", "-", "Low", 8, 1),
                new DuplicationRow("Main.java:26", "-", "Low", 3, 1));
    }

    /**
     * Verifies that the content of the issues table is as expected.
     */
    @Test
    public void firstTableRowShouldHaveRightContent() {
        FreeStyleProject project = createJobWithWorkspaceFiles(CPD_REPORT);
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
                        + "{\n    System.out.println(&quot;testfile for redundancy&quot;);</code></pre>");
        DomElement file = firstTableRowCells.get(1);
        assertThat(file.getTextContent()).isEqualTo("Main.java:11");

        int[] duplications = {8, 17, 20, 26, 29};
        HtmlUnorderedList duplicationList = (HtmlUnorderedList) firstTableRowCells.get(4).getFirstElementChild();
        @SuppressWarnings("unchecked")
        NodeList duplicationListItems = duplicationList.getChildNodes();

        assertThat(duplicationListItems.getLength()).isEqualTo(duplications.length);
        for (int i = 0; i < duplications.length; i++) {
            Node otherFile = duplicationListItems.item(i);
            assertThat(otherFile.getTextContent()).isEqualTo("Main.java:" + duplications[i]);
        }
    }

    /**
     * Verifies that the duplicate code lines of cpd warnings are correct.
     */
    @Test
    public void duplicateCodeLinesShouldBeOfRightAmount() {
        FreeStyleProject project = createJobWithWorkspaceFiles(CPD_REPORT);
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
        FreeStyleProject project = createJobWithWorkspaceFiles(CPD_REPORT);
        Cpd cpd = new Cpd();
        enableWarnings(project, cpd);

        assertThatThresholdsAreEvaluated(25, 50, 20, 0, 0, cpd, project);
        assertThatThresholdsAreEvaluated(2, 4, 6, 9, 5, cpd, project);
        assertThatThresholdsAreEvaluated(1, 3, 0, 6, 14, cpd, project);
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
    private void assertThatThresholdsAreEvaluated(final int normalThreshold, final int highThreshold,
            final int low, final int normal, final int high,
            final DuplicateCodeScanner scanner, final FreeStyleProject project) {
        scanner.setNormalThreshold(normalThreshold);
        scanner.setHighThreshold(highThreshold);

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        HtmlPage page = getWebPage(result);
        checkAmountOfPriorityWarnings(page.getElementById("number-priorities"), low, normal, high);
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
    private void checkAmountOfPriorityWarnings(final DomElement heading, 
            final int low, final int normal, final int high) {
        assertThatAttributeContainsValue(heading, "data-low", low);
        assertThatAttributeContainsValue(heading, "data-normal", normal);
        assertThatAttributeContainsValue(heading, "data-high", high);
    }

    private void assertThatAttributeContainsValue(
            final DomElement heading, final String attributeName, final int value) {
        assertThat(heading.getAttribute(attributeName)).isEqualTo(String.valueOf(value));
    }

    /**
     * Helper-method which builds the result and returns the issues table of a specified result type.
     *
     * @param project
     *         the project, which shall be build.
     * @return the issues table as {@link HtmlTable} object.
     */
    private HtmlTable getIssuesTable(final FreeStyleProject project) {
        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        HtmlPage page = getWebPage(result);
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
     * @return a list of #{@link HtmlTableRow}-elements displayed in the issues-table.
     */
    private List<HtmlTableRow> getIssueTableRows(final FreeStyleProject project) {
        HtmlTable table = getIssuesTable(project);

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
        try {
            return element.click();
        }
        catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Helper-method which builds the project and checks the priority of the warning in the first row of the
     * issues-table.
     *
     * @param expectedPriority
     *         the expected priority of the first warning.
     * @param project
     *         the current {@link FreeStyleProject}.
     */
    private void checkPriorityOfFirstWarningInTable(final String expectedPriority, final FreeStyleProject project) {
        List<HtmlTableRow> tableRows = getIssueTableRows(project);
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
        checkPriorityOfFirstWarningInTable(expectedPriority, project);
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
    private void setNormalThresholdAndCheckPriority(final int normalThreshold, final String expectedPriority,
            final DuplicateCodeScanner scanner, final FreeStyleProject project) {
        scanner.setNormalThreshold(normalThreshold);
        checkPriorityOfFirstWarningInTable(expectedPriority, project);
    }
}

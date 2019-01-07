package io.jenkins.plugins.analysis.warnings.recorder;

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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import hudson.model.FreeStyleProject;
import hudson.model.Result;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.core.util.QualityGateStatus;
import io.jenkins.plugins.analysis.warnings.Cpd;
import io.jenkins.plugins.analysis.warnings.DuplicateCodeScanner;
import io.jenkins.plugins.analysis.warnings.Simian;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.DuplicationTable;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.DuplicationTable.DuplicationRow;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

/**
 * Integration tests for the DRY parsers of the warnings plug-in in freestyle jobs.
 *
 * @author Stephan Plöderl
 */
public class DryITest extends IntegrationTestWithJenkinsPerSuite {
    private static final String FOLDER = "dry/";
    private static final String SIMIAN_REPORT = FOLDER + "simian.xml";
    private static final String CPD_REPORT = FOLDER + "cpd.xml";

    /**
     * Verifies that the right amount of duplicate code warnings are detected.
     */
    @Test
    public void shouldHaveDuplicateCodeWarnings() {
        FreeStyleProject project = createFreeStyleProjectWithWorkspaceFiles(CPD_REPORT);
        enableGenericWarnings(project, new Cpd());

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(result).hasTotalSize(20);
        assertThat(result).hasQualityGateStatus(QualityGateStatus.INACTIVE);
    }

    /**
     * Verifies that the priority of the duplicate code warnings are changed corresponding to the defined thresholds for
     * cpd warnings.
     */
    @Test
    public void shouldConfigureSeverityThresholdsInJobConfigurationForCpd() {
        FreeStyleProject project = createFreeStyleProjectWithWorkspaceFiles(CPD_REPORT);
        Cpd cpd = new Cpd();
        cpd.setNormalThreshold(1);
        enableGenericWarnings(project, cpd);

        setHighThresholdAndCheckPriority(2, "High", cpd, project);
        setHighThresholdAndCheckPriority(5, "Normal", cpd, project);
        setNormalThresholdAndCheckPriority(4, "Low", cpd, project);
    }

    /**
     * Verifies that the priority of the duplicate code warnings are changed corresponding to the defined thresholds for
     * simian warnings.
     */

    @Test
    public void shouldConfigureSeverityThresholdsInJobConfigurationForSimian() {
        FreeStyleProject project = createFreeStyleProjectWithWorkspaceFiles(SIMIAN_REPORT);
        Simian simian = new Simian();
        simian.setNormalThreshold(1);
        enableGenericWarnings(project, simian);

        setHighThresholdAndCheckPriority(2, "High", simian, project);
        setHighThresholdAndCheckPriority(6, "Normal", simian, project);
        setNormalThresholdAndCheckPriority(5, "Low", simian, project);
    }

    /**
     * Verifies that the priority links are redirecting to a filtered side, showing only the warnings of this priority.
     */
    @Test
    public void shouldFilterIssuesBySeverity() {
        FreeStyleProject project = createFreeStyleProjectWithWorkspaceFiles(CPD_REPORT);
        Cpd cpd = new Cpd();
        cpd.setNormalThreshold(2);
        cpd.setHighThreshold(4);
        enableGenericWarnings(project, cpd);

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        HtmlPage details = getWebPage(result);

        DuplicationTable issues = new DuplicationTable(details, false);
        assertThat(issues.getRows()).hasSize(10); // paging of 10 is activated by default
        
        assertSizeOfSeverity(issues, 2, 5); // HIGH
        assertSizeOfSeverity(issues, 0, 9); // NORMAL
        assertSizeOfSeverity(issues, 5, 6); // LOW
    }

    private void assertSizeOfSeverity(final DuplicationTable table, final int row, 
            final int numberOfSelectedIssues) {
        DuplicationRow selectedRow = table.getRow(row);
        HtmlPage detailsOfSeverity = selectedRow.clickSeverity();

        DuplicationTable issues = new DuplicationTable(detailsOfSeverity, false);
        assertThat(issues.getRows()).hasSize(numberOfSelectedIssues);
    }

    /**
     * Verifies that the source code links are redirecting to a side displaying the source code.
     */
    @Test
    public void shouldNavigateToSourceCode() {
        FreeStyleProject project = createFreeStyleProjectWithWorkspaceFiles(CPD_REPORT);
        copySingleFileToWorkspace(project, FOLDER + "Main.source", "Main.java");
        Cpd cpd = new Cpd();
        cpd.setNormalThreshold(2);
        cpd.setHighThreshold(4);
        enableGenericWarnings(project, cpd);

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        HtmlPage details = getWebPage(result);

        DuplicationTable issues = new DuplicationTable(details, false);
        assertThat(issues.getRows()).hasSize(10);

        HtmlPage sourceCodePage = issues.getRow(0).clickSourceCode();

        String htmlFile = toString(FOLDER + "Main.source");
        assertThat(extractSourceCodeFromDetailsPage(sourceCodePage)).isEqualToIgnoringWhitespace(htmlFile);
    }

    /**
     * Verifies the structure of the duplications table. i.e. the table headers and column contents.
     */
    @Test
    public void shouldShowDuplicationColumnContent() {
        FreeStyleProject project = createFreeStyleProjectWithWorkspaceFiles(CPD_REPORT);
        Cpd cpd = new Cpd();
        enableGenericWarnings(project, cpd);

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
     * Verifies that the first table column shows the content of the duplicated code if clicked.
     */
    @Test
    public void shouldShowDetailsWithDuplicatedSourceCodeSnippet() {
        FreeStyleProject project = createFreeStyleProjectWithWorkspaceFiles(CPD_REPORT);
        Cpd cpd = new Cpd();
        cpd.setNormalThreshold(2);
        cpd.setHighThreshold(4);
        enableGenericWarnings(project, cpd);

        List<HtmlTableRow> tableRows = scheduleBuildAndGetRows(project);

        HtmlTableRow firstTableRow = tableRows.get(0);
        List<HtmlTableCell> firstTableRowCells = firstTableRow.getCells();
        HtmlDivision divElement = getCellAs(firstTableRowCells, 0, HtmlDivision.class);

        assertThat(divElement.getAttribute("class")).isEqualTo("details-control");
        assertThat(divElement.getAttribute("data-description")).isEqualTo(
                "<p><strong>Found duplicated code.</strong></p> <pre><code>public static void functionOne()\n  "
                        + "{\n    System.out.println(&#34;testfile for redundancy&#34;);</code></pre>");
        DomElement file = firstTableRowCells.get(1);
        assertThat(file.getTextContent()).isEqualTo("Main.java:11");

        int[] duplications = {8, 17, 20, 26, 29};
        HtmlUnorderedList duplicationList = getCellAs(firstTableRowCells, 4, HtmlUnorderedList.class);
        @SuppressWarnings("unchecked")
        NodeList duplicationListItems = duplicationList.getChildNodes();

        assertThat(duplicationListItems.getLength()).isEqualTo(duplications.length);
        for (int i = 0; i < duplications.length; i++) {
            Node otherFile = duplicationListItems.item(i);
            assertThat(otherFile.getTextContent()).isEqualTo("Main.java:" + duplications[i]);
        }
    }

    private <T> T getCellAs(final List<HtmlTableCell> cells, final int index, final Class<T> type) {
        DomElement firstElementChild = cells.get(index).getFirstElementChild();
        assertThat(firstElementChild).isInstanceOf(type);
        
        return type.cast(firstElementChild);
    }

    /**
     * Verifies that the duplicate code lines of cpd warnings are correct.
     */
    @Test
    public void duplicateCodeLinesShouldBeOfRightAmount() {
        FreeStyleProject project = createFreeStyleProjectWithWorkspaceFiles(CPD_REPORT);
        Cpd cpd = new Cpd();
        cpd.setNormalThreshold(2);
        cpd.setHighThreshold(4);
        enableGenericWarnings(project, cpd);

        List<HtmlTableRow> tableRows = scheduleBuildAndGetRows(project);
        //only 10 are displayed because of the paging

        assertThat(tableRows).hasSize(10);
        for (HtmlTableRow tableRow : tableRows) {
            List<HtmlTableCell> tableCells = tableRow.getCells();
            assertThat(tableCells).hasSize(6);
            HtmlDivision divElement = getCellAs(tableCells, 0, HtmlDivision.class);
            int lineCount = Integer.parseInt(tableCells.get(3).getTextContent());
            assertThat(divElement.getAttribute("data-description").split("\n")).hasSize(lineCount);
        }
    }

    /**
     * Verifies that the total amount of low, normal, and high warnings should change according to the thresholds.
     */
    @Test
    public void shouldDifferInAmountOfDuplicateWarningForPriorities() {
        FreeStyleProject project = createFreeStyleProjectWithWorkspaceFiles(CPD_REPORT);
        Cpd cpd = new Cpd();
        enableGenericWarnings(project, cpd);

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

        assertThat(result.getTotalHighPrioritySize()).isEqualTo(high);
        assertThat(result.getTotalNormalPrioritySize()).isEqualTo(normal);
        assertThat(result.getTotalLowPrioritySize()).isEqualTo(low);
    }

    @SuppressFBWarnings("BC")
    private HtmlTable getIssuesTable(final HtmlPage page) {
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
    private List<HtmlTableRow> scheduleBuildAndGetRows(final FreeStyleProject project) {
        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        HtmlPage page = getWebPage(result);
        HtmlTable table = getIssuesTable(page);

        List<HtmlTableBody> bodies = table.getBodies();
        assertThat(bodies).hasSize(1);
        
        HtmlTableBody tableBody = bodies.get(0);
        return tableBody.getRows();
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
        List<HtmlTableRow> tableRows = scheduleBuildAndGetRows(project);
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

package io.jenkins.plugins.analysis.warnings;

import java.time.Duration;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;

import io.jenkins.plugins.analysis.warnings.AnalysisResult.Tab;

import static io.jenkins.plugins.analysis.warnings.Assertions.*;

/**
 * Integration tests for the details tab part of issue overview page.
 *
 * @author Nils Engelbrecht
 * @author Kevin Richter
 * @author Simon Schönwiese
 */
@WithPlugins("warnings-ng")
@SuppressFBWarnings("BC")
public class DetailsTabUiTest extends UiTest {
    private static final String DETAILS_TAB_RESOURCES = "details_tab_test/";

    /**
     * When a single warning is being recognized only the issues-tab should be shown.
     */
    @Test
    public void shouldPopulateDetailsTabSingleWarning() {
        FreeStyleJob job = createFreeStyleJob(DETAILS_TAB_RESOURCES + "java1Warning.txt");
        job.addPublisher(IssuesRecorder.class, recorder -> recorder.setToolWithPattern(JAVA_COMPILER, "**/*.txt"));
        job.save();

        Build build = job.startBuild().waitUntilFinished();
        assertThat(build.isSuccess()).isTrue();

        AnalysisResult resultPage = new AnalysisResult(build, "java");
        resultPage.open();

        Collection<Tab> tabs = resultPage.getAvailableTabs();
        assertThat(tabs).containsOnlyOnce(Tab.ISSUES);
        assertThat(resultPage.getActiveTab()).isEqualTo(Tab.ISSUES);

        IssuesTable issuesTable = resultPage.openIssuesTable();
        assertThat(issuesTable.getTableRows()).hasSize(1);
    }

    /**
     * When two warnings are being recognized in one file the tabs issues, files and folders should be shown.
     */
    @Test
    public void shouldPopulateDetailsTabMultipleWarnings() {
        FreeStyleJob job = createFreeStyleJob(DETAILS_TAB_RESOURCES + "java2Warnings.txt");
        job.addPublisher(IssuesRecorder.class, recorder -> recorder.setToolWithPattern(JAVA_COMPILER, "**/*.txt"));
        job.save();

        Build build = job.startBuild().waitUntilFinished();
        assertThat(build.isSuccess()).isTrue();

        AnalysisResult resultPage = new AnalysisResult(build, "java");
        resultPage.open();

        assertThat(resultPage).hasOnlyAvailableTabs(Tab.FOLDERS, Tab.FILES, Tab.ISSUES);

        PropertyDetailsTable foldersDetailsTable = resultPage.openPropertiesTable(Tab.FOLDERS);
        assertThat(foldersDetailsTable.getTotal()).isEqualTo(2);

        PropertyDetailsTable filesDetailsTable = resultPage.openPropertiesTable(Tab.FILES);
        assertThat(filesDetailsTable.getTotal()).isEqualTo(2);

        IssuesTable issuesTable = resultPage.openIssuesTable();
        assertThat(issuesTable.getTotal()).isEqualTo(2);
    }

    /**
     * When switching details-tab and the page is being reloaded, the previously selected tab should be memorized and
     * still be active.
     */
    @Test
    public void shouldMemorizeSelectedTabAsActiveOnPageReload() {
        FreeStyleJob job = createFreeStyleJob("checkstyle-result.xml");
        job.addPublisher(IssuesRecorder.class, recorder -> recorder.setTool(CHECKSTYLE_TOOL));
        job.save();

        Build build = job.startBuild().waitUntilFinished();
        assertThat(build.isSuccess()).isTrue();

        AnalysisResult resultPage = new AnalysisResult(build, "checkstyle");
        resultPage.open();

        assertThat(resultPage).hasOnlyAvailableTabs(Tab.ISSUES, Tab.TYPES, Tab.CATEGORIES);

        assertThat(resultPage.getActiveTab()).isNotEqualTo(Tab.TYPES);
        resultPage.openTab(Tab.TYPES);
        assertThat(resultPage.getActiveTab()).isEqualTo(Tab.TYPES);

        resultPage.reload();
        assertThat(resultPage.getActiveTab()).isEqualTo(Tab.TYPES);
    }

    /**
     * When having a larger checkstyle result, the table should display all Tabs, tables and pages correctly and should
     * be able to change the page.
     */
    @Test
    public void shouldWorkWithMultipleTabsAndPages() {
        FreeStyleJob job = createFreeStyleJob("checkstyle-result.xml");
        job.addPublisher(IssuesRecorder.class, recorder -> recorder.setTool(CHECKSTYLE_TOOL));
        job.save();

        Build build = job.startBuild().waitUntilFinished();
        assertThat(build.isSuccess()).isTrue();

        AnalysisResult resultPage = new AnalysisResult(build, "checkstyle");
        resultPage.open();

        assertThat(resultPage).hasOnlyAvailableTabs(Tab.ISSUES, Tab.TYPES, Tab.CATEGORIES);

        PropertyDetailsTable categoriesDetailsTable = resultPage.openPropertiesTable(Tab.CATEGORIES);
        assertThat(categoriesDetailsTable.getHeaders()).containsOnly("Category", "Total", "New", "Distribution");
        assertThat(categoriesDetailsTable.getSize()).isEqualTo(5);
        assertThat(categoriesDetailsTable.getTotal()).isEqualTo(5);

        PropertyDetailsTable typesDetailsTable = resultPage.openPropertiesTable(Tab.TYPES);
        assertThat(typesDetailsTable.getHeaders()).containsOnly("Type", "Total", "New", "Distribution");
        assertThat(typesDetailsTable.getSize()).isEqualTo(7);
        assertThat(typesDetailsTable.getTotal()).isEqualTo(7);

        IssuesTable issuesTable = resultPage.openIssuesTable();
        assertThat(issuesTable.getColumnHeaders()).containsOnly(IssuesTable.Header.DETAILS, IssuesTable.Header.FILE, IssuesTable.Header.CATEGORY,
                IssuesTable.Header.TYPE, IssuesTable.Header.SEVERITY, IssuesTable.Header.AGE);
        assertThat(issuesTable.getSize()).isEqualTo(10);
        assertThat(issuesTable.getTotal()).isEqualTo(11);

        List<IssuesTableRow> tableRowListIssues = issuesTable.getTableRows();
        AbstractSeverityTableRow firstRow = tableRowListIssues.get(9);
        firstRow.toggleDetailsRow();

        issuesTable.openTablePage(2);
        assertThat(issuesTable.getSize()).isEqualTo(1);

        tableRowListIssues = issuesTable.getTableRows();
        AbstractSeverityTableRow lastIssueTableRow = tableRowListIssues.get(0);
        assertThat(lastIssueTableRow.getSeverity()).isEqualTo("Error");
        AnalysisResult analysisResult = lastIssueTableRow.clickOnSeverityLink();
        IssuesTable errorIssuesTable = analysisResult.openIssuesTable();
        assertThat(errorIssuesTable.getSize()).isEqualTo(6);
        for (int i = 0; i < errorIssuesTable.getSize(); i++) {
            AbstractSeverityTableRow row = errorIssuesTable.getTableRows().get(i);
            assertThat(row.getSeverity()).isEqualTo("Error");
        }
    }

    /**
     * Checks if the severity and age of the generated issue table from a Analysis Summary with the CPD tool shows the
     * correct severity and age.
     */
    @Test
    public void shouldShowCorrectSeverityAndAge() {
        FreeStyleJob job = createFreeStyleJob("cpd1Warning.xml");
        job.addPublisher(IssuesRecorder.class, recorder -> recorder.setToolWithPattern(CPD_TOOL, "**/*.xml"));
        job.save();

        Build build = job.startBuild().waitUntilFinished();
        build.open();

        AnalysisSummary cpd = new AnalysisSummary(build, "cpd");

        AnalysisResult cpdDetails = cpd.openOverallResult();

        DryTable issuesDetailsTable = cpdDetails.openDryTable();
        DryTableRow issuesTableFirstRow = issuesDetailsTable.getRow(0);
        assertThat(issuesTableFirstRow.getSeverity()).isEqualTo("Normal");
        assertThat(issuesTableFirstRow.getAge()).isEqualTo(1);
    }

    /**
     * When selecting different options in the dropdown menu that controls the numbers of displayed rows.
     */
    @Test
    public void shouldShowTheCorrectNumberOfRowsSelectedByLength() {
        FreeStyleJob job = createFreeStyleJob(DETAILS_TAB_RESOURCES + "findbugs-severities.xml");
        job.addPublisher(IssuesRecorder.class, recorder -> recorder.setToolWithPattern(FINDBUGS_TOOL, "**/*.xml"));
        job.save();

        Build build = job.startBuild().waitUntilFinished();
        build.open();

        AnalysisSummary resultPage = new AnalysisSummary(build, "findbugs");
        assertThat(resultPage).hasTitleText("FindBugs: 12 warnings");

        AnalysisResult findBugsAnalysisResult = resultPage.openOverallResult();

        assertThat(findBugsAnalysisResult).hasAvailableTabs(Tab.ISSUES);

        findBugsAnalysisResult.openPropertiesTable(Tab.ISSUES);

        Select issuesLengthSelect = findBugsAnalysisResult.getLengthSelectElementByActiveTab();
        issuesLengthSelect.selectByValue("10");

        WebElement issuesInfo = findBugsAnalysisResult.getInfoElementByActiveTab();
        waitUntilCondition(issuesInfo, "Showing 1 to 10 of 12 entries");

        assertThat(findBugsAnalysisResult.getPaginationButtons()).hasSize(2);

        issuesLengthSelect.selectByValue("25");
        waitUntilCondition(issuesInfo, "Showing 1 to 12 of 12 entries");

        assertThat(findBugsAnalysisResult.getPaginationButtons()).hasSize(1);
    }

    /**
     * When filling out the filter input field, the correct rows should be displayed.
     */
    @Test
    public void shouldDisplayTheFilteredRows() {
        FreeStyleJob job = createFreeStyleJob(DETAILS_TAB_RESOURCES + "findbugs-severities.xml");
        job.addPublisher(IssuesRecorder.class, recorder -> recorder.setToolWithPattern(FINDBUGS_TOOL, "**/*.xml"));
        job.save();

        Build build = job.startBuild().waitUntilFinished();
        build.open();

        AnalysisSummary resultPage = new AnalysisSummary(build, "findbugs");
        assertThat(resultPage).hasTitleText("FindBugs: 12 warnings");
        AnalysisResult findBugsAnalysisResult = resultPage.openOverallResult();

        assertThat(findBugsAnalysisResult).hasAvailableTabs(Tab.ISSUES);

        findBugsAnalysisResult.openPropertiesTable(Tab.ISSUES);

        WebElement issuesFilterInput = findBugsAnalysisResult.getFilterInputElementByActiveTab();

        issuesFilterInput.sendKeys("CalculateFrame");

        WebElement issuesInfo = findBugsAnalysisResult.getInfoElementByActiveTab();
        waitUntilCondition(issuesInfo, "Showing 1 to 2 of 2 entries (filtered from 12 total entries)");

        issuesFilterInput.clear();

        issuesFilterInput.sendKeys("STYLE");
        waitUntilCondition(issuesInfo, "Showing 1 to 7 of 7 entries (filtered from 12 total entries)");
    }

    /**
     * When selecting different options in the dropdown menu that controls the numbers of displayed rows.
     */
    @Test
    public void shouldMemorizeSelectedNumberOfRowsOnReload() {
        FreeStyleJob job = createFreeStyleJob(DETAILS_TAB_RESOURCES + "findbugs-severities.xml");
        job.addPublisher(IssuesRecorder.class, recorder -> recorder.setToolWithPattern(FINDBUGS_TOOL, "**/*.xml"));
        job.save();

        Build build = job.startBuild().waitUntilFinished();
        build.open();

        AnalysisSummary resultPage = new AnalysisSummary(build, "findbugs");
        assertThat(resultPage).hasTitleText("FindBugs: 12 warnings");
        AnalysisResult findBugsAnalysisResult = resultPage.openOverallResult();

        assertThat(findBugsAnalysisResult).hasAvailableTabs(Tab.ISSUES);

        findBugsAnalysisResult.openPropertiesTable(Tab.ISSUES);

        Select issuesLengthSelect = findBugsAnalysisResult.getLengthSelectElementByActiveTab();

        issuesLengthSelect.selectByValue("50");
        WebElement issuesInfo = findBugsAnalysisResult.getInfoElementByActiveTab();
        waitUntilCondition(issuesInfo, "Showing 1 to 12 of 12 entries");

        assertThat(findBugsAnalysisResult.getPaginationButtons()).hasSize(1);

        resultPage.open();
        findBugsAnalysisResult.reload();

        issuesInfo = findBugsAnalysisResult.getInfoElementByActiveTab();
        waitUntilCondition(issuesInfo, "Showing 1 to 12 of 12 entries");

        issuesLengthSelect = findBugsAnalysisResult.getLengthSelectElementByActiveTab();
        assertThat(issuesLengthSelect.getFirstSelectedOption().getText()).isEqualTo("50");

        assertThat(findBugsAnalysisResult.getPaginationButtons()).hasSize(1);
    }

    /**
     * Waits for a defined period of time for a string to be present inside a WebElement. If this is not the case, an
     * exception will be thrown and the test fails.
     *
     * @param expectedString
     *         String that should eventually be present in the element
     * @param target
     *         WebElement that should contain the expected string
     */
    private void waitUntilCondition(final WebElement target, final String expectedString) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(2), Duration.ofMillis(100));
        wait.until(ExpectedConditions.textToBePresentInElement(target, expectedString));
    }
}

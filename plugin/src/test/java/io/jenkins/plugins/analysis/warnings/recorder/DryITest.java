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
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.SourceCodeView;
import io.jenkins.plugins.datatables.TablePageObject;
import io.jenkins.plugins.datatables.TableRowPageObject;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

/**
 * Integration tests for the DRY parsers of the warnings plug-in in freestyle jobs.
 *
 * @author Stephan Plöderl
 */
public class DryITest extends IntegrationTestWithJenkinsPerSuite {
    private static final String DETAILS = "Details";
    private static final String FILE = "File";
    private static final String SEVERITY = "Severity";
    private static final String LINES = "#Lines";
    private static final String DUPLICATIONS = "Duplicated In";
    private static final String AGE = "Age";

    private static final String FOLDER = "dry/";
    private static final String CPD_REPORT = FOLDER + "cpd.xml";
    private static final int ROW_INDEX_OF_DUPLICATION_TO_INSPECT = 8;

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

        assertThat(result.getTotalHighPrioritySize()).isEqualTo(5);
        assertThat(result.getTotalNormalPrioritySize()).isEqualTo(9);
        assertThat(result.getTotalLowPrioritySize()).isEqualTo(6);
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

        HtmlPage page = getWebPage(JavaScriptSupport.JS_ENABLED, result);
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
        HtmlTableRow firstRow = tableRows.get(ROW_INDEX_OF_DUPLICATION_TO_INSPECT);
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

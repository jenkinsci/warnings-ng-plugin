package io.jenkins.plugins.analysis.warnings.steps;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.model.Run;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.IssuesDetail;
import io.jenkins.plugins.analysis.core.model.ResultAction;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.warnings.Cpd;
import io.jenkins.plugins.analysis.warnings.DuplicateCodeScanner;
import io.jenkins.plugins.analysis.warnings.DuplicateCodeScanner.DryModel.DuplicationRow;
import io.jenkins.plugins.datatables.TableColumn;
import io.jenkins.plugins.datatables.TableModel;
import io.jenkins.plugins.util.QualityGateStatus;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

/**
 * Integration tests for the DRY parsers of the warnings plug-in in freestyle jobs.
 *
 * @author Stephan Plöderl
 * @author Lukas Kirner
 */
class DryITest extends IntegrationTestWithJenkinsPerSuite {
    private static final String DETAILS = "Details";
    private static final String FILE = "File";
    private static final String SEVERITY = "Severity";
    private static final String LINES = "#Lines";
    private static final String DUPLICATIONS = "Duplicated In";
    private static final String AGE = "Age";

    private static final String FOLDER = "dry/";
    private static final String CPD_REPORT = FOLDER + "cpd.xml";

    /**
     * Verifies that the right number of duplicate code warnings is detected.
     */
    @Test
    void shouldHaveDuplicateCodeWarnings() {
        FreeStyleProject project = createFreeStyleProjectWithWorkspaceFilesWithSuffix(CPD_REPORT);
        enableGenericWarnings(project, new Cpd());

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        assertThat(result).hasTotalSize(20);
        assertThat(result).hasQualityGateStatus(QualityGateStatus.INACTIVE);

        Run<?, ?> build = result.getOwner();
        TableModel table = getDryTableModel(build);
        assertThatColumnsAreCorrect(table.getColumns());

        table.getRows().stream()
                .map(DuplicateCodeScanner.DryModel.DuplicationRow.class::cast)
                .forEach(row -> assertThat(row.getSeverity()).contains("LOW"));
    }

    /**
     * Verifies that the priority of the duplicate code warnings is changed corresponding to the defined thresholds for
     * cpd warnings.
     */
    @Test
    void shouldConfigureSeverityThresholdTo2InJobConfigurationForCpd() {
        FreeStyleProject project = createFreeStyleProjectWithWorkspaceFilesWithSuffix(CPD_REPORT);
        Cpd cpd = new Cpd();
        cpd.setNormalThreshold(1);
        enableGenericWarnings(project, cpd);

        cpd.setHighThreshold(2);
        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(result.getTotalHighPrioritySize()).isEqualTo(14);
        assertThat(result.getTotalNormalPrioritySize()).isEqualTo(6);
        assertThat(result.getTotalLowPrioritySize()).isEqualTo(0);
        assertThat(result.getTotalErrorsSize()).isEqualTo(0);

        Run<?, ?> build = result.getOwner();
        TableModel table = getDryTableModel(build);
        assertThatColumnsAreCorrect(table.getColumns());

        assertThatLineCountForSeverityIsCorrect(table.getRows(), "NORMAL", 1, 1);
        assertThatLineCountForSeverityIsCorrect(table.getRows(), "HIGH", 2, Integer.MAX_VALUE);
    }

    /**
     * Verifies that the priority of the duplicate code warnings are changed corresponding to the defined thresholds for
     * cpd warnings.
     */
    @Test
    void shouldConfigureSeverityThresholdTo5InJobConfigurationForCpd() {
        FreeStyleProject project = createFreeStyleProjectWithWorkspaceFilesWithSuffix(CPD_REPORT);
        Cpd cpd = new Cpd();
        cpd.setNormalThreshold(1);
        enableGenericWarnings(project, cpd);

        cpd.setHighThreshold(5);
        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(result.getTotalHighPrioritySize()).isEqualTo(5);
        assertThat(result.getTotalNormalPrioritySize()).isEqualTo(15);
        assertThat(result.getTotalLowPrioritySize()).isEqualTo(0);
        assertThat(result.getTotalErrorsSize()).isEqualTo(0);

        Run<?, ?> build = result.getOwner();
        TableModel table = getDryTableModel(build);
        assertThatColumnsAreCorrect(table.getColumns());

        assertThatLineCountForSeverityIsCorrect(table.getRows(), "NORMAL", 1, 4);
        assertThatLineCountForSeverityIsCorrect(table.getRows(), "HIGH", 5, Integer.MAX_VALUE);
    }

    /**
     * Verifies that the priority of the duplicate code warnings is changed corresponding to the defined thresholds for
     * cpd warnings.
     */
    @Test
    void shouldConfigureSeverityNormalThresholdTo4InJobConfigurationForCpd() {
        FreeStyleProject project = createFreeStyleProjectWithWorkspaceFilesWithSuffix(CPD_REPORT);
        Cpd cpd = new Cpd();
        cpd.setNormalThreshold(4);
        enableGenericWarnings(project, cpd);

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(result.getTotalHighPrioritySize()).isEqualTo(0);
        assertThat(result.getTotalNormalPrioritySize()).isEqualTo(5);
        assertThat(result.getTotalLowPrioritySize()).isEqualTo(15);
        assertThat(result.getTotalErrorsSize()).isEqualTo(0);

        Run<?, ?> build = result.getOwner();
        TableModel table = getDryTableModel(build);
        assertThatColumnsAreCorrect(table.getColumns());

        assertThatLineCountForSeverityIsCorrect(table.getRows(), "NORMAL", 4, Integer.MAX_VALUE);
    }

    /**
     * Verifies that the priority links are redirecting to a filtered side, showing only the warnings of this priority.
     */
    @Test
    void shouldFilterIssuesBySeverity() {
        FreeStyleProject project = createFreeStyleProjectWithWorkspaceFilesWithSuffix(CPD_REPORT);
        Cpd cpd = new Cpd();
        cpd.setNormalThreshold(2);
        cpd.setHighThreshold(4);
        enableGenericWarnings(project, cpd);

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(result.getTotalHighPrioritySize()).isEqualTo(5);
        assertThat(result.getTotalNormalPrioritySize()).isEqualTo(9);
        assertThat(result.getTotalLowPrioritySize()).isEqualTo(6);

        Run<?, ?> build = result.getOwner();
        TableModel table = getDryTableModel(build);
        assertThatColumnsAreCorrect(table.getColumns());

        assertThatLineCountForSeverityIsCorrect(table.getRows(), "NORMAL", 2, 3);
        assertThatLineCountForSeverityIsCorrect(table.getRows(), "HIGH", 4, Integer.MAX_VALUE);
    }

    /**
     * Verifies that the total amount of low, normal, and high warnings should change according to the thresholds.
     */
    @Test
    void shouldDifferInAmountOfDuplicateWarningForPriorities() {
        FreeStyleProject project = createFreeStyleProjectWithWorkspaceFilesWithSuffix(CPD_REPORT);
        Cpd cpd = new Cpd();
        enableGenericWarnings(project, cpd);

        assertThatThresholdsAreEvaluated(25, 50, 20, 0, 0, cpd, project);
        assertThatThresholdsAreEvaluated(2, 4, 6, 9, 5, cpd, project);
        assertThatThresholdsAreEvaluated(1, 3, 0, 6, 14, cpd, project);
    }

    private TableModel getDryTableModel(final Run<?, ?> build) {
        IssuesDetail issuesDetail = build.getAction(ResultAction.class).getTarget();
        return issuesDetail.getTableModel("issues");
    }

    private void assertThatColumnsAreCorrect(final List<TableColumn> columns) {
        assertThat(columns.stream().map(TableColumn::getHeaderLabel).collect(Collectors.toList()))
                .contains(DETAILS, FILE, SEVERITY, LINES, DUPLICATIONS, AGE);
    }

    private void assertThatLineCountForSeverityIsCorrect(final List<Object> data, final String severity, final Integer min, final Integer max) {
        data.stream()
                .map(DuplicateCodeScanner.DryModel.DuplicationRow.class::cast)
                .filter(row -> row.getSeverity().contains(severity))
                .map(DuplicationRow::getLinesCount)
                .map(Integer::valueOf)
                .forEach(count -> assertThat(count).isBetween(min, max));
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
}

package io.jenkins.plugins.analysis.core.model;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;
import static org.mockito.Mockito.*;

import edu.hm.hafner.analysis.Severity;
import hudson.model.Job;
import hudson.model.Run;
import io.jenkins.plugins.analysis.core.util.WarningsQualityGate;
import io.jenkins.plugins.analysis.core.util.WarningsQualityGate.QualityGateType;
import io.jenkins.plugins.util.QualityGate.QualityGateCriticality;
import io.jenkins.plugins.util.QualityGateResult;
import io.jenkins.plugins.util.QualityGateStatus;
import java.util.List;
import java.util.Optional;
import org.eclipse.collections.impl.factory.Lists;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Tests the class {@link OverviewModel}.
 *
 * @author Ullrich Hafner
 */
class OverviewModelTest {
    private final Job<?, ?> job = mock(Job.class);

    /**
     * Verifies that the sizes of all results are summed up and that only severities with warnings are shown.
     */
    @Test
    void shouldSumUpSizesOfAllResults() {
        var checkstyle = createAction("checkstyle", 5, 2, 1, QualityGateStatus.INACTIVE);
        var pmd = createAction("pmd", 3, 0, 4, QualityGateStatus.INACTIVE);
        when(checkstyle.getResult().getTotalSizeOf(Severity.WARNING_NORMAL)).thenReturn(5);
        when(pmd.getResult().getTotalSizeOf(Severity.WARNING_NORMAL)).thenReturn(1);
        when(pmd.getResult().getTotalSizeOf(Severity.ERROR)).thenReturn(2);

        var model = createModel(checkstyle, pmd);

        assertThat(model)
                .hasDisplayName("Overview")
                .hasUrlName("overview")
                .hasTotalSize(8)
                .hasNewSize(2)
                .hasFixedSize(5)
                .hasActions(checkstyle, pmd);
        assertThat(model.getTotalSizeOf(Severity.WARNING_NORMAL)).isEqualTo(6);
        assertThat(model.getTotalSizeOf(Severity.WARNING_LOW)).isZero();
        assertThat(model.getSeverities()).containsExactly(Severity.ERROR, Severity.WARNING_NORMAL);
        assertThat(model.getSeverityLabel(Severity.ERROR)).isEqualTo("Error");

        var segments = model.getTotalSegments();
        assertThat(segments).hasSize(2);
        assertThat(segments.get(0)).hasCount(2).hasLabel("Error").hasModifier("error");
        assertThat(segments.get(1)).hasCount(6).hasLabel("Normal").hasModifier("normal");
    }

    /**
     * Verifies that the distribution of the new warnings by severity is computed from the new warnings only.
     */
    @Test
    void shouldCreateSegmentsForNewWarnings() {
        var checkstyle = createAction("checkstyle", 5, 3, 0, QualityGateStatus.INACTIVE);
        var pmd = createAction("pmd", 3, 1, 0, QualityGateStatus.INACTIVE);
        when(checkstyle.getResult().getNewSizeOf(Severity.WARNING_HIGH)).thenReturn(1);
        when(checkstyle.getResult().getNewSizeOf(Severity.WARNING_LOW)).thenReturn(2);
        when(pmd.getResult().getNewSizeOf(Severity.WARNING_LOW)).thenReturn(1);
        when(pmd.getResult().getTotalSizeOf(Severity.ERROR)).thenReturn(3);

        var model = createModel(checkstyle, pmd);

        assertThat(model.getNewSizeOf(Severity.WARNING_LOW)).isEqualTo(3);
        assertThat(model.getNewSizeOf(Severity.ERROR)).isZero();

        var segments = model.getNewSegments();
        assertThat(segments).hasSize(2);
        assertThat(segments.get(0)).hasCount(1).hasLabel("High").hasModifier("high");
        assertThat(segments.get(1)).hasCount(3).hasLabel("Low").hasModifier("low");
    }

    /**
     * Verifies that an overview without results shows no values and no quality gates.
     */
    @Test
    void shouldHandleRunWithoutResults() {
        var model = createModel();

        assertThat(model)
                .hasTotalSize(0)
                .hasNewSize(0)
                .hasFixedSize(0)
                .hasNoActions()
                .hasNoSeverities()
                .hasNoTotalSegments()
                .hasNoNewSegments()
                .hasNoQualityGateActions()
                .doesNotHaveQualityGates()
                .doesNotHaveErrorMessages()
                .hasErrorMessagesSize(0)
                .hasQualityGateStatus(QualityGateStatus.INACTIVE);
    }

    /**
     * Verifies that only results with active quality gates are shown and that the worst status determines the
     * overall status.
     */
    @Test
    void shouldReportWorstQualityGateStatus() {
        var checkstyle = createAction("checkstyle", 1, 0, 0, QualityGateStatus.PASSED);
        var pmd = createAction("pmd", 1, 0, 0, QualityGateStatus.INACTIVE);
        var spotbugs = createAction("spotbugs", 1, 0, 0, QualityGateStatus.WARNING);

        var model = createModel(checkstyle, pmd, spotbugs);

        assertThat(model)
                .hasQualityGates()
                .hasQualityGateActions(checkstyle, spotbugs)
                .hasQualityGateStatus(QualityGateStatus.WARNING);
    }

    /**
     * Verifies that the quality gates of all results are counted, and that only the quality gates that are not
     * successful are counted as missed.
     */
    @Test
    void shouldCountMissedQualityGates() {
        var checkstyle = createAction("checkstyle", 1, 0, 0, QualityGateStatus.INACTIVE);
        var pmd = createAction("pmd", 1, 0, 0, QualityGateStatus.INACTIVE);
        when(checkstyle.getQualityGateResult())
                .thenReturn(createQualityGateResult(QualityGateStatus.PASSED, QualityGateStatus.WARNING));
        when(pmd.getQualityGateResult())
                .thenReturn(createQualityGateResult(QualityGateStatus.FAILED, QualityGateStatus.PASSED));

        assertThat(createModel(checkstyle, pmd))
                .hasQualityGatesSize(4)
                .hasMissedQualityGatesSize(2)
                .hasQualityGateStatus(QualityGateStatus.FAILED);
        assertThat(createModel()).hasQualityGatesSize(0).hasMissedQualityGatesSize(0);
    }

    /**
     * Verifies that the quality gate status is visualized with the build status symbols of Jenkins.
     *
     * @param status
     *         the quality gate status
     * @param icon
     *         the expected symbol
     */
    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource({
        "INACTIVE, symbol-status-nobuilt",
        "PASSED, symbol-status-blue",
        "NOTE, symbol-status-yellow",
        "WARNING, symbol-status-yellow",
        "ERROR, symbol-status-red",
        "FAILED, symbol-status-red"
    })
    void shouldUseBuildStatusSymbolsForQualityGates(final QualityGateStatus status, final String icon) {
        assertThat(createModel().getStatusIcon(status)).isEqualTo(icon);
    }

    /**
     * Verifies that the reference build is shown if all results use the same reference build.
     */
    @Test
    void shouldShowSingleReferenceBuild() {
        var reference = createBuild("#48", "main #48", "job/main/48/");
        var checkstyle = createAction("checkstyle", 1, 1, 0, QualityGateStatus.INACTIVE);
        var pmd = createAction("pmd", 1, 0, 1, QualityGateStatus.INACTIVE);
        setReferenceBuild(checkstyle, reference);
        setReferenceBuild(pmd, reference);

        assertThat(createModel(checkstyle, pmd))
                .hasReferenceBuild()
                .hasReferenceBuilds(reference)
                .hasReferenceBuildName("#48")
                .hasReferenceBuildText(Messages.OverviewModel_ReferenceBuild("#48"))
                .hasReferenceBuildUrl("job/main/48/")
                .hasReferenceBuildExceptions("")
                .hasReferenceBuildTooltip("checkstyle: #48, pmd: #48")
                .hasComparisonCaption(Messages.OverviewModel_ComparedTo("#48"));
    }

    /**
     * Verifies that the full name of the reference build is shown if it belongs to another job (e.g., the target
     * branch of a pull request).
     */
    @Test
    void shouldShowFullNameOfReferenceBuildOfOtherJob() {
        Run<?, ?> owner = mock(Run.class);
        doReturn(mock(Job.class)).when(owner).getParent();
        var reference = createBuild("#48", "main #48", "job/main/48/");
        doReturn(mock(Job.class)).when(reference).getParent();
        var checkstyle = createAction("checkstyle", 1, 1, 0, QualityGateStatus.INACTIVE);
        setReferenceBuild(checkstyle, reference);

        assertThat(new OverviewModel(owner, List.of(checkstyle)))
                .hasReferenceBuildName("main #48")
                .hasComparisonCaption(Messages.OverviewModel_ComparedTo("main #48"));
    }

    /**
     * Verifies that the reference build of most results is shown, and that the results with a different reference
     * build (or without a reference build) are named as exceptions.
     */
    @Test
    void shouldShowMainReferenceBuildWithExceptions() {
        var main = createBuild("#36", "main #36", "job/main/36/");
        var checkstyle = createAction("checkstyle", 1, 1, 0, QualityGateStatus.INACTIVE);
        var cpd = createAction("cpd", 66, 66, 0, QualityGateStatus.INACTIVE);
        var pmd = createAction("pmd", 1, 0, 1, QualityGateStatus.INACTIVE);
        var spotbugs = createAction("spotbugs", 1, 0, 0, QualityGateStatus.INACTIVE);
        setReferenceBuild(checkstyle, main);
        setReferenceBuild(cpd, createBuild("#2", "main #2", "job/main/2/"));
        setReferenceBuild(pmd, main);

        assertThat(createModel(checkstyle, cpd, pmd, spotbugs))
                .hasReferenceBuild()
                .hasReferenceBuildName("#36")
                .hasReferenceBuildUrl("job/main/36/")
                .hasReferenceBuildExceptions("cpd: #2, spotbugs: " + Messages.OverviewModel_NoReferenceBuild())
                .hasReferenceBuildText(Messages.OverviewModel_ReferenceBuildExceptions(
                        Messages.OverviewModel_ReferenceBuild("#36"),
                        "cpd: #2, spotbugs: " + Messages.OverviewModel_NoReferenceBuild()))
                .hasComparisonCaption(Messages.OverviewModel_ReferenceBuildExceptions(
                        Messages.OverviewModel_ComparedTo("#36"),
                        "cpd: #2, spotbugs: " + Messages.OverviewModel_NoReferenceBuild()));
    }

    /**
     * Verifies that the reference build of the first result is used if several reference builds are used equally
     * often.
     */
    @Test
    void shouldUseReferenceBuildOfFirstResultOnTie() {
        var checkstyle = createAction("checkstyle", 1, 1, 0, QualityGateStatus.INACTIVE);
        var pmd = createAction("pmd", 1, 0, 1, QualityGateStatus.INACTIVE);
        setReferenceBuild(checkstyle, createBuild("#48", "main #48", "job/main/48/"));
        setReferenceBuild(pmd, createBuild("#47", "main #47", "job/main/47/"));

        assertThat(createModel(checkstyle, pmd))
                .hasReferenceBuildName("#48")
                .hasReferenceBuildExceptions("pmd: #47")
                .hasReferenceBuildTooltip("checkstyle: #48, pmd: #47");
    }

    /**
     * Verifies that a hint is shown if there is no reference build.
     */
    @Test
    void shouldShowHintWithoutReferenceBuild() {
        var checkstyle = createAction("checkstyle", 1, 1, 0, QualityGateStatus.INACTIVE);

        assertThat(createModel(checkstyle))
                .doesNotHaveReferenceBuild()
                .hasNoReferenceBuilds()
                .hasReferenceBuildName("")
                .hasReferenceBuildText(Messages.OverviewModel_NoReferenceBuild())
                .hasReferenceBuildUrl("")
                .hasReferenceBuildExceptions("")
                .hasComparisonCaption(Messages.OverviewModel_NoReferenceBuild());
    }

    /**
     * Creates a build stub.
     *
     * @param displayName
     *         the display name of the build
     * @param fullDisplayName
     *         the full display name of the build (including the job name)
     * @param url
     *         the URL of the build, relative to the Jenkins root
     *
     * @return the build
     */
    private Run<?, ?> createBuild(final String displayName, final String fullDisplayName, final String url) {
        Run<?, ?> build = mock(Run.class);
        when(build.getDisplayName()).thenReturn(displayName);
        when(build.getFullDisplayName()).thenReturn(fullDisplayName);
        when(build.getUrl()).thenReturn(url);
        doReturn(job).when(build).getParent();
        return build;
    }

    /**
     * Sets the reference build of the specified result action stub.
     *
     * @param action
     *         the action
     * @param reference
     *         the reference build
     */
    private void setReferenceBuild(final ResultAction action, final Run<?, ?> reference) {
        var result = action.getResult(); // must not be called within a stubbing
        doReturn(Optional.of(reference)).when(result).getReferenceBuild();
    }

    /**
     * Creates a quality gate result with a quality gate for each of the specified statuses.
     *
     * @param statuses
     *         the status of each quality gate
     *
     * @return the quality gate result
     */
    private QualityGateResult createQualityGateResult(final QualityGateStatus... statuses) {
        var result = new QualityGateResult();
        for (QualityGateStatus status : statuses) {
            result.add(new WarningsQualityGate(0, QualityGateType.TOTAL, QualityGateCriticality.UNSTABLE), status, "1");
        }
        return result;
    }

    /**
     * Verifies that a passed quality gate is reported as passed, even if other results have no quality gates.
     */
    @Test
    void shouldReportPassedQualityGate() {
        var checkstyle = createAction("checkstyle", 1, 0, 0, QualityGateStatus.PASSED);
        var pmd = createAction("pmd", 1, 0, 0, QualityGateStatus.INACTIVE);

        assertThat(createModel(checkstyle, pmd))
                .hasQualityGates()
                .hasQualityGateActions(checkstyle)
                .hasQualityGateStatus(QualityGateStatus.PASSED);
    }

    /**
     * Verifies that the error messages of all results are counted and that each result links to its tool on the
     * log page.
     */
    @Test
    void shouldCountErrorMessages() {
        var checkstyle = createAction("checkstyle", 1, 0, 0, QualityGateStatus.INACTIVE);
        var pmd = createAction("pmd", 1, 0, 0, QualityGateStatus.INACTIVE);
        when(pmd.getResult().getErrorMessages()).thenReturn(Lists.immutable.of("No files found", "No blame"));

        var model = createModel(checkstyle, pmd);

        assertThat(model).hasErrorMessages().hasErrorMessagesSize(2);
        assertThat(model.getErrorMessagesSizeOf(checkstyle)).isZero();
        assertThat(model.getErrorMessagesSizeOf(pmd)).isEqualTo(2);
        assertThat(model.getLogUrlOf(pmd)).isEqualTo("log#tool=pmd");
    }

    /**
     * Verifies that the criticality of a quality gate is visualized with an error icon (if the step or build fails)
     * or a warning icon (if the step or build is marked as unstable).
     *
     * @param criticality
     *         the criticality of the quality gate
     * @param icon
     *         the expected icon
     * @param color
     *         the expected color
     * @param description
     *         the expected description
     */
    @ParameterizedTest(name = "{0}")
    @CsvSource({
        "NOTE,     symbol-warning-outline plugin-ionicons-api,      jenkins-!-warning-color, Mark the step as unstable",
        "UNSTABLE, symbol-warning-outline plugin-ionicons-api,      jenkins-!-warning-color, Mark the step and the build as unstable",
        "ERROR,    symbol-close-circle-outline plugin-ionicons-api, jenkins-!-error-color,   Fail the step but not the build",
        "FAILURE,  symbol-close-circle-outline plugin-ionicons-api, jenkins-!-error-color,   Fail the step and the build"
    })
    void shouldVisualizeCriticality(
            final QualityGateCriticality criticality, final String icon, final String color, final String description) {
        var qualityGate = new WarningsQualityGate(10, QualityGateType.TOTAL, criticality);

        var model = createModel();

        assertThat(model.getCriticalityIcon(qualityGate)).isEqualTo(icon);
        assertThat(model.getCriticalityColor(qualityGate)).isEqualTo(color);
        assertThat(model.getCriticalityDescription(qualityGate)).isEqualTo(description);
    }

    /**
     * Creates a model for a run with the specified results.
     *
     * @param actions
     *         the results of the run
     *
     * @return the model
     */
    private OverviewModel createModel(final ResultAction... actions) {
        Run<?, ?> owner = mock(Run.class);
        doReturn(job).when(owner).getParent();
        return new OverviewModel(owner, List.of(actions));
    }

    /**
     * Creates a result action stub with the specified values.
     *
     * @param id
     *         the ID of the action
     * @param totalSize
     *         the total number of warnings
     * @param newSize
     *         the number of new warnings
     * @param fixedSize
     *         the number of fixed warnings
     * @param status
     *         the overall quality gate status
     *
     * @return the action
     */
    private ResultAction createAction(
            final String id,
            final int totalSize,
            final int newSize,
            final int fixedSize,
            final QualityGateStatus status) {
        var action = mock(ResultAction.class);
        var result = mock(AnalysisResult.class);

        when(action.getId()).thenReturn(id);
        when(action.getDisplayName()).thenReturn(id);
        when(action.getUrlName()).thenReturn(id);
        when(action.getResult()).thenReturn(result);
        when(action.getQualityGateResult()).thenReturn(new QualityGateResult(status));
        when(result.getTotalSize()).thenReturn(totalSize);
        when(result.getNewSize()).thenReturn(newSize);
        when(result.getFixedSize()).thenReturn(fixedSize);
        when(result.getErrorMessages()).thenReturn(Lists.immutable.empty());

        return action;
    }
}

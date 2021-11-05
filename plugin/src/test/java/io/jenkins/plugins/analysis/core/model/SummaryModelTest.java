package io.jenkins.plugins.analysis.core.model;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.factory.Maps;
import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.echarts.Build;

import hudson.model.BallColor;
import hudson.model.Run;

import io.jenkins.plugins.analysis.core.model.SummaryModel.LabelProviderFactoryFacade;
import io.jenkins.plugins.analysis.core.util.QualityGateStatus;
import io.jenkins.plugins.util.JenkinsFacade;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link SummaryModel}.
 *
 * @author Ullrich Hafner
 */
class SummaryModelTest {
    private static final ImmutableList<String> EMPTY_ERRORS = Lists.immutable.empty();
    private static final String TOOL_ID = "test";
    private static final String TOOL_NAME = "SummaryTest";
    private static final String CHECK_STYLE_ID = "checkstyle";
    private static final String CHECK_STYLE_NAME = "CheckStyle";
    private static final String PMD_ID = "pmd";
    private static final String ERROR_MESSAGE = "Error Message";
    private static final String PMD_NAME = "PMD";

    @Test
    void shouldCreateTitleMessageIfThereAreNoWarnings() {
        AnalysisResult analysisResult = createAnalysisResult(
                Maps.fixedSize.of(CHECK_STYLE_ID, 0), 0, 0,
                EMPTY_ERRORS, 0);

        SummaryModel summary = createSummary(analysisResult);
        assertThat(summary)
                .hasId(TOOL_ID)
                .hasName(TOOL_NAME)
                .hasTitle(Messages.Tool_NoIssues())
                .hasTotalSize(0)
                .hasNewSize(0)
                .hasFixedSize(0)
                .hasQualityGateStatus(QualityGateStatus.INACTIVE)
                .hasReferenceBuild(Optional.empty())
                .isNotZeroIssuesHighscore()
                .hasNoErrors()
                .isNotResetQualityGateVisible();

        assertThat(summary.getTools()).hasSize(1)
                .extracting(StaticAnalysisLabelProvider::getId, StaticAnalysisLabelProvider::getName)
                .containsExactly(tuple(CHECK_STYLE_ID, CHECK_STYLE_NAME));
    }

    @Test
    void shouldCreateTitleMessageIfThereIsOneWarning() {
        AnalysisResult analysisResult = createAnalysisResult(
                Maps.fixedSize.of(CHECK_STYLE_ID, 1), 0, 0,
                EMPTY_ERRORS, 0);

        SummaryModel summary = createSummary(analysisResult);
        assertThat(summary)
                .hasId(TOOL_ID)
                .hasName(TOOL_NAME)
                .hasTitle(Messages.Tool_OneIssue())
                .hasNoErrors();
    }

    @Test
    void shouldCreateTitleMessageIfThereAreMultipleWarnings() {
        AnalysisResult analysisResult = createAnalysisResult(
                Maps.fixedSize.of(CHECK_STYLE_ID, 2), 0, 0,
                EMPTY_ERRORS, 0);

        SummaryModel summary = createSummary(analysisResult);
        assertThat(summary)
                .hasId(TOOL_ID)
                .hasName(TOOL_NAME)
                .hasTitle(Messages.Tool_MultipleIssues(2))
                .hasNoErrors();
    }

    @Test
    void shouldCreateFixedAndNewWarnings() {
        AnalysisResult analysisResult = createAnalysisResult(
                Maps.fixedSize.of(CHECK_STYLE_ID, 2), 1, 3,
                EMPTY_ERRORS, 1);

        SummaryModel summary = createSummary(analysisResult);
        assertThat(summary)
                .hasId(TOOL_ID)
                .hasName(TOOL_NAME)
                .hasTitle(Messages.Tool_MultipleIssues(2))
                .hasNewSize(1)
                .hasTotalSize(2)
                .hasFixedSize(3)
                .isZeroIssuesHighscore()
                .hasZeroIssuesHighscoreMessage("No issues for 2 builds, i.e. since build:");
    }

    @Test
    void shouldCreateTitleMessageIfThereAreWarningsFromMultipleTools() {
        AnalysisResult analysisResult = createAnalysisResult(
                Maps.fixedSize.of(CHECK_STYLE_ID, 2, PMD_ID, 3), 0, 0,
                EMPTY_ERRORS, 0);

        SummaryModel summary = createSummary(analysisResult);
        assertThat(summary)
                .hasId(TOOL_ID)
                .hasName(TOOL_NAME)
                .hasTitle(Messages.Tools_MultipleIssues(2, 5))
                .hasNoErrors();

        assertThat(summary.getTools()).hasSize(2)
                .extracting(StaticAnalysisLabelProvider::getId, StaticAnalysisLabelProvider::getName)
                .containsExactly(
                        tuple(CHECK_STYLE_ID, CHECK_STYLE_NAME),
                        tuple(PMD_ID, PMD_NAME));

    }

    @Test
    void shouldCreateTitleMessageWithErrors() {
        AnalysisResult analysisResult = createAnalysisResult(
                Maps.fixedSize.of(CHECK_STYLE_ID, 2), 0, 0,
                Lists.immutable.of(ERROR_MESSAGE), 0);

        SummaryModel summary = createSummary(analysisResult);
        assertThat(summary)
                .hasId(TOOL_ID)
                .hasName(TOOL_NAME)
                .hasTitle(Messages.Tool_MultipleIssues(2))
                .hasOnlyErrors(ERROR_MESSAGE);
    }

    @Test
    void shouldUseReferenceBuildOfResult() {
        AnalysisResult analysisResult = createAnalysisResult(
                Maps.fixedSize.of(CHECK_STYLE_ID, 2), 0, 0,
                Lists.immutable.of(ERROR_MESSAGE), 0);

        Run<?, ?> run = mock(Run.class);
        when(run.getFullDisplayName()).thenReturn("Job #15");
        when(run.getUrl()).thenReturn("job/my-job/15");
        when(analysisResult.getReferenceBuild()).thenReturn(Optional.of(run));

        when(analysisResult.getBuild()).thenReturn(new Build(2));

        SummaryModel summary = createSummary(analysisResult);

        assertThat(summary).hasReferenceBuild(Optional.of(run));
    }

    @Test
    void shouldUseQualityStatusOfResult() {
        AnalysisResult analysisResult = createAnalysisResult(
                Maps.fixedSize.of(CHECK_STYLE_ID, 2), 0, 0,
                Lists.immutable.of(ERROR_MESSAGE), 0);

        QualityGateStatus qualityGateStatus = QualityGateStatus.FAILED;
        when(analysisResult.getQualityGateStatus()).thenReturn(qualityGateStatus);

        SummaryModel summary = createSummary(analysisResult);

        assertThat(summary).hasQualityGateStatus(qualityGateStatus);
    }

    private SummaryModel createSummary(final AnalysisResult analysisResult) {
        Locale.setDefault(Locale.ENGLISH);

        LabelProviderFactoryFacade facade = mock(LabelProviderFactoryFacade.class);
        StaticAnalysisLabelProvider checkStyleLabelProvider = createLabelProvider(CHECK_STYLE_ID, CHECK_STYLE_NAME);
        when(facade.get(CHECK_STYLE_ID)).thenReturn(checkStyleLabelProvider);
        StaticAnalysisLabelProvider pmdLabelProvider = createLabelProvider(PMD_ID, PMD_NAME);
        when(facade.get(PMD_ID)).thenReturn(pmdLabelProvider);

        SummaryModel summaryModel = new SummaryModel(createLabelProvider(TOOL_ID, TOOL_NAME), analysisResult, facade);
        summaryModel.setResetQualityGateCommand(createResetReferenceAction(false));
        return summaryModel;
    }

    private StaticAnalysisLabelProvider createLabelProvider(final String id, final String name) {
        JenkinsFacade jenkins = mock(JenkinsFacade.class);
        when(jenkins.getImagePath(any(BallColor.class))).thenReturn("color");
        when(jenkins.getImagePath(contains(StaticAnalysisLabelProvider.ERROR_ICON))).thenReturn("/path/to/error");
        when(jenkins.getImagePath(contains(StaticAnalysisLabelProvider.INFO_ICON))).thenReturn("/path/to/info");
        when(jenkins.getAbsoluteUrl(any())).thenReturn("absoluteUrl");
        return new StaticAnalysisLabelProvider(id, name, StaticAnalysisLabelProvider.EMPTY_DESCRIPTION, jenkins);
    }

    private AnalysisResult createAnalysisResult(final Map<String, Integer> sizesPerOrigin,
            final int newSize, final int fixedSize,
            final ImmutableList<String> errorMessages,
            final int numberOfIssuesSinceBuild) {
        AnalysisResult analysisRun = mock(AnalysisResult.class);
        when(analysisRun.getTotalSize()).thenReturn(sizesPerOrigin.values().stream().mapToInt(Integer::intValue).sum());
        when(analysisRun.getSizePerOrigin()).thenReturn(sizesPerOrigin);
        when(analysisRun.getNewSize()).thenReturn(newSize);
        when(analysisRun.getFixedSize()).thenReturn(fixedSize);
        when(analysisRun.getErrorMessages()).thenReturn(errorMessages);
        when(analysisRun.getNoIssuesSinceBuild()).thenReturn(numberOfIssuesSinceBuild);
        when(analysisRun.getQualityGateStatus()).thenReturn(QualityGateStatus.INACTIVE);
        when(analysisRun.getIssues()).thenReturn(createReport(sizesPerOrigin.keySet()));
        Run<?, ?> build = mock(Run.class);
        when(build.getNumber()).thenReturn(2);
        when(analysisRun.getOwner()).thenAnswer(i -> build);

        return analysisRun;
    }

    private Report createReport(final Set<String> ids) {
        IssueBuilder builder = new IssueBuilder();
        Report container = new Report();
        container.setOrigin("container", "Aggregation");
        for (String id : ids) {
            Report subReport = new Report(id, "Name of " + id, id + ".xml");
            Issue checkstyleWarning = builder.setFileName("A.java")
                    .setCategory("Style")
                    .setLineStart(1)
                    .buildAndClean();
            subReport.add(checkstyleWarning);
            subReport.add(builder.setFileName("A.java").setCategory("Style").setLineStart(1).buildAndClean());
            container.addAll(subReport);
        }
        return container;
    }

    private ResetQualityGateCommand createResetReferenceAction(final boolean isEnabled) {
        ResetQualityGateCommand resetQualityGateCommand = mock(ResetQualityGateCommand.class);
        when(resetQualityGateCommand.isEnabled(any(), any())).thenReturn(isEnabled);
        return resetQualityGateCommand;
    }
}

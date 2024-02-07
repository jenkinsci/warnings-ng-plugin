package io.jenkins.plugins.analysis.core.model;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.collections.api.list.ImmutableList;

import edu.hm.hafner.util.VisibleForTesting;

import hudson.model.Run;

import io.jenkins.plugins.util.QualityGateStatus;

/**
 * Summary message of a static analysis run. This message is shown as part of the 'summary.jelly' information of the
 * associated {@link ResultAction}.
 * <pre>
 *     Tool Name: %d issues from %d analyses
 *        - Results from analyses {%s, ... %s}
 *        - %d new issues (since build %d)
 *        - %d outstanding issues
 *        - %d fixed issues
 *        - No issues since build %d
 *        - Quality gates: passed (Reference build %d)
 * </pre>
 *
 * @author Ullrich Hafner
 */
public class SummaryModel {
    private final StaticAnalysisLabelProvider labelProvider;
    private final AnalysisResult analysisResult;
    private final LabelProviderFactoryFacade facade;

    private ResetQualityGateCommand resetQualityGateCommand = new ResetQualityGateCommand();

    /**
     * Creates a new {@link SummaryModel}.
     *
     * @param labelProvider
     *         the label provider to get the labels for the static analysis tool
     * @param result
     *         the result of the static analysis tool
     */
    public SummaryModel(final StaticAnalysisLabelProvider labelProvider, final AnalysisResult result) {
        this(labelProvider, result, new LabelProviderFactoryFacade());
    }

    @VisibleForTesting
    SummaryModel(final StaticAnalysisLabelProvider labelProvider, final AnalysisResult result,
            final LabelProviderFactoryFacade facade) {
        this.labelProvider = labelProvider;
        analysisResult = result;
        this.facade = facade;
    }

    @VisibleForTesting
    void setResetQualityGateCommand(final ResetQualityGateCommand resetQualityGateCommand) {
        this.resetQualityGateCommand = resetQualityGateCommand;
    }

    public String getId() {
        return labelProvider.getId();
    }

    public String getName() {
        return labelProvider.getName();
    }

    /**
     * Returns the title for the small information box in the corresponding build page.
     *
     * @return the title
     */
    public String getTitle() {
        int totalSize = analysisResult.getTotalSize();
        if (totalSize == 0) {
            return Messages.Tool_NoIssues();
        }
        if (totalSize == 1) {
            return Messages.Tool_OneIssue();
        }
        return Messages.Tool_MultipleIssues(totalSize);
    }

    /**
     * Returns the number of analysis files that have been parsed in this step.
     *
     * @return the number of analysis files
     */
    public int getAnalysesCount() {
        return analysisResult.getIssues().getOriginReportFiles().size();
    }

    public ImmutableList<String> getErrors() {
        return analysisResult.getErrorMessages();
    }

    public int getTotalSize() {
        return analysisResult.getTotalSize();
    }

    public int getNewSize() {
        return analysisResult.getNewSize();
    }

    public int getFixedSize() {
        return analysisResult.getFixedSize();
    }

    public int getModifiedSize() {
        return analysisResult.getTotals().getTotalModifiedSize();
    }

    public int getModifiedNewSize() {
        return analysisResult.getTotals().getNewModifiedSize();
    }

    public int getModifiedOutstandingSize() {
        return getModifiedSize() - getModifiedNewSize();
    }

    /**
     * Returns the tools that contribute to this result.
     *
     * @return the tools that have been used in this report
     */
    public List<StaticAnalysisLabelProvider> getTools() {
        return analysisResult.getSizePerOrigin().keySet().stream()
                .map(facade::get)
                .collect(Collectors.toList());
    }

    /**
     * Returns the number of issues for a specific tool given by its {@code origin} value.
     *
     * @param origin
     *         the ID of the tool
     *
     * @return the number of issues for this tool
     */
    public int totalSize(final String origin) {
        return analysisResult.getSizePerOrigin().get(origin);
    }

    public QualityGateStatus getQualityGateStatus() {
        return analysisResult.getQualityGateResult().getOverallStatus();
    }

    public boolean isResetQualityGateVisible() {
        return resetQualityGateCommand.isEnabled(analysisResult.getOwner(), analysisResult.getId());
    }

    public Optional<Run<?, ?>> getReferenceBuild() {
        return analysisResult.getReferenceBuild();
    }

    public boolean isZeroIssuesHighscore() {
        return getNoIssuesSinceBuild() > 0 && analysisResult.getOwner().getNumber() > getNoIssuesSinceBuild();
    }

    public int getNoIssuesSinceBuild() {
        return analysisResult.getNoIssuesSinceBuild();
    }

    public String getZeroIssuesHighscoreMessage() {
        return Messages.Summary_NoIssuesSinceBuild(
                analysisResult.getOwner().getNumber() - analysisResult.getNoIssuesSinceBuild() + 1);
    }

    /**
     * Provides a way to stub the label provider factory during tests.
     */
    static class LabelProviderFactoryFacade {
        public StaticAnalysisLabelProvider get(final String id) {
            return new LabelProviderFactory().create(id);
        }
    }
}

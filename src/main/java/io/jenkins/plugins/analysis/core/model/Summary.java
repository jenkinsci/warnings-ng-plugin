package io.jenkins.plugins.analysis.core.model;

import java.util.stream.Collectors;

import edu.hm.hafner.util.VisibleForTesting;

import j2html.tags.ContainerTag;

import hudson.model.Run;

import io.jenkins.plugins.analysis.core.util.QualityGateStatus;

import static j2html.TagCreator.*;

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
// TODO: number of parsed reports should be shown as well
public class Summary {
    private final StaticAnalysisLabelProvider labelProvider;
    private final AnalysisResult analysisResult;
    private final LabelProviderFactoryFacade facade;

    private ResetQualityGateCommand resetQualityGateCommand = new ResetQualityGateCommand();

    @VisibleForTesting
    void setResetQualityGateCommand(final ResetQualityGateCommand resetQualityGateCommand) {
        this.resetQualityGateCommand = resetQualityGateCommand;
    }

    /**
     * Creates a new {@link Summary}.
     *
     * @param labelProvider
     *         the label provider to get the labels for the static analysis tool
     * @param result
     *         the result of the static analysis tool
     */
    public Summary(final StaticAnalysisLabelProvider labelProvider, final AnalysisResult result) {
        this(labelProvider, result, new LabelProviderFactoryFacade());
    }

    @VisibleForTesting
    Summary(final StaticAnalysisLabelProvider labelProvider, final AnalysisResult result,
            final LabelProviderFactoryFacade facade) {
        this.labelProvider = labelProvider;
        this.analysisResult = result;
        this.facade = facade;
    }

    /**
     * Creates the summary as HTML string.
     *
     * @return the summary
     */
    public String create() {
        return div(labelProvider.getTitle(analysisResult, !analysisResult.getErrorMessages().isEmpty()),
                createDescription())
                .withId(labelProvider.getId() + "-summary")
                .renderFormatted();
    }

    private ContainerTag createDescription() {
        int currentBuild = analysisResult.getBuild().getNumber();
        ContainerTag ul = ul()
                .condWith(analysisResult.getSizePerOrigin().size() > 1,
                        li(getToolNames()))
                .condWith(analysisResult.getTotalSize() == 0
                                && currentBuild > analysisResult.getNoIssuesSinceBuild(),
                        li(labelProvider.getNoIssuesSinceLabel(currentBuild, analysisResult.getNoIssuesSinceBuild())))
                .condWith(analysisResult.getNewSize() > 0,
                        li(labelProvider.getNewIssuesLabel(analysisResult.getNewSize())))
                .condWith(analysisResult.getFixedSize() > 0,
                        li(labelProvider.getFixedIssuesLabel(analysisResult.getFixedSize())))
                .condWith(analysisResult.getQualityGateStatus() != QualityGateStatus.INACTIVE,
                        li(labelProvider.getQualityGateResult(analysisResult.getQualityGateStatus(),
                                hasResetReference(analysisResult.getOwner(), analysisResult.getId()))));
        return analysisResult.getReferenceBuild()
                .map(reference -> ul.with(li(labelProvider.getReferenceBuild(reference))))
                .orElse(ul);
    }

    private boolean hasResetReference(final Run<?, ?> owner, final String id) {
        return resetQualityGateCommand.isEnabled(owner, id);
    }

    private String getToolNames() {
        String tools = analysisResult.getSizePerOrigin()
                .keySet()
                .stream()
                .map(id -> facade.get(id).getName())
                .collect(Collectors.joining(", "));
        return Messages.Tool_ParticipatingTools(tools);
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

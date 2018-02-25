package io.jenkins.plugins.analysis.core.model;

import java.util.stream.Collectors;

import edu.hm.hafner.util.VisibleForTesting;
import io.jenkins.plugins.analysis.core.quality.StaticAnalysisRun;
import io.jenkins.plugins.analysis.core.views.ResultAction;
import static j2html.TagCreator.*;
import j2html.tags.ContainerTag;

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
// FIXME: is the number of parsed reports available yet?
// FIXME: %d issues should be part of label provider
public class Summary {
    private final StaticAnalysisLabelProvider labelProvider;
    private final StaticAnalysisRun analysisRun;
    private final LabelProviderFactoryFacade facade;

    public Summary(final StaticAnalysisLabelProvider labelProvider, final StaticAnalysisRun analysisRun) {
        this(labelProvider, analysisRun, new LabelProviderFactoryFacade());
    }

    @VisibleForTesting
    Summary(final StaticAnalysisLabelProvider labelProvider, final StaticAnalysisRun analysisRun,
            final LabelProviderFactoryFacade facade) {
        this.labelProvider = labelProvider;
        this.analysisRun = analysisRun;
        this.facade = facade;
    }

    public String create() {
        return div(labelProvider.getTitle(analysisRun), createDescription())
                .withId(labelProvider.getId() + "-summary")
                .renderFormatted();
    }

    private ContainerTag createDescription() {
        int currentBuild = analysisRun.getBuild().getNumber();
        return ul()
                .condWith(analysisRun.getSizePerOrigin().size() > 1,
                        li(getToolNames()))
                .condWith(analysisRun.getTotalSize() == 0
                                && currentBuild > analysisRun.getNoIssuesSinceBuild(),
                        li(labelProvider.getNoIssuesSinceLabel(currentBuild, analysisRun.getNoIssuesSinceBuild())))
                .condWith(analysisRun.getNewSize() > 0,
                        li(labelProvider.getNewIssuesLabel(analysisRun.getNewSize())))
                .condWith(analysisRun.getFixedSize() > 0,
                        li(labelProvider.getFixedIssuesLabel(analysisRun.getFixedSize())))
                .condWith(analysisRun.getQualityGate().isEnabled(),
                        li(labelProvider.getQualityGateResult(analysisRun.getOverallResult(),
                                analysisRun.getReferenceBuild())));
    }

    private String getToolNames() {
        String tools = analysisRun.getSizePerOrigin()
                .keySet()
                .stream()
                .map((id) -> facade.get(id).getName())
                .collect(Collectors.joining(", "));
        return Messages.Tool_ParticipatingTools(tools);
    }

    /*
    @Override
    public String toString() {
        HtmlPrinter printer = new HtmlPrinter();
        printer.append(createDeltaMessage());

        if (result.getTotalSize() == 0 && result.getNoIssuesSinceBuild() > 0) {
            printer.append(printer.item(Messages.ResultAction_NoWarningsSince(result.getNoIssuesSinceBuild())));
            printer.append(printer.item(createHighScoreMessage()));
        }
        else if (result.isSuccessfulTouched()) {
            printer.append(printer.item(createPluginResultMessage()));
            if (result.isSuccessful()) {
                printer.append(printer.item(createSuccessfulHighScoreMessage()));
            }
        }
        return printer.toString();
    }

    private String createPluginResultMessage() {
        return Messages.ResultAction_Status() + getResultIcon() + " - " + result.getReason() + getReferenceBuildUrl();
    }

    private String getReferenceBuildUrl() {
        HtmlPrinter printer = new HtmlPrinter();
        int referenceBuild = result.getReferenceBuild();
        if (referenceBuild > 0) {
            printer.append("&nbsp;");
            printer.append("(");
            printer.append(Messages.ReferenceBuild());
            printer.append(": ");
            printer.append(printer.link(String.format("../%d", referenceBuild), String.format("#%d", referenceBuild)));
            printer.append(")");
        }
        return printer.toString();
    }

    private String createHighScoreMessage() {
        if (result.isNewZeroWarningsHighScore()) {
            long days = getDays(result.getZeroWarningsHighScore());
            if (days == 1) {
                return Messages.ResultAction_OneHighScore();
            }
            else {
                return Messages.ResultAction_MultipleHighScore(days);
            }
        }
        else {
            long days = getDays(result.getHighScoreGap());
            if (days == 1) {
                return Messages.ResultAction_OneNoHighScore();
            }
            else {
                return Messages.ResultAction_MultipleNoHighScore(days);
            }
        }
    }

    private String createSuccessfulHighScoreMessage() {
        if (result.isNewSuccessfulHighScore()) {
            long days = getDays(result.getSuccessfulHighScore());
            if (days == 1) {
                return Messages.ResultAction_SuccessfulOneHighScore();
            }
            else {
                return Messages.ResultAction_SuccessfulMultipleHighScore(days);
            }
        }
        else {
            long days = getDays(result.getSuccessfulHighScoreGap());
            if (days == 1) {
                return Messages.ResultAction_SuccessfulOneNoHighScore();
            }
            else {
                return Messages.ResultAction_SuccessfulMultipleNoHighScore(days);
            }
        }
    }

    private String createDeltaMessage() {
        return tool.getDeltaMessage(result.getNewSize(), result.getFixedSize());
    }

    private long getDays(final long ms) {
        return Math.max(1, ms / DateUtils.MILLIS_PER_DAY);
    }

    */

    static class LabelProviderFactoryFacade {
        public StaticAnalysisLabelProvider get(final String id) {
            return new LabelProviderFactory().create(id);
        }
    }
}

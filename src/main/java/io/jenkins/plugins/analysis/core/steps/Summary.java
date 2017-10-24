package io.jenkins.plugins.analysis.core.steps;

import org.apache.commons.lang.time.DateUtils;
import org.kohsuke.stapler.Stapler;

import jenkins.model.Jenkins;

import hudson.model.Result;
import hudson.plugins.analysis.Messages;
import hudson.plugins.analysis.util.HtmlPrinter;

/**
 * Summary message of a static analysis run. This message is shown as part of the 'summary.jelly' information of the
 * associated {@link ResultAction}.
 *
 * @author Ullrich Hafner
 */
public class Summary {
    private static final String UNSTABLE = "yellow.png";
    private static final String FAILED = "red.png";
    private static final String SUCCESS = "blue.png";
    private final StaticAnalysisLabelProvider tool;
    private final StaticAnalysisRun2 result;

    public Summary(final String id, final String name, StaticAnalysisRun2 result) {
        tool = StaticAnalysisTool.find(id, name);
        this.result = result;
    }

    @Override
    public String toString() {
        HtmlPrinter printer = new HtmlPrinter();
        printer.append(createDeltaMessage());

        if (result.getTotalSize() == 0 && result.getZeroWarningsSinceBuild() > 0) {
            printer.append(printer.item(Messages.ResultAction_NoWarningsSince(result.getZeroWarningsSinceBuild())));
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

    private String getResultIcon() {
        String message = "<img src=\"" + Stapler.getCurrentRequest().getContextPath() + Jenkins.RESOURCE_PATH
                + "/images/16x16/%s\" alt=\"%s\" title=\"%s\"/>";
        if (result.getPluginResult() == Result.FAILURE) {
            return String.format(message, FAILED,
                    hudson.model.Messages.BallColor_Failed(), hudson.model.Messages.BallColor_Failed());
        }
        else if (result.getPluginResult() == Result.UNSTABLE) {
            return String.format(message, UNSTABLE,
                    hudson.model.Messages.BallColor_Unstable(), hudson.model.Messages.BallColor_Unstable());
        }
        else {
            return String.format(message, SUCCESS,
                    hudson.model.Messages.BallColor_Success(), hudson.model.Messages.BallColor_Success());
        }
    }
}

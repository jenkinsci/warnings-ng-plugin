package io.jenkins.plugins.analysis.core.steps;

import java.util.Collection;
import java.util.Optional;

import hudson.model.Result;
import hudson.plugins.analysis.Messages;
import hudson.plugins.analysis.core.BuildResultEvaluator;
import hudson.plugins.analysis.core.Thresholds;
import hudson.plugins.analysis.util.PluginLogger;
import hudson.plugins.analysis.util.model.FileAnnotation;

/**
 * FIXME: write comment.
 *
 * @author Ullrich Hafner
 */
public class ResultEvaluator extends BuildResultEvaluator {
    private final String id;
    private Thresholds thresholds;
    private final PluginLogger logger;

    /**
     * Creates a new instance of {@link ResultEvaluator}.
     *
     * @param id
     *         the ID of the result
     */
    public ResultEvaluator(final String id, final Thresholds thresholds, final PluginLogger logger) {
        super(IssueParser.find(id).getResultUrl());

        this.id = id;
        this.thresholds = thresholds;
        this.logger = logger;
    }

    // FIXME: i18n of reason
    public Evaluation evaluate(final Optional<AnalysisResult> previousResult, Collection<FileAnnotation> issues,
            Collection<FileAnnotation> newIssues) {
        StringBuilder messages = new StringBuilder();
        Result result;
        if (!previousResult.isPresent()) {
            logger.log("Ignore new warnings since this is the first valid build");
            result = evaluateBuildResult(messages, thresholds, issues);
        }
        else {
            result = evaluateBuildResult(messages, thresholds, issues, newIssues);
        }
        String reason = messages.toString();
        logger.log(String.format("%s %s - %s", Messages.ResultAction_Status(), result.color.getDescription(), reason));
        return new Evaluation(result, reason);
    }

    /**
     * Return object that stores the result and the reason.
     */
    static class Evaluation {
        final Result result;
        final String reason;

        Evaluation(final Result result, final String reason) {
            this.result = result;
            this.reason = reason;
        }
    }

}

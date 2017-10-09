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
 * Evaluates the build result using the defined thresholds.
 *
 * @author Ullrich Hafner
 */
public class ResultEvaluator extends BuildResultEvaluator {
    private final String id;
    private final Thresholds thresholds;
    private final PluginLogger logger;

    /**
     * Creates a new instance of {@link ResultEvaluator}.
     *
     * @param id
     *         the ID of the result
     */
    public ResultEvaluator(final String id, final Thresholds thresholds, final PluginLogger logger) {
        super(StaticAnalysisTool.find(id).getResultUrl());

        this.id = id;
        this.thresholds = thresholds;
        this.logger = logger;
    }

    /**
     * Returns whether at least one of the thresholds is set.
     *
     * @return {@code true}  if at least one of the thresholds is set, {@code false} if no threshold is set
     */
    public boolean isEnabled() {
        return thresholds.isValid();
    }

    // FIXME: i18n of reason
    public Evaluation evaluate(final Optional<AnalysisResult> previousResult, Collection<FileAnnotation> issues,
            Collection<FileAnnotation> newIssues) {
        StringBuilder messages = new StringBuilder();
        Result result;
        if (!previousResult.isPresent()) {
            logger.log("Ignoring new issues since this is the first valid build");
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

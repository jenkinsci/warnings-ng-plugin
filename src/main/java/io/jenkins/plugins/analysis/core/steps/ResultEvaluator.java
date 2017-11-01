package io.jenkins.plugins.analysis.core.steps;

import java.util.Optional;

import edu.hm.hafner.analysis.Issues;
import io.jenkins.plugins.analysis.core.util.Logger;

import hudson.model.Result;
import hudson.plugins.analysis.Messages;
import hudson.plugins.analysis.core.Thresholds;

/**
 * Evaluates the build result using the defined thresholds.
 *
 * @author Ullrich Hafner
 */
public class ResultEvaluator extends BuildResultEvaluator {
    private final Thresholds thresholds;
    private final Logger logger;

    /**
     * Creates a new instance of {@link ResultEvaluator}.
     *
     * @param id
     *         the ID of the static analysis tool
     * @param name
     *         the name of the static analysis tool
     */
    public ResultEvaluator(final String id, final String name, final Thresholds thresholds, final Logger logger) {
        super(StaticAnalysisTool.find(id, name).getResultUrl());

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
    public Evaluation evaluate(final Optional<AnalysisResult> previousResult, Issues allIssues, Issues newIssues) {
        StringBuilder messages = new StringBuilder();
        Result result;
        if (!previousResult.isPresent()) {
            logger.log("Ignoring new issues since this is the first valid build");
            result = evaluateBuildResult(messages, thresholds, allIssues);
        }
        else {
            result = evaluateBuildResult(messages, thresholds, allIssues, newIssues);
        }
        String reason = messages.toString();
        logger.log("%s %s - %s", Messages.ResultAction_Status(), result.color.getDescription(), reason);
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

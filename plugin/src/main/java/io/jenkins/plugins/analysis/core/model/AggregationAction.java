package io.jenkins.plugins.analysis.core.model;

import edu.hm.hafner.analysis.Report;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.model.Action;
import hudson.model.Api;
import hudson.model.Run;
import io.jenkins.plugins.analysis.core.restapi.AggregatedReportApi;
import io.jenkins.plugins.analysis.core.restapi.AggregationApi;
import io.jenkins.plugins.analysis.core.restapi.ReportApi;
import io.jenkins.plugins.analysis.core.restapi.ToolApi;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import jenkins.model.RunAction2;
import jenkins.tasks.SimpleBuildStep.LastBuildAction;

/**
 * Aggregates the results of all analysis results. Provides an entry point for the remote API. Currently, the aggregated
 * results are only visualized in the associated trend chart but not in a detail view.
 *
 * <p>
 * The remote API provides a summary of all tools ({@code warnings-ng/api/json}) and the issues of all tools
 * ({@code warnings-ng/all/api/json}), optionally only the new, outstanding, or fixed issues
 * ({@code warnings-ng/new/api/json}, {@code warnings-ng/outstanding/api/json}, or
 * {@code warnings-ng/fixed/api/json}). These are the same URLs as for the results of a single tool.
 * </p>
 *
 * @author Ullrich Hafner
 * @see AggregatedTrendAction
 */
@SuppressFBWarnings(value = "UWF", justification = "transient field owner ist restored using a Jenkins callback")
public class AggregationAction implements RunAction2, LastBuildAction {
    /** The URLs of the issues of all tools, mapped to the selection of the issues of an analysis result. */
    private static final Map<String, Function<AnalysisResult, Report>> ISSUES = Map.of(
            "all", AnalysisResult::getIssues,
            "new", AnalysisResult::getNewIssues,
            "outstanding", AnalysisResult::getOutstandingIssues,
            "fixed", AnalysisResult::getFixedIssues);

    private transient Run<?, ?> owner;

    @CheckForNull
    @Override
    public String getIconFileName() {
        return null; // No UI representation up to now
    }

    @CheckForNull
    @Override
    public String getDisplayName() {
        return Messages.Aggregation_Name();
    }

    @CheckForNull
    @Override
    public String getUrlName() {
        return "warnings-ng";
    }

    /**
     * Gets the remote API for this action. Depending on the path, a different result is selected.
     *
     * @return the remote API
     */
    public Api getApi() {
        return new Api(new AggregationApi(findActions()));
    }

    public List<ToolApi> getTools() {
        return findActions();
    }

    /**
     * Returns the issues of all tools for the remote API. The URL selects the issues: {@code all}, {@code new},
     * {@code outstanding}, or {@code fixed}.
     *
     * @param name
     *         the URL of the issues
     *
     * @return the issues of all tools
     * @throws NoSuchElementException
     *         if there are no such issues
     */
    public AggregatedIssues getDynamic(final String name) {
        return Optional.ofNullable(ISSUES.get(name))
                .map(this::createIssues)
                .orElseThrow(() -> new NoSuchElementException("No such issues: " + name));
    }

    private AggregatedIssues createIssues(final Function<AnalysisResult, Report> selector) {
        return new AggregatedIssues(owner.getActions(ResultAction.class).stream()
                .map(ResultAction::getResult)
                .map(result -> new ReportApi(selector.apply(result), result.getBlames()))
                .toList());
    }

    private List<ToolApi> findActions() {
        return owner.getActions(ResultAction.class).stream()
                .map(this::createToolApi)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<? extends Action> getProjectActions() {
        return Set.of(new AggregatedTrendAction(owner.getParent()));
    }

    private ToolApi createToolApi(final ResultAction result) {
        return new ToolApi(
                result.getId(),
                result.getDisplayName(),
                result.getAbsoluteUrl(),
                result.getResult().getTotalSize(),
                result.getResult().getSizePerSeverity());
    }

    @Override
    public void onAttached(final Run<?, ?> r) {
        owner = r;
    }

    @Override
    public void onLoad(final Run<?, ?> r) {
        owner = r;
    }

    /**
     * The issues of all tools: the entry point for the remote API of the issues (e.g., {@code new/api/json}).
     */
    public static final class AggregatedIssues {
        private final List<ReportApi> reports;

        private AggregatedIssues(final List<ReportApi> reports) {
            this.reports = reports;
        }

        /**
         * Gets the remote API for the issues of all tools.
         *
         * @return the remote API
         */
        public Api getApi() {
            return new Api(new AggregatedReportApi(reports));
        }
    }
}

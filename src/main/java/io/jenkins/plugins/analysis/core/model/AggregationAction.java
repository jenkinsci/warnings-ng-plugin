package io.jenkins.plugins.analysis.core.model;

import java.util.List;
import java.util.stream.Collectors;

import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import hudson.model.Api;
import hudson.model.Run;
import jenkins.model.RunAction2;

import io.jenkins.plugins.analysis.core.restapi.AggregationApi;
import io.jenkins.plugins.analysis.core.restapi.ToolApi;

/**
 * Aggregates the results of all analysis results.
 *
 * @author Ullrich Hafner
 */
@SuppressFBWarnings(value = "UWF", justification = "transient field owner ist restored using a Jenkins callback")
public class AggregationAction implements RunAction2 {
    private transient Run<?, ?> owner;

    @Nullable
    @Override
    public String getIconFileName() {
        return null; // No UI representation up to now
    }

    @Nullable
    @Override
    public String getDisplayName() {
        return Messages.Aggregation_Name();
    }

    @Nullable
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

    private List<ToolApi> findActions() {
        return owner.getActions(ResultAction.class).stream().map(this::createToolApi).collect(Collectors.toList());
    }

    @SuppressWarnings("deprecation") // this is the only way for remote API calls to obtain the absolute path
    private ToolApi createToolApi(final ResultAction result) {
        return new ToolApi(result.getId(), result.getDisplayName(),
                result.getOwner().getAbsoluteUrl() + result.getUrlName(), result.getResult().getTotalSize());
    }

    @Override
    public void onAttached(final Run<?, ?> r) {
        owner = r;
    }

    @Override
    public void onLoad(final Run<?, ?> r) {
        owner = r;
    }
}

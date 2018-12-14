package io.jenkins.plugins.analysis.core.steps;

import javax.annotation.CheckForNull;

import java.util.Collection;

import io.jenkins.plugins.analysis.core.model.Messages;
import io.jenkins.plugins.analysis.core.restapi.AggregationApi;
import io.jenkins.plugins.analysis.core.views.ResultAction;
import jenkins.model.RunAction2;

import hudson.model.Api;
import hudson.model.Run;

/**
 * Aggregates the results of all analysis results.
 *
 * @author Ullrich Hafner
 */
class AggregationAction implements RunAction2 {
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

    private Collection<ResultAction> findActions() {
        return owner.getActions(ResultAction.class);
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

package io.jenkins.plugins.analysis.core.portlets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;

import edu.hm.hafner.analysis.Severity;

import org.kohsuke.stapler.bind.JavaScriptMethod;
import hudson.Extension;
import hudson.model.Run;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.ResultAction;
import io.jenkins.plugins.analysis.core.util.QualityGateStatus;
import io.jenkins.plugins.monitoring.MonitorPortlet;
import io.jenkins.plugins.monitoring.MonitorPortletFactory;

/**
 * A portlet that can be used for the
 * <a href="https://github.com/jenkinsci/pull-request-monitoring-plugin">pull-request-monitoring</a> dashboard.
 * It renders an interactive sunburst diagram for each {@link ResultAction},
 * which is registered at the current users {@link Run}.
 *
 * @author Simon Symhoven
 */
public class PullRequestMonitoringPortlet extends MonitorPortlet {

    private final ResultAction action;

    /**
     * Creates a new {@link PullRequestMonitoringPortlet}.
     *
     * @param action
     *              the corresponding {@link ResultAction}.
     */
    public PullRequestMonitoringPortlet(final ResultAction action) {
        this.action = action;
    }

    @Override
    public String getTitle() {
        return action.getLabelProvider().getName();
    }

    @Override
    @JavaScriptMethod
    public String getId() {
        return "warnings-ng-" + action.getResult().getId();
    }

    @Override
    public boolean isDefault() {
        return true;
    }

    @Override
    public int getPreferredWidth() {
        return 350;
    }

    @Override
    public int getPreferredHeight() {
        return 350;
    }

    @Override
    public Optional<String> getIconUrl() {
        return Optional.ofNullable(action.getIconFileName());
    }

    @Override
    public Optional<String> getDetailViewUrl() {
        return Optional.ofNullable(action.getUrlName());
    }

    /**
     * Get the json data for the sunburst diagram. (used by jelly view)
     *
     * @return
     *          the data as json string.
     */
    public String getResultIssuesAsJsonModel() {
        JsonObject sunburstData = new JsonObject();
        sunburstData.addProperty("fixed", action.getResult().getFixedIssues().getSize());
        sunburstData.addProperty("outstanding", action.getResult().getOutstandingIssues().getSize());

        JsonObject newIssues = new JsonObject();
        newIssues.addProperty("total", action.getResult().getNewIssues().getSize());
        newIssues.addProperty("low", action.getResult().getNewIssues().getSizeOf(Severity.WARNING_LOW));
        newIssues.addProperty("normal", action.getResult().getNewIssues().getSizeOf(Severity.WARNING_NORMAL));
        newIssues.addProperty("high", action.getResult().getNewIssues().getSizeOf(Severity.WARNING_HIGH));
        newIssues.addProperty("error", action.getResult().getNewIssues().getSizeOf(Severity.ERROR));

        sunburstData.add("new", newIssues);

        return sunburstData.toString();
    }

    /**
     * Check if {@link AnalysisResult} issues are empty.
     *
     * @return
     *          true if {@link AnalysisResult} issues are empty, else false.
     */
    public boolean isEmpty() {
        return action.getResult().isEmpty();
    }

    /**
     * Check if action has a quality gate.
     *
     * @return
     *          true if action has a quality gate, else false.
     */
    public boolean hasQualityGate() {
        return !action.getResult().getQualityGateStatus().equals(QualityGateStatus.INACTIVE);
    }

    /**
     * Get the icon of the quality gate.
     *
     * @return
     *          the image url of the icon.
     */
    public String getQualityGateResultIconUrl() {
        return action.getResult().getQualityGateStatus().getResult().color.getImageOf("16x16");
    }

    /**
     * Get the human readable description of quality gate.
     *
     * @return
     *          the description.
     */
    public String getQualityGateResultDescription() {
        return action.getResult().getQualityGateStatus().getResult().color.getDescription();
    }

    /**
     * The factory for the {@link PullRequestMonitoringPortlet}.
     */
    @Extension(optional = true)
    public static class PortletFactory extends MonitorPortletFactory {

        @Override
        public Collection<MonitorPortlet> getPortlets(final Run<?, ?> build) {
            List<ResultAction> actions = build.getActions(ResultAction.class);

            return actions.stream()
                    .map(PullRequestMonitoringPortlet::new)
                    .collect(Collectors.toCollection(ArrayList::new));
        }

        @Override
        public String getDisplayName() {
            return Messages.PullRequestMonitoringPortlet_Name();
        }
    }
}

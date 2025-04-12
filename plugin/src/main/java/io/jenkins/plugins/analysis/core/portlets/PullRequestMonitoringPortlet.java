package io.jenkins.plugins.analysis.core.portlets;

import com.google.gson.JsonObject;

import edu.hm.hafner.analysis.Severity;
import edu.hm.hafner.echarts.JacksonFacade;
import edu.hm.hafner.echarts.Palette;
import edu.hm.hafner.echarts.PieChartModel;
import edu.hm.hafner.echarts.PieData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.kohsuke.stapler.bind.JavaScriptMethod;
import hudson.Extension;
import hudson.model.Run;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.ResultAction;
import io.jenkins.plugins.monitoring.MonitorPortlet;
import io.jenkins.plugins.monitoring.MonitorPortletFactory;
import io.jenkins.plugins.util.QualityGateStatus;

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
    private final AnalysisResult result;

    /**
     * Creates a new {@link PullRequestMonitoringPortlet}.
     *
     * @param action
     *              the corresponding {@link ResultAction}.
     */
    public PullRequestMonitoringPortlet(final ResultAction action) {
        super();

        this.action = action;
        result = action.getResult();
    }

    @Override
    public String getTitle() {
        return action.getLabelProvider().getName();
    }

    @Override
    @JavaScriptMethod
    public String getId() {
        return "warnings-ng-" + result.getId();
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
     * Get the json data for the hierarchical sunburst diagram (used by jelly view).
     *
     * @return
     *          the data as json string.
     */
    public String getWarningsModel() {
        var sunburstData = new JsonObject();
        sunburstData.addProperty("fixed", result.getFixedIssues().getSize());
        sunburstData.addProperty("outstanding", result.getOutstandingIssues().getSize());

        var newIssues = new JsonObject();
        newIssues.addProperty("total", result.getNewIssues().getSize());
        newIssues.addProperty("low", result.getNewIssues().getSizeOf(Severity.WARNING_LOW));
        newIssues.addProperty("normal", result.getNewIssues().getSizeOf(Severity.WARNING_NORMAL));
        newIssues.addProperty("high", result.getNewIssues().getSizeOf(Severity.WARNING_HIGH));
        newIssues.addProperty("error", result.getNewIssues().getSizeOf(Severity.ERROR));

        sunburstData.add("new", newIssues);

        return sunburstData.toString();
    }

    /**
     * Get the json data for the simplified sunburst diagram (used by jelly view).
     *
     * @return
     *          the data as json string.
     */
    @SuppressWarnings("unused") // used by jelly view
    public String getNoNewWarningsModel() {
        var model = new PieChartModel();
        model.add(new PieData("outstanding", result.getOutstandingIssues().getSize()), Palette.YELLOW);
        model.add(new PieData("fixed", result.getFixedIssues().getSize()), Palette.GREEN);
        return new JacksonFacade().toJson(model);
    }

    /**
     * Check if {@link AnalysisResult} issues are empty.
     *
     * @return
     *          true if {@link AnalysisResult} issues are empty, else false.
     */
    public boolean isEmpty() {
        return result.isEmpty();
    }

    /**
     * Check if {@link AnalysisResult} issues have no new warnings.
     *
     * @return
     *          true if {@link AnalysisResult} issues have now new warnings.
     */
    @SuppressWarnings("unused") // used by jelly view
    public boolean hasNoNewWarnings() {
        return result.hasNoNewWarnings();
    }

    /**
     * Check if action has a quality gate.
     *
     * @return
     *          true if action has a quality gate, else false.
     */
    @SuppressWarnings("unused") // used by jelly view
    public boolean hasQualityGate() {
        return !result.getQualityGateStatus().equals(QualityGateStatus.INACTIVE);
    }

    /**
     * Get the icon class of the quality gate.
     *
     * @return
     *          the image class of the Jenkins status icon.
     */
    @SuppressWarnings("unused") // used by jelly view
    public String getQualityGateResultClass() {
        return result.getQualityGateStatus().getIconClass();
    }

    /**
     * Get the human-readable description of quality gate.
     *
     * @return
     *          the description.
     */
    @SuppressWarnings("unused") // used by jelly view
    public String getQualityGateResultDescription() {
        return result.getQualityGateStatus().getResult().color.getDescription();
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

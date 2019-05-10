package io.jenkins.plugins.analysis.warnings.recorder.pageobj;

import com.gargoylesoftware.htmlunit.ScriptResult;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

/**
 * Page Object for the charts in details views.
 */
public class DetailsViewCharts {

    private final HtmlPage detailsViewWebPage;

    /**
     * Creates the Chart PageObject for the details view web page. E.g. {buildNr}/java
     *
     * @param detailsViewWebPage
     *         The details view webpage to get the charts from.
     */
    public DetailsViewCharts(final HtmlPage detailsViewWebPage) {
        this.detailsViewWebPage = detailsViewWebPage;
    }

    /**
     * Returns the model of a chart in the specified HTML page.
     *
     * @param id
     *         the element ID of the chart placeholder (that has the EChart instance attached in property @{@code
     *         echart}
     *
     * @return the model (as JSON representation)
     */
    public JSONObject getChartModel(final String id) {
        // Workaround for Javascript dependency, problem is explained here:
        // https://stackoverflow.com/questions/29637962/json-stringify-turned-the-value-array-into-a-string
        detailsViewWebPage.executeJavaScript("delete(Array.prototype.toJSON)");
        ScriptResult scriptResult = detailsViewWebPage.executeJavaScript(
                String.format("JSON.stringify(echarts.getInstanceByDom(document.getElementById(\"%s\")).getOption())",
                        id));

        return JSONObject.fromObject(scriptResult.getJavaScriptResult().toString());
    }

    /**
     * Get the Severities Distribution Chart.
     *
     * @return the severities chart
     */
    public JSONObject getSeveritiesChart() {
        return getChartModel("single-severities-chart");
    }

    /**
     * Get the Reference Comparison Chart.
     *
     * @return the reference comparison chart
     */
    public JSONObject getReferenceChart() {
        return getChartModel("single-trend-chart");
    }

    /**
     * Get the History Chart.
     *
     * @return the history chart
     */
    public JSONObject getHistoryChart() {
        return getChartModel("severities-trend-chart");
    }
}

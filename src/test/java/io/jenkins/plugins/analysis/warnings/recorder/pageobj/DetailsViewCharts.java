package io.jenkins.plugins.analysis.warnings.recorder.pageobj;

import com.gargoylesoftware.htmlunit.ScriptResult;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Page Object for the charts in details views.
 */
public class DetailsViewCharts {

    private final HtmlPage detailsViewWebPage;

    /**
     * Creates the Chart PageObject for the details view web page. E.g. {buildNr}/java
     *
     * @param detailsViewWebPage The details view webpage to get the charts from.
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
    private String getChartModel(final String id) {
        ScriptResult scriptResult = detailsViewWebPage.executeJavaScript(
                String.format("JSON.stringify(echarts.getInstanceByDom(document.getElementById(\"%s\")).getOption())", id));

        return scriptResult.getJavaScriptResult().toString();
    }

    /**
     * Get the Severities Distribution Chart
     *
     * @return the severities chart
     */
    public String getSeveritiesChart() {
        return getChartModel("single-severities-chart");
    }

    /**
     * Get the Reference Comparison Chart
     *
     * @return the reference comparison chart
     */
    public String getReferenceChart() {
        return getChartModel("single-trend-chart");
    }

    /**
     * Get the History Chart
     *
     * @return the history chart
     */
    public String getHistoryChart() {
        return getChartModel("severities-trend-chart");
    }
}

const issuesChartPortletId = document.querySelector(".issues-chart-portlet-data-holder").getAttribute("data-id");
echartsJenkinsApi.renderTrendChart(`${issuesChartPortletId}-issues-chart`, `false`, trendProxy);

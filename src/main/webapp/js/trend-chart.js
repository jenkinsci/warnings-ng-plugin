(function ($) {
    var numberIssues = $('#number-issues');

    var trend = document.getElementById("trend-chart");
    var context = trend.getContext("2d");
    context.height = 200;
    context.width = 200;
    var trendSummaryChart = new Chart(context, {
        type: 'doughnut',
        data: {
            // FIXME: i18n
            labels: ["New", "Fixed", "Outstanding"],
            urls: ["new", "fixed", "outstanding"],
            datasets: [{
                label: 'New issues, Fixed issues, Existing issues',
                data: [numberIssues.data('new-issues'), numberIssues.data('fixed-issues'), numberIssues.data('outstanding-issues')],
                backgroundColor: [
                    '#f5c6cb',
                    '#b8daff',
                    '#ffeeba'
                ],
                hoverBackgroundColor: [
                    '#f5929f',
                    '#53bdff',
                    '#ffeb75'
                ],
                hoverBorderColor: [
                    '#fff', '#fff', '#fff'
                ]
            }]
        }
    });

    /**
     * @summary adding a on click event to the chart
     * @link http://www.chartjs.org/docs/latest/developers/api.html
     * @event click
     */
    trend.onclick = function (evt) {
        var activePoints = trendSummaryChart.getElementsAtEvent(evt);
        if (activePoints[0]) {
            var chartData = activePoints[0]['_chart'].config.data;
            var idx = activePoints[0]['_index'];

            var url = chartData.urls[idx];
            window.open(url, '_self');
        }
    };
})(jQuery);

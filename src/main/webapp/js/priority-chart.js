(function ($) {
    var numberPriorities = $('#number-priorities');

    var summary = document.getElementById("priorities-chart");
    var context = summary.getContext("2d");
    context.height = 200;
    context.width = 200;
    var prioritiesSummaryChart = new Chart(context, {
        type: 'doughnut',
        data: {
            // FIXME: i18n
            labels: ["High", "Normal", "Low"],
            datasets: [{
                label: 'High priority, Normal priority, Low priority',
                data: [numberPriorities.data('high'), numberPriorities.data('normal'), numberPriorities.data('low')],
                backgroundColor: [
                    '#f5c6cb',
                    '#ffeeba',
                    '#b8daff'
                ],
                hoverBackgroundColor: [
                    '#f5929f',
                    '#ffeb75',
                    '#53bdff'
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
    summary.onclick = function (evt) {
        var activePoints = prioritiesSummaryChart.getElementsAtEvent(evt);
        if (activePoints[0]) {
            var chartData = activePoints[0]['_chart'].config.data;
            var idx = activePoints[0]['_index'];

            var url = chartData.labels[idx];
            window.open(url, '_self');
        }
    };

})(jQuery);

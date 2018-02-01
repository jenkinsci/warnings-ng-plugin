(function ($) {
    var newWarning = $('#numberOfNewWarnings').text();
    var fixedWarning = $('#numberOfFixedWarnings').text();
    var existingWarning = $('#numberOfExistingWarnings').text();

    var trend = document.getElementById("doughnut-trend-summary");
    var context = trend.getContext("2d");
    context.height = 200;
    context.width = 200;
    var trendSummaryChart = new Chart(context, {
        type: 'doughnut',
        data: {
            labels: ["New", "Fixed", "Existing"],
            urls: ["new", "fixed", "old"],
            datasets: [{
                label: 'New issues, Fixed issues, Existing issues',
                data: [newWarning, fixedWarning, existingWarning],
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

    trend.onclick = function (evt) {
        var activePoints = trendSummaryChart.getElementsAtEvent(evt);
        if (activePoints[0]) {
            var chartData = activePoints[0]['_chart'].config.data;
            var idx = activePoints[0]['_index'];

            var url = chartData.urls[idx];
            window.open(url, "_self");
        }
    };
})(jQuery);

// .getElementsAtEvent(e)
//
// Looks for the element under the event point, then returns all elements at
// the same data index. This is used internally for 'label' mode highlighting.
//
// Calling getElementsAtEvent(event) on your Chart instance passing an
// argument of an event, or jQuery event, will return the point elements
// that are at that the same position of that event.

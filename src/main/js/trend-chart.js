var jQuery = require('jquery');
var Chart = require('chart.js');

/**
 * Creates a doughnut chart that shows the issues trend, i.e. new, fixed or outstanding issues.
 * Requires that a DOM <canvas> element exists with the ID '#trend-chart' and a div
 * with ID '#number-issues' that holds the three values to render.
 */
(function ($) {
    var numberIssues = $('#number-issues');

    var trend = $('#trend-chart');
    var trendSummaryChart = new Chart(trend, {
        type: 'doughnut',
        data: {
            // FIXME: i18n
            labels: ['New', 'Fixed', 'Outstanding'],
            urls: ['new', 'fixed', 'outstanding'],
            datasets: [{
                label: 'New issues, Fixed issues, Existing issues',
                data: [
                    numberIssues.data('new-issues'),
                    numberIssues.data('fixed-issues'),
                    numberIssues.data('outstanding-issues')],
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

    var selectUrl = require('./select-url');
    selectUrl.openSelectedUrl(trend, trendSummaryChart);
})(jQuery);

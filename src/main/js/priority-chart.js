var jQuery = require('jquery');
var Chart = require('chart.js');

/**
 * Creates a doughnut chart that shows the number of issues per priority.
 * Requires that a DOM <canvas> element exists with the ID '#priorities-chart'
 * and a div with ID '#number-priorities' that holds the three values to render.
 */
(function ($) {
    var priorityValues = $('#number-priorities');
    var priorities = $('#priorities-chart');
    var prioritiesSummaryChart = new Chart(priorities, {
        type: 'doughnut',
        data: {
            // FIXME: i18n
            labels: ['High', 'Normal', 'Low'],
            urls: ['high', 'normal', 'low'],
            datasets: [{
                label: [
                    'High priority',
                    'Normal priority',
                    'Low priority'],
                data: [
                    priorityValues.data('high'),
                    priorityValues.data('normal'),
                    priorityValues.data('low')],
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

    var selectUrl = require('./select-url');
    selectUrl.openSelectedUrl(priorities, prioritiesSummaryChart);
})(jQuery);

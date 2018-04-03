(function ($) {
    /**
     * Solves Bootstrap and Prototype.js conflict.
     *
     * @link http://jsfiddle.net/dgervalle/hhBc6/
     * @link http://www.softec.lu/site/DevelopersCorner/BootstrapPrototypeConflict
     */
    jQuery.noConflict();
    if (Prototype.BrowserFeatures.ElementExtensions) {
        var disablePrototypeJS = function (method, pluginsToDisable) {
                var handler = function (event) {
                    event.target[method] = undefined;
                    setTimeout(function () {
                        delete event.target[method];
                    }, 0);
                };
                pluginsToDisable.each(function (plugin) {
                    jQuery(window).on(method + '.bs.' + plugin, handler);
                });
            },
            pluginsToDisable = ['collapse', 'dropdown', 'modal', 'tooltip', 'popover', 'tab'];
        disablePrototypeJS('show', pluginsToDisable);
        disablePrototypeJS('hide', pluginsToDisable);
    }

    /**
     * Creates a doughnut chart that shows the number of issues per priority.
     * Requires that a DOM <canvas> element exists with the ID '#priorities-chart'
     * and a div with ID '#number-priorities' that holds the three values to render.
     */
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
    openSelectedUrl(priorities, prioritiesSummaryChart);

    /**
     * Creates a doughnut chart that shows the issues trend, i.e. new, fixed or outstanding issues.
     * Requires that a DOM <canvas> element exists with the ID '#trend-chart' and a div
     * with ID '#number-issues' that holds the three values to render.
     */
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
    openSelectedUrl(trend, trendSummaryChart);

    /**
     * Activate the tab that has been visited the last time. If there is no such tab, highlight the first one.
     */
    var detailsTabs = $('#tab-details');
    detailsTabs.find('li:first-child a').tab('show');
    $('a[data-toggle="tab"]').on('show.bs.tab', function (e) {
        localStorage.setItem('activeTab', $(e.target).attr('href'));
    });
    var activeTab = localStorage.getItem('activeTab');
    if (activeTab) {
        detailsTabs.find('a[href="' + activeTab + '"]').tab('show');
    }

    /**
     * Create a data table instance for all tables that are marked with class "display".
     */
    $('table.display').DataTable({
        pagingType: 'numbers',  // Page number button only
        columnDefs: [{
            targets: 'no-sort', // Columns with class 'no-sort' are not orderable
            orderable: false
        }]
    });

    /**
     * Create a data table instance for all tables that are marked with class "display".
     */
    var issues = $('#issues');
    var table = issues.DataTable({
        pagingType: 'numbers',  // Page number button only
        order: [[ 1, 'asc' ]],
        columnDefs: [{
            targets: 0,         // First column contains details button
            orderable: false
        }]
    });

    // Add event listener for opening and closing details
    issues.on('click', 'div.details-control', function () {
        var tr = $(this).parents('tr');
        var row = table.row(tr);

        if (row.child.isShown()) {
            // This row is already open - close it
            row.child.hide();
            tr.removeClass('shown');
        } else {
            // Open this row
            row.child($(this).data('description')).show();
            tr.addClass('shown');
        }
    });

    // FIXME: Ajax call should be done if the tab is selected
    view.getTableModel(function (t) {
        (function ($) {
            var table = $('#issues').DataTable();
            table.rows.add(t.responseObject().data).draw()
        })(jQuery);
    });

    /**
     * Opens the selected URL. For each value in a chart a different URL is registered.
     *
     * @param {jQuery} element - the <canvas> element that will be clicked
     * @param {Chart} chart - the Chart.js Chart instance that will be clicked
     */
    function openSelectedUrl(element, chart) {
        element[0].onclick = function (evt) {
            var activePoints = chart.getElementsAtEvent(evt);
            if (activePoints[0]) {
                var chartData = activePoints[0]['_chart'].config.data;
                var idx = activePoints[0]['_index'];

                var url = chartData.urls[idx];
                window.open(url, '_self');
            }
        };
    }
})(jQuery);




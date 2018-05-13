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
            labels: [
                priorityValues.data('high-label'),
                priorityValues.data('normal-label'),
                priorityValues.data('low-label')],
            urls: ['high', 'normal', 'low'],
            datasets: [{
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
            labels: [
                numberIssues.data('new-label'),
                numberIssues.data('fixed-label'),
                numberIssues.data('outstanding-label')],

            urls: ['new', 'fixed', 'outstanding'],
            datasets: [{
                data: [
                    numberIssues.data('new'),
                    numberIssues.data('fixed'),
                    numberIssues.data('outstanding')],
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

    /**
     * Issues are loaded on demand: if the active tab shows the issues table, then the content is loaded using an Ajax call.
     */
    var tabToggleLink = $('a[data-toggle="tab"]');
    tabToggleLink.on('show.bs.tab', function (e) {
        var activeTab = $(e.target).attr('href');
        if (activeTab === '#issuesContent' && table.data().length === 0 ) {
            view.getTableModel(function (t) {
                (function ($) {
                    var table = $('#issues').DataTable();
                    table.rows.add(t.responseObject().data).draw()
                })(jQuery);
            });
        }
    });

    /**
     * Activate the tab that has been visited the last time. If there is no such tab, highlight the first one.
     */
    var detailsTabs = $('#tab-details');
    detailsTabs.find('li:first-child a').tab('show');

    /**
     * Store the selected tab in Browser's local storage.
     */
    tabToggleLink.on('show.bs.tab', function (e) {
        var activeTab = $(e.target).attr('href');
        localStorage.setItem('activeTab', activeTab);
    });

    var activeTab = localStorage.getItem('activeTab');
    if (activeTab) {
        detailsTabs.find('a[href="' + activeTab + '"]').tab('show');
    }

    /**
     * Stores the order of every table in the local storage of the browser.
     */
    var tables = $('#statistics').find('table').not('#details');
    tables.on('order.dt', function (e) {
        var table = $(e.target);
        var order = table.DataTable().order();
        var id = table.attr('id');
        localStorage.setItem(id + '#orderBy', order[0][0]);
        localStorage.setItem(id + '#orderDirection', order[0][1]);
    });

    /**
     * Restores the order of every table by reading the local storage of the browser.
     * If no order has been stored yet, the table is skipped.
     */
    tables.each(function () {
        var id = $(this).attr('id');
        var orderBy = localStorage.getItem(id + '#orderBy');
        var orderDirection = localStorage.getItem(id + '#orderDirection');
        if (orderBy && orderDirection) {
            var order = [orderBy, orderDirection];
            $(this).DataTable().order(order).draw();
        }
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




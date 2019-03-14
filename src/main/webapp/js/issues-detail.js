(function ($) {
    if ($('#severities-chart').length) {
        initializePieCharts();
    }

    redrawTrendCharts();
    storeAndRestoreCarousel('trend-carousel');

    /**
     * Create a data table instance for all tables that are marked with class "display".
     */
    $('table.property-table').DataTable({
        pagingType: 'numbers',  // Page number button only
        columnDefs: [{
            targets: 'no-sort', // Columns with class 'no-sort' are not orderable
            orderable: false
        }]
    });

    /**
     * Create data table instances for the detail tables.
     */
    showTable('#issues');
    showTable('#scm');

    /**
     * Activate the tab that has been visited the last time. If there is no such tab, highlight the first one.
     * If the user selects the tab using an #anchor prefer this tab.
     */
    var detailsTabs = $('#tab-details');
    detailsTabs.find('li:first-child a').tab('show');

    var url = document.location.toString();
    if (url.match('#')) {
        var tabName = url.split('#')[1];

        detailsTabs.find('a[href="#' + tabName + '"]').tab('show');
    }
    else {
        var activeTab = localStorage.getItem('activeTab');
        if (activeTab) {
            detailsTabs.find('a[href="' + activeTab + '"]').tab('show');
        }
    }

    /**
     * Store the selected tab in browser's local storage.
     */
    var tabToggleLink = $('a[data-toggle="tab"]');
    tabToggleLink.on('show.bs.tab', function (e) {
        window.location.hash = e.target.hash;
        var activeTab = $(e.target).attr('href');
        localStorage.setItem('activeTab', activeTab);
    });

    /**
     * Stores the order of every table in the local storage of the browser.
     */
    var allTables = $('#statistics').find('table');
    allTables.on('order.dt', function (e) {
        var table = $(e.target);
        var order = table.DataTable().order();
        var id = table.attr('id');
        localStorage.setItem(id + '#orderBy', order[0][0]);
        localStorage.setItem(id + '#orderDirection', order[0][1]);
    });

    /**
     * Restores the order of every table by reading the local storage of the browser.
     * If no order has been stored yet, the table is skipped.
     * Also saves the default length of the number of table columns.
     */
    allTables.each(function () {
        // Restore order
        var id = $(this).attr('id');
        var orderBy = localStorage.getItem(id + '#orderBy');
        var orderDirection = localStorage.getItem(id + '#orderDirection');
        var dataTable = $(this).DataTable();
        if (orderBy && orderDirection) {
            var order = [orderBy, orderDirection];
            try {
                dataTable.order(order).draw();
            } catch (ignore) { // TODO: find a way to determine the number of columns here
                dataTable.order([[1, 'asc']]).draw();
            }
        }
        // Store paging size
        $(this).on('length.dt', function (e, settings, len) {
            localStorage.setItem(id + '#table-length', len);
        });
        var storedLength = localStorage.getItem(id + '#table-length');
        if ($.isNumeric(storedLength)) {
            dataTable.page.len(storedLength).draw();
        }
    });

    /**
     * Activate tooltips.
     */
    $(function () {
        $('[data-toggle="tooltip"]').tooltip()
    });

    /**
     * Initializes the pie chart. There are two different variants: on small screens we have a carousel that
     * switches between the two charts. On large screens both pie charts are drawn side-by-side.
     */
    function initializePieCharts() {
        /**
         * Creates a doughnut chart that shows the number of issues per severity.
         * Requires that DOM <div> elements exist with the IDs '#severities-chart', '#single-severities-chart'.
         */
        view.getSeverityModel(function (severityModel) {
            $('#severities-chart').renderPieChart(severityModel.responseJSON, true); // small screens
            $('#single-severities-chart').renderPieChart(severityModel.responseJSON, false); // large screens

        });
        /**
         * Creates a doughnut chart that shows the number of new, fixed and outstanding issues.
         * Requires that DOM <div> elements exist with the IDs '#trend-chart', '#single-trend-chart'.
         */
        view.getTrendModel(function (pieModel) {
            $('#trend-chart').renderPieChart(pieModel.responseJSON, true);
            $('#single-trend-chart').renderPieChart(pieModel.responseJSON, false);
        });

        storeAndRestoreCarousel('overview-carousel');
    }

    /**
     * Redraws the trend charts. Reads the last selected X-Axis type from the browser local storage and
     * redraws the trend charts.
     */
    function redrawTrendCharts() {
        var isBuildOnXAxis = !(localStorage.getItem('#trendBuildAxis') === 'date');

        /**
         * Creates a build trend chart that shows the number of issues for a couple of builds.
         * Requires that a DOM <div> element exists with the ID '#severities-trend-chart'.
         */
        view.getBuildTrend(isBuildOnXAxis, function (lineModel) {
            $('#severities-trend-chart').renderTrendChart(lineModel.responseJSON, redrawTrendCharts);
        });

        /**
         * Creates a build trend chart that shows the number of issues per tool.
         * Requires that a DOM <div> element exists with the ID '#tools-trend-chart'.
         */
        view.getToolsTrend(isBuildOnXAxis, function (lineModel) {
            $('#tools-trend-chart').renderTrendChart(lineModel.responseJSON, redrawTrendCharts);
        });

        /**
         * Creates a build trend chart that shows the number of issues per tool.
         * Requires that a DOM <div> element exists with the ID '#new-versus-fixed-trend-chart'.
         */
        view.getNewVersusFixedTrend(isBuildOnXAxis, function (lineModel) {
            $('#new-versus-fixed-trend-chart').renderTrendChart(lineModel.responseJSON, redrawTrendCharts);
        });

        /**
         * Creates a build trend chart that shows the number of issues colored by the health report ranges.
         * Requires that a DOM <div> element exists with the ID '#health-trend-chart'.
         */
        if ($('#health-trend-chart').length) {
            view.getHealthTrend(isBuildOnXAxis, function (lineModel) {
                $('#health-trend-chart').renderTrendChart(lineModel.responseJSON, redrawTrendCharts);
            });
        }
    }

    /**
     * Store and restore the selected carousel image in browser's local storage.
     * Additionally, the trend chart is redrawn.
     *
     * @param {String} carouselId - ID of the carousel
     */
    function storeAndRestoreCarousel(carouselId) {
        var carousel = $('#' + carouselId);
        carousel.on('slid.bs.carousel', function (e) {
            localStorage.setItem(carouselId, e.to);
            var chart = $(e.relatedTarget).find(">:first-child").data('chart');
            chart.resize();
        });
        var activeCarousel = localStorage.getItem(carouselId);
        if (activeCarousel) {
            carousel.carousel(parseInt(activeCarousel));
        }
    }

    /**
     * Initializes the specified table.
     *
     * @param {String} id - the ID of the table
     */
    function showTable(id) {
        // Create a data table instance for the issues table. 
        var table = $(id);
        if (table.length) {
            var dataTable = table.DataTable({
                language: {
                    emptyTable: "Loading - please wait ..."
                },
                deferRender: true,
                pagingType: 'numbers',  // Page number button only
                order: [[1, 'asc']],
                columnDefs: [{
                    targets: 0,         // First column contains details button
                    orderable: false
                }]
            });

            // Add event listener for opening and closing details
            table.on('click', 'div.details-control', function () {
                var tr = $(this).parents('tr');
                var row = dataTable.row(tr);

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

            // Content is loaded on demand: if the active tab shows the table, then content is loaded using Ajax
            var tabToggleLink = $('a[data-toggle="tab"]');
            tabToggleLink.on('show.bs.tab', function (e) {
                var activeTab = $(e.target).attr('href');
                if (activeTab === (id + 'Content') && dataTable.data().length === 0) {
                    view.getTableModel(id, function (t) {
                        (function ($) {
                            var table = $(id).DataTable();
                            table.rows.add(t.responseObject().data).draw()
                        })(jQuery);
                    });
                }
            });
        }
    }
})(jQuery);


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

    $.fn.extend({
        /**
         * Renders a trend chart in the specified div using ECharts.
         *
         * @param {JSON} chartModel - the line chart model
         */
        renderTrendChart: function (chartModel) {
            var chart = echarts.init($(this)[0]);
            var options = {
                tooltip: {
                    trigger: 'axis',
                    axisPointer: {
                        type: 'cross',
                        label: {
                            backgroundColor: '#6a7985'
                        }
                    }
                },
                toolbox: {
                    itemSize: 16,
                    feature: {
                        myTool1: {
                            show: true,
                            title: 'Date',
                            icon: 'path://M148 288h-40c-6.6 0-12-5.4-12-12v-40c0-6.6 5.4-12 12-12h40c6.6 0 12 5.4 12 12v40c0 6.6-5.4 12-12 12zm108-12v-40c0-6.6-5.4-12-12-12h-40c-6.6 0-12 5.4-12 12v40c0 6.6 5.4 12 12 12h40c6.6 0 12-5.4 12-12zm96 0v-40c0-6.6-5.4-12-12-12h-40c-6.6 0-12 5.4-12 12v40c0 6.6 5.4 12 12 12h40c6.6 0 12-5.4 12-12zm-96 96v-40c0-6.6-5.4-12-12-12h-40c-6.6 0-12 5.4-12 12v40c0 6.6 5.4 12 12 12h40c6.6 0 12-5.4 12-12zm-96 0v-40c0-6.6-5.4-12-12-12h-40c-6.6 0-12 5.4-12 12v40c0 6.6 5.4 12 12 12h40c6.6 0 12-5.4 12-12zm192 0v-40c0-6.6-5.4-12-12-12h-40c-6.6 0-12 5.4-12 12v40c0 6.6 5.4 12 12 12h40c6.6 0 12-5.4 12-12zm96-260v352c0 26.5-21.5 48-48 48H48c-26.5 0-48-21.5-48-48V112c0-26.5 21.5-48 48-48h48V12c0-6.6 5.4-12 12-12h40c6.6 0 12 5.4 12 12v52h128V12c0-6.6 5.4-12 12-12h40c6.6 0 12 5.4 12 12v52h48c26.5 0 48 21.5 48 48zm-48 346V160H48v298c0 3.3 2.7 6 6 6h340c3.3 0 6-2.7 6-6z',
                            onclick: function () {
                                localStorage.setItem('#trendBuildAxis', 'date');
                                redrawTrendCharts($);
                            }
                        },
                        myTool2: {
                            show: true,
                            title: 'Build#',
                            icon: 'ipath://M440.667 182.109l7.143-40c1.313-7.355-4.342-14.109-11.813-14.109h-74.81l14.623-81.891C377.123 38.754 371.468 32 363.997 32h-40.632a12 12 0 0 0-11.813 9.891L296.175 128H197.54l14.623-81.891C213.477 38.754 207.822 32 200.35 32h-40.632a12 12 0 0 0-11.813 9.891L132.528 128H53.432a12 12 0 0 0-11.813 9.891l-7.143 40C33.163 185.246 38.818 192 46.289 192h74.81L98.242 320H19.146a12 12 0 0 0-11.813 9.891l-7.143 40C-1.123 377.246 4.532 384 12.003 384h74.81L72.19 465.891C70.877 473.246 76.532 480 84.003 480h40.632a12 12 0 0 0 11.813-9.891L151.826 384h98.634l-14.623 81.891C234.523 473.246 240.178 480 247.65 480h40.632a12 12 0 0 0 11.813-9.891L315.472 384h79.096a12 12 0 0 0 11.813-9.891l7.143-40c1.313-7.355-4.342-14.109-11.813-14.109h-74.81l22.857-128h79.096a12 12 0 0 0 11.813-9.891zM261.889 320h-98.634l22.857-128h98.634l-22.857 128z',
                            onclick: function () {
                                localStorage.setItem('#trendBuildAxis', 'build');
                                redrawTrendCharts($);
                            }
                        }
                    }
                },
                dataZoom: [{
                    id: 'dataZoomX',
                    type: 'slider',
                    xAxisIndex: [0],
                    filterMode: 'filter',
                    top: 'bottom',
                    startValue: Math.max(0, chartModel.XAxisLabels.length - 50),
                    handleIcon: 'M 239.33984 -0.052734375 C 230.53984 -0.052734375 223.33984 7.1472658 223.33984 15.947266 L 223.33984 226 L 134.05859 226 L 134.05859 179.94141 C 134.05859 158.55941 108.20789 147.8517 93.087891 162.9707 L 7.0292969 249.0293 C -2.3437031 258.4023 -2.3437031 273.5977 7.0292969 282.9707 L 93.087891 369.0293 C 108.20689 384.1483 134.05859 373.44059 134.05859 352.05859 L 134.05859 306 L 223.33984 306 L 223.33984 495.94727 C 223.33984 504.74727 230.53984 511.94727 239.33984 511.94727 L 271.33984 511.94727 C 280.13984 511.94727 287.33984 504.74727 287.33984 495.94727 L 287.33984 306 L 377.94141 306 L 377.94141 352.05859 C 377.94141 373.44059 403.79211 384.1483 418.91211 369.0293 L 504.9707 282.9707 C 514.3437 273.5977 514.3437 258.4023 504.9707 249.0293 L 418.91211 162.9707 C 403.79311 147.8507 377.94141 158.55941 377.94141 179.94141 L 377.94141 226 L 287.33984 226 L 287.33984 15.947266 C 287.33984 7.1472658 280.13984 -0.052734375 271.33984 -0.052734375 L 239.33984 -0.052734375 z',
                    handleSize: '70%',
                    handleStyle: {
                        color: '#b4b4b4'
                    }
                }],
                legend: {
                    orient: 'horizontal',
                    type: 'scroll',
                    x: 'center',
                    y: 'top'
                },
                grid: {
                    left: '20',
                    right: '10',
                    bottom: '15%',
                    top: '15%',
                    containLabel: true
                },
                xAxis: [{
                    type: 'category',
                    boundaryGap: false,
                    data: chartModel.XAxisLabels
                }
                ],
                yAxis: [{
                    type: 'value'
                }
                ],
                series: chartModel.series
            };
            chart.setOption(options);
            chart.resize();

            $(window).on('resize', function () {
                chart.resize();
            });
            $(this).data("chart", chart);
        }
    });

    /**
     * Creates a doughnut chart that shows the number of issues per severity.
     * Requires that DOM <div> elements exist with the IDs '#severities-chart', '#single-severities-chart'.
     */
    if ($('#severities-chart').length) {
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
    } else {
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
            $('#severities-trend-chart').renderTrendChart(lineModel.responseJSON);
        });

        /**
         * Creates a build trend chart that shows the number of issues per tool.
         * Requires that a DOM <div> element exists with the ID '#tools-trend-chart'.
         */
        view.getToolsTrend(isBuildOnXAxis, function (lineModel) {
            $('#tools-trend-chart').renderTrendChart(lineModel.responseJSON);
        });

        /**
         * Creates a build trend chart that shows the number of issues per tool.
         * Requires that a DOM <div> element exists with the ID '#new-versus-fixed-trend-chart'.
         */
        view.getNewVersusFixedTrend(isBuildOnXAxis, function (lineModel) {
            $('#new-versus-fixed-trend-chart').renderTrendChart(lineModel.responseJSON);
        });

        /**
         * Creates a build trend chart that shows the number of issues colored by the health report ranges.
         * Requires that a DOM <div> element exists with the ID '#health-trend-chart'.
         */
        if ($('#health-trend-chart').length) {
            view.getHealthTrend(isBuildOnXAxis, function (lineModel) {
                $('#health-trend-chart').renderTrendChart(lineModel.responseJSON);
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


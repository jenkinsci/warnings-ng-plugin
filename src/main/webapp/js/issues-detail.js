/* global jQuery, view */
(function ($) {
    if ($('#severities-chart').length) {
        initializePieCharts();
    }

    redrawTrendCharts();
    storeAndRestoreCarousel('trend-carousel');

    /**
     * Create a data table instance for all tables that are marked with class "property-table".
     */
    $('table.property-table').DataTable({
        pagingType: 'numbers', // Page number button only
        columnDefs: [{
            targets: 'no-sort', // Columns with class 'no-sort' are not orderable
            orderable: false
        }]
    });

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
     * Activate tooltips.
     */
    $(function () {
        $('[data-toggle="tooltip"]').tooltip();
    });

    /**
     * Initializes the pie chart. There are two different variants: on small screens we have a carousel that
     * switches between the two charts. On large screens both pie charts are drawn side-by-side.
     */
    function initializePieCharts () {
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
    function redrawTrendCharts () {
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
    function storeAndRestoreCarousel (carouselId) {
        var carousel = $('#' + carouselId);
        carousel.on('slid.bs.carousel', function (e) {
            localStorage.setItem(carouselId, e.to);
            var chart = $(e.relatedTarget).find('>:first-child').data('chart');
            chart.resize();
        });
        var activeCarousel = localStorage.getItem(carouselId);
        if (activeCarousel) {
            carousel.carousel(parseInt(activeCarousel));
        }
    }

})(jQuery);

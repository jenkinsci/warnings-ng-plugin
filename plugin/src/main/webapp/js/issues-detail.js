/* global jQuery3, view, echartsJenkinsApi */
(function ($) {
    redrawTrendCharts();
    storeAndRestoreCarousel('trend-carousel');
    storeAndRestoreCarousel('overview-carousel');

    /**
     * Create a data table instance for all tables that are marked with class "property-table".
     */
    const propertyTables = $('table.property-table');
    propertyTables.each(function () {
        const table = jQuery3(this);
        table.DataTable({
            pagingType: 'numbers', // Page number button only
            columnDefs: [{
                targets: 'no-sort', // Columns with class 'no-sort' are not orderable
                orderable: false
            }]
        });
    });

    /**
     * Activate the tab that has been visited the last time. If there is no such tab, highlight the first one.
     * If the user selects the tab using an #anchor prefer this tab.
     */
    const detailsTabs = $('#tab-details');
    detailsTabs.find('li:first-child a').tab('show');

    const url = document.location.toString();
    if (url.match('#')) {
        const tabName = url.split('#')[1];

        detailsTabs.find('a[href="#' + tabName + '"]').tab('show');
    }
    else {
        const activeTab = localStorage.getItem('activeTab');
        if (activeTab) {
            detailsTabs.find('a[href="' + activeTab + '"]').tab('show');
        }
    }

    /**
     * Store the selected tab in browser's local storage.
     */
    const tabToggleLink = $('a[data-toggle="tab"]');
    tabToggleLink.on('show.bs.tab', function (e) {
        window.location.hash = e.target.hash;
        const activeTab = $(e.target).attr('href');
        localStorage.setItem('activeTab', activeTab);
    });

    /**
     * Activate tooltips.
     */
    $(function () {
        $('[data-toggle="tooltip"]').tooltip();
    });

    /**
     * Redraws the trend charts. Reads the last selected X-Axis type from the browser local storage and
     * redraws the trend charts.
     */
    function redrawTrendCharts () {
        const isBuildOnXAxis = !(localStorage.getItem('#trendBuildAxis') === 'date');

        /**
         * Creates a build trend chart that shows the number of issues for a couple of builds.
         * Requires that a DOM <div> element exists with the ID '#severities-trend-chart'.
         */
        view.getBuildTrend(isBuildOnXAxis, function (lineModel) {
            echartsJenkinsApi.renderZoomableTrendChart('severities-trend-chart', lineModel.responseJSON, redrawTrendCharts);
        });

        /**
         * Creates a build trend chart that shows the number of issues per tool.
         * Requires that a DOM <div> element exists with the ID '#tools-trend-chart'.
         */
        view.getToolsTrend(isBuildOnXAxis, function (lineModel) {
            echartsJenkinsApi.renderZoomableTrendChart('tools-trend-chart', lineModel.responseJSON, redrawTrendCharts);
        });

        /**
         * Creates a build trend chart that shows the number of issues per tool.
         * Requires that a DOM <div> element exists with the ID '#new-versus-fixed-trend-chart'.
         */
        view.getNewVersusFixedTrend(isBuildOnXAxis, function (lineModel) {
            echartsJenkinsApi.renderZoomableTrendChart('new-versus-fixed-trend-chart', lineModel.responseJSON, redrawTrendCharts);
        });

        /**
         * Creates a build trend chart that shows the number of issues colored by the health report ranges.
         * Requires that a DOM <div> element exists with the ID '#health-trend-chart'.
         */
        if ($('#health-trend-chart').length) {
            view.getHealthTrend(isBuildOnXAxis, function (lineModel) {
                echartsJenkinsApi.renderZoomableTrendChart('health-trend-chart', lineModel.responseJSON, redrawTrendCharts);
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
        const carousel = $('#' + carouselId);
        carousel.on('slid.bs.carousel', function (e) {
            localStorage.setItem(carouselId, e.to);
            const chart = $(e.relatedTarget).find('>:first-child')[0].echart;
            if (chart) {
                chart.resize();
            }
        });
        const activeCarousel = localStorage.getItem(carouselId);
        if (activeCarousel) {
            carousel.carousel(parseInt(activeCarousel));
        }
    }
})(jQuery3);

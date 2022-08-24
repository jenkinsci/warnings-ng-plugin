/* global jQuery3, view, echartsJenkinsApi, bootstrap5 */
(function ($) {
    const trendConfigurationDialogId = 'chart-configuration-issues-history';

    $('#' + trendConfigurationDialogId).on('hidden.bs.modal', function () {
        redrawTrendCharts();
    });

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
    selectTab('li:first-child a');
    const url = document.location.toString();
    if (url.match('#')) {
        const tabName = url.split('#')[1];
        selectTab('a[data-bs-target="#' + tabName + '"]');
    }
    else {
        const activeTab = localStorage.getItem('activeTab');
        if (activeTab) {
            selectTab('a[data-bs-target="' + activeTab + '"]');
        }
    }

    /**
     * Store the selected tab in browser's local storage.
     */
    const tabToggleLink = $('a[data-bs-toggle="tab"]');
    tabToggleLink.on('show.bs.tab', function (e) {
        window.location.hash = e.target.hash;
        const activeTab = $(e.target).attr('data-bs-target');
        localStorage.setItem('activeTab', activeTab);
    });

    /**
     * Activate tooltips.
     */
    $(function () {
        $('[data-bs-toggle="tooltip"]').each(function () {
            const tooltip = new bootstrap5.Tooltip($(this)[0]);
            tooltip.enable();
        });
    });

    /**
     * Activates the specified tab.
     *
     * @param {String} selector - selector of the tab
     */
    function selectTab (selector) {
        const detailsTabs = $('#tab-details');
        const selectedTab = detailsTabs.find(selector);

        if (selectedTab.length !== 0) {
            const tab = new bootstrap5.Tab(selectedTab[0]);
            tab.show();
        }
    }

    /**
     * Redraws the trend charts. Reads the last selected X-Axis type from the browser local storage and
     * redraws the trend charts.
     */
    function redrawTrendCharts () {
        const openBuild = function (build) {
            view.getUrlForBuild(build, window.location.href, function (buildUrl) {
                if (buildUrl.responseJSON.startsWith('http')) {
                    window.location.assign(buildUrl.responseJSON);
                }
            });
        };

        const configuration = JSON.stringify(echartsJenkinsApi.readFromLocalStorage('jenkins-echarts-chart-configuration-issues-history'));

        /**
         * Creates a build trend chart that shows the number of issues for a couple of builds.
         * Requires that a DOM <div> element exists with the ID '#severities-trend-chart'.
         */
        view.getBuildTrend(configuration, function (lineModel) {
            echartsJenkinsApi.renderConfigurableZoomableTrendChart('severities-trend-chart',
                lineModel.responseJSON, trendConfigurationDialogId, openBuild);
        });

        /**
         * Creates a build trend chart that shows the number of issues per tool.
         * Requires that a DOM <div> element exists with the ID '#tools-trend-chart'.
         */
        view.getToolsTrend(configuration, function (lineModel) {
            echartsJenkinsApi.renderConfigurableZoomableTrendChart('tools-trend-chart',
                lineModel.responseJSON, trendConfigurationDialogId, openBuild);
        });

        /**
         * Creates a build trend chart that shows the number of issues per tool.
         * Requires that a DOM <div> element exists with the ID '#new-versus-fixed-trend-chart'.
         */
        view.getNewVersusFixedTrend(configuration, function (lineModel) {
            echartsJenkinsApi.renderConfigurableZoomableTrendChart('new-versus-fixed-trend-chart',
                lineModel.responseJSON, trendConfigurationDialogId, openBuild);
        });

        /**
         * Creates a build trend chart that shows the number of issues colored by the health report ranges.
         * Requires that a DOM <div> element exists with the ID '#health-trend-chart'.
         */
        if ($('#health-trend-chart').length) {
            view.getHealthTrend(configuration, function (lineModel) {
                echartsJenkinsApi.renderConfigurableZoomableTrendChart('health-trend-chart',
                    lineModel.responseJSON, trendConfigurationDialogId, openBuild);
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
        if (activeCarousel && carousel.is(':visible')) {
            const carouselControl = new bootstrap5.Carousel(carousel[0]);
            carouselControl.to(parseInt(activeCarousel));
            carouselControl.pause();
        }
    }
})(jQuery3);

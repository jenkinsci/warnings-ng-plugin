/* global jQuery3, proxy, echarts, echartsJenkinsApi */

(function ($) {
    const CHART_ID = 'wng-trend';
    const CHART_SELECTOR = '#' + CHART_ID;
    const TOOL_SELECTOR = '#wng-trend-tool';
    const CHART_TYPE_SELECTOR = '#wng-trend-chart-type';
    const BUILDS_SELECTOR = '#wng-trend-builds';
    const FOOTER_SELECTOR = '.page-footer';

    const MIN_CHART_HEIGHT = 300;
    const BOTTOM_MARGIN = 40;
    const MIN_BUILDS = 2;
    const MAX_BUILDS = 500;
    const DEFAULT_NUMBER_OF_BUILDS = 50;

    // The trend page shows all tools of a run, so the settings are not stored per report ID. Only the chart type
    // is stored per tool, since each tool provides a different set of chart types.
    const STORAGE_PREFIX = 'wng-trend-';
    const TOOL_KEY = 'tool';
    const BUILDS_KEY = 'builds';
    const CHART_KEY = 'chart-';

    let resizeTimeout;

    function readStorage(key) {
        try {
            return localStorage.getItem(STORAGE_PREFIX + key);
        }
        catch (e) {
            return null;
        }
    }

    function writeStorage(key, value) {
        try {
            localStorage.setItem(STORAGE_PREFIX + key, value);
        }
        catch (e) {
            // ignore: settings are not persisted if the storage is not available
        }
    }

    function hasOption(selector, value) {
        return value !== null && $(selector + ' option').filter(function () {
            return this.value === value;
        }).length > 0;
    }

    function getSelectedTool() {
        return $(TOOL_SELECTOR).val();
    }

    function getAvailableCharts() {
        const charts = $(TOOL_SELECTOR + ' option:selected').attr('data-charts') || '';
        return charts.split(' ').filter(chart => chart.length > 0);
    }

    // Shows only the chart types that are available for the selected tool, and selects the stored chart type of
    // this tool (or the default chart type of the tool if nothing has been stored yet).
    function updateChartTypes() {
        const available = getAvailableCharts();
        $(CHART_TYPE_SELECTOR + ' option').each(function () {
            const isAvailable = available.includes(this.value);
            this.disabled = !isAvailable;
            this.hidden = !isAvailable;
        });

        const stored = readStorage(CHART_KEY + getSelectedTool());
        const defaultChart = $(TOOL_SELECTOR + ' option:selected').attr('data-default-chart');
        $(CHART_TYPE_SELECTOR).val(available.includes(stored) ? stored : defaultChart);
    }

    function getNumberOfBuilds() {
        const parsed = parseInt($(BUILDS_SELECTOR).val(), 10);
        if (isNaN(parsed)) {
            return DEFAULT_NUMBER_OF_BUILDS;
        }
        return Math.max(MIN_BUILDS, Math.min(MAX_BUILDS, parsed));
    }

    function initializeControls() {
        const storedTool = readStorage(TOOL_KEY);
        if (hasOption(TOOL_SELECTOR, storedTool)) {
            $(TOOL_SELECTOR).val(storedTool);
        }
        updateChartTypes();

        const storedBuilds = parseInt(readStorage(BUILDS_KEY), 10);
        $(BUILDS_SELECTOR).val(isNaN(storedBuilds) ? DEFAULT_NUMBER_OF_BUILDS : storedBuilds);
    }

    function readConfiguration() {
        return {
            tool: getSelectedTool(),
            chart: $(CHART_TYPE_SELECTOR).val(),
            numberOfBuilds: getNumberOfBuilds()
        };
    }

    function persistConfiguration(configuration) {
        writeStorage(TOOL_KEY, configuration.tool);
        writeStorage(CHART_KEY + configuration.tool, configuration.chart);
        writeStorage(BUILDS_KEY, String(configuration.numberOfBuilds));
    }

    function getFooterHeight() {
        const footer = document.querySelector(FOOTER_SELECTOR);
        return footer ? footer.getBoundingClientRect().height : 0;
    }

    function fitChartToViewport() {
        const element = document.querySelector(CHART_SELECTOR);
        if (!element) {
            return;
        }
        const top = element.getBoundingClientRect().top;
        const availableHeight = window.innerHeight - top - getFooterHeight() - BOTTOM_MARGIN;
        element.style.height = Math.max(availableHeight, MIN_CHART_HEIGHT) + 'px';
    }

    function resizeChart() {
        fitChartToViewport();
        const element = document.querySelector(CHART_SELECTOR);
        if (element && element.echart) {
            element.echart.resize();
        }
    }

    function renderTrendChart() {
        fitChartToViewport();
        const configuration = readConfiguration();
        persistConfiguration(configuration);
        proxy.getTrendChart(JSON.stringify(configuration), function (t) {
            const element = document.getElementById(CHART_ID);
            const existing = echarts.getInstanceByDom(element);
            if (existing) {
                existing.dispose();
            }
            echartsJenkinsApi.renderConfigurableZoomableTrendChart(CHART_ID, t.responseJSON, null, null);
            resizeChart();
        });
    }

    $(document).ready(function () {
        initializeControls();
        renderTrendChart();

        $(TOOL_SELECTOR).on('change', function () {
            updateChartTypes();
            renderTrendChart();
        });
        $([CHART_TYPE_SELECTOR, BUILDS_SELECTOR].join(', ')).on('change', function () {
            renderTrendChart();
        });

        $(window).on('resize', function () {
            clearTimeout(resizeTimeout);
            resizeTimeout = setTimeout(resizeChart, 150);
        });
    });
})(jQuery3);

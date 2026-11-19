/* global jQuery3, proxy, echarts, WarningsCharts */

(function ($) {
    const CHART_ID = 'wng-hotspots-chart';
    const HISTOGRAM_ID = 'wng-hotspots-histogram';
    const EMPTY_ID = 'wng-hotspots-empty';
    const TOOL_SELECTOR = '#wng-hotspots-tool';
    const GROUPING_SELECTOR = '#wng-hotspots-grouping';
    const BASELINE_SELECTOR = '#wng-hotspots-baseline';
    const TOP_SELECTOR = '#wng-hotspots-top';
    const HIDDEN_CLASS = WarningsCharts.HIDDEN_CLASS;

    const MIN_HOTSPOTS = 1;
    const MAX_HOTSPOTS = 100;
    const DEFAULT_HOTSPOTS = 10;
    const BAR_HEIGHT = 28;
    const CHART_PADDING = 80;
    const MIN_CHART_HEIGHT = 200;
    const MAX_LABEL_LENGTH = 40;

    // The hotspots page shows all tools of a run, so the settings are not stored per report ID.
    const STORAGE_PREFIX = 'wng-hotspots-';
    const TOOL_KEY = 'tool';
    const GROUPING_KEY = 'grouping';
    const BASELINE_KEY = 'baseline';
    const TOP_KEY = 'top';

    const storage = WarningsCharts.createStorage(STORAGE_PREFIX);
    const getCssColor = WarningsCharts.getCssColor;

    let resizeTimeout;
    let latestRequest = 0;

    function getNumberOfHotspots() {
        const parsed = parseInt($(TOP_SELECTOR).val(), 10);
        if (isNaN(parsed)) {
            return DEFAULT_HOTSPOTS;
        }
        return Math.max(MIN_HOTSPOTS, Math.min(MAX_HOTSPOTS, parsed));
    }

    function initializeControls() {
        storage.restoreSelection(TOOL_SELECTOR, TOOL_KEY);
        storage.restoreSelection(GROUPING_SELECTOR, GROUPING_KEY);
        storage.restoreSelection(BASELINE_SELECTOR, BASELINE_KEY);
        WarningsCharts.showToolIcon(TOOL_SELECTOR);

        const storedTop = parseInt(storage.read(TOP_KEY), 10);
        $(TOP_SELECTOR).val(isNaN(storedTop) ? DEFAULT_HOTSPOTS : storedTop);
    }

    function readConfiguration() {
        return {
            tool: $(TOOL_SELECTOR).val(),
            grouping: $(GROUPING_SELECTOR).val(),
            baseline: $(BASELINE_SELECTOR).val(),
            numberOfHotspots: getNumberOfHotspots()
        };
    }

    function persistConfiguration(configuration) {
        storage.write(TOOL_KEY, configuration.tool);
        storage.write(GROUPING_KEY, configuration.grouping);
        storage.write(BASELINE_KEY, configuration.baseline);
        storage.write(TOP_KEY, String(configuration.numberOfHotspots));
    }

    function shorten(label) {
        if (label.length <= MAX_LABEL_LENGTH) {
            return label;
        }
        return '…' + label.substring(label.length - MAX_LABEL_LENGTH + 1);
    }

    // Renders the hotspots as horizontal bars, stacked by severity. The element with the most issues is on top.
    function renderHotspots(model) {
        const element = document.getElementById(CHART_ID);
        const isEmpty = model.hotspots.length === 0;
        $('#' + EMPTY_ID).toggleClass(HIDDEN_CLASS, !isEmpty);
        $(element).toggleClass(HIDDEN_CLASS, isEmpty);
        if (isEmpty) {
            return;
        }

        const hotspots = model.hotspots.slice().reverse();
        element.style.height = Math.max(MIN_CHART_HEIGHT, hotspots.length * BAR_HEIGHT + CHART_PADDING) + 'px';

        const series = Object.keys(model.severities)
            .filter(severity => hotspots.some(hotspot => hotspot.counts[severity] > 0))
            .map(severity => ({
                name: model.severities[severity],
                type: 'bar',
                stack: 'total',
                barMaxWidth: 20,
                itemStyle: {color: getCssColor(model.colors[severity])},
                data: hotspots.map(hotspot => hotspot.counts[severity])
            }));
        const axisStyle = WarningsCharts.createAxisStyle();
        const totalLabel = element.getAttribute('data-total-label');

        const chart = WarningsCharts.getChart(CHART_ID);
        chart.setOption({
            tooltip: {
                trigger: 'axis',
                axisPointer: {type: 'shadow'},
                formatter: function (params) {
                    const hotspot = hotspots[params[0].dataIndex];
                    const lines = params
                        .filter(param => param.value > 0)
                        .map(param => param.marker + WarningsCharts.escapeHtml(param.seriesName) + ': ' + param.value);
                    return '<b>' + WarningsCharts.escapeHtml(hotspot.name) + '</b><br/>'
                        + lines.join('<br/>') + '<br/>' + WarningsCharts.escapeHtml(totalLabel) + ': ' + hotspot.total;
                }
            },
            legend: {top: 0, textStyle: {color: getCssColor('--text-color')}},
            grid: {left: 10, right: 30, top: 40, bottom: 10, containLabel: true},
            xAxis: Object.assign({type: 'value', minInterval: 1}, axisStyle),
            yAxis: Object.assign({}, axisStyle, {
                type: 'category',
                data: hotspots.map(hotspot => shorten(hotspot.label)),
                splitLine: {show: false}
            }),
            series: series
        }, true);
        chart.resize();
    }

    // Renders the histogram: the number of elements (e.g., files) that have a given number of issues.
    function renderHistogram(model) {
        const element = document.getElementById(HISTOGRAM_ID);
        const axisStyle = WarningsCharts.createAxisStyle();
        const chart = WarningsCharts.getChart(HISTOGRAM_ID);
        chart.setOption({
            tooltip: {trigger: 'axis', axisPointer: {type: 'shadow'}},
            grid: {left: 10, right: 30, top: 30, bottom: 30, containLabel: true},
            xAxis: Object.assign({}, axisStyle, {
                type: 'category',
                name: element.getAttribute('data-x-label'),
                nameLocation: 'middle',
                nameGap: 30,
                data: model.buckets.map(bucket => bucket.label),
                splitLine: {show: false}
            }),
            yAxis: Object.assign({type: 'value', minInterval: 1}, axisStyle, {
                name: $(GROUPING_SELECTOR + ' option:selected').text()
            }),
            series: [{
                type: 'bar',
                name: $(GROUPING_SELECTOR + ' option:selected').text(),
                barMaxWidth: 60,
                itemStyle: {color: getCssColor('--blue')},
                data: model.buckets.map(bucket => bucket.count)
            }]
        }, true);
        chart.resize();
    }

    function render() {
        const configuration = readConfiguration();
        persistConfiguration(configuration);
        const request = ++latestRequest;
        proxy.getHotspots(JSON.stringify(configuration), function (t) {
            if (request !== latestRequest) {
                return;
            }
            // HotspotsModel#getHotspots returns the model as JSON string, so Stapler wraps it in another JSON string
            const model = JSON.parse(t.responseJSON);
            renderHotspots(model);
            renderHistogram(model);
        });
    }

    function resizeCharts() {
        [CHART_ID, HISTOGRAM_ID].forEach(function (id) {
            const chart = echarts.getInstanceByDom(document.getElementById(id));
            if (chart) {
                chart.resize();
            }
        });
    }

    $(document).ready(function () {
        initializeControls();
        render();

        $(TOOL_SELECTOR).on('change', function () {
            WarningsCharts.showToolIcon(TOOL_SELECTOR);
            render();
        });
        $([GROUPING_SELECTOR, BASELINE_SELECTOR, TOP_SELECTOR].join(', ')).on('change', function () {
            render();
        });

        $(window).on('resize', function () {
            clearTimeout(resizeTimeout);
            resizeTimeout = setTimeout(resizeCharts, 150);
        });
    });
})(jQuery3);

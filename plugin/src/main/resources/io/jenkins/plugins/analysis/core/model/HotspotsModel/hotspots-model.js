/* global jQuery3, proxy, echarts */

(function ($) {
    const CHART_ID = 'wng-hotspots-chart';
    const HISTOGRAM_ID = 'wng-hotspots-histogram';
    const EMPTY_ID = 'wng-hotspots-empty';
    const TOOL_SELECTOR = '#wng-hotspots-tool';
    const GROUPING_SELECTOR = '#wng-hotspots-grouping';
    const BASELINE_SELECTOR = '#wng-hotspots-baseline';
    const TOP_SELECTOR = '#wng-hotspots-top';
    const TOOL_ICON_SELECTOR = '.wng-app-bar__tool-icon';
    const HIDDEN_CLASS = 'jenkins-hidden';

    const MIN_HOTSPOTS = 1;
    const MAX_HOTSPOTS = 100;
    const DEFAULT_HOTSPOTS = 10;
    const BAR_HEIGHT = 28;
    const CHART_PADDING = 80;
    const MIN_CHART_HEIGHT = 200;
    const MAX_LABEL_LENGTH = 40;

    // Colors of the severities: the same CSS variables as the severity bar of the overview page
    const SEVERITY_COLORS = {
        error: '--red',
        high: '--orange',
        normal: '--yellow',
        low: '--text-color-secondary'
    };

    // The hotspots page shows all tools of a run, so the settings are not stored per report ID.
    const STORAGE_PREFIX = 'wng-hotspots-';
    const TOOL_KEY = 'tool';
    const GROUPING_KEY = 'grouping';
    const BASELINE_KEY = 'baseline';
    const TOP_KEY = 'top';

    let resizeTimeout;
    let latestRequest = 0;

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

    function restoreSelection(selector, key) {
        const stored = readStorage(key);
        if (hasOption(selector, stored)) {
            $(selector).val(stored);
        }
    }

    // Resolves a CSS variable to an rgba() color. Jenkins defines its colors with oklch() and relative color syntax,
    // which ECharts cannot parse: it computes the emphasis (hover) colors from the series colors, and these
    // computations fail for oklch() colors, so the bars disappear on hover. The computed style resolves the variable
    // to an absolute color, and the canvas converts this color to sRGB.
    function getCssColor(variable) {
        const element = document.createElement('span');
        element.style.display = 'none';
        element.style.color = 'var(' + variable + ')';
        document.body.appendChild(element);
        const color = getComputedStyle(element).color;
        element.remove();

        const canvas = document.createElement('canvas');
        canvas.width = 1;
        canvas.height = 1;
        const context = canvas.getContext('2d', {willReadFrequently: true});
        context.fillStyle = color;
        context.fillRect(0, 0, 1, 1);
        const [red, green, blue, alpha] = context.getImageData(0, 0, 1, 1).data;
        return 'rgba(' + red + ', ' + green + ', ' + blue + ', ' + (alpha / 255).toFixed(3) + ')';
    }

    // Shows the icon of the selected tool next to the select box and hides the icons of all other tools.
    function showToolIcon() {
        const tool = $(TOOL_SELECTOR).val();
        $(TOOL_ICON_SELECTOR).each(function () {
            $(this).toggleClass(HIDDEN_CLASS, this.getAttribute('data-tool') !== tool);
        });
    }

    function getNumberOfHotspots() {
        const parsed = parseInt($(TOP_SELECTOR).val(), 10);
        if (isNaN(parsed)) {
            return DEFAULT_HOTSPOTS;
        }
        return Math.max(MIN_HOTSPOTS, Math.min(MAX_HOTSPOTS, parsed));
    }

    function initializeControls() {
        restoreSelection(TOOL_SELECTOR, TOOL_KEY);
        restoreSelection(GROUPING_SELECTOR, GROUPING_KEY);
        restoreSelection(BASELINE_SELECTOR, BASELINE_KEY);
        showToolIcon();

        const storedTop = parseInt(readStorage(TOP_KEY), 10);
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
        writeStorage(TOOL_KEY, configuration.tool);
        writeStorage(GROUPING_KEY, configuration.grouping);
        writeStorage(BASELINE_KEY, configuration.baseline);
        writeStorage(TOP_KEY, String(configuration.numberOfHotspots));
    }

    function getChart(id) {
        const element = document.getElementById(id);
        return echarts.getInstanceByDom(element) || echarts.init(element);
    }

    function shorten(label) {
        if (label.length <= MAX_LABEL_LENGTH) {
            return label;
        }
        return '…' + label.substring(label.length - MAX_LABEL_LENGTH + 1);
    }

    function escapeHtml(text) {
        return $('<div>').text(text).html();
    }

    function createAxisStyle() {
        const textColor = getCssColor('--text-color');
        const lineColor = getCssColor('--jenkins-border-color');
        return {
            axisLabel: {color: textColor},
            axisLine: {lineStyle: {color: lineColor}},
            splitLine: {lineStyle: {color: lineColor}},
            nameTextStyle: {color: textColor}
        };
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
                itemStyle: {color: getCssColor(SEVERITY_COLORS[severity])},
                data: hotspots.map(hotspot => hotspot.counts[severity])
            }));
        const axisStyle = createAxisStyle();
        const totalLabel = element.getAttribute('data-total-label');

        const chart = getChart(CHART_ID);
        chart.setOption({
            tooltip: {
                trigger: 'axis',
                axisPointer: {type: 'shadow'},
                formatter: function (params) {
                    const hotspot = hotspots[params[0].dataIndex];
                    const lines = params
                        .filter(param => param.value > 0)
                        .map(param => param.marker + escapeHtml(param.seriesName) + ': ' + param.value);
                    return '<b>' + escapeHtml(hotspot.name) + '</b><br/>'
                        + lines.join('<br/>') + '<br/>' + escapeHtml(totalLabel) + ': ' + hotspot.total;
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
        const axisStyle = createAxisStyle();
        const chart = getChart(HISTOGRAM_ID);
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
            // The Java method returns the model as JSON string, so Stapler wraps it in another JSON string
            const model = typeof t.responseJSON === 'string' ? JSON.parse(t.responseJSON) : t.responseJSON;
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
            showToolIcon();
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

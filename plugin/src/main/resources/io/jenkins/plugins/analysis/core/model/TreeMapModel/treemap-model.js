/* global jQuery3, proxy, echarts, WarningsCharts */

(function ($) {
    const CHART_ID = 'wng-treemap-chart';
    const EMPTY_ID = 'wng-treemap-empty';
    const TOOL_SELECTOR = '#wng-treemap-tool';
    const BASELINE_SELECTOR = '#wng-treemap-baseline';
    const COLOR_SELECTOR = '#wng-treemap-color';
    const FOOTER_SELECTOR = '.page-footer';
    const HIDDEN_CLASS = WarningsCharts.HIDDEN_CLASS;

    const COLOR_BY_NEW = 'new';
    const MIN_CHART_HEIGHT = 300;
    const SHADOW_MARGIN = 4;
    const BREADCRUMB_HEIGHT = 40;
    const BOTTOM_MARGIN = 40;
    const NO_NEW_ISSUES_COLOR = '--text-color-secondary';
    const ONLY_NEW_ISSUES_COLOR = '--red';
    const DARK_LABEL = 'rgba(0, 0, 0, 0.87)';
    const LIGHT_LABEL = 'rgba(255, 255, 255, 0.95)';
    const LIGHT_BACKGROUND_LUMINANCE = 0.6;

    // The treemap page shows all tools of a run, so the settings are not stored per report ID.
    const storage = WarningsCharts.createStorage('wng-treemap-');
    const TOOL_KEY = 'tool';
    const BASELINE_KEY = 'baseline';
    const COLOR_KEY = 'color';

    let resizeTimeout;
    let latestRequest = 0;
    let currentModel = null;

    function initializeControls() {
        storage.restoreSelection(TOOL_SELECTOR, TOOL_KEY);
        storage.restoreSelection(BASELINE_SELECTOR, BASELINE_KEY);
        storage.restoreSelection(COLOR_SELECTOR, COLOR_KEY);
        WarningsCharts.showToolIcon(TOOL_SELECTOR);
    }

    function readConfiguration() {
        return {
            tool: $(TOOL_SELECTOR).val(),
            baseline: $(BASELINE_SELECTOR).val()
        };
    }

    function getFooterHeight() {
        const footer = document.querySelector(FOOTER_SELECTOR);
        return footer ? footer.getBoundingClientRect().height : 0;
    }

    function fitChartToViewport() {
        const element = document.getElementById(CHART_ID);
        const top = element.getBoundingClientRect().top;
        const availableHeight = window.innerHeight - top - getFooterHeight() - BOTTOM_MARGIN;
        element.style.height = Math.max(availableHeight, MIN_CHART_HEIGHT) + 'px';
    }

    function interpolate(from, to, ratio) {
        return {
            red: from.red + (to.red - from.red) * ratio,
            green: from.green + (to.green - from.green) * ratio,
            blue: from.blue + (to.blue - from.blue) * ratio,
            alpha: from.alpha + (to.alpha - from.alpha) * ratio
        };
    }

    function getLabelColor(rgba) {
        const luminance = (0.299 * rgba.red + 0.587 * rgba.green + 0.114 * rgba.blue) / 255;
        return luminance > LIGHT_BACKGROUND_LUMINANCE ? DARK_LABEL : LIGHT_LABEL;
    }

    // Creates the function that computes the background color of a node: either the color of the highest severity
    // of its issues, or a color between grey (no new issues) and red (only new issues).
    function createColorizer(model, colorBy) {
        if (colorBy === COLOR_BY_NEW) {
            const noNew = WarningsCharts.getRgba(NO_NEW_ISSUES_COLOR);
            const onlyNew = WarningsCharts.getRgba(ONLY_NEW_ISSUES_COLOR);
            return node => interpolate(noNew, onlyNew, node.total === 0 ? 0 : node.numberOfNewIssues / node.total);
        }
        const severities = Object.keys(model.severities);
        const colors = {};
        severities.forEach(severity => {
            colors[severity] = WarningsCharts.getRgba(model.colors[severity]);
        });
        return function (node) {
            const highest = severities.find(severity => node.counts[severity] > 0) || severities[0];
            return colors[highest];
        };
    }

    function toTreeMapData(node, colorize) {
        const background = colorize(node);
        const data = {
            name: node.label,
            value: node.total,
            node: node,
            itemStyle: {color: WarningsCharts.toCss(background)},
            label: {color: getLabelColor(background)}
        };
        if (node.children.length > 0) {
            data.children = node.children.map(child => toTreeMapData(child, colorize));
        }
        return data;
    }

    // Returns the background color of the card that contains the chart. ECharts draws the borders and the gaps
    // between the elements with the border color (white by default), so the borders are drawn with the background
    // color of the card. Otherwise, there would be white frames in dark mode.
    function getBackgroundColor(element) {
        const card = element.closest('.jenkins-card') || document.body;
        const color = getComputedStyle(card).backgroundColor;
        if (!color || color === 'transparent' || color === 'rgba(0, 0, 0, 0)') {
            return WarningsCharts.getCssColor('--background');
        }
        return color;
    }

    function getLevels(borderColor, textColor) {
        // ECharts fills the area of a parent element (and thus the background of its upper label) with the border
        // color, so the upper labels use the text color of the page rather than a color that depends on the node.
        const levels = [{
            itemStyle: {borderWidth: 0, gapWidth: 4, borderColor: borderColor},
            upperLabel: {show: true, height: 26, color: textColor}
        }];
        for (let i = 0; i < 2; i++) {
            levels.push({
                itemStyle: {borderWidth: 2, gapWidth: 2, borderColor: borderColor},
                upperLabel: {show: true, height: 22, color: textColor}
            });
        }
        levels.push({itemStyle: {borderWidth: 1, gapWidth: 1, borderColor: borderColor}});
        return levels;
    }

    function createTooltip(model, element) {
        const totalLabel = element.getAttribute('data-total-label');
        const newLabel = element.getAttribute('data-new-label');
        const escape = WarningsCharts.escapeHtml;
        return function (info) {
            const node = info.data && info.data.node;
            if (!node) {
                return '';
            }
            const lines = Object.keys(model.severities)
                .filter(severity => node.counts[severity] > 0)
                .map(severity => '<span style="display:inline-block;margin-right:4px;border-radius:2px;'
                    + 'width:10px;height:10px;background-color:'
                    + WarningsCharts.getCssColor(model.colors[severity]) + '"></span>'
                    + escape(model.severities[severity]) + ': ' + node.counts[severity]);
            return '<b>' + escape(node.name) + '</b><br/>' + lines.join('<br/>')
                + '<br/>' + escape(totalLabel) + ': ' + node.total
                + '<br/>' + escape(newLabel) + ': ' + node.numberOfNewIssues;
        };
    }

    function renderTreeMap() {
        const model = currentModel;
        const element = document.getElementById(CHART_ID);
        const isEmpty = model.root.total === 0;
        $('#' + EMPTY_ID).toggleClass(HIDDEN_CLASS, !isEmpty);
        $(element).toggleClass(HIDDEN_CLASS, isEmpty);
        if (isEmpty) {
            return;
        }

        fitChartToViewport();
        const colorBy = $(COLOR_SELECTOR).val();
        const colorize = createColorizer(model, colorBy);
        const textColor = WarningsCharts.getCssColor('--text-color');
        const backgroundColor = getBackgroundColor(element);

        const chart = WarningsCharts.getChart(CHART_ID);
        chart.setOption({
            tooltip: {formatter: createTooltip(model, element)},
            series: [{
                type: 'treemap',
                name: $(TOOL_SELECTOR + ' option:selected').text(),
                // Leave a small margin, otherwise the shadow of the outer elements is clipped at the canvas border
                left: SHADOW_MARGIN,
                right: SHADOW_MARGIN,
                top: SHADOW_MARGIN,
                bottom: BREADCRUMB_HEIGHT,
                roam: false,
                leafDepth: null,
                breadcrumb: {
                    show: true,
                    bottom: 0,
                    itemStyle: {
                        color: WarningsCharts.getCssColor('--button-background'),
                        borderColor: backgroundColor,
                        shadowBlur: 0
                    },
                    textStyle: {color: textColor, textBorderWidth: 0, textShadowBlur: 0}
                },
                label: {show: true, formatter: '{b}', textBorderWidth: 0},
                upperLabel: {show: true, color: textColor, textBorderWidth: 0},
                // As in the treemap of the Coverage plugin, a shadow frames each element. The shadow uses the text
                // color, so it is dark in light mode and light in dark mode.
                itemStyle: {borderColor: backgroundColor, shadowColor: textColor, shadowBlur: 3},
                levels: getLevels(backgroundColor, textColor),
                data: model.root.children.map(child => toTreeMapData(child, colorize))
            }]
        }, true);
        chart.resize();
    }

    function render() {
        const configuration = readConfiguration();
        storage.write(TOOL_KEY, configuration.tool);
        storage.write(BASELINE_KEY, configuration.baseline);
        const request = ++latestRequest;
        proxy.getTreeMap(JSON.stringify(configuration), function (t) {
            if (request !== latestRequest) {
                return;
            }
            // TreeMapModel#getTreeMap returns the model as JSON string, so Stapler wraps it in another JSON string
            currentModel = JSON.parse(t.responseJSON);
            renderTreeMap();
        });
    }

    $(document).ready(function () {
        initializeControls();
        render();

        $(TOOL_SELECTOR).on('change', function () {
            WarningsCharts.showToolIcon(TOOL_SELECTOR);
            render();
        });
        $(BASELINE_SELECTOR).on('change', render);
        // The colors are computed on the client side, so the data does not need to be loaded again
        $(COLOR_SELECTOR).on('change', function () {
            storage.write(COLOR_KEY, $(COLOR_SELECTOR).val());
            if (currentModel) {
                renderTreeMap();
            }
        });

        $(window).on('resize', function () {
            clearTimeout(resizeTimeout);
            resizeTimeout = setTimeout(function () {
                if (currentModel && currentModel.root.total > 0) {
                    fitChartToViewport();
                    WarningsCharts.getChart(CHART_ID).resize();
                }
            }, 150);
        });
    });
})(jQuery3);

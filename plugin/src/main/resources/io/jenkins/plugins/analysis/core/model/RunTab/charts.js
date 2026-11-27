/* global jQuery3, echarts */

// Helpers that are shared by the chart pages of the Warnings tab (hotspots, treemap). The pages include this script
// as adjunct and use the global object WarningsCharts.
window.WarningsCharts = (function ($) {
    const TOOL_ICON_SELECTOR = '.wng-app-bar__tool-icon';
    const HIDDEN_CLASS = 'jenkins-hidden';

    // Creates an accessor for the settings of a page in the local storage. All keys get the specified prefix.
    function createStorage(prefix) {
        function read(key) {
            try {
                return localStorage.getItem(prefix + key);
            }
            catch (e) {
                return null;
            }
        }

        function write(key, value) {
            try {
                localStorage.setItem(prefix + key, value);
            }
            catch (e) {
                // ignore: settings are not persisted if the storage is not available
            }
        }

        // Selects the stored value in the select box, if the select box offers this value.
        function restoreSelection(selector, key) {
            const stored = read(key);
            const hasOption = stored !== null && $(selector + ' option').filter(function () {
                return this.value === stored;
            }).length > 0;
            if (hasOption) {
                $(selector).val(stored);
            }
        }

        return {read: read, write: write, restoreSelection: restoreSelection};
    }

    // Resolves a CSS variable to an rgba() color. Jenkins defines its colors with oklch() and relative color syntax,
    // which ECharts cannot parse: it computes the emphasis (hover) colors from the series colors, and these
    // computations fail for oklch() colors, so the bars disappear on hover. The computed style resolves the variable
    // to an absolute color, and the canvas converts this color to sRGB.
    function getRgba(variable) {
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
        return {red: red, green: green, blue: blue, alpha: alpha / 255};
    }

    function toCss(rgba) {
        return 'rgba(' + Math.round(rgba.red) + ', ' + Math.round(rgba.green) + ', ' + Math.round(rgba.blue) + ', '
            + rgba.alpha.toFixed(3) + ')';
    }

    function getCssColor(variable) {
        return toCss(getRgba(variable));
    }

    // Shows the icon of the selected tool next to the select box and hides the icons of all other tools.
    function showToolIcon(toolSelector) {
        const tool = $(toolSelector).val();
        $(TOOL_ICON_SELECTOR).each(function () {
            $(this).toggleClass(HIDDEN_CLASS, this.getAttribute('data-tool') !== tool);
        });
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

    function getChart(id) {
        const element = document.getElementById(id);
        return echarts.getInstanceByDom(element) || echarts.init(element);
    }

    return {
        HIDDEN_CLASS: HIDDEN_CLASS,
        createStorage: createStorage,
        getRgba: getRgba,
        toCss: toCss,
        getCssColor: getCssColor,
        showToolIcon: showToolIcon,
        escapeHtml: escapeHtml,
        createAxisStyle: createAxisStyle,
        getChart: getChart
    };
})(jQuery3);

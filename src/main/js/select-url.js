module.exports = {
    /**
     * Opens the selected URL. For each value in a chart a different URL is registered.
     *
     * @param {jQuery} element - the <canvas> element that will be clicked
     * @param {Chart} chart - the Chart.js Chart instance that will be clicked
     */
    openSelectedUrl: function (element, chart) {
        element[0].onclick = function (evt) {
            var activePoints = chart.getElementsAtEvent(evt);
            if (activePoints[0]) {
                var chartData = activePoints[0]['_chart'].config.data;
                var idx = activePoints[0]['_index'];

                var url = chartData.urls[idx];
                window.open(url, '_self');
            }
        };
    }
};

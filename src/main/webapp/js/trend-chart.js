/**
 * Renders a trend chart in the specified div using ECharts.
 *
 * @param {String} chartDivId - the ID of the div where the chart should be shown in
 * @param {JSON} chartModel - the line chart model
 */
function renderTrendChart(chartDivId, chartModel) {
    var chart = echarts.init(document.getElementById(chartDivId));
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
        legend: {
            orient: 'horizontal',
            x: 'center',
            y: 'bottom'
        },
        grid: {
            left: '20',
            right: '10',
            bottom: '20',
            top: '10',
            containLabel: true
        },
        xAxis: [
            {
                type: 'category',
                boundaryGap: false,
                data: chartModel.XAxisLabels
            }
        ],
        yAxis: [
            {
                type: 'value'
            }
        ],
        series: chartModel.series
    };
    chart.setOption(options);
    chart.resize();
    window.onresize = function() {
        chart.resize();
    };
}




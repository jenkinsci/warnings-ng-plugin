/* global echarts */
/**
 * Renders a trend chart in the specified div using ECharts.
 *
 * @param {String} chartDivId - the ID of the div where the chart should be shown in
 * @param {String} model - the line chart model
 * @param {String} urlName - the URL to the results, if empty or unset then clicking on the chart is disabled
 */
function renderTrendChart (chartDivId, model, urlName) { // eslint-disable-line no-unused-vars
    const chartModel = JSON.parse(model);
    const chartPlaceHolder = document.getElementById(chartDivId);

    let selectedBuild; // the tooltip formatter will change this value while hoovering

    if (urlName) {
        chartPlaceHolder.onclick = function () {
            if (urlName && selectedBuild > 0) {
                window.location.assign(selectedBuild + '/' + urlName);
            }
        };
    }

    const chart = echarts.init(chartPlaceHolder);
    chartPlaceHolder.echart = chart;
    const options = {
        tooltip: {
            trigger: 'axis',
            axisPointer: {
                type: 'cross',
                label: {
                    backgroundColor: '#6a7985'
                }
            },
            formatter: function (params, ticket, callback) {
                if (params.componentType === 'legend') {
                    selectedBuild = 0;
                    return params.name;
                }

                const builds = chartModel.buildNumbers;
                const labels = chartModel.xAxisLabels;
                for (let i = 0; i < builds.length; i++) {
                    if (params[0].name === labels[i]) {
                        selectedBuild = builds[i];
                        break;
                    }
                }

                let text = 'Build ' + params[0].name;
                for (let i = 0, l = params.length; i < l; i++) {
                    text += '<br/>' + params[i].marker + params[i].seriesName + ' : ' + params[i].value;
                }
                text += '<br />';
                return '<div style="text-align:left">' + text + '</div>';
            }
        },
        legend: {
            orient: 'horizontal',
            type: 'scroll',
            x: 'center',
            y: 'top'
        },
        grid: {
            left: '20',
            right: '10',
            bottom: '10',
            top: '30',
            containLabel: true
        },
        xAxis: [{
            type: 'category',
            boundaryGap: false,
            data: chartModel.xAxisLabels
        }
        ],
        yAxis: [{
            type: 'value'
        }
        ],
        series: chartModel.series
    };
    chart.setOption(options);
    chart.on('legendselectchanged', function (params) {
        selectedBuild = 0; // clear selection to avoid navigating to the selected build
    });
    chart.resize();
    window.onresize = function () {
        chart.resize();
    };
}
